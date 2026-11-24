package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.DutyRole;
import com.example.busstationmanagementsystem.service.DutyAssignmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/duty-assignments")
public class DutyAssignmentController {

    private final DutyAssignmentService dutyAssignmentService;

    public DutyAssignmentController(DutyAssignmentService dutyAssignmentService) {
        this.dutyAssignmentService = dutyAssignmentService;
    }

    @GetMapping
    public String getAllAssignments(Model model) {
        model.addAttribute("assignments", dutyAssignmentService.getAllAssignments());
        return "duty-assignment/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("assignment", new DutyAssignment());
        model.addAttribute("roles", DutyRole.values());
        return "duty-assignment/form";
    }

    @PostMapping
    public String createAssignment(
            @ModelAttribute("assignment") DutyAssignment assignment,
            Model model) {
        try {
            dutyAssignmentService.assignDuty(assignment);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("roles", DutyRole.values());
            return "duty-assignment/form";
        }
        return "redirect:/duty-assignments";
    }

    @GetMapping("/{id}")
    public String showAssignmentDetails(@PathVariable("id") String id, Model model) {
        DutyAssignment assignment = dutyAssignmentService.getAssignmentById(id);
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", assignment.getId());
        fields.put("Trip ID", assignment.getTripId());
        fields.put("Staff ID", assignment.getStaffId());
        fields.put("Role", assignment.getRole());
        model.addAttribute("title", "Duty Assignment " + assignment.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/duty-assignments");
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable("id") String id) {
        dutyAssignmentService.deleteAssignment(id);
        return "redirect:/duty-assignments";
    }
}