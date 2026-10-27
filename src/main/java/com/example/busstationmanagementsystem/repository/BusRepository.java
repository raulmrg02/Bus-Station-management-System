package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Bus;
import org.springframework.stereotype.Repository;

@Repository
public class BusRepository extends InMemoryRepository<Bus, String> {
    public BusRepository() {
        super(Bus::getId);
    }
}
