package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.BusTripFileRepository;
import com.example.busstationmanagementsystem.repository.BusTripRepository;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.DutyAssignmentFileRepository;
import com.example.busstationmanagementsystem.repository.DutyAssignmentRepository;
import com.example.busstationmanagementsystem.repository.BusStationFileRepository;
import com.example.busstationmanagementsystem.repository.BusStationRepository;
import com.example.busstationmanagementsystem.repository.RouteFileRepository;
import com.example.busstationmanagementsystem.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DutyAssignmentService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<DutyAssignment, String> inMemoryAssignmentRepository;
    private final CrudRepository<DutyAssignment, String> inFileAssignmentRepository;
    private final BusTripRepository inMemoryBusTripRepository;
    private final BusTripFileRepository inFileBusTripRepository;
    private final CrudRepository<Route, String> inMemoryRouteRepository;
    private final CrudRepository<Route, String> inFileRouteRepository;
    private final CrudRepository<BusStation, String> inMemoryStationRepository;
    private final CrudRepository<BusStation, String> inFileStationRepository;

    public DutyAssignmentService(DutyAssignmentRepository dutyAssignmentRepository,
                                 DutyAssignmentFileRepository dutyAssignmentFileRepository,
                                 BusTripRepository busTripRepository,
                                 BusTripFileRepository busTripFileRepository,
                                 RouteRepository routeRepository,
                                 RouteFileRepository routeFileRepository,
                                 BusStationRepository stationRepository,
                                 BusStationFileRepository stationFileRepository) {
        this.inMemoryAssignmentRepository = dutyAssignmentRepository;
        this.inFileAssignmentRepository = dutyAssignmentFileRepository;
        this.inMemoryBusTripRepository = busTripRepository;
        this.inFileBusTripRepository = busTripFileRepository;
        this.inMemoryRouteRepository = routeRepository;
        this.inFileRouteRepository = routeFileRepository;
        this.inMemoryStationRepository = stationRepository;
        this.inFileStationRepository = stationFileRepository;
    }

    public DutyAssignment assignDuty(DutyAssignment assignment, RepositoryType repositoryType) {
        CrudRepository<DutyAssignment, String> assignmentRepository = getRepository(repositoryType);
        if (assignment.getId() == null || assignment.getId().isBlank()) {
            throw new IllegalArgumentException("Duty assignment ID cannot be empty");
        }
        if (assignmentRepository.findById(assignment.getId()).isPresent()) {
            throw new IllegalArgumentException("Duty assignment already exists with ID: " + assignment.getId());
        }
        DutyAssignment saved = assignmentRepository.save(assignment);

        // Keep the trip's internal list in sync if tripId exists
        if (assignment.getTripId() != null) {
            inFileBusTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                trip.getAssignments().add(saved);
                inFileBusTripRepository.save(trip);
            });
            inMemoryBusTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                trip.getAssignments().add(saved);
                inMemoryBusTripRepository.save(trip);
            });
            syncTripReferences(assignment.getTripId(), repositoryType);
        }
        return saved;
    }

    public List<DutyAssignment> getAllAssignments(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public DutyAssignment getAssignmentById(String id) {
        return inFileAssignmentRepository.findById(id)
                .or(() -> inMemoryAssignmentRepository.findById(id))
                .orElseThrow(() -> new NoSuchElementException("Duty assignment not found: " + id));
    }

    public void deleteAssignment(String id, RepositoryType repositoryType) {
        CrudRepository<DutyAssignment, String> assignmentRepository = getRepository(repositoryType);
        assignmentRepository.findById(id).ifPresent(assignment -> {
            if (assignment.getTripId() != null) {
                getTripRepository(repositoryType).findById(assignment.getTripId()).ifPresent(trip -> {
                    trip.getAssignments().removeIf(a -> id.equals(a.getId()));
                    getTripRepository(repositoryType).save(trip);
                });
                syncTripReferences(assignment.getTripId(), repositoryType);
            }
        });

        assignmentRepository.deleteById(id);
    }

    private CrudRepository<DutyAssignment, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileAssignmentRepository
                : inMemoryAssignmentRepository;
    }

    private CrudRepository<BusTrip, String> getTripRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusTripRepository
                : inMemoryBusTripRepository;
    }

    private void syncTripReferences(String tripId, RepositoryType repositoryType) {
        CrudRepository<BusTrip, String> tripRepository = repositoryType == RepositoryType.IN_FILE
                ? inFileBusTripRepository
                : inMemoryBusTripRepository;
        tripRepository.findById(tripId).ifPresent(trip -> {
            CrudRepository<Route, String> routeRepository = repositoryType == RepositoryType.IN_FILE
                    ? inFileRouteRepository
                    : inMemoryRouteRepository;
            CrudRepository<BusStation, String> stationRepository = repositoryType == RepositoryType.IN_FILE
                    ? inFileStationRepository
                    : inMemoryStationRepository;
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