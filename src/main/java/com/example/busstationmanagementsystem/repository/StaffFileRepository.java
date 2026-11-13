package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class StaffFileRepository implements CrudRepository<Staff, String> {
    private final Map<String, Staff> storage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final File file = new File("src/main/resources/data/staff.json");
    private final File assignmentFile = new File("src/main/resources/data/duty_assignments.json");

    public StaffFileRepository() {
        loadFromFile();
        watchAssignmentFile();
    }

    private void loadFromFile() {
        if (!file.exists() || file.length() == 0) {
            return;
        }
        try {
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
            loadAssignments();
            saveFile();
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
            staff.getAssignments().add(assignment);
        }
        }
    }

    private void watchAssignmentFile() {
        Thread watcher = new Thread(() -> {
        Path directory = assignmentFile.toPath().toAbsolutePath().getParent();
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            directory.register(watchService,
                                StandardWatchEventKinds.ENTRY_CREATE,
                                StandardWatchEventKinds.ENTRY_MODIFY);

            while (!Thread.currentThread().isInterrupted()) {
                            WatchKey key = watchService.take();
                            boolean assignmentFileChanged = key.pollEvents().stream()
                                    .anyMatch(event -> assignmentFile.getName().equals(
                                            event.context().toString()));
                            if (assignmentFileChanged) {
                                synchronized (this) {
                                    loadAssignments();
                                    saveFile();
                                }
                            }
                            if (!key.reset()) {
                                break;
                            }
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to watch " + assignmentFile, exception);
        }
        }, "staff-assignment-file-watcher");
        watcher.setDaemon(true);
        watcher.start();
    }

        @Override
    public synchronized Staff save(Staff staff) {
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
            List<ObjectNode> staffJson = new ArrayList<>();
            for (Staff storedStaff : storage.values()) {
                ObjectNode node = objectMapper.valueToTree(storedStaff);
                node.put("type", storedStaff instanceof Driver ? "DRIVER" : "MANAGER");
                staffJson.add(node);
            }
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, staffJson);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write JSON to " + file, exception);
        }
    }
}
