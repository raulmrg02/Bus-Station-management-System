package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RouteService {
    private final CrudRepository<Route, String> routeRepository;
    private final CrudRepository<BusStation, String> stationRepository;
    private final BusTripService busTripService;

    public RouteService(CrudRepository<Route, String> routeRepository,
                        CrudRepository<BusStation, String> stationRepository,
                        BusTripService busTripService) {
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
        this.busTripService = busTripService;
    }

    public void createRoute(Route route) {
        if (route.getId() == null || route.getId().isBlank()) {
            throw new IllegalArgumentException("Route ID cannot be empty");
        }
        if (routeRepository.findById(route.getId()).isPresent()) {
            throw new IllegalArgumentException("Route already exists with ID: " + route.getId());
        }
        validateRoute(route);
        route.setOrigin(getStation(route.getOrigin().getId()));
        route.setDestination(getStation(route.getDestination().getId()));
        routeRepository.save(route);
    }

    public Route getRouteById(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + id));
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public void updateStationReference(BusStation station) {
        for (Route route : routeRepository.findAll()) {
            boolean changed = false;
            if (route.getOrigin() != null && station.getId().equals(route.getOrigin().getId())) {
                route.setOrigin(station);
                changed = true;
            }
            if (route.getDestination() != null && station.getId().equals(route.getDestination().getId())) {
                route.setDestination(station);
                changed = true;
            }
            if (changed) {
                routeRepository.save(route);
            }
        }
    }

    public Route updateRoute(String id, Route updatedRoute) {
        Route existingRoute = routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + id));
        validateRoute(updatedRoute);
        updatedRoute.setId(id);
        updatedRoute.setTrips(existingRoute.getTrips());
        updatedRoute.setOrigin(getStation(updatedRoute.getOrigin().getId()));
        updatedRoute.setDestination(getStation(updatedRoute.getDestination().getId()));
        Route savedRoute = routeRepository.save(updatedRoute);
        existingRoute.getTrips().forEach(trip -> {
            removeTripFromStation(existingRoute.getOrigin(), trip.getId());
            removeTripFromStation(existingRoute.getDestination(), trip.getId());
            addTripToStation(savedRoute.getOrigin(), trip);
            addTripToStation(savedRoute.getDestination(), trip);
        });
        return savedRoute;
    }

    private void validateRoute(Route route) {
        if (route.getOrigin() == null || route.getDestination() == null) {
            throw new IllegalArgumentException("A route must have both an origin and destination station");
        }
        if (route.getOrigin().getId() != null
                && route.getOrigin().getId().equals(route.getDestination().getId())) {
            throw new IllegalArgumentException("Origin and destination cannot be the same station");
        }
    }

    public boolean deleteRoute(String id) {
        getTripsByRoute(id).forEach(trip -> busTripService.deleteTrip(trip.getId()));
        return routeRepository.deleteById(id);
    }

    private BusStation getStation(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Station not found: " + id));
    }

    private List<BusTrip> getTripsByRoute(String routeId) {
        return busTripService.getAllTrips().stream()
                .filter(trip -> routeId.equals(trip.getRouteId()))
                .toList();
    }

    private void addTripToStation(BusStation station, BusTrip trip) {
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            storedStation.getTrips().removeIf(existing -> trip.getId().equals(existing.getId()));
            storedStation.getTrips().add(trip);
            stationRepository.save(storedStation);
        });
    }

    private void removeTripFromStation(BusStation station, String tripId) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            storedStation.getTrips().removeIf(existing -> tripId.equals(existing.getId()));
            stationRepository.save(storedStation);
        });
    }
}