package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.StaffFileRepository;
import com.example.busstationmanagementsystem.repository.StaffRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StaffService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<Staff, String> inMemoryStaffRepository;
    private final CrudRepository<Staff, String> inFileStaffRepository;

    public StaffService(StaffRepository staffRepository, StaffFileRepository staffFileRepository) {
        this.inMemoryStaffRepository = staffRepository;
        this.inFileStaffRepository = staffFileRepository;
    }

    public Staff registerStaff(Staff staff, RepositoryType repositoryType) {
        CrudRepository<Staff, String> staffRepository = getRepository(repositoryType);
        if (staff.getId() == null || staff.getId().isBlank()) {
            throw new IllegalArgumentException("Staff ID cannot be empty");
        }
        if (staffRepository.findById(staff.getId()).isPresent()) {
            throw new IllegalArgumentException("Staff member already exists with ID: " + staff.getId());
        }
        return staffRepository.save(staff);
    }

    public Staff getStaffById(String id) {
        return inMemoryStaffRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Staff member not found: " + id));
    }

    public void deleteById(String id, RepositoryType repositoryType) {
        getRepository(repositoryType).deleteById(id);
    }

    public List<Staff> getAllStaff(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public List<Driver> getAllDrivers() {
        return inMemoryStaffRepository.findAll().stream()
                .filter(Driver.class::isInstance)
                .map(Driver.class::cast)
                .toList();
    }

    public List<TripManager> getAllTripManagers() {
        return inMemoryStaffRepository.findAll().stream()
                .filter(TripManager.class::isInstance)
                .map(TripManager.class::cast)
                .toList();
    }

    private CrudRepository<Staff, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileStaffRepository
                : inMemoryStaffRepository;
    }
}