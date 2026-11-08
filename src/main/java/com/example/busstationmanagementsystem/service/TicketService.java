package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Passenger;
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

    public TicketService(CrudRepository<Ticket, String> ticketRepository,
                         CrudRepository<Passenger, String> passengerRepository,
                         CrudRepository<BusTrip, String> busTripRepository,
                         CrudRepository<Bus, String> busRepository) {
        this.ticketRepository = ticketRepository;
        this.passengerRepository = passengerRepository;
        this.busTripRepository = busTripRepository;
        this.busRepository = busRepository;
    }

    public Passenger registerPassenger(Passenger passenger) {
        return passengerRepository.save(passenger);
    }

    public Passenger getPassengerById(String id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public Ticket bookTicket(Ticket ticket) {
        if (ticketRepository.findById(ticket.getId()).isPresent()) {
            throw new IllegalArgumentException("Ticket already exists with ID: " + ticket.getId());
        }
        BusTrip trip = busTripRepository.findById(ticket.getTripId())
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + ticket.getTripId()));

        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new NoSuchElementException("Bus not found for trip: " + trip.getBusId()));

        if (trip.getTickets().size() >= bus.getCapacity()) {
            throw new IllegalStateException("Cannot book ticket: bus capacity (" + bus.getCapacity() + ") reached.");
        }

        boolean seatTaken = trip.getTickets().stream()
                .anyMatch(t -> t.getSeatNumber().equalsIgnoreCase(ticket.getSeatNumber()));
        if (seatTaken) {
            throw new IllegalArgumentException("Seat " + ticket.getSeatNumber() + " is already booked.");
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        trip.getTickets().add(savedTicket);
        busTripRepository.save(trip);

        passengerRepository.findById(ticket.getPassengerId()).ifPresent(passenger -> {
            passenger.getTickets().add(savedTicket);
            passengerRepository.save(passenger);
        });

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

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public boolean deleteTicket(String id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + id));

        busTripRepository.findById(ticket.getTripId())
                .ifPresent(trip -> {
                    trip.getTickets().removeIf(t -> id.equals(t.getId()));
                    busTripRepository.save(trip);
                });
        passengerRepository.findById(ticket.getPassengerId())
                .ifPresent(passenger -> {
                    passenger.getTickets().removeIf(t -> id.equals(t.getId()));
                    passengerRepository.save(passenger);
                });

        return ticketRepository.deleteById(id);
    }
}