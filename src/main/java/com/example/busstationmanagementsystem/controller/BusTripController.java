package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.TripStatus;
import com.example.busstationmanagementsystem.service.BusTripService;
import com.example.busstationmanagementsystem.service.BusTripService.RepositoryType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;
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
        model.addAttribute("isUpdate", false);
        return "bus-trip/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id,
                                 @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                 Model model) {
        BusTrip trip = tripService.getAllTrips(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Bus trip not found: " + id));
        model.addAttribute("trip", trip);
        model.addAttribute("statuses", TripStatus.values());
        model.addAttribute("selectedRepository", repository);
        model.addAttribute("isUpdate", true);
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
            model.addAttribute("isUpdate", false);
            return "bus-trip/form";
        }
        return "redirect:/trips?repository=" + repository;
    }

    @PostMapping("/{id}")
    public String updateTrip(@PathVariable("id") String id,
                             @ModelAttribute("trip") BusTrip trip,
                             @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                             Model model) {
        try {
            tripService.updateTrip(id, trip, repository);
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("statuses", TripStatus.values());
            model.addAttribute("selectedRepository", repository);
            model.addAttribute("isUpdate", true);
            return "bus-trip/form";
        }
        return "redirect:/trips?repository=" + repository;
    }

    @GetMapping("/{id}")
    public String showTripDetails(@PathVariable("id") String id,
                                  @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                  Model model) {
        BusTrip trip = tripService.getAllTrips(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Bus trip not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", trip.getId());
        fields.put("Route ID", trip.getRouteId());
        fields.put("Bus ID", trip.getBusId());
        fields.put("Start Time", trip.getStartTime());
        fields.put("Arrival Time", trip.getArrivalTime());
        fields.put("Status", trip.getStatus());
        fields.put("Ticket IDs", trip.getTickets().stream().map(ticket -> ticket.getId()).toList());
        fields.put("Duty Assignment IDs", trip.getAssignments().stream().map(assignment -> assignment.getId()).toList());
        model.addAttribute("title", "Bus Trip " + trip.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/trips");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteTrip(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        tripService.deleteTrip(id, repository);
        return "redirect:/trips?repository=" + repository;
    }
}