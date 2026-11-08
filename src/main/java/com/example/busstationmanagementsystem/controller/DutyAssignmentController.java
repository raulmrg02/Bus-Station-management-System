package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.DutyRole;
import com.example.busstationmanagementsystem.service.DutyAssignmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String createAssignment(@ModelAttribute("assignment") DutyAssignment assignment, Model model) {
        try {
            dutyAssignmentService.assignDuty(assignment);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("roles", DutyRole.values());
            return "duty-assignment/form";
        }
        return "redirect:/duty-assignments";
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(@PathVariable("id") String id) {
        dutyAssignmentService.deleteAssignment(id);
        return "redirect:/duty-assignments";
    }
}