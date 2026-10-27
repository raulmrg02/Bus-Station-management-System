package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Staff;
import org.springframework.stereotype.Repository;

@Repository
public class StaffRepository extends InMemoryRepository<Staff, String> {
    public StaffRepository() {
        super(Staff::getId);
    }
}
