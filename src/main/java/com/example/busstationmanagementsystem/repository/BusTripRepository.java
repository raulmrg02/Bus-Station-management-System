package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusTrip;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class BusTripRepository extends InMemoryRepository<BusTrip, String> {
    public BusTripRepository() {
        super(BusTrip::getId);
    }

    @Repository
    @Profile("file")
    public static class BusTripFileRepository extends InFileRepository<BusTrip> {
        public BusTripFileRepository() {
            super(Path.of(DATA_DIRECTORY, "bus_trips.json").toString(), BusTrip.class, BusTrip::getId);
        }
    }
}
