package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.service.StationAndRouteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stations-and-routes")
public class StationAndRouteController {

    private final StationAndRouteService stationAndRouteService;

    public StationAndRouteController(StationAndRouteService stationAndRouteService) {
        this.stationAndRouteService = stationAndRouteService;
    }

    @PostMapping("/stations")
    public BusStation createStation(@RequestBody BusStation station) {
        return stationAndRouteService.createStation(station);
    }

    @GetMapping("/stations/{id}")
    public BusStation getStationById(@PathVariable String id) {
        return stationAndRouteService.getStationById(id);
    }

    @GetMapping("/stations")
    public List<BusStation> getAllStations() {
        return stationAndRouteService.getAllStations();
    }

    @GetMapping("/stations/by-city")
    public List<BusStation> getStationsByCity(@RequestParam String city) {
        return stationAndRouteService.getStationsByCity(city);
    }

    @PostMapping("/routes")
    public Route createRoute(@RequestBody Route route) {
        return stationAndRouteService.createRoute(route);
    }

    @GetMapping("/routes/{id}")
    public Route getRouteById(@PathVariable String id) {
        return stationAndRouteService.getRouteById(id);
    }

    @GetMapping("/routes")
    public List<Route> getAllRoutes() {
        return stationAndRouteService.getAllRoutes();
    }
}