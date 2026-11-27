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
    public String getAllPassengers(Model model) {
        model.addAttribute("passengers", passengerService.getAllPassengers());
        return "passenger/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("passenger", new Passenger());
        model.addAttribute("isUpdate", false);
        return "passenger/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id, Model model) {
        Passenger passenger = passengerService.getPassengerById(id);
        model.addAttribute("passenger", passenger);
        model.addAttribute("isUpdate", true);
        return "passenger/form";
    }

    @PostMapping
    public String createPassenger(@ModelAttribute("passenger") Passenger passenger,
                                  Model model) {
        try {
            passengerService.createPassenger(passenger);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("isUpdate", false);
            return "passenger/form";
        }
        return "redirect:/passengers";
    }

    @PostMapping("/{id}")
    public String updatePassenger(@PathVariable("id") String id,
                                  @ModelAttribute("passenger") Passenger passenger,
                                  Model model) {
        try {
            passengerService.updatePassenger(id, passenger);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("isUpdate", true);
            return "passenger/form";
        }
        return "redirect:/passengers";
    }

    @GetMapping("/{id}")
    public String showPassengerDetails(@PathVariable("id") String id, Model model) {
        Passenger passenger = passengerService.getPassengerById(id);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", passenger.getId());
        fields.put("Name", passenger.getName());
        fields.put("Currency", passenger.getCurrency());
        fields.put("Email", passenger.getEmail());
        fields.put("Ticket IDs", passenger.getTickets().stream().map(Ticket -> Ticket.getId()).toList());
        model.addAttribute("title", "Passenger " + passenger.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/passengers");
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deletePassenger(@PathVariable("id") String id) {
        passengerService.deletePassenger(id);
        return "redirect:/passengers";
    }
}