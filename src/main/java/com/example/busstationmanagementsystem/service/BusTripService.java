package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.*;
import com.example.busstationmanagementsystem.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusTripService {
    private final CrudRepository<BusTrip, String> busTripRepository;
    private final CrudRepository<DutyAssignment, String> dutyAssignmentRepository;
    private final CrudRepository<Bus, String> busRepository;
    private final CrudRepository<Route, String> routeRepository;
    private final CrudRepository<BusStation, String> stationRepository;

    public BusTripService(CrudRepository<BusTrip, String> busTripRepository,
                          CrudRepository<DutyAssignment, String> dutyAssignmentRepository,
                          CrudRepository<Bus, String> busRepository,
                          CrudRepository<Route, String> routeRepository,
                          CrudRepository<BusStation, String> stationRepository) {
        this.busTripRepository = busTripRepository;
        this.dutyAssignmentRepository = dutyAssignmentRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    public BusTrip scheduleTrip(BusTrip trip) {
        if (trip.getId() == null || trip.getId().isBlank()) {
            throw new IllegalArgumentException("Bus trip ID cannot be empty");
        }
        if (busTripRepository.findById(trip.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus trip already exists with ID: " + trip.getId());
        }
        Route route = routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new NoSuchElementException("Assigned route does not exist: " + trip.getRouteId()));
        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new NoSuchElementException("Assigned bus does not exist: " + trip.getBusId()));

        if (bus.getStatus() != BusStatus.ACTIVE) {
            throw new IllegalStateException("Cannot schedule trip with a non-active bus");
        }

        if (trip.getArrivalTime() != null && trip.getArrivalTime().isBefore(trip.getStartTime())) {
            throw new IllegalArgumentException("Arrival time cannot be before start time");
        }

        BusTrip savedTrip = busTripRepository.save(trip);
        addTripToRouteAndStations(savedTrip, route);
        return savedTrip;
    }

    public BusTrip getTripById(String id) {
        return busTripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("BusTrip not found: " + id));
    }

    public List<BusTrip> getAllTrips() {
        return busTripRepository.findAll();
    }

    public List<BusTrip> getTripsByRoute(String routeId) {
        return busTripRepository.findAll().stream()
                .filter(t -> routeId.equals(t.getRouteId()))
                .toList();
    }

    public BusTrip updateTripStatus(String tripId, TripStatus status) {
        BusTrip trip = getTripById(tripId);
        trip.setStatus(status);
        return busTripRepository.save(trip);
    }

    public BusTrip assignDuty(String tripId, DutyAssignment assignment) {
        BusTrip trip = getTripById(tripId);
        assignment.setTripId(tripId);
        DutyAssignment saved = dutyAssignmentRepository.save(assignment);

        trip.getAssignments().add(saved);
        return busTripRepository.save(trip);
    }

    public void deleteTrip(String id) {
        busTripRepository.findById(id).ifPresent(trip -> {
            removeTripFromRouteAndStations(trip);
            busTripRepository.deleteById(id);
        });
    }

    private void addTripToRouteAndStations(BusTrip trip, Route route) {
        addIfMissing(route.getTrips(), trip);
        routeRepository.save(route);

        addTripToStation(trip, route.getOrigin(), stationRepository);
        addTripToStation(trip, route.getDestination(), stationRepository);
    }

    private void addTripToStation(BusTrip trip, BusStation station,
                                  CrudRepository<BusStation, String> stationRepository) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            addIfMissing(storedStation.getTrips(), trip);
            stationRepository.save(storedStation);
        });
    }

    private void addIfMissing(List<BusTrip> trips, BusTrip trip) {
        if (trips.stream().noneMatch(existing -> trip.getId().equals(existing.getId()))) {
            trips.add(trip);
        }
    }

    private void removeTripFromRouteAndStations(BusTrip trip) {
        routeRepository.findById(trip.getRouteId()).ifPresent(route -> {
            route.getTrips().removeIf(existing -> trip.getId().equals(existing.getId()));
            routeRepository.save(route);
            removeTripFromStation(route.getOrigin(), trip, stationRepository);
            removeTripFromStation(route.getDestination(), trip, stationRepository);
        });
    }

    private void removeTripFromStation(BusStation station, BusTrip trip,
                                       CrudRepository<BusStation, String> stationRepository) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            storedStation.getTrips().removeIf(existing -> trip.getId().equals(existing.getId()));
            stationRepository.save(storedStation);
        });
    }
}