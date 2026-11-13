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
    public String getAllTickets(@RequestParam(defaultValue = "IN_MEMORY") TicketService.RepositoryType repository,
                                Model model) {
        model.addAttribute("tickets", ticketService.getAllTickets(repository));
        model.addAttribute("selectedRepository", repository);
        return "ticket/index";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(defaultValue = "IN_MEMORY") TicketService.RepositoryType repository,
                                 Model model) {
        model.addAttribute("trips", busTripService.getAllTrips());
        model.addAttribute("passengers", ticketService.getAllPassengers(repository));
        model.addAttribute("selectedRepository", repository);
        return "ticket/form";
    }

    @PostMapping
    public String bookTicket(@RequestParam String id,
                             @RequestParam String tripId,
                             @RequestParam String passengerId,
                             @RequestParam String seatNumber,
                             @RequestParam double price,
                             @RequestParam(defaultValue = "IN_MEMORY") TicketService.RepositoryType repository,
                             Model model) {
        try {
            ticketService.bookTicket(new Ticket(id, tripId, passengerId, seatNumber, price), repository);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("trips", busTripService.getAllTrips());
            model.addAttribute("passengers", ticketService.getAllPassengers(repository));
            model.addAttribute("selectedRepository", repository);
            return "ticket/form";
        }
        return "redirect:/tickets?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deleteTicket(@PathVariable String id,
                               @RequestParam(defaultValue = "IN_MEMORY") TicketService.RepositoryType repository) {
        ticketService.deleteTicket(id, repository);
        return "redirect:/tickets?repository=" + repository;
    }
}