package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Route;
import org.springframework.stereotype.Repository;

@Repository
public class RouteRepository extends InMemoryRepository<Route, String> {
    private final CrudRepository<Route, String> fileRepository;

    public RouteRepository() {
        super(Route::getId);
        fileRepository = new InFileRepository<Route>("src/main/resources/data/routes.json", Route.class, Route::getId) {};
    }

    public CrudRepository<Route, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
