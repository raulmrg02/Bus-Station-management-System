package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.PassengerFileRepository;
import com.example.busstationmanagementsystem.repository.PassengerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PassengerService {

    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<Passenger, String> inMemoryPassengerRepository;
    private final CrudRepository<Passenger, String> inFilePassengerRepository;

    public PassengerService(PassengerRepository passengerRepository,
                            PassengerFileRepository passengerFileRepository) {
        this.inMemoryPassengerRepository = passengerRepository;
        this.inFilePassengerRepository = passengerFileRepository;
    }

    public Passenger createPassenger(Passenger passenger, RepositoryType repositoryType) {
        CrudRepository<Passenger, String> passengerRepository = getRepository(repositoryType);
        if (passengerRepository.findById(passenger.getId()).isPresent()) {
            throw new IllegalArgumentException("Passenger already exists with ID: " + passenger.getId());
        }
        if (passenger.getEmail() != null
                && !passenger.getEmail().isBlank()
                && passengerRepository.findAll().stream()
                .anyMatch(existingPassenger -> passenger.getEmail().equalsIgnoreCase(existingPassenger.getEmail()))) {
            throw new IllegalArgumentException("Passenger already exists with email: " + passenger.getEmail());
        }
        return passengerRepository.save(passenger);
    }

    public Passenger createPassenger(Passenger passenger) {
        return createPassenger(passenger, RepositoryType.IN_MEMORY);
    }

    public List<Passenger> getAllPassengers(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public List<Passenger> getAllPassengers() {
        return getAllPassengers(RepositoryType.IN_MEMORY);
    }

    public Passenger getPassengerById(String id) {
        return inMemoryPassengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public void deletePassenger(String id, RepositoryType repositoryType) {
        getRepository(repositoryType).deleteById(id);
    }

    public void deletePassenger(String id) {
        deletePassenger(id, RepositoryType.IN_MEMORY);
    }

    private CrudRepository<Passenger, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFilePassengerRepository
                : inMemoryPassengerRepository;
    }
}