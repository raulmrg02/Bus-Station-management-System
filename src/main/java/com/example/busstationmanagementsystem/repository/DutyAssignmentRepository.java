package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class DutyAssignmentRepository extends InMemoryRepository<DutyAssignment, String> {
    public DutyAssignmentRepository() {
        super(DutyAssignment::getId);
    }

    @Repository
    @Profile("file")
    public static class DutyAssignmentFileRepository extends InFileRepository<DutyAssignment> {
        public DutyAssignmentFileRepository() {
            super(Path.of(DATA_DIRECTORY, "duty_assignments.json").toString(), DutyAssignment.class, DutyAssignment::getId);
        }
    }
}
