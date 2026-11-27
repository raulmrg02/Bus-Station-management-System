package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.repository.CrudRepository;
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

    public PassengerService(PassengerRepository passengerRepository) {
        this.inMemoryPassengerRepository = passengerRepository.forMode(com.example.busstationmanagementsystem.repository.StorageMode.IN_MEMORY);
        this.inFilePassengerRepository = passengerRepository.forMode(com.example.busstationmanagementsystem.repository.StorageMode.IN_FILE);
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

    public List<Passenger> getAllPassengers(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public Passenger updatePassenger(String id, Passenger updatedPassenger, RepositoryType repositoryType) {
        CrudRepository<Passenger, String> passengerRepository = getRepository(repositoryType);
        Passenger existingPassenger = passengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
        if (updatedPassenger.getEmail() != null
                && !updatedPassenger.getEmail().isBlank()
                && passengerRepository.findAll().stream()
                .anyMatch(existing -> !id.equals(existing.getId())
                        && updatedPassenger.getEmail().equalsIgnoreCase(existing.getEmail()))) {
            throw new IllegalArgumentException("Passenger already exists with email: " + updatedPassenger.getEmail());
        }
        updatedPassenger.setId(id);
        updatedPassenger.setTickets(existingPassenger.getTickets());
        return passengerRepository.save(updatedPassenger);
    }

    public Passenger getPassengerById(String id) {
        return inMemoryPassengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public void deletePassenger(String id, RepositoryType repositoryType) {
        getRepository(repositoryType).deleteById(id);
    }

    private CrudRepository<Passenger, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFilePassengerRepository
                : inMemoryPassengerRepository;
    }
}