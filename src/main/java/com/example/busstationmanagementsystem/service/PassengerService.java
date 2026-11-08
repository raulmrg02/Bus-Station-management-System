package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.repository.PassengerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class PassengerService {

    private final PassengerRepository passengerRepository;

    public PassengerService(PassengerRepository passengerRepository) {
        this.passengerRepository = passengerRepository;
    }

    public Passenger createPassenger(Passenger passenger) {
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

    public List<Passenger> getAllPassengers() {
        return passengerRepository.findAll();
    }

    public Passenger getPassengerById(String id) {
        return passengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public void deletePassenger(String id) {
        passengerRepository.deleteById(id);
    }
}