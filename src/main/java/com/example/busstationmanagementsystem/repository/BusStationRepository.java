package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusStation;
import org.springframework.stereotype.Repository;

@Repository
public class BusStationRepository extends InMemoryRepository<BusStation, String> {
    public BusStationRepository() {
        super(BusStation::getId);
    }
}

