package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.example.busstationmanagementsystem.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/staff")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String getAllStaff(Model model) {
        model.addAttribute("staffList", staffService.getAllStaff());
        return "staff/index";
    }

    @GetMapping("/new")
    public String showCreateForm() {
        return "staff/form";
    }

    @PostMapping
    public String createStaff(@RequestParam("id") String id,
                              @RequestParam("name") String name,
                              @RequestParam("staffType") String staffType,
    @RequestParam(value = "yearsOfExperience", defaultValue = "0") int yearsOfExperience,
                              @RequestParam(value = "employeeCode", required = false) String employeeCode,
                              Model model) {

        Staff staff;
        if ("DRIVER".equalsIgnoreCase(staffType)) {
            Driver driver = new Driver();
            driver.setId(id);
            driver.setName(name);
            driver.setYearsOfExperience(yearsOfExperience);
            staff = driver;
        } else {
            TripManager manager = new TripManager();
            manager.setId(id);
            manager.setName(name);
            manager.setEmployeeCode(employeeCode != null ? employeeCode : "");
            staff = manager;
        }

        try {
            staffService.registerStaff(staff);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            return "staff/form";
        }

        return "redirect:/staff";
    }

    @PostMapping("/{id}/delete")
    public String deleteStaff(@PathVariable("id") String id) {
        staffService.deleteById(id);
        return "redirect:/staff";
    }
}