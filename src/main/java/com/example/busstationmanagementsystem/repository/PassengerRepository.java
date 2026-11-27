package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Passenger;
import org.springframework.stereotype.Repository;

@Repository
public class PassengerRepository extends InMemoryRepository<Passenger, String> {
    private final CrudRepository<Passenger, String> fileRepository;

    public PassengerRepository() {
        super(Passenger::getId);
        fileRepository = new InFileRepository<Passenger>("src/main/resources/data/passengers.json", Passenger.class, Passenger::getId) {};
    }

    public CrudRepository<Passenger, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
