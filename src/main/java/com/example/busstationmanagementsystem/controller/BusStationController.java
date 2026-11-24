package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.service.BusStationService;
import com.example.busstationmanagementsystem.service.BusStationService.RepositoryType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/stations")
public class BusStationController {

    private final BusStationService stationService;

    public BusStationController(BusStationService stationService) {
        this.stationService = stationService;
    }

    @GetMapping
    public String getAllStations(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("stations", stationService.getAllStations(repository));
        model.addAttribute("selectedRepository", repository);
        return "bus-station/index";
    }

    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("station", new BusStation());
        model.addAttribute("selectedRepository", repository);
        model.addAttribute("isUpdate", false);
        return "bus-station/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id,
                                 @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                 Model model) {
        model.addAttribute("station", stationService.getStationById(id, repository));
        model.addAttribute("selectedRepository", repository);
        model.addAttribute("isUpdate", true);
        return "bus-station/form";
    }

    @PostMapping
    public String createStation(
            @ModelAttribute("station") BusStation station,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        try {
            stationService.registerStation(station, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("selectedRepository", repository);
            model.addAttribute("isUpdate", false);
            return "bus-station/form";
        }
        return "redirect:/stations?repository=" + repository;
    }

    @PostMapping("/{id}")
    public String updateStation(@PathVariable("id") String id,
                                @ModelAttribute("station") BusStation station,
                                @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                Model model) {
        try {
            stationService.updateStation(id, station, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("selectedRepository", repository);
            model.addAttribute("isUpdate", true);
            return "bus-station/form";
        }
        return "redirect:/stations?repository=" + repository;
    }

    @GetMapping("/{id}")
    public String showStationDetails(@PathVariable("id") String id,
                                     @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                     Model model) {
        BusStation station = stationService.getAllStations(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Bus station not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", station.getId());
        fields.put("Name", station.getName());
        fields.put("City", station.getCity());
        fields.put("Trip IDs", station.getTrips().stream().map(trip -> trip.getId()).toList());
        model.addAttribute("title", "Bus Station " + station.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/stations");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteStation(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        stationService.deleteById(id, repository);
        return "redirect:/stations?repository=" + repository;
    }
}