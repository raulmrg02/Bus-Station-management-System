package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusStation;
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
public class BusStationFileRepository implements CrudRepository<BusStation, String> {
    private final Map<String, BusStation> storage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final File file = new File("src/main/resources/data/bus_stations.json");

    public BusStationFileRepository() {
        loadFromFile();
    }

    private void loadFromFile() {
        if (!file.exists() || file.length() == 0) {
            return;
        }
        try {
            List<BusStation> stations = objectMapper.readValue(file, new TypeReference<List<BusStation>>() {});
            for (BusStation station : stations) {
                if (station.getId() != null) {
                    storage.put(station.getId(), station);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read JSON from " + file, exception);
        }
    }

    @Override
    public synchronized BusStation save(BusStation station) {
        storage.put(station.getId(), station);
        saveFile();
        return station;
    }

    @Override
    public Optional<BusStation> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<BusStation> findAll() {
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