package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.service.BusStationService;
import com.example.busstationmanagementsystem.service.RouteService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

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
        BusStationService.RepositoryType stationRepository = repository == RouteService.RepositoryType.IN_FILE
                ? BusStationService.RepositoryType.IN_FILE
                : BusStationService.RepositoryType.IN_MEMORY;
        try {
            Route route = new Route(
                    id,
                    stationService.getStationById(originId, stationRepository),
                    stationService.getStationById(destinationId, stationRepository),
                    distance
            );
            routeService.createRoute(route, repository);
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("stations", stationService.getAllStations(repository == RouteService.RepositoryType.IN_FILE
                    ? BusStationService.RepositoryType.IN_FILE
                    : BusStationService.RepositoryType.IN_MEMORY));
            model.addAttribute("selectedRepository", repository);
            return "route/form";
        }
        return "redirect:/routes?repository=" + repository;
    }

    @GetMapping("/{id}")
    public String showRouteDetails(@PathVariable String id,
                                   @RequestParam(defaultValue = "IN_MEMORY") RouteService.RepositoryType repository,
                                   Model model) {
        Route route = routeService.getAllRoutes(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", route.getId());
        fields.put("Origin", route.getOrigin() != null ? route.getOrigin().getName() + " (" + route.getOrigin().getId() + ")" : null);
        fields.put("Destination", route.getDestination() != null ? route.getDestination().getName() + " (" + route.getDestination().getId() + ")" : null);
        fields.put("Distance", route.getDistance());
        fields.put("Trip IDs", route.getTrips().stream().map(trip -> trip.getId()).toList());
        model.addAttribute("title", "Route " + route.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/routes");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteRoute(@PathVariable String id,
                              @RequestParam(defaultValue = "IN_MEMORY") RouteService.RepositoryType repository) {
        routeService.deleteRoute(id, repository);
        return "redirect:/routes?repository=" + repository;
    }
}