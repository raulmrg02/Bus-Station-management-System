package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PassengerService {
    private final CrudRepository<Passenger, String> passengerRepository;
    private final TicketService ticketService;

    public PassengerService(CrudRepository<Passenger, String> passengerRepository, TicketService ticketService) {
        this.passengerRepository = passengerRepository;
        this.ticketService = ticketService;
    }

    public Passenger createPassenger(Passenger passenger) {
        if (passenger.getId() == null || passenger.getId().isBlank()) {
            throw new IllegalArgumentException("Passenger ID cannot be empty");
        }
        if (passengerRepository.findById(passenger.getId()).isPresent()) {
            throw new IllegalArgumentException("Passenger already exists with ID: " + passenger.getId());
        }
        if (passenger.getEmail() != null
                && !passenger.getEmail().isBlank()
                && passengerRepository.findAll().stream()
                .anyMatch(existingPassenger -> passenger.getEmail().equalsIgnoreCase(existingPassenger.getEmail()))) {
            throw new IllegalArgumentException("Passenger already exists with email: " + passenger.getEmail());
        }
        return passengerRepository.save(passenger);
    }

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public Passenger updatePassenger(String id, Passenger updatedPassenger) {
        Passenger existingPassenger = passengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
        if (updatedPassenger.getEmail() != null
                && !updatedPassenger.getEmail().isBlank()
                && passengerRepository.findAll().stream()
                .anyMatch(existing -> !id.equals(existing.getId())
                        && updatedPassenger.getEmail().equalsIgnoreCase(existing.getEmail()))) {
            throw new IllegalArgumentException("Passenger already exists with email: " + updatedPassenger.getEmail());
        }
        updatedPassenger.setId(id);
        updatedPassenger.setTickets(existingPassenger.getTickets());
        return passengerRepository.save(updatedPassenger);
    }

    public Passenger getPassengerById(String id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public void deletePassenger(String id) {
        ticketService.getAllTickets().stream()
                .filter(ticket -> id.equals(ticket.getPassengerId()))
                .map(ticket -> ticket.getId())
                .toList()
                .forEach(ticketService::deleteTicket);
        passengerRepository.deleteById(id);
    }
}