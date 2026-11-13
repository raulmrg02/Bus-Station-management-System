package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.RouteFileRepository;
import com.example.busstationmanagementsystem.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RouteService {

    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<Route, String> inMemoryRouteRepository;
    private final CrudRepository<Route, String> inFileRouteRepository;

    public RouteService(RouteRepository routeRepository, RouteFileRepository routeFileRepository) {
        this.inMemoryRouteRepository = routeRepository;
        this.inFileRouteRepository = routeFileRepository;
    }

    public void createRoute(Route route, RepositoryType repositoryType) {
        CrudRepository<Route, String> routeRepository = getRepository(repositoryType);
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
        return inMemoryRouteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Route not found: " + id));
    }

    public List<Route> getAllRoutes(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public boolean deleteRoute(String id, RepositoryType repositoryType) {
        return getRepository(repositoryType).deleteById(id);
    }

    private CrudRepository<Route, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileRouteRepository
                : inMemoryRouteRepository;
    }
}