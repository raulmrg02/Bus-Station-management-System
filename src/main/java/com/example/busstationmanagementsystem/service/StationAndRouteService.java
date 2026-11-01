package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.BusStationRepository;
import com.example.busstationmanagementsystem.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StationAndRouteService {
    private final BusStationRepository stationRepository;
    private final RouteRepository routeRepository;

    public StationAndRouteService(BusStationRepository stationRepository, RouteRepository routeRepository) {
        this.stationRepository = stationRepository;
        this.routeRepository = routeRepository;
    }

    public BusStation createStation(BusStation station) {
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
}