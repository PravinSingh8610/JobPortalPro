
package com.jobportal.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.service.SavedJobService;

@Controller
@RequestMapping("/candidate/saved-jobs")
public class SavedJobController {

    private final SavedJobService savedJobService;

    public SavedJobController(
            SavedJobService savedJobService) {

        this.savedJobService = savedJobService;
    }

    // ==========================================
    // 1. DISPLAY ALL SAVED JOBS
    // ==========================================

    @GetMapping
    public String showSavedJobs(
            Principal principal,
            Model model) {

        model.addAttribute(
                "savedJobs",
                savedJobService.getSavedJobs(
                        principal.getName()
                )
        );

        model.addAttribute(
                "totalSavedJobs",
                savedJobService.countSavedJobs(
                        principal.getName()
                )
        );

        return "candidate-saved-jobs";
    }

    // ==========================================
    // 2. SAVE A JOB
    // ==========================================

    @PostMapping("/{jobId}/save")
    public String saveJob(
            @PathVariable Long jobId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            savedJobService.saveJob(
                    jobId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Job saved successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/candidate/jobs/" + jobId;
    }

    // ==========================================
    // 3. REMOVE A SAVED JOB
    // ==========================================

    @PostMapping("/{jobId}/remove")
    public String removeSavedJob(
            @PathVariable Long jobId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            savedJobService.removeSavedJob(
                    jobId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Job removed from saved jobs."
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/candidate/saved-jobs";
    }
}
