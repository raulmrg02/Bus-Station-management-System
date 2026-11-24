package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import org.springframework.stereotype.Repository;

@Repository
public class DutyAssignmentRepository extends InMemoryRepository<DutyAssignment, String> {
    private final CrudRepository<DutyAssignment, String> fileRepository;

    public DutyAssignmentRepository() {
        super(DutyAssignment::getId);
        fileRepository = new InFileRepository<DutyAssignment>("src/main/resources/data/duty_assignments.json", DutyAssignment.class, DutyAssignment::getId) {};
    }

    public CrudRepository<DutyAssignment, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
