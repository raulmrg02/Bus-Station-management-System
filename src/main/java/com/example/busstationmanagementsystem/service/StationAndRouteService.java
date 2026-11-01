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

    // BusStation methods
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

    // Route methods
    public Route createRoute(Route route) {
        if (route.getOrigin() == null || route.getDestination() == null) {
            throw new IllegalArgumentException("A route must have both an origin and destination station");
        }
        if (route.getOrigin().getId().equals(route.getDestination().getId())) {
            throw new IllegalArgumentException("Origin and destination cannot be the same station");
        }
        return routeRepository.save(route);
    }

    public Route getRouteById(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + id));
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }
}