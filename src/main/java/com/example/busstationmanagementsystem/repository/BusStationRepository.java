package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusStation;
import org.springframework.stereotype.Repository;

@Repository
public class BusStationRepository extends InMemoryRepository<BusStation, String> {
    private final CrudRepository<BusStation, String> fileRepository;

    public BusStationRepository() {
        super(BusStation::getId);
        fileRepository = new InFileRepository<BusStation>("src/main/resources/data/bus_stations.json", BusStation.class, BusStation::getId) {};
    }

    public CrudRepository<BusStation, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
