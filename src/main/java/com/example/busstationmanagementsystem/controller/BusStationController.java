package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.service.BusStationService;
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
    public String getAllStations(Model model) {
        model.addAttribute("stations", stationService.getAllStations());
        return "station/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("station", new BusStation(null, null, null));
        return "station/form";
    }

    @PostMapping
    public String createStation(@ModelAttribute("station") BusStation station) {
        stationService.createStation(station);
        return "redirect:/stations";
    }

    @PostMapping("/{id}/delete")
    public String deleteStation(@PathVariable("id") String id) {
        stationService.deleteStation(id);
        return "redirect:/stations";
    }
}