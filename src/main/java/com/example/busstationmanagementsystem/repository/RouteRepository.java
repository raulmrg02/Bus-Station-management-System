package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Route;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class RouteRepository extends InMemoryRepository<Route, String> {
    public RouteRepository() {
        super(Route::getId);
    }

    @Repository
    @Profile("file")
    public static class RouteFileRepository extends InFileRepository<Route> {
        public RouteFileRepository() {
            super(Path.of(DATA_DIRECTORY, "routes.json").toString(), Route.class, Route::getId);
        }
    }
}
