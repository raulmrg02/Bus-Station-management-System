package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusStationService {
    private final CrudRepository<BusStation, String> stationRepository;

    public BusStationService(CrudRepository<BusStation, String> stationRepository) {
        this.stationRepository = stationRepository;
    }

    public BusStation registerStation(BusStation station) {
        if (station.getId() == null || station.getId().isBlank()) {
            throw new IllegalArgumentException("Station ID cannot be empty");
        }
        if (stationRepository.findById(station.getId()).isPresent()) {
            throw new IllegalArgumentException("Station already exists with ID: " + station.getId());
        }
        return stationRepository.save(station);
    }

    public BusStation getStationById(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Station not found: " + id));
    }

    public void deleteById(String id) {
        stationRepository.deleteById(id);
    }

    public List<BusStation> getAllStations() {
        return stationRepository.findAll();
    }
}