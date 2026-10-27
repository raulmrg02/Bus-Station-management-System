package com.example.busstationmanagementsystem.model;

public class TripManager extends Staff {
    private String employeeCode;

    public TripManager(String id, String name, String employeeCode) {
        super(id, name);
        this.employeeCode = employeeCode;
    }

    public String getEmployeeCode() {
        return this.employeeCode;
    }

    public void setEmployeeCode(String newEmployeeCode) {
        this.employeeCode = newEmployeeCode;
    }
}
