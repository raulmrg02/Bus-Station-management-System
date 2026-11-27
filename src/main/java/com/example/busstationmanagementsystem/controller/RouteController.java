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
    public String getAllRoutes(Model model) {
        model.addAttribute("routes", routeService.getAllRoutes());
        return "route/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("route", new Route());
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("isUpdate", false);
        return "route/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Route route = routeService.getRouteById(id);
        model.addAttribute("route", route);
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("isUpdate", true);
        return "route/form";
    }

    @PostMapping
    public String createRoute(@RequestParam String id,
                              @RequestParam String originId,
                              @RequestParam String destinationId,
                              @RequestParam BigDecimal distance,
                              Model model) {
        try {
            Route route = new Route(
                    id,
                    stationService.getStationById(originId),
                    stationService.getStationById(destinationId),
                    distance
            );
            routeService.createRoute(route);
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("route", new Route());
            model.addAttribute("stations", stationService.getAllStations());
            model.addAttribute("isUpdate", false);
            return "route/form";
        }
        return "redirect:/routes";
    }

    @PostMapping("/{id}")
    public String updateRoute(@PathVariable String id,
                              @RequestParam String originId,
                              @RequestParam String destinationId,
                              @RequestParam BigDecimal distance,
                              Model model) {
       try {
           routeService.updateRoute(id, new Route(
                   id,
                   stationService.getStationById(originId),
                   stationService.getStationById(destinationId),
                   distance
           ));
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("route", new Route(
                    id,
                    stationService.getAllStations().stream()
                            .filter(station -> station.getId().equals(originId))
                            .findFirst().orElse(null),
                    stationService.getAllStations().stream()
                            .filter(station -> station.getId().equals(destinationId))
                            .findFirst().orElse(null),
                    distance
            ));
            model.addAttribute("stations", stationService.getAllStations());
            model.addAttribute("isUpdate", true);
            return "route/form";
        }
        return "redirect:/routes";
    }


    @GetMapping("/{id}")
    public String showRouteDetails(@PathVariable String id, Model model) {
        Route route = routeService.getRouteById(id);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", route.getId());
        fields.put("Origin", route.getOrigin() != null ? route.getOrigin().getName() + " (" + route.getOrigin().getId() + ")" : null);
        fields.put("Destination", route.getDestination() != null ? route.getDestination().getName() + " (" + route.getDestination().getId() + ")" : null);
        fields.put("Distance", route.getDistance());
        fields.put("Trip IDs", route.getTrips().stream().map(trip -> trip.getId()).toList());
        model.addAttribute("title", "Route " + route.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/routes");
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteRoute(@PathVariable String id) {
        routeService.deleteRoute(id);
        return "redirect:/routes";
    }
}