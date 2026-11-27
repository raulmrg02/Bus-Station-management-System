package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class DutyAssignmentRepository extends InMemoryRepository<DutyAssignment, String> {
    public DutyAssignmentRepository() {
        super(DutyAssignment::getId);
    }
    public List<DutyAssignment> findByStaffId(String staffId) {
        return findAll().stream()
                .filter(a -> staffId.equals(a.getStaffId()))
                .toList();
    }

    public List<DutyAssignment> findByTripId(String tripId) {
        return findAll().stream()
                .filter(a -> tripId.equals(a.getTripId()))
                .toList();
    }
}
