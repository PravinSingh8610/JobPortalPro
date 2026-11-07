
package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.jobportal.entity.Interview;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.User;

import com.jobportal.repository.InterviewRepository;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final NotificationService notificationService;

    public InterviewService(
            InterviewRepository interviewRepository,
            JobApplicationRepository applicationRepository,
            UserRepository userRepository,
            JobRepository jobRepository,
            NotificationService notificationService) {

        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.notificationService = notificationService;
    }

    // ==========================================
    // 1. SCHEDULE INTERVIEW
    // ==========================================
    @Transactional
    public void scheduleInterview(
            Long applicationId,
            String recruiterEmail,
            LocalDateTime interviewDateTime,
            String interviewMode,
            String meetingLink,
            String interviewLocation,
            String notes) {

        JobApplication application =
                getAuthorizedApplication(
                        applicationId,
                        recruiterEmail
                );

        if (!"SHORTLISTED".equals(application.getStatus())) {
            throw new IllegalArgumentException(
                    "Only shortlisted candidates can be scheduled"
            );
        }

        validateInterviewDetails(
                interviewDateTime,
                interviewMode,
                meetingLink,
                interviewLocation
        );

        Interview interview = new Interview();

        interview.setApplication(application);
        interview.setInterviewDateTime(interviewDateTime);
        interview.setInterviewMode(interviewMode);

        interview.setMeetingLink(
                "ONLINE".equals(interviewMode)
                        ? meetingLink.trim()
                        : null
        );

        interview.setInterviewLocation(
                "OFFLINE".equals(interviewMode)
                        ? interviewLocation.trim()
                        : null
        );

        interview.setNotes(notes);
        interview.setStatus("SCHEDULED");

        interviewRepository.save(interview);

        // Send interview scheduled notification
        User candidate = application.getCandidate();
        String jobTitle = application.getJob().getTitle();

        String message = "Your interview for "
                + jobTitle
                + " has been scheduled on "
                + interviewDateTime.toString()
                + ". Mode: "
                + interviewMode
                + ". Please check your application for details.";

        notificationService.createNotification(
                candidate.getEmail(),
                "New Interview Scheduled!",
                message,
                "INTERVIEW_SCHEDULED",
                "/candidate/applications"
        );
    }

    // ==========================================
    // 2. VIEW CANDIDATE INTERVIEWS
    // ==========================================
    @Transactional(readOnly = true)
    public List<Interview> getCandidateInterviews(
            String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate not found"
                        )
                );

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can view their interviews"
            );
        }

        return interviewRepository
                .findByApplicationCandidateIdOrderByInterviewDateTimeAsc(
                        candidate.getId()
                );
    }

    // ==========================================
    // 3. VIEW RECRUITER JOB INTERVIEWS
    // ==========================================
    @Transactional(readOnly = true)
    public List<Interview> getRecruiterJobInterviews(
            Long jobId,
            String recruiterEmail) {

        User recruiter = getRecruiter(recruiterEmail);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Job not found"
                        )
                );

        if (job.getCompany() == null
                || job.getCompany().getRecruiter() == null
                || !job.getCompany()
                        .getRecruiter()
                        .getId()
                        .equals(recruiter.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view interviews for this job"
            );
        }

        return interviewRepository
                .findByApplicationJobIdOrderByInterviewDateTimeAsc(
                        jobId
                );
    }

    // ==========================================
    // 4. RESCHEDULE INTERVIEW
    // ==========================================
    @Transactional
    public void rescheduleInterview(
            Long interviewId,
            String recruiterEmail,
            LocalDateTime newDateTime,
            String interviewMode,
            String meetingLink,
            String interviewLocation,
            String notes) {

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Interview not found"
                        )
                );

        getAuthorizedApplication(
                interview.getApplication().getId(),
                recruiterEmail
        );

        if (!"SCHEDULED".equals(interview.getStatus())) {
            throw new IllegalArgumentException(
                    "Only scheduled interviews can be rescheduled"
            );
        }

        validateInterviewDetails(
                newDateTime,
                interviewMode,
                meetingLink,
                interviewLocation
        );

        interview.setInterviewDateTime(newDateTime);
        interview.setInterviewMode(interviewMode);

        interview.setMeetingLink(
                "ONLINE".equals(interviewMode)
                        ? meetingLink.trim()
                        : null
        );

        interview.setInterviewLocation(
                "OFFLINE".equals(interviewMode)
                        ? interviewLocation.trim()
                        : null
        );

        interview.setNotes(notes);

        interviewRepository.save(interview);

        // Send reschedule notification
        User candidate = interview.getApplication().getCandidate();
        String jobTitle = interview.getApplication().getJob().getTitle();

        notificationService.createNotification(
                candidate.getEmail(),
                "Interview Rescheduled!",
                "Your interview for "
                        + jobTitle
                        + " has been rescheduled to "
                        + newDateTime.toString()
                        + ". Please check your application for updated details.",
                "INTERVIEW_RESCHEDULED",
                "/candidate/applications"
        );
    }

    // ==========================================
    // 5. VALIDATE INTERVIEW DETAILS
    // ==========================================
    private void validateInterviewDetails(
            LocalDateTime interviewDateTime,
            String interviewMode,
            String meetingLink,
            String interviewLocation) {

        if (interviewDateTime == null
                || !interviewDateTime.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Interview date and time must be in the future"
            );
        }

        if (!"ONLINE".equals(interviewMode)
                && !"OFFLINE".equals(interviewMode)) {

            throw new IllegalArgumentException(
                    "Select ONLINE or OFFLINE interview mode"
            );
        }

        if ("ONLINE".equals(interviewMode)
                && (meetingLink == null
                || meetingLink.isBlank())) {

            throw new IllegalArgumentException(
                    "Meeting link is required for online interviews"
            );
        }

        if ("OFFLINE".equals(interviewMode)
                && (interviewLocation == null
                || interviewLocation.isBlank())) {

            throw new IllegalArgumentException(
                    "Location is required for offline interviews"
            );
        }
    }

    // ==========================================
    // 6. VERIFY APPLICATION OWNERSHIP
    // ==========================================
    private JobApplication getAuthorizedApplication(
            Long applicationId,
            String recruiterEmail) {

        getRecruiter(recruiterEmail);

        boolean authorized = applicationRepository
                .existsByIdAndJobCompanyRecruiterEmail(
                        applicationId,
                        recruiterEmail
                );

        if (!authorized) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot manage this application"
            );
        }

        return applicationRepository.findById(applicationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"
                        )
                );
    }

    // ==========================================
    // 7. VERIFY RECRUITER
    // ==========================================
    private User getRecruiter(String recruiterEmail) {

        User recruiter = userRepository
                .findByEmail(recruiterEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Recruiter not found"
                        )
                );

        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only recruiters can manage interviews"
            );
        }

        return recruiter;
    }

    // ==========================================
    // 8. GET RECRUITER INTERVIEW
    // ==========================================
    @Transactional(readOnly = true)
    public Interview getRecruiterInterview(
            Long jobId,
            Long interviewId,
            String recruiterEmail) {

        getRecruiterJobInterviews(
                jobId,
                recruiterEmail
        );

        Interview interview = interviewRepository
                .findById(interviewId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Interview not found"
                        )
                );

        if (!interview.getApplication()
                .getJob()
                .getId()
                .equals(jobId)) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Interview does not belong to this job"
            );
        }

        return interview;
    }

    // ==========================================
    // 9. CANCEL INTERVIEW
    // ==========================================
    @Transactional
    public void cancelInterview(
            Long jobId,
            Long interviewId,
            String recruiterEmail) {

        Interview interview = getRecruiterInterview(
                jobId,
                interviewId,
                recruiterEmail
        );

        if (!"SCHEDULED".equals(interview.getStatus())) {
            throw new IllegalArgumentException(
                    "Only scheduled interviews can be cancelled"
            );
        }

        interview.setStatus("CANCELLED");

        interviewRepository.save(interview);

        // Send cancellation notification
        User candidate = interview.getApplication().getCandidate();
        String jobTitle = interview.getApplication().getJob().getTitle();

        notificationService.createNotification(
                candidate.getEmail(),
                "Interview Cancelled",
                "Your interview for "
                        + jobTitle
                        + " has been cancelled by the recruiter. "
                        + "Please check your application for further updates.",
                "INTERVIEW_CANCELLED",
                "/candidate/applications"
        );
    }

    // ==========================================
    // 10. MARK INTERVIEW COMPLETED
    // ==========================================
    @Transactional
    public void completeInterview(
            Long jobId,
            Long interviewId,
            String recruiterEmail) {

        Interview interview = getRecruiterInterview(
                jobId,
                interviewId,
                recruiterEmail
        );

        if (!"SCHEDULED".equals(interview.getStatus())) {
            throw new IllegalArgumentException(
                    "Only scheduled interviews can be completed"
            );
        }

        if (interview.getInterviewDateTime()
                .isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException(
                    "Cannot complete an interview before its scheduled time"
            );
        }

        interview.setStatus("COMPLETED");

        interviewRepository.save(interview);
    }

    // ==========================================
    // 11. DECLARE INTERVIEW RESULT
    // ==========================================
    @Transactional
    public void declareInterviewResult(
            Long jobId,
            Long interviewId,
            String recruiterEmail,
            String result,
            String feedback) {

        Interview interview = getRecruiterInterview(
                jobId,
                interviewId,
                recruiterEmail
        );

        if (!"COMPLETED".equals(interview.getStatus())) {
            throw new IllegalArgumentException(
                    "Only completed interviews can have results"
            );
        }

        // FIX: PENDING is not a declared result.
        String existingResult = interview.getResult() == null
                ? null
                : interview.getResult().toString();

        if (existingResult != null
                && !"PENDING".equalsIgnoreCase(existingResult)) {

            throw new IllegalArgumentException(
                    "Interview result has already been declared"
            );
        }

        // Normalize result
        String normalizedResult = result == null
                ? ""
                : result.trim().toUpperCase(Locale.ROOT);

        if (!"SELECTED".equals(normalizedResult)
                && !"REJECTED".equals(normalizedResult)
                && !"ON_HOLD".equals(normalizedResult)) {

            throw new IllegalArgumentException(
                    "Invalid interview result"
            );
        }

        if (feedback != null && feedback.length() > 2000) {
            throw new IllegalArgumentException(
                    "Feedback must not exceed 2000 characters"
            );
        }

        interview.setResult(normalizedResult);

        interview.setFeedback(
                feedback == null || feedback.isBlank()
                        ? null
                        : feedback.trim()
        );

        interview.setResultDeclaredAt(
                LocalDateTime.now()
        );

        interviewRepository.save(interview);

        // Send interview result notification
        User candidate = interview.getApplication().getCandidate();
        String jobTitle = interview.getApplication().getJob().getTitle();

        notificationService.createNotification(
                candidate.getEmail(),
                "Interview Result Declared",
                "Your interview result for "
                        + jobTitle
                        + " is "
                        + normalizedResult
                        + ". Please check your application for feedback.",
                "INTERVIEW_RESULT",
                "/candidate/applications"
        );
    }
}
