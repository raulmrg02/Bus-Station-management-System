package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.repository.BusTripFileRepository;
import com.example.busstationmanagementsystem.repository.BusTripRepository;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.DutyAssignmentFileRepository;
import com.example.busstationmanagementsystem.repository.DutyAssignmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DutyAssignmentService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<DutyAssignment, String> inMemoryAssignmentRepository;
    private final CrudRepository<DutyAssignment, String> inFileAssignmentRepository;
    private final BusTripRepository inMemoryBusTripRepository;
    private final BusTripFileRepository inFileBusTripRepository;

    public DutyAssignmentService(DutyAssignmentRepository dutyAssignmentRepository,
                                 DutyAssignmentFileRepository dutyAssignmentFileRepository,
                                 BusTripRepository busTripRepository,
                                 BusTripFileRepository busTripFileRepository) {
        this.inMemoryAssignmentRepository = dutyAssignmentRepository;
        this.inFileAssignmentRepository = dutyAssignmentFileRepository;
        this.inMemoryBusTripRepository = busTripRepository;
        this.inFileBusTripRepository = busTripFileRepository;
    }

    public DutyAssignment assignDuty(DutyAssignment assignment) {
        return assignDuty(assignment, RepositoryType.IN_FILE);
    }

    public DutyAssignment assignDuty(DutyAssignment assignment, RepositoryType repositoryType) {
        CrudRepository<DutyAssignment, String> assignmentRepository = getRepository(repositoryType);
        if (assignment.getId() == null || assignment.getId().isBlank()) {
            throw new IllegalArgumentException("Duty assignment ID cannot be empty");
        }
        if (assignmentRepository.findById(assignment.getId()).isPresent()) {
            throw new IllegalArgumentException("Duty assignment already exists with ID: " + assignment.getId());
        }
        DutyAssignment saved = assignmentRepository.save(assignment);

        // Keep the trip's internal list in sync if tripId exists
        if (assignment.getTripId() != null) {
            inFileBusTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                trip.getAssignments().add(saved);
                inFileBusTripRepository.save(trip);
            });
            inMemoryBusTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                trip.getAssignments().add(saved);
                inMemoryBusTripRepository.save(trip);
            });
        }
        return saved;
    }

    public List<DutyAssignment> getAllAssignments() {
        return getAllAssignments(RepositoryType.IN_FILE);
    }

    public List<DutyAssignment> getAllAssignments(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public DutyAssignment getAssignmentById(String id) {
        return inFileAssignmentRepository.findById(id)
                .or(() -> inMemoryAssignmentRepository.findById(id))
                .orElseThrow(() -> new NoSuchElementException("Duty assignment not found: " + id));
    }

    public void deleteAssignment(String id) {
        deleteAssignment(id, RepositoryType.IN_FILE);
    }

    public void deleteAssignment(String id, RepositoryType repositoryType) {
        CrudRepository<DutyAssignment, String> assignmentRepository = getRepository(repositoryType);
        assignmentRepository.findById(id).ifPresent(assignment -> {
            if (assignment.getTripId() != null) {
                inFileBusTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                    trip.getAssignments().removeIf(a -> id.equals(a.getId()));
                    inFileBusTripRepository.save(trip);
                });
                inMemoryBusTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                    trip.getAssignments().removeIf(a -> id.equals(a.getId()));
                    inMemoryBusTripRepository.save(trip);
                });
            }
        });

        assignmentRepository.deleteById(id);
    }

    private CrudRepository<DutyAssignment, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileAssignmentRepository
                : inMemoryAssignmentRepository;
    }
}