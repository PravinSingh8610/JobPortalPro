
package com.jobportal.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.CandidateResume;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.repository.JobRepository;
import com.jobportal.service.CandidateResumeService;
import com.jobportal.service.JobApplicationService;

@Controller
@RequestMapping("/recruiter/jobs")
public class RecruiterApplicationController {

    private final JobApplicationService applicationService;
    private final JobRepository jobRepository;
    private final CandidateResumeService resumeService;

    public RecruiterApplicationController(
            JobApplicationService applicationService,
            JobRepository jobRepository,
            CandidateResumeService resumeService) {

        this.applicationService = applicationService;
        this.jobRepository = jobRepository;
        this.resumeService = resumeService;
    }

    // 1. VIEW APPLICATIONS FOR RECRUITER'S JOB
    @GetMapping("/{jobId}/applications")
    public String viewJobApplications(
            @PathVariable Long jobId,
            Principal principal,
            Model model) {

        List<JobApplication> applications =
                applicationService.getRecruiterJobApplications(
                        jobId,
                        principal.getName()
                );

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Job not found"
                        ));

        // Check resume availability for every application
        Map<Long, Boolean> resumeAvailability = new HashMap<>();

        for (JobApplication application : applications) {

            boolean available = resumeService.hasResumeForApplication(
                    application.getId(),
                    principal.getName()
            );

            resumeAvailability.put(
                    application.getId(),
                    available
            );
        }

        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());

        // Send resume availability to Thymeleaf page
        model.addAttribute("resumeAvailability", resumeAvailability);

        return "recruiter-job-applications";
    }

    // 2. SHORTLIST OR REJECT APPLICATION
    @PostMapping("/{jobId}/applications/{applicationId}/status")
    public String updateApplicationStatus(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @RequestParam String status,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        List<JobApplication> applications =
                applicationService.getRecruiterJobApplications(
                        jobId,
                        principal.getName()
                );

        boolean belongsToJob = applications.stream()
                .anyMatch(application ->
                        application.getId().equals(applicationId)
                );

        if (!belongsToJob) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Application not found for this job"
            );
        }

        try {

            applicationService.updateApplicationStatus(
                    applicationId,
                    status,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Application status updated successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs/"
                + jobId + "/applications";
    }

    // 3. DECLARE FINAL HIRING DECISION
    @PostMapping("/{jobId}/applications/{applicationId}/decision")
    public String updateFinalHiringDecision(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @RequestParam String finalDecision,
            @RequestParam(required = false) String recruiterFeedback,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        List<JobApplication> applications =
                applicationService.getRecruiterJobApplications(
                        jobId,
                        principal.getName()
                );

        boolean belongsToJob = applications.stream()
                .anyMatch(application ->
                        application.getId().equals(applicationId)
                );

        if (!belongsToJob) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Application not found for this job"
            );
        }

        try {

            applicationService.updateFinalHiringDecision(
                    applicationId,
                    finalDecision,
                    recruiterFeedback,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Final hiring decision declared successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs/"
                + jobId + "/applications";
    }

    // 4. SECURE RECRUITER RESUME DOWNLOAD
    @GetMapping("/{jobId}/applications/{applicationId}/resume")
    public ResponseEntity<byte[]> downloadCandidateResume(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            Principal principal) {

        List<JobApplication> applications =
                applicationService.getRecruiterJobApplications(
                        jobId,
                        principal.getName()
                );

        boolean belongsToJob = applications.stream()
                .anyMatch(application ->
                        application.getId().equals(applicationId)
                );

        if (!belongsToJob) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Application not found for this job"
            );
        }

        CandidateResume resume =
                resumeService.getResumeForRecruiter(
                        applicationId,
                        principal.getName()
                );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"candidate-resume.pdf\""
                )
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store"
                )
                .header(
                        "X-Content-Type-Options",
                        "nosniff"
                )
                .body(resume.getFileData());
    }

}
