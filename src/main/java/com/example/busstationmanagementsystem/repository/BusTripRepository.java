package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusTrip;
import org.springframework.beans.factory.annotation.Value;
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
        public BusTripFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            super(Path.of(dataDirectory, "bus_trips.json").toString(), BusTrip.class, BusTrip::getId);
        }
    }
}
