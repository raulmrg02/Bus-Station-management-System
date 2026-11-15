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
    private final CrudRepository<Route, String> inMemoryRouteRepository;
    private final CrudRepository<Route, String> inFileRouteRepository;
    private final CrudRepository<BusStation, String> inMemoryStationRepository;
    private final CrudRepository<BusStation, String> inFileStationRepository;

    public BusTripService(BusTripRepository busTripRepository,
                          BusTripFileRepository busTripFileRepository,
                          CrudRepository<DutyAssignment, String> dutyAssignmentRepository,
                          BusRepository busRepository,
                          BusFileRepository busFileRepository,
                          RouteRepository routeRepository,
                          RouteFileRepository routeFileRepository,
                          BusStationRepository stationRepository,
                          BusStationFileRepository stationFileRepository) {
        this.inMemoryBusTripRepository = busTripRepository;
        this.inFileBusTripRepository = busTripFileRepository;
        this.dutyAssignmentRepository = dutyAssignmentRepository;
        this.inMemoryBusRepository = busRepository;
        this.inFileBusRepository = busFileRepository;
        this.inMemoryRouteRepository = routeRepository;
        this.inFileRouteRepository = routeFileRepository;
        this.inMemoryStationRepository = stationRepository;
        this.inFileStationRepository = stationFileRepository;
    }

    public BusTrip scheduleTrip(BusTrip trip, RepositoryType repositoryType) {
        CrudRepository<BusTrip, String> tripRepository = getRepository(repositoryType);
        if (trip.getId() == null || trip.getId().isBlank()) {
            throw new IllegalArgumentException("Bus trip ID cannot be empty");
        }
        if (tripRepository.findById(trip.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus trip already exists with ID: " + trip.getId());
        }
        CrudRepository<Route, String> routeRepository = getRouteRepository(repositoryType);
        Route route = routeRepository.findById(trip.getRouteId())
                .orElseThrow(() -> new NoSuchElementException("Assigned route does not exist: " + trip.getRouteId()));
        Bus bus = getBusRepository(repositoryType).findById(trip.getBusId())
                .orElseThrow(() -> new NoSuchElementException("Assigned bus does not exist: " + trip.getBusId()));

        if (bus.getStatus() != BusStatus.ACTIVE) {
            throw new IllegalStateException("Cannot schedule trip with a non-active bus");
        }

        if (trip.getArrivalTime() != null && trip.getArrivalTime().isBefore(trip.getStartTime())) {
            throw new IllegalArgumentException("Arrival time cannot be before start time");
        }

        BusTrip savedTrip = tripRepository.save(trip);
        addTripToRouteAndStations(savedTrip, route, repositoryType, routeRepository);
        return savedTrip;
    }

    public BusTrip getTripById(String id) {
        return inFileBusTripRepository.findById(id)
                .or(() -> inMemoryBusTripRepository.findById(id))
                .orElseThrow(() -> new NoSuchElementException("BusTrip not found: " + id));
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

    public void deleteTrip(String id, RepositoryType repositoryType) {
        CrudRepository<BusTrip, String> tripRepository = getRepository(repositoryType);
        tripRepository.findById(id).ifPresent(trip -> {
            removeTripFromRouteAndStations(trip, repositoryType);
            tripRepository.deleteById(id);
        });
    }

    private CrudRepository<BusTrip, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusTripRepository
                : inMemoryBusTripRepository;
    }

    private CrudRepository<Route, String> getRouteRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileRouteRepository
                : inMemoryRouteRepository;
    }

    private CrudRepository<Bus, String> getBusRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusRepository
                : inMemoryBusRepository;
    }

    private void addTripToRouteAndStations(BusTrip trip, Route route,
                                           RepositoryType repositoryType,
                                           CrudRepository<Route, String> routeRepository) {
        addIfMissing(route.getTrips(), trip);
        routeRepository.save(route);

        CrudRepository<BusStation, String> stationRepository = repositoryType == RepositoryType.IN_FILE
                ? inFileStationRepository
                : inMemoryStationRepository;
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

    private void removeTripFromRouteAndStations(BusTrip trip, RepositoryType repositoryType) {
        CrudRepository<Route, String> routeRepository = getRouteRepository(repositoryType);
        CrudRepository<BusStation, String> stationRepository = repositoryType == RepositoryType.IN_FILE
                ? inFileStationRepository
                : inMemoryStationRepository;

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