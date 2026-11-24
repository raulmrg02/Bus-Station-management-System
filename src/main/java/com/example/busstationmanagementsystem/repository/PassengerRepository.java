package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Passenger;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class PassengerRepository extends InMemoryRepository<Passenger, String> {
    public PassengerRepository() {
        super(Passenger::getId);
    }

    @Repository
    @Profile("file")
    public static class PassengerFileRepository extends InFileRepository<Passenger> {
        public PassengerFileRepository() {
            super(Path.of(DATA_DIRECTORY, "passengers.json").toString(), Passenger.class, Passenger::getId);
        }
    }
}
