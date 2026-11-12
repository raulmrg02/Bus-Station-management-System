package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Passenger;
import org.springframework.stereotype.Repository;

@Repository
public class PassengerFileRepository extends InFileRepository<Passenger> {
    public PassengerFileRepository() {
        super("src/main/resources/data/passengers.json", Passenger.class, Passenger::getId);
    }
}
