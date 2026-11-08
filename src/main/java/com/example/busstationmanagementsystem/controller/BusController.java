package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusStatus;
import com.example.busstationmanagementsystem.service.BusService;
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
    public String getAllBuses(Model model) {
        model.addAttribute("buses", busService.getAllBuses());
        return "bus/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bus", new Bus());
        model.addAttribute("statuses", BusStatus.values());
        return "bus/form";
    }

    @PostMapping
    public String createBus(@ModelAttribute("bus") Bus bus, Model model) {
        try {
            busService.createBus(bus);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("statuses", BusStatus.values());
            return "bus/form";
        }
        return "redirect:/buses";
    }

    @PostMapping("/{id}/delete")
    public String deleteBus(@PathVariable("id") String id) {
        busService.deleteBus(id);
        return "redirect:/buses";
    }
}