package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Passenger;
import org.springframework.stereotype.Repository;

@Repository
public class PassengerRepository extends InMemoryRepository<Passenger, String> {
    public PassengerRepository() {
        super(Passenger::getId);
    }
}
