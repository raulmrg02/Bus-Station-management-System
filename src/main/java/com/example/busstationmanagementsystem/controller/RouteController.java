package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.service.BusStationService;
import com.example.busstationmanagementsystem.service.RouteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;
    private final BusStationService stationService;

    public RouteController(RouteService routeService, BusStationService stationService) {
        this.routeService = routeService;
        this.stationService = stationService;
    }

    @GetMapping
    public String getAllRoutes(@RequestParam(defaultValue = "IN_MEMORY") RouteService.RepositoryType repository,
                               Model model) {
        model.addAttribute("routes", routeService.getAllRoutes(repository));
        model.addAttribute("selectedRepository", repository);
        return "route/index";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(defaultValue = "IN_MEMORY") RouteService.RepositoryType repository,
                                 Model model) {
        model.addAttribute("stations", stationService.getAllStations(repository == RouteService.RepositoryType.IN_FILE
                ? BusStationService.RepositoryType.IN_FILE
                : BusStationService.RepositoryType.IN_MEMORY));
        model.addAttribute("selectedRepository", repository);
        return "route/form";
    }

    @PostMapping
    public String createRoute(@RequestParam String id,
                              @RequestParam String originId,
                              @RequestParam String destinationId,
                              @RequestParam BigDecimal distance,
                              @RequestParam(defaultValue = "IN_MEMORY") RouteService.RepositoryType repository,
                              Model model) {
        Route route = new Route(
                id,
                stationService.getStationById(originId),
                stationService.getStationById(destinationId),
                distance
        );
        try {
            routeService.createRoute(route, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("stations", stationService.getAllStations(repository == RouteService.RepositoryType.IN_FILE
                    ? BusStationService.RepositoryType.IN_FILE
                    : BusStationService.RepositoryType.IN_MEMORY));
            model.addAttribute("selectedRepository", repository);
            return "route/form";
        }
        return "redirect:/routes?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deleteRoute(@PathVariable String id,
                              @RequestParam(defaultValue = "IN_MEMORY") RouteService.RepositoryType repository) {
        routeService.deleteRoute(id, repository);
        return "redirect:/routes?repository=" + repository;
    }
}