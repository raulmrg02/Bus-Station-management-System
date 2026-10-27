package com.example.busstationmanagementsystem.model;

public class Driver extends Staff {
    private String yearsOfExperience;

    public Driver(String id, String name, String yearsOfExperience) {
        super(id, name);
        this.yearsOfExperience = yearsOfExperience;
    }

    public String getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(String yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}
