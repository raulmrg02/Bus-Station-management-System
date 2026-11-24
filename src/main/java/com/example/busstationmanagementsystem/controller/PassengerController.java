package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.service.PassengerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/passengers")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping
    public String getAllPassengers(@RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository,
                                   Model model) {
        model.addAttribute("passengers", passengerService.getAllPassengers(repository));
        model.addAttribute("selectedRepository", repository);
        return "passenger/index";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository,
                                 Model model) {
        model.addAttribute("passenger", new Passenger());
        model.addAttribute("selectedRepository", repository);
        model.addAttribute("isUpdate", false);
        return "passenger/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id,
                                 @RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository,
                                 Model model) {
        Passenger passenger = passengerService.getAllPassengers(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Passenger not found: " + id));
        model.addAttribute("passenger", passenger);
        model.addAttribute("selectedRepository", repository);
        model.addAttribute("isUpdate", true);
        return "passenger/form";
    }

    @PostMapping
    public String createPassenger(@ModelAttribute("passenger") Passenger passenger,
                                  @RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository,
                                  Model model) {
        try {
            passengerService.createPassenger(passenger, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("selectedRepository", repository);
            model.addAttribute("isUpdate", false);
            return "passenger/form";
        }
        return "redirect:/passengers?repository=" + repository;
    }

    @PostMapping("/{id}")
    public String updatePassenger(@PathVariable("id") String id,
                                  @ModelAttribute("passenger") Passenger passenger,
                                  @RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository,
                                  Model model) {
        try {
            passengerService.updatePassenger(id, passenger, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("selectedRepository", repository);
            model.addAttribute("isUpdate", true);
            return "passenger/form";
        }
        return "redirect:/passengers?repository=" + repository;
    }

    @GetMapping("/{id}")
    public String showPassengerDetails(@PathVariable("id") String id,
                                       @RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository,
                                       Model model) {
        Passenger passenger = passengerService.getAllPassengers(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Passenger not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", passenger.getId());
        fields.put("Name", passenger.getName());
        fields.put("Currency", passenger.getCurrency());
        fields.put("Email", passenger.getEmail());
        fields.put("Ticket IDs", passenger.getTickets().stream().map(Ticket -> Ticket.getId()).toList());
        model.addAttribute("title", "Passenger " + passenger.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/passengers");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deletePassenger(@PathVariable("id") String id,
                                  @RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository) {
        passengerService.deletePassenger(id, repository);
        return "redirect:/passengers?repository=" + repository;
    }
}