package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Bus;
import org.springframework.stereotype.Repository;

@Repository
public class BusRepository extends InMemoryRepository<Bus, String> {
    private final CrudRepository<Bus, String> fileRepository;

    public BusRepository() {
        super(Bus::getId);
        fileRepository = new InFileRepository<Bus>("src/main/resources/data/buses.json", Bus.class, Bus::getId) {};
    }

    public CrudRepository<Bus, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
