package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Bus;
import org.springframework.stereotype.Repository;

@Repository
public class BusFileRepository extends InFileRepository<Bus> {
    public BusFileRepository() {
        super("src/main/resources/data/buses.json", Bus.class, Bus::getId);
    }
}