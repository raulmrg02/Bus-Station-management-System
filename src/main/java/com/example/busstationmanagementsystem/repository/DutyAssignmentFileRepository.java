package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import org.springframework.stereotype.Repository;

@Repository
public class DutyAssignmentFileRepository extends InFileRepository<DutyAssignment> {
    public DutyAssignmentFileRepository() {
        super("src/main/resources/data/duty_assignments.json", DutyAssignment.class, DutyAssignment::getId);
    }
}
