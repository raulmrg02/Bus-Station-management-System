package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
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

    public StaffFileRepository() {
        loadFromFile();
    }

    private void loadFromFile() {
        if (!file.exists() || file.length() == 0) {
            return;
        }
        try {
            for (JsonNode node : objectMapper.readTree(file)) {
                Staff staff = node.has("yearsOfExperience")
                        ? objectMapper.treeToValue(node, Driver.class)
                        : objectMapper.treeToValue(node, TripManager.class);
                storage.put(staff.getId(), staff);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read JSON from " + file, exception);
        }
    }

    @Override
    public synchronized Staff save(Staff staff) {
        storage.put(staff.getId(), staff);
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, new ArrayList<>(storage.values()));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write JSON to " + file, exception);
        }
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
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(file, new ArrayList<>(storage.values()));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write JSON to " + file, exception);
        }
    }
}
