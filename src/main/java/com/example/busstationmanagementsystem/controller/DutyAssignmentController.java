package com.example.busstationmanagementsystem.controller;

import com.example.busstationmanagementsystem.model.DutyAssignment;
import com.example.busstationmanagementsystem.model.DutyRole;
import com.example.busstationmanagementsystem.service.DutyAssignmentService;
import com.example.busstationmanagementsystem.service.DutyAssignmentService.RepositoryType;
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
        model.addAttribute("isUpdate", false);
        return "duty-assignment/form";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable("id") String id,
                                 @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                 Model model) {
        DutyAssignment assignment = dutyAssignmentService.getAllAssignments(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Duty assignment not found: " + id));
        model.addAttribute("assignment", assignment);
        model.addAttribute("roles", DutyRole.values());
        model.addAttribute("selectedRepository", repository);
        model.addAttribute("isUpdate", true);
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
            model.addAttribute("isUpdate", false);
            return "duty-assignment/form";
        }
        return "redirect:/duty-assignments?repository=" + repository;
    }

    @PostMapping("/{id}")
    public String updateAssignment(@PathVariable("id") String id,
                                   @ModelAttribute("assignment") DutyAssignment assignment,
                                   @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                   Model model) {
        try {
            dutyAssignmentService.updateAssignment(id, assignment, repository);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("errorMessage", exception.getMessage());
            model.addAttribute("roles", DutyRole.values());
            model.addAttribute("selectedRepository", repository);
            model.addAttribute("isUpdate", true);
            return "duty-assignment/form";
        }
        return "redirect:/duty-assignments?repository=" + repository;
    }

    @GetMapping("/{id}")
    public String showAssignmentDetails(@PathVariable("id") String id,
                                        @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository,
                                        Model model) {
        DutyAssignment assignment = dutyAssignmentService.getAllAssignments(repository).stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new java.util.NoSuchElementException("Duty assignment not found: " + id));
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("ID", assignment.getId());
        fields.put("Trip ID", assignment.getTripId());
        fields.put("Staff ID", assignment.getStaffId());
        fields.put("Role", assignment.getRole());
        model.addAttribute("title", "Duty Assignment " + assignment.getId());
        model.addAttribute("fields", fields);
        model.addAttribute("backUrl", "/duty-assignments");
        model.addAttribute("selectedRepository", repository);
        return "details";
    }

    @PostMapping("/{id}/delete")
    public String deleteAssignment(
            @PathVariable("id") String id,
            @RequestParam(value = "repository", defaultValue = "IN_FILE") RepositoryType repository) {
        dutyAssignmentService.deleteAssignment(id, repository);
        return "redirect:/duty-assignments?repository=" + repository;
    }
}