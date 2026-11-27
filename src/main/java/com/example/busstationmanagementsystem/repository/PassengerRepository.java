package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Passenger;
import org.springframework.beans.factory.annotation.Value;
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
        public PassengerFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            super(Path.of(dataDirectory, "passengers.json").toString(), Passenger.class, Passenger::getId);
        }
    }
}
