
package com.jobportal.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.Interview;
import com.jobportal.service.InterviewService;
import com.jobportal.service.JobApplicationService;

@Controller
public class InterviewController {

    private final InterviewService interviewService;
    private final JobApplicationService applicationService;

    public InterviewController(
            InterviewService interviewService,
            JobApplicationService applicationService) {

        this.interviewService = interviewService;
        this.applicationService = applicationService;
    }

    // ==========================================
    // 1. RECRUITER SCHEDULE INTERVIEW PAGE
    // ==========================================
    @GetMapping("/recruiter/jobs/{jobId}/applications/{applicationId}/interview")
    public String showScheduleInterviewPage(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            Principal principal,
            Model model) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        var applications =
                applicationService.getRecruiterJobApplications(
                        jobId, principal.getName());

        var application = applications.stream()
                .filter(a -> a.getId().equals(applicationId))
                .findFirst()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found for this job"));

        if (!"SHORTLISTED".equals(application.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only shortlisted candidates can be scheduled");
        }

        model.addAttribute("jobId", jobId);
        model.addAttribute("jobApp", application);

        return "recruiter-schedule-interview";
    }

    // ==========================================
    // 2. SAVE INTERVIEW
    // ==========================================
    @PostMapping("/recruiter/jobs/{jobId}/applications/{applicationId}/interview")
    public String scheduleInterview(
            @PathVariable Long jobId,
            @PathVariable Long applicationId,
            @RequestParam String interviewDateTime,
            @RequestParam String interviewMode,
            @RequestParam(required = false) String meetingLink,
            @RequestParam(required = false) String interviewLocation,
            @RequestParam(required = false) String notes,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        boolean belongsToJob =
                applicationService.getRecruiterJobApplications(
                        jobId, principal.getName())
                        .stream()
                        .anyMatch(a ->
                                a.getId().equals(applicationId));

        if (!belongsToJob) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Application not found for this job");
        }

        try {
            LocalDateTime dateTime =
                    LocalDateTime.parse(interviewDateTime);

            interviewService.scheduleInterview(
                    applicationId,
                    principal.getName(),
                    dateTime,
                    interviewMode,
                    meetingLink,
                    interviewLocation,
                    notes
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Interview scheduled successfully!");

            return "redirect:/recruiter/jobs/"
                    + jobId + "/applications";

        } catch (DateTimeParseException | IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to schedule interview: "
                            + ex.getMessage());

            return "redirect:/recruiter/jobs/"
                    + jobId + "/applications/"
                    + applicationId + "/interview";
        }
    }

    // ==========================================
    // 3. CANDIDATE VIEW INTERVIEWS
    // ==========================================
    @GetMapping("/candidate/interviews")
    public String candidateInterviews(
            Principal principal,
            Model model) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        model.addAttribute(
                "interviews",
                interviewService.getCandidateInterviews(
                        principal.getName()));

        return "candidate-interviews";
    }

    // ==========================================
    // 4. RECRUITER MANAGE INTERVIEWS
    // ==========================================
    @GetMapping("/recruiter/jobs/{jobId}/interviews")
    public String recruiterJobInterviews(
            @PathVariable Long jobId,
            Principal principal,
            Model model) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        model.addAttribute("jobId", jobId);

        model.addAttribute(
                "interviews",
                interviewService.getRecruiterJobInterviews(
                        jobId,
                        principal.getName()
                )
        );

        return "recruiter-manage-interviews";
    }

    // ==========================================
    // 5. OPEN RESCHEDULE INTERVIEW PAGE
    // ==========================================
    @GetMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/reschedule")
    public String showRescheduleInterviewPage(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            Principal principal,
            Model model) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        Interview interview =
                interviewService.getRecruiterInterview(
                        jobId,
                        interviewId,
                        principal.getName()
                );

        if (!"SCHEDULED".equals(interview.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only scheduled interviews can be rescheduled"
            );
        }

        model.addAttribute("jobId", jobId);
        model.addAttribute("interview", interview);

        return "recruiter-reschedule-interview";
    }

    // ==========================================
    // 6. SAVE RESCHEDULED INTERVIEW
    // ==========================================
    @PostMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/reschedule")
    public String rescheduleInterview(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            @RequestParam String interviewDateTime,
            @RequestParam String interviewMode,
            @RequestParam(required = false) String meetingLink,
            @RequestParam(required = false) String interviewLocation,
            @RequestParam(required = false) String notes,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        interviewService.getRecruiterInterview(
                jobId,
                interviewId,
                principal.getName()
        );

        try {
            LocalDateTime newDateTime =
                    LocalDateTime.parse(interviewDateTime);

            interviewService.rescheduleInterview(
                    interviewId,
                    principal.getName(),
                    newDateTime,
                    interviewMode,
                    meetingLink,
                    interviewLocation,
                    notes
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Interview rescheduled successfully!"
            );

            return "redirect:/recruiter/jobs/"
                    + jobId + "/interviews";

        } catch (DateTimeParseException | IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Unable to reschedule interview: "
                            + ex.getMessage()
            );

            return "redirect:/recruiter/jobs/"
                    + jobId + "/interviews/"
                    + interviewId + "/reschedule";
        }
    }

    // ==========================================
    // 7. CANCEL INTERVIEW
    // ==========================================
    @PostMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/cancel")
    public String cancelInterview(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        try {
            interviewService.cancelInterview(
                    jobId,
                    interviewId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Interview cancelled successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs/"
                + jobId + "/interviews";
    }

    // ==========================================
    // 8. MARK INTERVIEW COMPLETED
    // ==========================================
    @PostMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/complete")
    public String completeInterview(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        try {
            interviewService.completeInterview(
                    jobId,
                    interviewId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Interview marked as completed successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs/"
                + jobId + "/interviews";
    }

    // ==========================================
    // 9. OPEN DECLARE RESULT PAGE
    // ==========================================
    @GetMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/result")
    public String showDeclareResultPage(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            Principal principal,
            Model model) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        Interview interview =
                interviewService.getRecruiterInterview(
                        jobId,
                        interviewId,
                        principal.getName()
                );

        if (!"COMPLETED".equals(interview.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only completed interviews can have results"
            );
        }

        // FIX: PENDING means result has not yet been declared.
        if (interview.getResult() != null
                && !"PENDING".equalsIgnoreCase(
                        interview.getResult().toString())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Interview result has already been declared"
            );
        }

        model.addAttribute("jobId", jobId);
        model.addAttribute("interview", interview);

        return "recruiter-declare-interview-result";
    }

    // ==========================================
    // 10. SAVE INTERVIEW RESULT
    // ==========================================
    @PostMapping("/recruiter/jobs/{jobId}/interviews/{interviewId}/result")
    public String declareInterviewResult(
            @PathVariable Long jobId,
            @PathVariable Long interviewId,
            @RequestParam String result,
            @RequestParam(required = false) String feedback,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }

        try {
            interviewService.declareInterviewResult(
                    jobId,
                    interviewId,
                    principal.getName(),
                    result,
                    feedback
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Interview result declared successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs/"
                + jobId + "/interviews";
    }
}
