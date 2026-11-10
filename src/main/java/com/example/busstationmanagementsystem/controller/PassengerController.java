package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.service.PassengerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            return "passenger/form";
        }
        return "redirect:/passengers?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deletePassenger(@PathVariable("id") String id,
                                  @RequestParam(defaultValue = "IN_MEMORY") PassengerService.RepositoryType repository) {
        passengerService.deletePassenger(id, repository);
        return "redirect:/passengers?repository=" + repository;
    }
}