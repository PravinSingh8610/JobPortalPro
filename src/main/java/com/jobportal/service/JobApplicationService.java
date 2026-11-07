
package com.jobportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.User;

import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;

@Service
public class JobApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public JobApplicationService(
            JobApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            NotificationService notificationService) {

        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    // 1. CANDIDATE APPLY FOR JOB
    public void applyForJob(Long jobId, String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate account not found"
                        ));

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can apply for jobs"
            );
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Job not found"
                        ));

        if (!"ACTIVE".equals(job.getStatus())) {
            throw new IllegalArgumentException(
                    "This job is currently closed"
            );
        }

        if (job.getApplicationDeadline() == null
                || job.getApplicationDeadline()
                        .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Application deadline has expired"
            );
        }

        boolean alreadyApplied =
                applicationRepository.existsByJobIdAndCandidateId(
                        jobId,
                        candidate.getId()
                );

        if (alreadyApplied) {
            throw new IllegalArgumentException(
                    "You have already applied for this job"
            );
        }

        JobApplication application = new JobApplication();

        application.setJob(job);
        application.setCandidate(candidate);
        application.setStatus("APPLIED");

        try {
            applicationRepository.saveAndFlush(application);

        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException(
                    "Application could not be submitted. "
                    + "You may have already applied."
            );
        }
    }

    // 2. CANDIDATE VIEW OWN APPLICATIONS
    @Transactional(readOnly = true)
    public List<JobApplication> getCandidateApplications(
            String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate account not found"
                        ));

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can view their applications"
            );
        }

        return applicationRepository
                .findByCandidateIdOrderByAppliedAtDesc(
                        candidate.getId()
                );
    }

    // 3. RECRUITER VIEW APPLICATIONS FOR OWN JOB
    @Transactional(readOnly = true)
    public List<JobApplication> getRecruiterJobApplications(
            Long jobId,
            String recruiterEmail) {

        User recruiter = userRepository
                .findByEmail(recruiterEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Recruiter account not found"
                        ));

        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can view job applications"
            );
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Job not found"
                        ));

        verifyRecruiterOwnsJob(job, recruiter);

        return applicationRepository
                .findByJobIdOrderByAppliedAtDesc(jobId);
    }

    // 4. RECRUITER SHORTLIST OR REJECT APPLICATION
    @Transactional
    public void updateApplicationStatus(
            Long applicationId,
            String newStatus,
            String recruiterEmail) {

        if (!"SHORTLISTED".equals(newStatus)
                && !"REJECTED".equals(newStatus)) {

            throw new IllegalArgumentException(
                    "Invalid application status"
            );
        }

        User recruiter = userRepository
                .findByEmail(recruiterEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Recruiter account not found"
                        ));

        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can update applications"
            );
        }

        JobApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"
                        ));

        verifyRecruiterOwnsJob(application.getJob(), recruiter);

        String oldStatus = application.getStatus();

        application.setStatus(newStatus);

        applicationRepository.save(application);

        // Send notification only when status actually changes
        if (!newStatus.equals(oldStatus)) {

            User candidate = application.getCandidate();
            String jobTitle = application.getJob().getTitle();

            if ("SHORTLISTED".equals(newStatus)) {

                notificationService.createNotification(
                        candidate.getEmail(),
                        "Application Shortlisted!",
                        "Congratulations! Your application for "
                                + jobTitle
                                + " has been shortlisted by the recruiter.",
                        "SHORTLISTED",
                        "/candidate/applications"
                );

            } else if ("REJECTED".equals(newStatus)) {

                notificationService.createNotification(
                        candidate.getEmail(),
                        "Application Status Updated",
                        "Your application for "
                                + jobTitle
                                + " has been rejected. Thank you for your interest.",
                        "APPLICATION_REJECTED",
                        "/candidate/applications"
                );
            }
        }
    }

    // 5. RECRUITER DECLARE FINAL HIRING DECISION
    @Transactional
    public void updateFinalHiringDecision(
            Long applicationId,
            String finalDecision,
            String recruiterFeedback,
            String recruiterEmail) {

        if (!"SELECTED".equals(finalDecision)
                && !"REJECTED".equals(finalDecision)
                && !"ON_HOLD".equals(finalDecision)) {

            throw new IllegalArgumentException(
                    "Invalid final hiring decision"
            );
        }

        if (recruiterFeedback != null
                && recruiterFeedback.length() > 2000) {

            throw new IllegalArgumentException(
                    "Feedback cannot exceed 2000 characters"
            );
        }

        User recruiter = userRepository
                .findByEmail(recruiterEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Recruiter account not found"
                        ));

        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can declare final decisions"
            );
        }

        JobApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"
                        ));

        // Security: recruiter must own the job
        verifyRecruiterOwnsJob(application.getJob(), recruiter);

        String previousDecision = application.getFinalDecision();

        // Save final hiring decision separately
        application.setFinalDecision(finalDecision);

        application.setDecisionDeclaredAt(
                LocalDateTime.now()
        );

        application.setRecruiterFeedback(
                recruiterFeedback == null
                        ? null
                        : recruiterFeedback.trim()
        );

        applicationRepository.save(application);

        // Send notification when final decision changes
        if (!finalDecision.equals(previousDecision)) {

            User candidate = application.getCandidate();
            String jobTitle = application.getJob().getTitle();

            String notificationTitle;
            String notificationMessage;

            switch (finalDecision) {

                case "SELECTED":
                    notificationTitle = "Congratulations! You Are Selected!";
                    notificationMessage =
                            "You have been selected for the "
                            + jobTitle
                            + " position. Please check your application "
                            + "for recruiter feedback.";
                    break;

                case "REJECTED":
                    notificationTitle = "Final Hiring Decision Declared";
                    notificationMessage =
                            "Your final application decision for "
                            + jobTitle
                            + " is REJECTED. Please check your application "
                            + "for recruiter feedback.";
                    break;

                case "ON_HOLD":
                    notificationTitle = "Application Put On Hold";
                    notificationMessage =
                            "Your application for "
                            + jobTitle
                            + " is currently ON HOLD. Please check "
                            + "your application for further details.";
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Invalid final hiring decision"
                    );
            }

            notificationService.createNotification(
                    candidate.getEmail(),
                    notificationTitle,
                    notificationMessage,
                    "FINAL_DECISION",
                    "/candidate/applications"
            );
        }
    }

    // 6. COMMON RECRUITER OWNERSHIP SECURITY CHECK
    private void verifyRecruiterOwnsJob(
            Job job,
            User recruiter) {

        if (job == null
                || job.getCompany() == null
                || job.getCompany().getRecruiter() == null
                || !recruiter.getId().equals(
                        job.getCompany().getRecruiter().getId()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access another recruiter's job applications"
            );
        }
    }
}
