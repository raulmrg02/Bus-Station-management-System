package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Bus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class BusRepository extends InMemoryRepository<Bus, String> {
    public BusRepository() {
        super(Bus::getId);
    }
    @Repository
    @Profile("file")
    public static class BusFileRepository extends InFileRepository<Bus> {
        public BusFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            super(Path.of(dataDirectory, "buses.json").toString(), Bus.class, Bus::getId);
        }
    }
}
