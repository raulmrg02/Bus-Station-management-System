package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.service.TicketService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class TicketController {

    private final TicketService ticketBookingService;

    public TicketController(TicketService ticketBookingService) {
        this.ticketBookingService = ticketBookingService;
    }

    @PostMapping("/passengers")
    public Passenger registerPassenger(@RequestBody Passenger passenger) {
        return ticketBookingService.registerPassenger(passenger);
    }

    @GetMapping("/passengers/{id}")
    public Passenger getPassengerById(@PathVariable String id) {
        return ticketBookingService.getPassengerById(id);
    }

    @PostMapping("/tickets")
    public Ticket bookTicket(@RequestBody Ticket ticket) {
        return ticketBookingService.bookTicket(ticket);
    }

    @GetMapping("/tickets/by-trip/{tripId}")
    public List<Ticket> getTicketsByTrip(@PathVariable String tripId) {
        return ticketBookingService.getTicketsByTrip(tripId);
    }
}