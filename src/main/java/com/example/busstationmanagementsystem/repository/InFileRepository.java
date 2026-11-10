package com.example.busstationmanagementsystem.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public abstract class InFileRepository<T> implements CrudRepository<T, String> {
    protected final Map<String, T> storage = new ConcurrentHashMap<>();
    protected final Function<T, String> idExtractor;
    protected final String filePath;
    protected final Class<T> entityClass;
    protected final ObjectMapper objectMapper;

    public InFileRepository(String filePath, Class<T> entityClass, Function<T, String> idExtractor) {
        this.filePath = filePath;
        this.entityClass = entityClass;
        this.idExtractor = idExtractor;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        loadFromFile();
    }

    private synchronized void loadFromFile() {
        try {
            File file = new File(filePath);
            if (!file.exists() || file.length() == 0) {
                storage.clear();
                return;
            }
            JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, entityClass);
            List<T> list = objectMapper.readValue(file, listType);

            storage.clear();
            if (list != null) {
                for (T item : list) {
                    String id = idExtractor.apply(item);
                    if (id != null) {
                        storage.put(id, item);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read JSON from " + filePath, e);
        }
    }

    private synchronized void saveToFile() {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), new ArrayList<>(storage.values()));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to write JSON to " + filePath, e);
        }
    }

    @Override
    public synchronized T save(T entity) {
        String id = idExtractor.apply(entity);
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Entity ID cannot be null or empty");
        }
        storage.put(id, entity);
        saveToFile();
        return entity;
    }

    @Override
    public Optional<T> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public synchronized boolean deleteById(String id) {
        boolean removed = storage.remove(id) != null;
        if (removed) {
            saveToFile();
        }
        return removed;
    }
}