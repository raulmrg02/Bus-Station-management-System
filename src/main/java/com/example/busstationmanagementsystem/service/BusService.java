package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.repository.BusFileRepository;
import com.example.busstationmanagementsystem.repository.BusRepository;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BusService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<Bus, String> inMemoryBusRepository;
    private final CrudRepository<Bus, String> inFileBusRepository;

    public BusService(BusRepository busRepository, BusFileRepository busFileRepository) {
        this.inMemoryBusRepository = busRepository;
        this.inFileBusRepository = busFileRepository;
    }

    public Bus registerBus(Bus bus, RepositoryType repositoryType) {
        CrudRepository<Bus, String> busRepository = getRepository(repositoryType);
        if (bus.getId() == null || bus.getId().isBlank()) {
            throw new IllegalArgumentException("Bus ID cannot be empty");
        }
        if (busRepository.findById(bus.getId()).isPresent()) {
            throw new IllegalArgumentException("Bus already exists with ID: " + bus.getId());
        }
        return busRepository.save(bus);
    }

    public Bus getBusById(String id) {
        return inMemoryBusRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bus not found: " + id));
    }

    public void deleteById(String id, RepositoryType repositoryType) {
        getRepository(repositoryType).deleteById(id);
    }

    public List<Bus> getAllBuses(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    private CrudRepository<Bus, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusRepository
                : inMemoryBusRepository;
    }
}