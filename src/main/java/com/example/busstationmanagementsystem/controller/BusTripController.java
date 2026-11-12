package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.TripStatus;
import com.example.busstationmanagementsystem.service.BusTripService;
import com.example.busstationmanagementsystem.service.BusTripService.RepositoryType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/trips")
public class BusTripController {

    private final BusTripService tripService;

    public BusTripController(BusTripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public String getAllTrips(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("trips", tripService.getAllTrips(repository));
        model.addAttribute("selectedRepository", repository);
        return "bus-trip/index";
    }

    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("trip", new BusTrip());
        model.addAttribute("statuses", TripStatus.values());
        model.addAttribute("selectedRepository", repository);
        return "bus-trip/form";
    }

    @PostMapping
    public String createTrip(
            @ModelAttribute("trip") BusTrip trip,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        try {
            tripService.scheduleTrip(trip, repository);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("statuses", TripStatus.values());
            model.addAttribute("selectedRepository", repository);
            return "bus-trip/form";
        }
        return "redirect:/trips?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deleteTrip(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        tripService.deleteTrip(id, repository);
        return "redirect:/trips?repository=" + repository;
    }
}