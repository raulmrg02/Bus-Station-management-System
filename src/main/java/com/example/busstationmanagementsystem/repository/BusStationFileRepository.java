package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusStation;
import org.springframework.stereotype.Repository;

@Repository
public class BusStationFileRepository extends InFileRepository<BusStation> {
    public BusStationFileRepository() {
        super("src/main/resources/data/bus_stations.json", BusStation.class, BusStation::getId);
    }
}