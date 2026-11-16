package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusTrip;
import org.springframework.stereotype.Repository;

@Repository
public class BusTripFileRepository extends InFileRepository<BusTrip> {
    public BusTripFileRepository() {
        super("src/main/resources/data/bus_trips.json", BusTrip.class, BusTrip::getId);
    }
}