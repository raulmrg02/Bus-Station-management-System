package com.example.busstationmanagementsystem.Controller;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.example.busstationmanagementsystem.service.StaffService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping
    public Staff registerStaff(@RequestBody Staff staff) {
        return staffService.registerStaff(staff);
    }

    @PostMapping("/drivers")
    public Driver registerDriver(@RequestBody Driver driver) {
        return (Driver) staffService.registerStaff(driver);
    }

    @PostMapping("/managers")
    public TripManager registerManager(@RequestBody TripManager manager) {
        return (TripManager) staffService.registerStaff(manager);
    }

    @GetMapping("/{id}")
    public Staff getStaffById(@PathVariable String id) {
        return staffService.getStaffById(id);
    }

    @GetMapping
    public List<Staff> getAllStaff() {
        return staffService.getAllStaff();
    }

    @GetMapping("/drivers")
    public List<Driver> getAllDrivers() {
        return staffService.getAllDrivers();
    }

    @GetMapping("/managers")
    public List<TripManager> getAllTripManagers() {
        return staffService.getAllTripManagers();
    }
}