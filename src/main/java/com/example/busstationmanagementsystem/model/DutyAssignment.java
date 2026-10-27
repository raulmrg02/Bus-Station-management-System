package com.example.busstationmanagementsystem.model;

import java.util.Optional;

public class DutyAssignment {
    private String id;
    private String tripId;
    private String staffId;
    private DutyRole role;

    public DutyAssignment(String id, String tripId, String staffId, DutyRole role) {
        this.id = id;
        this.tripId = tripId;
        this.staffId = staffId;
        this.role = role;
    }

    public DutyAssignment(String id, String tripId, String staffId) {
        this.id = id;
        this.tripId = tripId;
        this.staffId = staffId;
        this.role = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getStaffId() {
        return staffId;
    }

    public void setStaffId(String staffId) {
        this.staffId = staffId;
    }

    public Optional<DutyRole> getRole() {
        return Optional.ofNullable(role);
    }

    public void setRole(DutyRole role) {
        this.role = role;
    }
}
