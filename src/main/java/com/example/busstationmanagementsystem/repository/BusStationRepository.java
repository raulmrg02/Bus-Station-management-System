package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.BusStation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class BusStationRepository extends InMemoryRepository<BusStation, String> {
    public BusStationRepository() {
        super(BusStation::getId);
    }
    @Repository
    @Profile("file")
    public static class BusStationFileRepository extends InFileRepository<BusStation> {
        public BusStationFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            super(Path.of(dataDirectory, "bus_stations.json").toString(), BusStation.class, BusStation::getId);
        }
    }
}
