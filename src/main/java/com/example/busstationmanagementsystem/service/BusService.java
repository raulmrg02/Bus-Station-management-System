package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusStatus;
import com.example.busstationmanagementsystem.repository.BusRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusService {
    private final BusRepository busRepository;

    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    public Bus createBus(Bus bus) {
        if (bus.getId() == null || bus.getId().isBlank()) {
            throw new IllegalArgumentException("Bus ID cannot be empty");
        }
        return busRepository.save(bus);
    }

    public Bus getBusById(String id) {
        return busRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bus not found with ID: " + id));
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public List<Bus> getActiveBuses() {
        return busRepository.findAll().stream()
                .filter(b -> b.getStatus() == BusStatus.ACTIVE)
                .toList();
    }

    public Bus updateBusStatus(String id, BusStatus status) {
        Bus bus = getBusById(id);
        bus.setStatus(status);
        return busRepository.save(bus);
    }

    public boolean deleteBus(String id) {
        return busRepository.deleteById(id);
    }
}