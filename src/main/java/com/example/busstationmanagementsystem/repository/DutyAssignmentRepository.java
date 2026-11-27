package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

@Repository
@Profile("memory")
public class DutyAssignmentRepository extends InMemoryRepository<DutyAssignment, String> {
    public DutyAssignmentRepository() {
        super(DutyAssignment::getId);
    }
    public List<DutyAssignment> findByStaffId(String staffId) {
        return findAll().stream()
                .filter(a -> staffId.equals(a.getStaffId()))
                .toList();
    }
    @Repository
    @Profile("file")
    public static class DutyAssignmentFileRepository extends InFileRepository<DutyAssignment> {
        public DutyAssignmentFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            super(Path.of(dataDirectory, "duty_assignments.json").toString(), DutyAssignment.class, DutyAssignment::getId);
        }
    }
}
