package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.*;
import com.example.busstationmanagementsystem.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusTripService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<BusTrip, String> inMemoryBusTripRepository;
    private final CrudRepository<BusTrip, String> inFileBusTripRepository;
    private final CrudRepository<DutyAssignment, String> dutyAssignmentRepository;
    private final BusRepository inMemoryBusRepository;
    private final BusFileRepository inFileBusRepository;
    private final CrudRepository<Route, String> routeRepository;

    public BusTripService(BusTripRepository busTripRepository,
                          BusTripFileRepository busTripFileRepository,
                          CrudRepository<DutyAssignment, String> dutyAssignmentRepository,
                          BusRepository busRepository,
                          BusFileRepository busFileRepository,
                          CrudRepository<Route, String> routeRepository) {
        this.inMemoryBusTripRepository = busTripRepository;
        this.inFileBusTripRepository = busTripFileRepository;
        this.dutyAssignmentRepository = dutyAssignmentRepository;
        this.inMemoryBusRepository = busRepository;
        this.inFileBusRepository = busFileRepository;
        this.routeRepository = routeRepository;
    }

    public BusTrip scheduleTrip(BusTrip trip) {
        return scheduleTrip(trip, RepositoryType.IN_FILE);
    }

    public BusTrip scheduleTrip(BusTrip trip, RepositoryType repositoryType) {
        CrudRepository<BusTrip, String> tripRepository = getRepository(repositoryType);
        if (trip.getId() == null || trip.getId().isBlank()) {
            throw new IllegalArgumentException("Bus trip ID cannot be empty");
        }
        if (tripRepository.findById(trip.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus trip already exists with ID: " + trip.getId());
        }
        routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new NoSuchElementException("Assigned route does not exist: " + trip.getRouteId()));
        Bus bus = inFileBusRepository.findById(trip.getBusId())
                .or(() -> inMemoryBusRepository.findById(trip.getBusId()))
                .orElseThrow(() -> new NoSuchElementException("Assigned bus does not exist: " + trip.getBusId()));

        if (bus.getStatus() != BusStatus.ACTIVE) {
            throw new IllegalStateException("Cannot schedule trip with a non-active bus");
        }

        if (trip.getArrivalTime() != null && trip.getArrivalTime().isBefore(trip.getStartTime())) {
            throw new IllegalArgumentException("Arrival time cannot be before start time");
        }

        return tripRepository.save(trip);
    }

    public BusTrip getTripById(String id) {
        return inFileBusTripRepository.findById(id)
                .or(() -> inMemoryBusTripRepository.findById(id))
                .orElseThrow(() -> new NoSuchElementException("BusTrip not found: " + id));
    }

    public List<BusTrip> getAllTrips() {
        return getAllTrips(RepositoryType.IN_FILE);
    }

    public List<BusTrip> getAllTrips(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public List<BusTrip> getTripsByRoute(String routeId) {
        return inFileBusTripRepository.findAll().stream()
                .filter(t -> routeId.equals(t.getRouteId()))
                .toList();
    }

    public BusTrip updateTripStatus(String tripId, TripStatus status) {
        BusTrip trip = getTripById(tripId);
        trip.setStatus(status);
        if (inFileBusTripRepository.findById(tripId).isPresent()) {
            return inFileBusTripRepository.save(trip);
        }
        return inMemoryBusTripRepository.save(trip);
    }

    public BusTrip assignDuty(String tripId, DutyAssignment assignment) {
        BusTrip trip = getTripById(tripId);
        assignment.setTripId(tripId);
        DutyAssignment saved = dutyAssignmentRepository.save(assignment);

        trip.getAssignments().add(saved);
        if (inFileBusTripRepository.findById(tripId).isPresent()) {
            return inFileBusTripRepository.save(trip);
        }
        return inMemoryBusTripRepository.save(trip);
    }

    public void deleteTrip(String id) {
        deleteTrip(id, RepositoryType.IN_FILE);
    }

    public void deleteTrip(String id, RepositoryType repositoryType) {
        getRepository(repositoryType).deleteById(id);
    }

    private CrudRepository<BusTrip, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusTripRepository
                : inMemoryBusTripRepository;
    }
}