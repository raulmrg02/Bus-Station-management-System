package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.service.RouteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public Route createRoute(@RequestBody Route route) {
        return routeService.createRoute(route);
    }

    @GetMapping("/{id}")
    public Route getRouteById(@PathVariable String id) {
        return routeService.getRouteById(id);
    }

    @GetMapping
    public List<Route> getAllRoutes() {
        return routeService.getAllRoutes();
    }
}