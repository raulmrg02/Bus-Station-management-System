package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.service.BusStationService;
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
    public String getAllStations(Model model) {
        model.addAttribute("stations", stationService.getAllStations());
        return "bus-station/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("station", new BusStation());
        model.addAttribute("isUpdate", false);
        return "bus-station/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id, Model model) {
        model.addAttribute("station", stationService.getStationById(id));
        model.addAttribute("isUpdate", true);
        return "bus-station/form";
    }

    @PostMapping
    public String createStation(
            @ModelAttribute("station") BusStation station,
            Model model) {
        try {
            stationService.registerStation(station);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("isUpdate", false);
            return "bus-station/form";
        }
        return "redirect:/stations";
    }

    @PostMapping("/{id}")
    public String updateStation(@PathVariable("id") String id,
                                @ModelAttribute("station") BusStation station,
                                Model model) {
        try {
            stationService.updateStation(id, station);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("isUpdate", true);
            return "bus-station/form";
        }
        return "redirect:/stations";
    }

    @GetMapping("/{id}")
    public String showStationDetails(@PathVariable("id") String id, Model model) {
        BusStation station = stationService.getStationById(id);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", station.getId());
        fields.put("Name", station.getName());
        fields.put("City", station.getCity());
        fields.put("Trip IDs", station.getTrips().stream().map(trip -> trip.getId()).toList());
        model.addAttribute("title", "Bus Station " + station.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/stations");
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteStation(@PathVariable("id") String id) {
        stationService.deleteById(id);
        return "redirect:/stations";
    }
}