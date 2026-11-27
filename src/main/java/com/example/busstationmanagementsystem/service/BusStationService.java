package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusStationService {
    private final CrudRepository<BusStation, String> stationRepository;
    private final RouteService routeService;

    public BusStationService(CrudRepository<BusStation, String> stationRepository, RouteService routeService) {
        this.stationRepository = stationRepository;
        this.routeService = routeService;
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

    public BusStation updateStation(String id, BusStation updatedStation) {
        BusStation existingStation = stationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Station not found: " + id));
        updatedStation.setId(id);
        updatedStation.setTrips(existingStation.getTrips());
        BusStation savedStation = stationRepository.save(updatedStation);
        routeService.updateStationReference(savedStation);
        return savedStation;
    }

    public void deleteById(String id) {
        routeService.getAllRoutes().stream()
                .filter(route -> route.getOrigin() != null && id.equals(route.getOrigin().getId())
                        || route.getDestination() != null && id.equals(route.getDestination().getId()))
                .map(Route::getId)
                .toList()
                .forEach(routeService::deleteRoute);
        stationRepository.deleteById(id);
    }

    public List<BusStation> getAllStations() {
        return stationRepository.findAll();
    }
}