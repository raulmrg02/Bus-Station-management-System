package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.*;
import com.example.busstationmanagementsystem.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusTripService {
    private final CrudRepository<BusTrip, String> busTripRepository;
    private final CrudRepository<Bus, String> busRepository;
    private final CrudRepository<Route, String> routeRepository;
    private final CrudRepository<BusStation, String> stationRepository;
    private final TicketService ticketService;
    private final DutyAssignmentService dutyAssignmentService;

    public BusTripService(CrudRepository<BusTrip, String> busTripRepository,
                          CrudRepository<Bus, String> busRepository,
                          CrudRepository<Route, String> routeRepository,
                          CrudRepository<BusStation, String> stationRepository,
                          TicketService ticketService,
                          DutyAssignmentService dutyAssignmentService) {
        this.busTripRepository = busTripRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
        this.ticketService = ticketService;
        this.dutyAssignmentService = dutyAssignmentService;
    }

    public BusTrip scheduleTrip(BusTrip trip) {
        if (trip.getId() == null || trip.getId().isBlank()) {
            throw new IllegalArgumentException("Bus trip ID cannot be empty");
        }
        if (busTripRepository.findById(trip.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus trip already exists with ID: " + trip.getId());
        }
        Route route = validateTrip(trip);
        BusTrip savedTrip = busTripRepository.save(trip);
        addTripToRouteAndStations(savedTrip, route);
        return savedTrip;
    }

    public BusTrip updateTrip(String id, BusTrip updatedTrip) {
        BusTrip existingTrip = busTripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("BusTrip not found: " + id));
        Route route = validateTrip(updatedTrip);
        removeTripFromRouteAndStations(existingTrip);
        updatedTrip.setId(id);
        updatedTrip.setTickets(existingTrip.getTickets());
        updatedTrip.setAssignments(existingTrip.getAssignments());
        BusTrip savedTrip = busTripRepository.save(updatedTrip);
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
        assignment.setTripId(tripId);
        dutyAssignmentService.assignDuty(assignment);
        return getTripById(tripId);
    }

    public void deleteTrip(String id) {
        busTripRepository.findById(id).ifPresent(trip -> {
            ticketService.getAllTickets().stream()
                    .filter(ticket -> id.equals(ticket.getTripId()))
                    .map(ticket -> ticket.getId())
                    .toList()
                    .forEach(ticketService::deleteTicket);
            dutyAssignmentService.getAllAssignments().stream()
                    .filter(assignment -> id.equals(assignment.getTripId()))
                    .map(assignment -> assignment.getId())
                    .toList()
                    .forEach(dutyAssignmentService::deleteAssignment);
            removeTripFromRouteAndStations(trip);
            busTripRepository.deleteById(id);
        });
    }

    private Route validateTrip(BusTrip trip) {
        if (trip.getRouteId() == null || trip.getRouteId().isBlank()) {
            throw new IllegalArgumentException("Trip route is required");
        }
        if (trip.getBusId() == null || trip.getBusId().isBlank()) {
            throw new IllegalArgumentException("Trip bus is required");
        }
        if (trip.getStartTime() == null) {
            throw new IllegalArgumentException("Trip start time is required");
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
        return route;
    }

    private void addTripToRouteAndStations(BusTrip trip, Route route) {
        Route storedRoute = routeRepository.findById(route.getId()).orElse(route);
        addIfMissing(storedRoute.getTrips(), trip);
        routeRepository.save(storedRoute);
        addTripToStation(trip, storedRoute.getOrigin());
        addTripToStation(trip, storedRoute.getDestination());
    }

    private void addTripToStation(BusTrip trip, BusStation station) {
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
            removeTripFromStation(route.getOrigin(), trip);
            removeTripFromStation(route.getDestination(), trip);
        });
    }

    private void removeTripFromStation(BusStation station, BusTrip trip) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            storedStation.getTrips().removeIf(existing -> trip.getId().equals(existing.getId()));
            stationRepository.save(storedStation);
        });
    }
}