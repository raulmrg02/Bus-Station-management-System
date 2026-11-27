package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusStatus;
import com.example.busstationmanagementsystem.service.BusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @GetMapping
    public String getAllBuses(Model model) {
        model.addAttribute("busList", busService.getAllBuses());
        return "bus/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bus", new Bus());
        model.addAttribute("statuses", BusStatus.values());
        model.addAttribute("isUpdate", false);
        return "bus/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("bus", busService.getBusById(id));
        model.addAttribute("statuses", BusStatus.values());
        model.addAttribute("isUpdate", true);
        return "bus/form";
    }

    @PostMapping
    public String createBus(
            @ModelAttribute("bus") Bus bus,
            Model model) {
        try {
            busService.registerBus(bus);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("statuses", BusStatus.values());
            model.addAttribute("isUpdate", false);
            return "bus/form";
        }
        return "redirect:/buses";
    }

    @PostMapping("/{id}")
    public String updateBus(@PathVariable("id") String id,
                            @ModelAttribute("bus") Bus bus,
                            Model model) {
        try {
            busService.updateBus(id, bus);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("statuses", BusStatus.values());
            model.addAttribute("isUpdate", true);
            return "bus/form";
        }
        return "redirect:/buses";
    }

    @GetMapping("/{id}")
    public String showBusDetails(@PathVariable("id") String id, Model model) {
        Bus bus = busService.getBusById(id);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", bus.getId());
        fields.put("Registration Number", bus.getRegistrationNumber());
        fields.put("Capacity", bus.getCapacity());
        fields.put("Model", bus.getModel());
        fields.put("Manufacture Year", bus.getManufactureYear());
        fields.put("Status", bus.getStatus());
        model.addAttribute("title", "Bus " + bus.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/buses");
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteBus(@PathVariable("id") String id) {
        busService.deleteById(id);
        return "redirect:/buses";
    }
}