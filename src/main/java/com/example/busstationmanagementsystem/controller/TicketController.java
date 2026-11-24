package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.service.BusTripService;
import com.example.busstationmanagementsystem.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

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
        model.addAttribute("trips", busTripService.getAllTrips(toBusTripRepository(repository)));
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
        } catch (IllegalArgumentException | IllegalStateException | java.util.NoSuchElementException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("trips", busTripService.getAllTrips(toBusTripRepository(repository)));
            model.addAttribute("passengers", ticketService.getAllPassengers(repository));
            model.addAttribute("selectedRepository", repository);
            return "ticket/form";
        }
        return "redirect:/tickets?repository=" + repository;
    }

    private BusTripService.RepositoryType toBusTripRepository(TicketService.RepositoryType repository) {
        return repository == TicketService.RepositoryType.IN_FILE
                ? BusTripService.RepositoryType.IN_FILE
                : BusTripService.RepositoryType.IN_MEMORY;
    }

    @GetMapping("/{id}")
    public String showTicketDetails(@PathVariable String id,
                                    @RequestParam(defaultValue = "IN_MEMORY") TicketService.RepositoryType repository,
                                    Model model) {
        Ticket ticket = ticketService.getAllTickets(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Ticket not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", ticket.getId());
        fields.put("Trip ID", ticket.getTripId());
        fields.put("Passenger ID", ticket.getPassengerId());
        fields.put("Seat Number", ticket.getSeatNumber());
        fields.put("Price", ticket.getPrice());
        model.addAttribute("title", "Ticket " + ticket.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/tickets");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteTicket(@PathVariable String id,
                               @RequestParam(defaultValue = "IN_MEMORY") TicketService.RepositoryType repository) {
        ticketService.deleteTicket(id, repository);
        return "redirect:/tickets?repository=" + repository;
    }
}