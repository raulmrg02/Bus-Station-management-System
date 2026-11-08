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

    public BusStation createStation(BusStation station) {
        if (stationRepository.findById(station.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus station already exists with ID: " + station.getId());
        }
        return stationRepository.save(station);
    }

    public BusStation getStationById(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Station not found: " + id));
    }

    public List<BusStation> getAllStations() {
        return stationRepository.findAll();
    }

    public List<BusStation> getStationsByCity(String city) {
        return stationRepository.findAll().stream()
                .filter(s -> s.getCity() != null && s.getCity().equalsIgnoreCase(city))
                .toList();
    }

    public void deleteStation(String id) {
        stationRepository.deleteById(id);
    }
}