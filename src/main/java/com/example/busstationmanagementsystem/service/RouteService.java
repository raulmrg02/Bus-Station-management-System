package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public void createRoute(Route route) {
        if (routeRepository.findById(route.getId()).isPresent()) {
            throw new IllegalArgumentException("Route already exists with ID: " + route.getId());
        }
        if (route.getOrigin() == null || route.getDestination() == null) {
            throw new IllegalArgumentException("A route must have both an origin and destination station");
        }
        if (route.getOrigin().getId() != null
                && route.getOrigin().getId().equals(route.getDestination().getId())) {
            throw new IllegalArgumentException("Origin and destination cannot be the same station");
        }
        routeRepository.save(route);
    }

    public Route getRouteById(String id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + id));
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public boolean deleteRoute(String id) {
        return routeRepository.deleteById(id);
    }
}