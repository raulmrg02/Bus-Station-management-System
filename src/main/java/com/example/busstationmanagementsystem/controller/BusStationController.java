package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.service.BusStationService;
import com.example.busstationmanagementsystem.service.BusStationService.RepositoryType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
            return "bus-station/form";
        }
        return "redirect:/stations?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deleteStation(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        stationService.deleteById(id, repository);
        return "redirect:/stations?repository=" + repository;
    }
}