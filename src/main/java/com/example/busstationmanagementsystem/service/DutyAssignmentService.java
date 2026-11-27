package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DutyAssignmentService {
    private final CrudRepository<DutyAssignment, String> assignmentRepository;
    private final CrudRepository<BusTrip, String> busTripRepository;
    private final CrudRepository<Staff, String> staffRepository;
    private final CrudRepository<Route, String> routeRepository;
    private final CrudRepository<BusStation, String> stationRepository;

    public DutyAssignmentService(CrudRepository<DutyAssignment, String> assignmentRepository,
                                 CrudRepository<BusTrip, String> busTripRepository,
                                 CrudRepository<Staff, String> staffRepository,
                                 CrudRepository<Route, String> routeRepository,
                                 CrudRepository<BusStation, String> stationRepository) {
        this.assignmentRepository = assignmentRepository;
        this.busTripRepository = busTripRepository;
        this.staffRepository = staffRepository;
        this.routeRepository = routeRepository;
        this.stationRepository = stationRepository;
    }

    public DutyAssignment assignDuty(DutyAssignment assignment) {
        if (assignment.getId() == null || assignment.getId().isBlank()) {
            throw new IllegalArgumentException("Duty assignment ID cannot be empty");
        }
        if (assignmentRepository.findById(assignment.getId()).isPresent()) {
            throw new IllegalArgumentException("Duty assignment already exists with ID: " + assignment.getId());
        }
        getTrip(assignment.getTripId());
        Staff staff = getStaff(assignment.getStaffId());
        DutyAssignment saved = assignmentRepository.save(assignment);
        addAssignmentToTrip(saved);
        addAssignmentToStaff(saved, staff);
        syncTripReferences(assignment.getTripId());
        return saved;
    }

    public List<DutyAssignment> getAllAssignments() {
        return assignmentRepository.findAll();
    }

    public DutyAssignment updateAssignment(String id, DutyAssignment updatedAssignment) {
        DutyAssignment existingAssignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Duty assignment not found: " + id));
        getTrip(updatedAssignment.getTripId());
        Staff updatedStaff = getStaff(updatedAssignment.getStaffId());
        updatedAssignment.setId(id);
        DutyAssignment saved = assignmentRepository.save(updatedAssignment);
        String oldTripId = existingAssignment.getTripId();
        String newTripId = saved.getTripId();
        if (oldTripId != null) {
            busTripRepository.findById(oldTripId).ifPresent(trip -> {
                trip.getAssignments().removeIf(assignment -> id.equals(assignment.getId()));
                busTripRepository.save(trip);
            });
            syncTripReferences(oldTripId);
        }
        staffRepository.findById(existingAssignment.getStaffId()).ifPresent(staff -> {
            staff.getAssignments().removeIf(assignment -> id.equals(assignment.getId()));
            staffRepository.save(staff);
        });
        if (newTripId != null) {
            busTripRepository.findById(newTripId).ifPresent(trip -> {
                trip.getAssignments().removeIf(assignment -> id.equals(assignment.getId()));
                trip.getAssignments().add(saved);
                busTripRepository.save(trip);
            });
            syncTripReferences(newTripId);
        }
        addAssignmentToStaff(saved, updatedStaff);
        return saved;
    }

    public DutyAssignment getAssignmentById(String id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Duty assignment not found: " + id));
    }

    public void deleteAssignment(String id) {
        assignmentRepository.findById(id).ifPresent(assignment -> {
            if (assignment.getTripId() != null) {
                busTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
                    trip.getAssignments().removeIf(a -> id.equals(a.getId()));
                    busTripRepository.save(trip);
                });
                syncTripReferences(assignment.getTripId());
            }
            staffRepository.findById(assignment.getStaffId()).ifPresent(staff -> {
                staff.getAssignments().removeIf(existing -> id.equals(existing.getId()));
                staffRepository.save(staff);
            });
        });

        assignmentRepository.deleteById(id);
    }

    private void syncTripReferences(String tripId) {
        busTripRepository.findById(tripId).ifPresent(trip -> {
            routeRepository.findById(trip.getRouteId()).ifPresent(route -> {
                replaceTrip(route.getTrips(), trip);
                routeRepository.save(route);
                syncStation(route.getOrigin(), trip, stationRepository);
                syncStation(route.getDestination(), trip, stationRepository);
            });
        });
    }

    private void syncStation(BusStation station, BusTrip trip,
                             CrudRepository<BusStation, String> stationRepository) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            replaceTrip(storedStation.getTrips(), trip);
            stationRepository.save(storedStation);
        });
    }

    private void replaceTrip(List<BusTrip> trips, BusTrip trip) {
        trips.removeIf(existing -> trip.getId().equals(existing.getId()));
        trips.add(trip);
    }

    private BusTrip getTrip(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Trip is required");
        }
        return busTripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + id));
    }

    private Staff getStaff(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Staff member is required");
        }
        return staffRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Staff member not found: " + id));
    }

    private void addAssignmentToTrip(DutyAssignment assignment) {
        busTripRepository.findById(assignment.getTripId()).ifPresent(trip -> {
            trip.getAssignments().removeIf(existing -> assignment.getId().equals(existing.getId()));
            trip.getAssignments().add(assignment);
            busTripRepository.save(trip);
        });
    }

    private void addAssignmentToStaff(DutyAssignment assignment, Staff staff) {
        staff.getAssignments().removeIf(existing -> assignment.getId().equals(existing.getId()));
        staff.getAssignments().add(assignment);
        staffRepository.save(staff);
    }
}