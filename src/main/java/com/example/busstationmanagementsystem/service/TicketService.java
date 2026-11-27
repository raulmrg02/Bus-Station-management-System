package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketService {
    private final CrudRepository<Ticket, String> ticketRepository;
    private final CrudRepository<Passenger, String> passengerRepository;
    private final CrudRepository<BusTrip, String> busTripRepository;
    private final CrudRepository<Bus, String> busRepository;
    private final CrudRepository<Route, String> routeRepository;
    private final CrudRepository<BusStation, String> stationRepository;

    public TicketService(CrudRepository<Ticket, String> ticketRepository,
                         CrudRepository<Passenger, String> passengerRepository,
                         CrudRepository<BusTrip, String> busTripRepository,
                         CrudRepository<Bus, String> busRepository,
                         CrudRepository<Route, String> routeRepository,
                         CrudRepository<BusStation, String> stationRepository) {
        this.ticketRepository = ticketRepository;
        this.passengerRepository = passengerRepository;
        this.busTripRepository = busTripRepository;
        this.busRepository = busRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    public Ticket bookTicket(Ticket ticket) {
        if (ticket.getId() == null || ticket.getId().isBlank()) {
            throw new IllegalArgumentException("Ticket ID cannot be empty");
        }
        if (ticketRepository.findById(ticket.getId()).isPresent()) {
            throw new IllegalArgumentException("Ticket already exists with ID: " + ticket.getId());
        }
        BusTrip trip = getTrip(ticket.getTripId());
        Passenger passenger = passengerRepository.findById(ticket.getPassengerId())
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + ticket.getPassengerId()));
        validateSeatAndCapacity(ticket, trip, false);
        Ticket savedTicket = ticketRepository.save(ticket);
        trip.getTickets().removeIf(existing -> savedTicket.getId().equals(existing.getId()));
        trip.getTickets().add(savedTicket);
        busTripRepository.save(trip);
        syncTripReferences(trip);
        passenger.getTickets().add(savedTicket);
        passengerRepository.save(passenger);

        return savedTicket;
    }

    public List<Ticket> getTicketsByTrip(String tripId) {
        return ticketRepository.findAll().stream()
                .filter(t -> tripId.equals(t.getTripId()))
                .toList();
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Ticket updateTicket(String id, Ticket updatedTicket) {
        Ticket existingTicket = ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + id));
        BusTrip updatedTrip = getTrip(updatedTicket.getTripId());
        Passenger updatedPassenger = passengerRepository.findById(updatedTicket.getPassengerId())
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + updatedTicket.getPassengerId()));
        boolean movingToAnotherTrip = !existingTicket.getTripId().equals(updatedTicket.getTripId());
        validateSeatAndCapacity(updatedTicket, updatedTrip, !movingToAnotherTrip, id);
        updatedTicket.setId(id);
        Ticket savedTicket = ticketRepository.save(updatedTicket);
        if (movingToAnotherTrip) {
            busTripRepository.findById(existingTicket.getTripId()).ifPresent(oldTrip -> {
                oldTrip.getTickets().removeIf(ticket -> id.equals(ticket.getId()));
                busTripRepository.save(oldTrip);
                syncTripReferences(oldTrip);
            });
        }
        if (!existingTicket.getPassengerId().equals(updatedTicket.getPassengerId())) {
            passengerRepository.findById(existingTicket.getPassengerId()).ifPresent(oldPassenger -> {
                oldPassenger.getTickets().removeIf(ticket -> id.equals(ticket.getId()));
                passengerRepository.save(oldPassenger);
            });
        }
        updatedTrip.getTickets().removeIf(ticket -> id.equals(ticket.getId()));
        updatedTrip.getTickets().add(savedTicket);
        busTripRepository.save(updatedTrip);
        syncTripReferences(updatedTrip);
        updatedPassenger.getTickets().removeIf(ticket -> id.equals(ticket.getId()));
        updatedPassenger.getTickets().add(savedTicket);
        passengerRepository.save(updatedPassenger);
        return savedTicket;
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public boolean deleteTicket(String id) {
        Ticket ticket = ticketRepository.findById(id).orElse(null);
        if (ticket == null) {
            return false;
        }

        busTripRepository.findById(ticket.getTripId())
                .ifPresent(trip -> {
                    trip.getTickets().removeIf(t -> id.equals(t.getId()));
                    busTripRepository.save(trip);
                    syncTripReferences(trip);
                });
        passengerRepository.findById(ticket.getPassengerId())
                .ifPresent(passenger -> {
                    passenger.getTickets().removeIf(t -> id.equals(t.getId()));
                    passengerRepository.save(passenger);
                });
        return ticketRepository.deleteById(id);
    }

    private void syncTripReferences(BusTrip trip) {
        routeRepository.findById(trip.getRouteId()).ifPresent(route -> {
            replaceTrip(route.getTrips(), trip);
            routeRepository.save(route);
            syncStation(route.getOrigin(), trip, stationRepository);
            syncStation(route.getDestination(), trip, stationRepository);
        });
    }

    private BusTrip getTrip(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Trip is required");
        }
        return busTripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + id));
    }

    private void validateSeatAndCapacity(Ticket ticket, BusTrip trip, boolean sameTrip) {
        validateSeatAndCapacity(ticket, trip, sameTrip, null);
    }

    private void validateSeatAndCapacity(Ticket ticket, BusTrip trip, boolean sameTrip, String excludedTicketId) {
        if (ticket.getSeatNumber() == null || ticket.getSeatNumber().isBlank()) {
            throw new IllegalArgumentException("Seat number is required");
        }
        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new NoSuchElementException("Bus not found for trip: " + trip.getBusId()));
        long occupiedSeats = trip.getTickets().stream()
                .filter(existing -> excludedTicketId == null || !excludedTicketId.equals(existing.getId()))
                .count();
        if (!sameTrip && occupiedSeats >= bus.getCapacity()) {
            throw new IllegalStateException("Cannot book ticket: bus capacity (" + bus.getCapacity() + ") reached.");
        }
        boolean seatTaken = trip.getTickets().stream()
                .anyMatch(existing -> !java.util.Objects.equals(excludedTicketId, existing.getId())
                        && ticket.getSeatNumber().equalsIgnoreCase(existing.getSeatNumber()));
        if (seatTaken) {
            throw new IllegalArgumentException("Seat " + ticket.getSeatNumber() + " is already booked.");
        }
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