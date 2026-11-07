package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.TripStatus;
import com.example.busstationmanagementsystem.service.BusTripService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/trips")
public class BusTripController {

    private final BusTripService tripService;

    public BusTripController(BusTripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public String getAllTrips(Model model) {
        model.addAttribute("trips", tripService.getAllTrips());
        return "bus-trip/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("trip", new BusTrip());
        model.addAttribute("statuses", TripStatus.values());
        return "bus-trip/form";
    }

    @PostMapping
    public String createTrip(@ModelAttribute("trip") BusTrip trip) {
        tripService.scheduleTrip(trip);
        return "redirect:/trips";
    }

    @PostMapping("/{id}/delete")
    public String deleteTrip(@PathVariable("id") String id) {
        tripService.deleteTrip(id);
        return "redirect:/trips";
    }
}