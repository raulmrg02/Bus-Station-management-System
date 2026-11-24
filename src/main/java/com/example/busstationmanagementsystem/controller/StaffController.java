package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.Driver;
import com.example.busstationmanagementsystem.model.Staff;
import com.example.busstationmanagementsystem.model.TripManager;
import com.example.busstationmanagementsystem.service.StaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class StaffController {
    private final StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    @GetMapping
    public String getAllStaff(@RequestParam(defaultValue = "IN_MEMORY") StaffService.RepositoryType repository,
                              Model model) {
        model.addAttribute("staffList", staffService.getAllStaff(repository));
        model.addAttribute("selectedRepository", repository);
        return "staff/index";
    }

    @GetMapping("/new")
    public String showCreateForm(@RequestParam(defaultValue = "IN_MEMORY") StaffService.RepositoryType repository,
                                 Model model) {
        model.addAttribute("selectedRepository", repository);
        return "staff/form";
    }

    @PostMapping
    public String createStaff(@RequestParam("id") String id,
                              @RequestParam("name") String name,
                              @RequestParam("staffType") String staffType,
    @RequestParam(value = "yearsOfExperience", defaultValue = "0") int yearsOfExperience,
                              @RequestParam(value = "employeeCode", required = false) String employeeCode,
                              @RequestParam(defaultValue = "IN_MEMORY") StaffService.RepositoryType repository,
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
            staffService.registerStaff(staff, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("selectedRepository", repository);
            return "staff/form";
        }

        return "redirect:/staff?repository=" + repository;
    }

    @GetMapping("/{id}")
    public String showStaffDetails(@PathVariable("id") String id,
                                   @RequestParam(defaultValue = "IN_MEMORY") StaffService.RepositoryType repository,
                                   Model model) {
        Staff staff = staffService.getAllStaff(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Staff member not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", staff.getId());
        fields.put("Name", staff.getName());
        fields.put("Type", staff.getClass().getSimpleName());
        if (staff instanceof Driver) {
            fields.put("Years of Experience", ((Driver) staff).getYearsOfExperience());
        } else if (staff instanceof TripManager) {
            fields.put("Employee Code", ((TripManager) staff).getEmployeeCode());
        }
        fields.put("Duty Assignment IDs", staff.getAssignments().stream().map(assignment -> assignment.getId()).toList());
        model.addAttribute("title", "Staff " + staff.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/staff");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteStaff(@PathVariable("id") String id,
                              @RequestParam(defaultValue = "IN_MEMORY") StaffService.RepositoryType repository) {
        staffService.deleteById(id, repository);
        return "redirect:/staff?repository=" + repository;
    }
}