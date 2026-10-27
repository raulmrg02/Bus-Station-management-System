package com.example.busstationmanagementsystem.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Staff {
    private String id;
    private String name;

    private List<DutyAssignment> assignments = new ArrayList<>();

    public Staff(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DutyAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<DutyAssignment> assignments) {
        this.assignments = assignments;
    }
}
