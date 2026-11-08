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
    public String getAllPassengers(Model model) {
        model.addAttribute("passengers", passengerService.getAllPassengers());
        return "passenger/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("passenger", new Passenger());
        return "passenger/form";
    }

    @PostMapping
    public String createPassenger(@ModelAttribute("passenger") Passenger passenger, Model model) {
        try {
            passengerService.createPassenger(passenger);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "passenger/form";
        }
        return "redirect:/passengers";
    }

    @PostMapping("/{id}/delete")
    public String deletePassenger(@PathVariable("id") String id) {
        passengerService.deletePassenger(id);
        return "redirect:/passengers";
    }
}