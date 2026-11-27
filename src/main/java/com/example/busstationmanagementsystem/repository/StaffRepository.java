package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@Profile("memory")
public class StaffRepository extends InMemoryRepository<Staff, String> {
    public StaffRepository() {
        super(Staff::getId);
    }

    @Repository
    @Profile("file")
    public static class StaffFileRepository implements CrudRepository<Staff, String> {
        private final Map<String, Staff> storage = new ConcurrentHashMap<>();
        private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        private final File file;
        private final File assignmentFile;

        public StaffFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            Path directory = Path.of(dataDirectory);
            this.file = directory.resolve("staff.json").toFile();
            this.assignmentFile = directory.resolve("duty_assignments.json").toFile();
            loadFromFile();
        }

        private void loadFromFile() {
            try {
                if (file.exists() && file.length() > 0) {
                    for (JsonNode node : objectMapper.readTree(file)) {
                        String type = node.path("type").asText("");
                        JsonNode staffData = node.deepCopy();
                        if (staffData.isObject()) {
                            ((ObjectNode) staffData).remove("type");
                        }
                        Staff staff = switch (type) {
                            case "DRIVER" -> objectMapper.treeToValue(staffData, Driver.class);
                            case "MANAGER" -> objectMapper.treeToValue(staffData, TripManager.class);
                            default -> staffData.has("yearsOfExperience")
                                    ? objectMapper.treeToValue(staffData, Driver.class)
                                    : objectMapper.treeToValue(staffData, TripManager.class);
                        };
                        storage.put(staff.getId(), staff);
                    }
                }
                loadAssignments();
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to read JSON from " + file, exception);
            }
        }

        private void loadAssignments() throws IOException {
            if (!assignmentFile.exists() || assignmentFile.length() == 0) {
                return;
            }
            List<DutyAssignment> assignments = objectMapper.readValue(
                    assignmentFile, new TypeReference<List<DutyAssignment>>() {});
            storage.values().forEach(staff -> staff.getAssignments().clear());
            for (DutyAssignment assignment : assignments) {
                Staff staff = storage.get(assignment.getStaffId());
                if (staff != null) {
                    staff.getAssignments().removeIf(existing -> assignment.getId().equals(existing.getId()));
                    staff.getAssignments().add(assignment);
                }
            }
        }

        @Override
        public synchronized Staff save(Staff staff) {
            if (staff.getId() == null || staff.getId().isBlank()) {
                throw new IllegalArgumentException("Staff ID cannot be null or empty");
            }
            storage.put(staff.getId(), staff);
            saveFile();
            return staff;
        }

        @Override
        public Optional<Staff> findById(String id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public List<Staff> findAll() {
            return new ArrayList<>(storage.values());
        }

        @Override
        public synchronized boolean deleteById(String id) {
            if (storage.remove(id) == null) {
                return false;
            }
            saveFile();
            return true;
        }

        private void saveFile() {
            try {
                Path parent = file.toPath().getParent();
                if (parent != null) {
                    Files.createDirectories(parent);
                }
                List<ObjectNode> staffJson = new ArrayList<>();
                for (Staff storedStaff : storage.values()) {
                    ObjectNode node = objectMapper.valueToTree(storedStaff);
                    node.put("type", storedStaff instanceof Driver ? "DRIVER" : "MANAGER");
                    staffJson.add(node);
                }
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, staffJson);
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to write JSON to " + file, exception);
            }
        }
    }
}
