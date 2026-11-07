
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.entity.OnboardingDocument;

import com.jobportal.service.CandidateOnboardingService;
import com.jobportal.service.OnboardingDocumentService;

@Controller
@RequestMapping("/candidate/onboarding")
public class CandidateOnboardingController {

    private final CandidateOnboardingService onboardingService;
    private final OnboardingDocumentService documentService;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CandidateOnboardingController(
            CandidateOnboardingService onboardingService,
            OnboardingDocumentService documentService) {

        this.onboardingService = onboardingService;
        this.documentService = documentService;
    }

    // ==========================================
    // START OR CONTINUE ONBOARDING
    // ==========================================

    @PostMapping("/start/{offerId}")
    public String startOnboarding(
            @PathVariable Long offerId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        CandidateOnboarding onboarding;

        try {
            onboarding = onboardingService.startOnboarding(
                    offerId,
                    principal.getName()
            );
        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/candidate/offers";
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Onboarding opened successfully!"
        );

        return "redirect:/candidate/onboarding/"
                + onboarding.getId();
    }

    // ==========================================
    // STEP 72: VIEW ONBOARDING DASHBOARD
    // WITH LATEST DOCUMENT STATUS
    // ==========================================

    @GetMapping("/{onboardingId}")
    public String viewOnboarding(
            @PathVariable Long onboardingId,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        String candidateEmail = principal.getName();

        // STEP 1: Verify ownership and load onboarding
        CandidateOnboarding onboarding =
                onboardingService.getCandidateOnboarding(
                        onboardingId,
                        candidateEmail
                );

        // STEP 2: All documents for upload history
        List<OnboardingDocument> documents =
                documentService.getCandidateDocuments(
                        onboardingId,
                        candidateEmail
                );

        // STEP 3: Latest active document of each type
        List<OnboardingDocument> latestDocuments =
                documentService.getLatestDocuments(
                        onboardingId,
                        candidateEmail
                );

        // STEP 4: Calculate onboarding progress
        int onboardingProgress =
                onboardingService.getOnboardingProgress(
                        onboardingId,
                        candidateEmail
                );

        // STEP 5: Count latest approved documents
        long approvedDocuments = latestDocuments.stream()
                .filter(doc ->
                        "APPROVED".equals(
                                doc.getVerificationStatus()
                        )
                )
                .count();

        // STEP 6: Count latest pending documents
        long pendingDocuments = latestDocuments.stream()
                .filter(doc ->
                        !"APPROVED".equals(
                                doc.getVerificationStatus()
                        )
                        && !"REJECTED".equals(
                                doc.getVerificationStatus()
                        )
                )
                .count();

        // STEP 7: Count latest rejected documents
        long rejectedDocuments = latestDocuments.stream()
                .filter(doc ->
                        "REJECTED".equals(
                                doc.getVerificationStatus()
                        )
                )
                .count();

        // STEP 8: Send data to Thymeleaf page
        model.addAttribute("onboarding", onboarding);

        // Full upload history
        model.addAttribute("documents", documents);

        // Latest active documents
        model.addAttribute("latestDocuments", latestDocuments);

        model.addAttribute(
                "onboardingProgress",
                onboardingProgress
        );

        model.addAttribute(
                "approvedDocuments",
                approvedDocuments
        );

        model.addAttribute(
                "pendingDocuments",
                pendingDocuments
        );

        model.addAttribute(
                "rejectedDocuments",
                rejectedDocuments
        );

        return "candidate-onboarding";
    }

    // ==========================================
    // UPLOAD DOCUMENT
    // ==========================================

    @PostMapping("/{onboardingId}/documents/upload")
    public String uploadDocument(
            @PathVariable Long onboardingId,
            @RequestParam("documentType") String documentType,
            @RequestParam("file") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            documentService.uploadDocument(
                    onboardingId,
                    principal.getName(),
                    documentType,
                    file
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Document uploaded successfully!"
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (AccessDeniedException e) {

            // Do not allow access to another candidate's onboarding
            throw e;
        }

        return "redirect:/candidate/onboarding/"
                + onboardingId;
    }

    // ==========================================
    // STEP 70: REPLACE ONBOARDING DOCUMENT
    // ==========================================

    @PostMapping("/{onboardingId}/documents/{documentId}/replace")
    public String replaceDocument(
            @PathVariable Long onboardingId,
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            documentService.replaceDocument(
                    onboardingId,
                    documentId,
                    principal.getName(),
                    file
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Document replaced successfully! "
                    + "The new file is awaiting recruiter verification."
            );

        } catch (IllegalArgumentException | IllegalStateException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        } catch (AccessDeniedException e) {

            throw e;
        }

        return "redirect:/candidate/onboarding/"
                + onboardingId;
    }
}
