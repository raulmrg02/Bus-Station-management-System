package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusTrip;
import org.springframework.stereotype.Repository;

@Repository
public class BusTripRepository extends InMemoryRepository<BusTrip, String> {
    private final CrudRepository<BusTrip, String> fileRepository;

    public BusTripRepository() {
        super(BusTrip::getId);
        fileRepository = new InFileRepository<BusTrip>("src/main/resources/data/bus_trips.json", BusTrip.class, BusTrip::getId) {};
    }

    public CrudRepository<BusTrip, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
