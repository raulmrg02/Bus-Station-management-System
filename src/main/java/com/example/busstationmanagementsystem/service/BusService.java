package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusService {
    private final CrudRepository<Bus, String> busRepository;
    private final BusTripService busTripService;

    public BusService(CrudRepository<Bus, String> busRepository, BusTripService busTripService) {
        this.busRepository = busRepository;
        this.busTripService = busTripService;
    }

    public Bus registerBus(Bus bus) {
        if (bus.getId() == null || bus.getId().isBlank()) {
            throw new IllegalArgumentException("Bus ID cannot be empty");
        }
        if (busRepository.findById(bus.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus already exists with ID: " + bus.getId());
        }
        return busRepository.save(bus);
    }

    public Bus getBusById(String id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bus not found: " + id));
    }

    public Bus updateBus(String id, Bus updatedBus) {
        busRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bus not found: " + id));
        updatedBus.setId(id);
        return busRepository.save(updatedBus);
    }

    public void deleteById(String id) {
        busTripService.getAllTrips().stream()
                .filter(trip -> id.equals(trip.getBusId()))
                .map(trip -> trip.getId())
                .toList()
                .forEach(busTripService::deleteTrip);
        busRepository.deleteById(id);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }
}