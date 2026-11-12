package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.DutyRole;
import com.example.busstationmanagementsystem.service.DutyAssignmentService;
import com.example.busstationmanagementsystem.service.DutyAssignmentService.RepositoryType;
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
    public String getAllAssignments(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("assignments", dutyAssignmentService.getAllAssignments(repository));
        model.addAttribute("selectedRepository", repository);
        return "duty-assignment/index";
    }

    @GetMapping("/new")
    public String showCreateForm(
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        model.addAttribute("assignment", new DutyAssignment());
        model.addAttribute("roles", DutyRole.values());
        model.addAttribute("selectedRepository", repository);
        return "duty-assignment/form";
    }

    @PostMapping
    public String createAssignment(
            @ModelAttribute("assignment") DutyAssignment assignment,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
            Model model) {
        try {
            dutyAssignmentService.assignDuty(assignment, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("roles", DutyRole.values());
            model.addAttribute("selectedRepository", repository);
            return "duty-assignment/form";
        }
        return "redirect:/duty-assignments?repository=" + repository;
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        dutyAssignmentService.deleteAssignment(id, repository);
        return "redirect:/duty-assignments?repository=" + repository;
    }
}