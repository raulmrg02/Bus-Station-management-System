package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import org.springframework.stereotype.Repository;

@Repository
public class DutyAssignmentRepository extends InMemoryRepository<DutyAssignment, String> {
    public DutyAssignmentRepository() {
        super(DutyAssignment::getId);
    }
}
