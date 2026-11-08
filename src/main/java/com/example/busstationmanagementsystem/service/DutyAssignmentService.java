package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DutyAssignmentService {

    private final CrudRepository<DutyAssignment, String> dutyAssignmentRepository;
    private final CrudRepository<BusTrip, String> busTripRepository;

    public DutyAssignmentService(CrudRepository<DutyAssignment, String> dutyAssignmentRepository,
                                 CrudRepository<BusTrip, String> busTripRepository) {
        this.dutyAssignmentRepository = dutyAssignmentRepository;
        this.busTripRepository = busTripRepository;
    }

    public DutyAssignment assignDuty(DutyAssignment assignment) {
        if (dutyAssignmentRepository.findById(assignment.getId()).isPresent()) {
            throw new IllegalArgumentException("Duty assignment already exists with ID: " + assignment.getId());
        }
        DutyAssignment saved = dutyAssignmentRepository.save(assignment);

        // Keep the trip's internal list in sync if tripId exists
        if (assignment.getTripId() != null) {
            busTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                trip.getAssignments().add(saved);
                busTripRepository.save(trip);
            });
        }
        return saved;
    }

    public List<DutyAssignment> getAllAssignments() {
        return dutyAssignmentRepository.findAll();
    }

    public DutyAssignment getAssignmentById(String id) {
        return dutyAssignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Duty assignment not found: " + id));
    }

    public void deleteAssignment(String id) {
        // Also remove from parent trip list if present
        dutyAssignmentRepository.findById(id).ifPresent(assignment -> {
            if (assignment.getTripId() != null) {
                busTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                    trip.getAssignments().removeIf(a -> id.equals(a.getId()));
                    busTripRepository.save(trip);
                });
            }
        });

        dutyAssignmentRepository.deleteById(id);
    }
}