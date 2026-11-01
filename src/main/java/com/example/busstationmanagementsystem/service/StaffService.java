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
        return staffRepository.save(staff);
    }

    public Staff getStaffById(String id) {
        return staffRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Staff member not found: " + id));
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