package com.example.busstationmanagementsystem.model;

public class Driver extends Staff {
    private int yearsOfExperience;

    public Driver() {
        super();
    }

    public Driver(String id, String name, int yearsOfExperience) {
        super(id, name);
        this.yearsOfExperience = yearsOfExperience;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        this.yearsOfExperience = yearsOfExperience;
    }
}
