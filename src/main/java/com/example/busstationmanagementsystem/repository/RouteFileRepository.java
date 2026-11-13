package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Route;
import org.springframework.stereotype.Repository;

@Repository
public class RouteFileRepository extends InFileRepository<Route> {
    public RouteFileRepository() {
        super("src/main/resources/data/routes.json", Route.class, Route::getId);
    }
}
