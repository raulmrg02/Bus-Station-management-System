package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusStatus;
import com.example.busstationmanagementsystem.service.BusService;
import com.example.busstationmanagementsystem.service.BusService.RepositoryType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public String getAllBuses(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("busList", busService.getAllBuses(repository));
        model.addAttribute("selectedRepository", repository);
        return "bus/index";
    }

    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("bus", new Bus());
        model.addAttribute("statuses", BusStatus.values());
        model.addAttribute("selectedRepository", repository);
        return "bus/form";
    }

    @PostMapping
    public String createBus(
            @ModelAttribute("bus") Bus bus,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        try {
            busService.registerBus(bus, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("statuses", BusStatus.values());
            model.addAttribute("selectedRepository", repository);
            return "bus/form";
        }
        return "redirect:/buses?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deleteBus(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        busService.deleteById(id, repository);
        return "redirect:/buses?repository=" + repository;
    }
}