
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.entity.OnboardingDocument;
import com.jobportal.service.RecruiterOnboardingService;

@Controller
@RequestMapping("/recruiter/onboarding")
public class RecruiterOnboardingController {

    private final RecruiterOnboardingService onboardingService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public RecruiterOnboardingController(
            RecruiterOnboardingService onboardingService) {

        this.onboardingService = onboardingService;
    }

    // ==========================================
    // ALL RECRUITER ONBOARDING CANDIDATES
    // ==========================================

    @GetMapping
    public String showAllOnboardings(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<CandidateOnboarding> onboardings =
                onboardingService.getAllRecruiterOnboardings(
                        principal.getName()
                );

        model.addAttribute("onboardings", onboardings);

        return "recruiter-onboarding-list";
    }

    // ==========================================
    // VIEW CANDIDATE ONBOARDING
    // ==========================================

    @GetMapping("/{onboardingId}")
    public String viewOnboarding(
            @PathVariable Long onboardingId,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        CandidateOnboarding onboarding =
                onboardingService.getRecruiterOnboarding(
                        onboardingId,
                        principal.getName()
                );

        List<OnboardingDocument> documents =
                onboardingService.getDocuments(
                        onboardingId,
                        principal.getName()
                );

        model.addAttribute("onboarding", onboarding);
        model.addAttribute("documents", documents);

        return "recruiter-onboarding";
    }

    // ==========================================
    // APPROVE OR REJECT DOCUMENT
    // ==========================================

    @PostMapping("/{onboardingId}/documents/{documentId}/verify")
    public String verifyDocument(
            @PathVariable Long onboardingId,
            @PathVariable Long documentId,
            @RequestParam("decision") String decision,
            @RequestParam(
                    value = "remarks",
                    required = false
            ) String remarks,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {

            onboardingService.verifyDocument(
                    onboardingId,
                    documentId,
                    principal.getName(),
                    decision,
                    remarks
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Document verification updated successfully!"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (AccessDeniedException e) {

            // Unauthorized access must remain forbidden
            throw e;
        }

        return "redirect:/recruiter/onboarding/" + onboardingId;
    }

    // ==========================================
    // NEW: COMPLETE CANDIDATE ONBOARDING
    // ==========================================

    @PostMapping("/{onboardingId}/complete")
    public String completeOnboarding(
            @PathVariable Long onboardingId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {

            onboardingService.completeOnboarding(
                    onboardingId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Candidate onboarding completed successfully!"
            );

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (AccessDeniedException e) {

            // Prevent access to another recruiter's onboarding
            throw e;
        }

        return "redirect:/recruiter/onboarding/" + onboardingId;
    }
}
