package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.service.BusTripService;
import com.example.busstationmanagementsystem.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final BusTripService busTripService;

    public TicketController(TicketService ticketService,
                            BusTripService busTripService) {
        this.ticketService = ticketService;
        this.busTripService = busTripService;
    }

    @GetMapping
    public String getAllTickets(Model model) {
        model.addAttribute("tickets", ticketService.getAllTickets());
        return "ticket/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("trips", busTripService.getAllTrips());
        model.addAttribute("passengers", ticketService.getAllPassengers());
        return "ticket/form";
    }

    @PostMapping
    public String bookTicket(@RequestParam String id,
                             @RequestParam String tripId,
                             @RequestParam String passengerId,
                             @RequestParam String seatNumber,
                             @RequestParam double price) {
        ticketService.bookTicket(new Ticket(id, tripId, passengerId, seatNumber, price));
        return "redirect:/tickets";
    }

    @PostMapping("/{id}/delete")
    public String deleteTicket(@PathVariable String id) {
        ticketService.deleteTicket(id);
        return "redirect:/tickets";
    }
}