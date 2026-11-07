
package com.jobportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.InterviewRepository;
import com.jobportal.repository.JobOfferRepository;
import com.jobportal.repository.CandidateOnboardingRepository;

@Service
@Transactional(readOnly = true)
public class AdminDateWiseReportService {

    // ==========================================
    // REPOSITORIES
    // ==========================================

    private final JobApplicationRepository jobApplicationRepository;

    private final InterviewRepository interviewRepository;

    private final JobOfferRepository jobOfferRepository;

    private final CandidateOnboardingRepository candidateOnboardingRepository;

    // ==========================================
    // CONSTRUCTOR INJECTION
    // ==========================================

    public AdminDateWiseReportService(
            JobApplicationRepository jobApplicationRepository,
            InterviewRepository interviewRepository,
            JobOfferRepository jobOfferRepository,
            CandidateOnboardingRepository candidateOnboardingRepository) {

        this.jobApplicationRepository = jobApplicationRepository;
        this.interviewRepository = interviewRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.candidateOnboardingRepository = candidateOnboardingRepository;
    }

    // ==========================================
    // 1. COUNT APPLICATIONS BETWEEN DATES
    // ==========================================

    public long getApplicationsBetweenDates(
            LocalDate fromDate,
            LocalDate toDate) {

        validateDates(fromDate, toDate);

        LocalDateTime startDate =
                fromDate.atStartOfDay();

        LocalDateTime endDateExclusive =
                toDate.plusDays(1).atStartOfDay();

        return jobApplicationRepository
                .countApplicationsBetweenDates(
                        startDate,
                        endDateExclusive
                );
    }

    // ==========================================
    // 2. COUNT INTERVIEWS BETWEEN DATES
    // ==========================================

    public long getInterviewsBetweenDates(
            LocalDate fromDate,
            LocalDate toDate) {

        validateDates(fromDate, toDate);

        LocalDateTime startDate =
                fromDate.atStartOfDay();

        LocalDateTime endDateExclusive =
                toDate.plusDays(1).atStartOfDay();

        return interviewRepository
                .countInterviewsBetweenDates(
                        startDate,
                        endDateExclusive
                );
    }

    // ==========================================
    // 3. COUNT JOB OFFERS BETWEEN DATES
    // ==========================================

    public long getOffersBetweenDates(
            LocalDate fromDate,
            LocalDate toDate) {

        validateDates(fromDate, toDate);

        LocalDateTime startDate =
                fromDate.atStartOfDay();

        LocalDateTime endDateExclusive =
                toDate.plusDays(1).atStartOfDay();

        return jobOfferRepository
                .countOffersBetweenDates(
                        startDate,
                        endDateExclusive
                );
    }

    // ==========================================
    // 4. NEW: COUNT ONBOARDINGS BETWEEN DATES
    // ==========================================

    public long getOnboardingsBetweenDates(
            LocalDate fromDate,
            LocalDate toDate) {

        validateDates(fromDate, toDate);

        LocalDateTime startDate =
                fromDate.atStartOfDay();

        LocalDateTime endDateExclusive =
                toDate.plusDays(1).atStartOfDay();

        return candidateOnboardingRepository
                .countOnboardingsBetweenDates(
                        startDate,
                        endDateExclusive
                );
    }

    // ==========================================
    // 5. COMMON DATE VALIDATION
    // ==========================================

    private void validateDates(
            LocalDate fromDate,
            LocalDate toDate) {

        if (fromDate == null || toDate == null) {

            throw new IllegalArgumentException(
                    "From Date and To Date are required."
            );
        }

        if (fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException(
                    "From Date cannot be after To Date."
            );
        }
    }
}
