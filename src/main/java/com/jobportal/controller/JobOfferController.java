
package com.jobportal.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.Interview;
import com.jobportal.service.InterviewService;
import com.jobportal.service.JobOfferService;
import com.jobportal.service.OfferLetterDownloadService;

@Controller
public class JobOfferController {

    private final JobOfferService jobOfferService;
    private final InterviewService interviewService;
    private final OfferLetterDownloadService offerLetterDownloadService;

    public JobOfferController(
            JobOfferService jobOfferService,
            InterviewService interviewService,
            OfferLetterDownloadService offerLetterDownloadService) {

        this.jobOfferService = jobOfferService;
        this.interviewService = interviewService;
        this.offerLetterDownloadService = offerLetterDownloadService;
    }

    // RECRUITER - CREATE OFFER PAGE
    @GetMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/offer")
    public String createOfferPage(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            Interview interview = interviewService.getRecruiterInterview(
                    jobId, interviewId, principal.getName());

            if (!"COMPLETED".equalsIgnoreCase(interview.getStatus())
                    || !"SELECTED".equalsIgnoreCase(interview.getResult())) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Only completed and selected candidates can receive offers.");

                return "redirect:/recruiter/jobs/" + jobId + "/interviews";
            }

            model.addAttribute("interview", interview);
            model.addAttribute("jobId", jobId);

            return "recruiter-create-offer";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute("error", e.getMessage());

            return "redirect:/recruiter/jobs/" + jobId + "/interviews";
        }
    }

    // RECRUITER - SUBMIT OFFER
    @PostMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/offer")
    public String createOffer(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,

            @RequestParam String designation,
            @RequestParam BigDecimal offeredSalary,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate joiningDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate offerExpiryDate,

            @RequestParam(required = false)
            String offerMessage,

            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            // Recheck interview eligibility before creating offer
            Interview interview = interviewService.getRecruiterInterview(
                    jobId, interviewId, principal.getName());

            if (!"COMPLETED".equalsIgnoreCase(interview.getStatus())
                    || !"SELECTED".equalsIgnoreCase(interview.getResult())) {

                throw new IllegalArgumentException(
                        "Only completed and selected candidates can receive offers.");
            }

            // Basic input validation
            if (designation == null || designation.isBlank()) {
                throw new IllegalArgumentException(
                        "Designation is required.");
            }

            if (offeredSalary == null
                    || offeredSalary.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Offered salary must be greater than zero.");
            }

            if (joiningDate == null || offerExpiryDate == null) {
                throw new IllegalArgumentException(
                        "Joining date and offer expiry date are required.");
            }

            if (!offerExpiryDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "Offer expiry date must be in the future.");
            }

            if (joiningDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException(
                        "Joining date cannot be in the past.");
            }

            if (!offerExpiryDate.isBefore(joiningDate)) {
                throw new IllegalArgumentException(
                        "Offer expiry date must be before joining date.");
            }

            jobOfferService.createOffer(
                    jobId,
                    interviewId,
                    principal.getName(),
                    designation.trim(),
                    offeredSalary,
                    joiningDate,
                    offerExpiryDate,
                    offerMessage);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Offer letter created successfully!");

            return "redirect:/recruiter/jobs/" + jobId + "/offers";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());

            return "redirect:/recruiter/jobs/"
                    + jobId + "/interviews/"
                    + interviewId + "/offer";
        }
    }

    // RECRUITER - VIEW JOB OFFERS
    @GetMapping("/recruiter/jobs/{jobId}/offers")
    public String recruiterOffers(
            @PathVariable Long jobId,
            Principal principal,
            Model model) {

        model.addAttribute(
                "offers",
                jobOfferService.getJobOffers(
                        jobId, principal.getName()));

        model.addAttribute("jobId", jobId);

        return "recruiter-manage-offers";
    }

    // CANDIDATE - VIEW MY OFFERS
    @GetMapping("/candidate/offers")
    public String candidateOffers(
            Principal principal,
            Model model) {

        model.addAttribute(
                "offers",
                jobOfferService.getCandidateOffers(
                        principal.getName()));

        return "candidate-offers";
    }

    // CANDIDATE - ACCEPT / REJECT OFFER
    @PostMapping("/candidate/offers/{offerId}/respond")
    public String respondToOffer(
            @PathVariable Long offerId,
            @RequestParam String decision,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            jobOfferService.respondToOffer(
                    offerId,
                    principal.getName(),
                    decision);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Offer response submitted successfully!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error", e.getMessage());
        }

        return "redirect:/candidate/offers";
    }

    // CANDIDATE - SECURE PDF DOWNLOAD
    @GetMapping("/candidate/offers/{offerId}/download")
    public ResponseEntity<byte[]> downloadCandidateOffer(
            @PathVariable Long offerId,
            Principal principal) {

        byte[] pdfBytes = offerLetterDownloadService
                .downloadCandidateOffer(
                        offerId,
                        principal.getName());

        return buildPdfResponse(pdfBytes, offerId);
    }

    // RECRUITER - SECURE PDF DOWNLOAD
    @GetMapping("/recruiter/jobs/{jobId}/offers/{offerId}/download")
    public ResponseEntity<byte[]> downloadRecruiterOffer(
            @PathVariable Long jobId,
            @PathVariable Long offerId,
            Principal principal) {

        byte[] pdfBytes = offerLetterDownloadService
                .downloadRecruiterOffer(
                        jobId,
                        offerId,
                        principal.getName());

        return buildPdfResponse(pdfBytes, offerId);
    }

    // COMMON PDF RESPONSE
    private ResponseEntity<byte[]> buildPdfResponse(
            byte[] pdfBytes,
            Long offerId) {

        String filename =
                "JobPortalPro-Offer-" + offerId + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentLength(pdfBytes.length)
                .body(pdfBytes);
    }
}
