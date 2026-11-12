package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusTrip;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
public class BusTripFileRepository implements CrudRepository<BusTrip, String> {
    private final Map<String, BusTrip> storage = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final File file = new File("src/main/resources/data/bus_trips.json");

    public BusTripFileRepository() {
        loadFromFile();
    }

    private void loadFromFile() {
        if (!file.exists() || file.length() == 0) {
            return;
        }
        try {
            List<BusTrip> trips = objectMapper.readValue(file, new TypeReference<List<BusTrip>>() {});
            for (BusTrip trip : trips) {
                if (trip.getId() != null) {
                    storage.put(trip.getId(), trip);
                }
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to read JSON from " + file, exception);
        }
    }

    @Override
    public synchronized BusTrip save(BusTrip trip) {
        storage.put(trip.getId(), trip);
        saveFile();
        return trip;
    }

    @Override
    public Optional<BusTrip> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<BusTrip> findAll() {
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