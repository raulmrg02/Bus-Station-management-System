package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.repository.BusStationRepository;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusStationService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<BusStation, String> inMemoryStationRepository;
    private final CrudRepository<BusStation, String> inFileStationRepository;

    public BusStationService(BusStationRepository stationRepository) {
        this.inMemoryStationRepository = stationRepository.forMode(com.example.busstationmanagementsystem.repository.StorageMode.IN_MEMORY);
        this.inFileStationRepository = stationRepository.forMode(com.example.busstationmanagementsystem.repository.StorageMode.IN_FILE);
    }

    public BusStation registerStation(BusStation station, RepositoryType repositoryType) {
        CrudRepository<BusStation, String> stationRepository = getRepository(repositoryType);
        if (station.getId() == null || station.getId().isBlank()) {
            throw new IllegalArgumentException("Station ID cannot be empty");
        }
        if (stationRepository.findById(station.getId()).isPresent()) {
            throw new IllegalArgumentException("Station already exists with ID: " + station.getId());
        }
        return stationRepository.save(station);
    }

    public BusStation getStationById(String id, RepositoryType repositoryType) {
        return getRepository(repositoryType).findById(id)
                .orElseThrow(() -> new NoSuchElementException("Station not found: " + id));
    }

    public BusStation updateStation(String id, BusStation updatedStation, RepositoryType repositoryType) {
        CrudRepository<BusStation, String> stationRepository = getRepository(repositoryType);
        BusStation existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Station not found: " + id));
        updatedStation.setId(id);
        updatedStation.setTrips(existingStation.getTrips());
        return stationRepository.save(updatedStation);
    }

    public void deleteById(String id, RepositoryType repositoryType) {
        getRepository(repositoryType).deleteById(id);
    }

    public List<BusStation> getAllStations(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    private CrudRepository<BusStation, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileStationRepository
                : inMemoryStationRepository;
    }
}