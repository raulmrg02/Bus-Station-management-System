package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.example.busstationmanagementsystem.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StaffService {
    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    public Staff registerStaff(Staff staff) {
        if (staff.getId() == null || staff.getId().isBlank()) {
            throw new IllegalArgumentException("Staff ID cannot be empty");
        }
        if (staffRepository.findById(staff.getId()).isPresent()) {
            throw new IllegalArgumentException("Staff member already exists with ID: " + staff.getId());
        }
        return staffRepository.save(staff);
    }

    public Staff getStaffById(String id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Staff member not found: " + id));
    }

    public void deleteById(String id) {
        staffRepository.deleteById(id);
    }

    public List<Staff> getAllStaff() {
        return staffRepository.findAll();
    }

    public List<Driver> getAllDrivers() {
        return staffRepository.findAll().stream()
                .filter(Driver.class::isInstance)
                .map(Driver.class::cast)
                .toList();
    }

    public List<TripManager> getAllTripManagers() {
        return staffRepository.findAll().stream()
                .filter(TripManager.class::isInstance)
                .map(TripManager.class::cast)
                .toList();
    }
}