package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.TicketFileRepository;
import com.example.busstationmanagementsystem.repository.TicketRepository;
import com.example.busstationmanagementsystem.repository.PassengerFileRepository;
import com.example.busstationmanagementsystem.repository.PassengerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<Ticket, String> inMemoryTicketRepository;
    private final CrudRepository<Ticket, String> inFileTicketRepository;
    private final CrudRepository<Passenger, String> inMemoryPassengerRepository;
    private final CrudRepository<Passenger, String> inFilePassengerRepository;
    private final CrudRepository<BusTrip, String> busTripRepository;
    private final CrudRepository<Bus, String> busRepository;

    public TicketService(TicketRepository ticketRepository,
                         TicketFileRepository ticketFileRepository,
                         PassengerRepository passengerRepository,
                         PassengerFileRepository passengerFileRepository,
                         CrudRepository<BusTrip, String> busTripRepository,
                         CrudRepository<Bus, String> busRepository) {
        this.inMemoryTicketRepository = ticketRepository;
        this.inFileTicketRepository = ticketFileRepository;
        this.inMemoryPassengerRepository = passengerRepository;
        this.inFilePassengerRepository = passengerFileRepository;
        this.busTripRepository = busTripRepository;
        this.busRepository = busRepository;
    }

    public Passenger registerPassenger(Passenger passenger) {
        return inMemoryPassengerRepository.save(passenger);
    }

    public Passenger getPassengerById(String id) {
        return inMemoryPassengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public Ticket bookTicket(Ticket ticket, RepositoryType repositoryType) {
        CrudRepository<Ticket, String> ticketRepository = getRepository(repositoryType);
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

        return savedTicket;
    }

    public List<Ticket> getTicketsByTrip(String tripId) {
        return inMemoryTicketRepository.findAll().stream()
                .filter(t -> tripId.equals(t.getTripId()))
                .toList();
    }

    public List<Ticket> getAllTickets(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public List<Passenger> getAllPassengers(RepositoryType repositoryType) {
        return getPassengerRepository(repositoryType).findAll();
    }

    public boolean deleteTicket(String id, RepositoryType repositoryType) {
        CrudRepository<Ticket, String> ticketRepository = getRepository(repositoryType);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + id));

        busTripRepository.findById(ticket.getTripId())
                .ifPresent(trip -> {
                    trip.getTickets().removeIf(t -> id.equals(t.getId()));
                    busTripRepository.save(trip);
                });
        return ticketRepository.deleteById(id);
    }

    private CrudRepository<Ticket, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileTicketRepository
                : inMemoryTicketRepository;
    }

    private CrudRepository<Passenger, String> getPassengerRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFilePassengerRepository
                : inMemoryPassengerRepository;
    }
}