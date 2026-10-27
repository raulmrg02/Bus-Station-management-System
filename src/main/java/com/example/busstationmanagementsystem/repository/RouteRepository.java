package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Route;
import org.springframework.stereotype.Repository;

@Repository
public class RouteRepository extends InMemoryRepository<Route, String> {
    public RouteRepository() {
        super(Route::getId);
    }
}

