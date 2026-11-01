package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.TripStatus;
import com.example.busstationmanagementsystem.service.BusTripService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trips")
public class BusTripController {

    private final BusTripService busTripService;

    public BusTripController(BusTripService busTripService) {
        this.busTripService = busTripService;
    }

    @PostMapping
    public BusTrip scheduleTrip(@RequestBody BusTrip trip) {
        return busTripService.scheduleTrip(trip);
    }

    @GetMapping("/{id}")
    public BusTrip getTripById(@PathVariable String id) {
        return busTripService.getTripById(id);
    }

    @GetMapping
    public List<BusTrip> getAllTrips() {
        return busTripService.getAllTrips();
    }

    @GetMapping("/by-route/{routeId}")
    public List<BusTrip> getTripsByRoute(@PathVariable String routeId) {
        return busTripService.getTripsByRoute(routeId);
    }

    @PutMapping("/{id}/status")
    public BusTrip updateStatus(@PathVariable String id, @RequestParam TripStatus status) {
        return busTripService.updateTripStatus(id, status);
    }

    @PostMapping("/{id}/assignments")
    public BusTrip assignDuty(@PathVariable String id, @RequestBody DutyAssignment assignment) {
        return busTripService.assignDuty(id, assignment);
    }
}