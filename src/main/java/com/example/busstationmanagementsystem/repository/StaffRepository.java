package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile("memory")
public class StaffRepository extends InMemoryRepository<Staff, String> {
    public StaffRepository() {
        super(Staff::getId);
    }

    @Repository
    @Profile("file")
    public static class StaffFileRepository extends InFileRepository<Staff> {
        public StaffFileRepository() {
            super(Path.of(DATA_DIRECTORY, "staff.json").toString(), Staff.class, Staff::getId);
        }

        @Override
        protected synchronized void loadFromFile() {
            try {
                File file = new File(filePath);
                storage.clear();
                if (!file.exists() || file.length() == 0) return;
                for (JsonNode node : objectMapper.readTree(file)) {
                    String kind = node.path("type").asText("");
                    ObjectNode data = (ObjectNode) node.deepCopy();
                    data.remove("type");
                    Staff staff = switch (kind) {
                        case "DRIVER" -> objectMapper.treeToValue(data, Driver.class);
                        case "MANAGER" -> objectMapper.treeToValue(data, TripManager.class);
                        default -> data.has("yearsOfExperience")
                                ? objectMapper.treeToValue(data, Driver.class)
                                : objectMapper.treeToValue(data, TripManager.class);
                    };
                    storage.put(staff.getId(), staff);
                }
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to read JSON from " + filePath, exception);
            }
        }

        @Override
        protected synchronized void saveToFile() {
            try {
                List<ObjectNode> records = new ArrayList<>();
                for (Staff staff : storage.values()) {
                    ObjectNode record = objectMapper.valueToTree(staff);
                    record.put("type", staff instanceof Driver ? "DRIVER" : "MANAGER");
                    records.add(record);
                }
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), records);
            } catch (IOException exception) {
                throw new UncheckedIOException("Failed to write JSON to " + filePath, exception);
            }
        }
    }
}
