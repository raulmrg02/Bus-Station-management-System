package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusService {
    private final CrudRepository<Bus, String> busRepository;

    public BusService(CrudRepository<Bus, String> busRepository) {
        this.busRepository = busRepository;
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

    public void deleteById(String id) {
        busRepository.deleteById(id);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }
}