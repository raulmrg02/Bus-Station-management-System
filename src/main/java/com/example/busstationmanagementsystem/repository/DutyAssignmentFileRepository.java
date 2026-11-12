package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
public class DutyAssignmentFileRepository implements CrudRepository<DutyAssignment, String> {
    private final Map<String, DutyAssignment> storage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final File file = new File("src/main/resources/data/duty_assignments.json");

    public DutyAssignmentFileRepository() {
        loadFromFile();
    }

    private void loadFromFile() {
        if (!file.exists() || file.length() == 0) {
            return;
        }
        try {
            List<DutyAssignment> assignments = objectMapper.readValue(file, new TypeReference<List<DutyAssignment>>() {});
            for (DutyAssignment assignment : assignments) {
                if (assignment.getId() != null) {
                    storage.put(assignment.getId(), assignment);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read JSON from " + file, exception);
        }
    }

    @Override
    public synchronized DutyAssignment save(DutyAssignment assignment) {
        storage.put(assignment.getId(), assignment);
        saveFile();
        return assignment;
    }

    @Override
    public Optional<DutyAssignment> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<DutyAssignment> findAll() {
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

    public List<DutyAssignment> findByStaffId(String staffId) {
        return findAll().stream()
                .filter(a -> staffId.equals(a.getStaffId()))
                .toList();
    }

    public List<DutyAssignment> findByTripId(String tripId) {
        return findAll().stream()
                .filter(a -> tripId.equals(a.getTripId()))
                .toList();
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
