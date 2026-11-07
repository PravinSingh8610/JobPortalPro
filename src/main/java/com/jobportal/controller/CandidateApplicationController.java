
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jobportal.entity.Interview;
import com.jobportal.entity.JobApplication;
import com.jobportal.service.InterviewService;
import com.jobportal.service.JobApplicationService;

@Controller
@RequestMapping("/candidate/applications")
public class CandidateApplicationController {

    private final JobApplicationService applicationService;
    private final InterviewService interviewService;

    public CandidateApplicationController(
            JobApplicationService applicationService,
            InterviewService interviewService) {

        this.applicationService = applicationService;
        this.interviewService = interviewService;
    }

    // ========================================
    // CANDIDATE APPLICATION TRACKING
    // ========================================
    @GetMapping
    public String myApplications(
            Principal principal,
            Model model) {

        String candidateEmail = principal.getName();

        // EXISTING: Fetch candidate applications
        List<JobApplication> applications =
                applicationService.getCandidateApplications(
                        candidateEmail
                );

        // NEW: Fetch candidate interviews
        List<Interview> interviews =
                interviewService.getCandidateInterviews(
                        candidateEmail
                );

        // NEW: Group interviews by application ID
        Map<Long, List<Interview>> interviewsByApplication =
                interviews.stream()
                        .collect(Collectors.groupingBy(
                                interview ->
                                        interview.getApplication()
                                                .getId()
                        ));

        // EXISTING
        model.addAttribute(
                "applications",
                applications
        );

        model.addAttribute(
                "totalApplications",
                applications.size()
        );

        // NEW
        model.addAttribute(
                "interviewsByApplication",
                interviewsByApplication
        );

        return "candidate-applications";
    }
}
