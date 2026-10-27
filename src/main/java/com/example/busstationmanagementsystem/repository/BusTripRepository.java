package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusTrip;
import org.springframework.stereotype.Repository;

@Repository
public class BusTripRepository extends InMemoryRepository<BusTrip, String> {
    public BusTripRepository() {
        super(BusTrip::getId);
    }
}
