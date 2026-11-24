package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DutyAssignmentService {
    private final CrudRepository<DutyAssignment, String> assignmentRepository;
    private final CrudRepository<BusTrip, String> busTripRepository;
    private final CrudRepository<Route, String> routeRepository;
    private final CrudRepository<BusStation, String> stationRepository;

    public DutyAssignmentService(CrudRepository<DutyAssignment, String> assignmentRepository,
                                 CrudRepository<BusTrip, String> busTripRepository,
                                 CrudRepository<Route, String> routeRepository,
                                 CrudRepository<BusStation, String> stationRepository) {
        this.assignmentRepository = assignmentRepository;
        this.busTripRepository = busTripRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    public DutyAssignment assignDuty(DutyAssignment assignment) {
        if (assignment.getId() == null || assignment.getId().isBlank()) {
            throw new IllegalArgumentException("Duty assignment ID cannot be empty");
        }
        if (assignmentRepository.findById(assignment.getId()).isPresent()) {
            throw new IllegalArgumentException("Duty assignment already exists with ID: " + assignment.getId());
        }
        DutyAssignment saved = assignmentRepository.save(assignment);

        // Keep the trip's internal list in sync if tripId exists
        if (assignment.getTripId() != null) {
            busTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                trip.getAssignments().add(saved);
                busTripRepository.save(trip);
            });
            syncTripReferences(assignment.getTripId());
        }
        return saved;
    }

    public List<DutyAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public DutyAssignment getAssignmentById(String id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Duty assignment not found: " + id));
    }

    public void deleteAssignment(String id) {
        assignmentRepository.findById(id).ifPresent(assignment -> {
            if (assignment.getTripId() != null) {
                busTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                    trip.getAssignments().removeIf(a -> id.equals(a.getId()));
                    busTripRepository.save(trip);
                });
                syncTripReferences(assignment.getTripId());
            }
        });

        assignmentRepository.deleteById(id);
    }

    private void syncTripReferences(String tripId) {
        busTripRepository.findById(tripId).ifPresent(trip -> {
            routeRepository.findById(trip.getRouteId()).ifPresent(route -> {
                replaceTrip(route.getTrips(), trip);
                routeRepository.save(route);
                syncStation(route.getOrigin(), trip, stationRepository);
                syncStation(route.getDestination(), trip, stationRepository);
            });
        });
    }

    private void syncStation(BusStation station, BusTrip trip,
                             CrudRepository<BusStation, String> stationRepository) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            replaceTrip(storedStation.getTrips(), trip);
            stationRepository.save(storedStation);
        });
    }

    private void replaceTrip(List<BusTrip> trips, BusTrip trip) {
        trips.removeIf(existing -> trip.getId().equals(existing.getId()));
        trips.add(trip);
    }
}