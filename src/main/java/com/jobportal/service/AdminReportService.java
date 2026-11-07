
package com.jobportal.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.repository.UserRepository;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.InterviewRepository;
import com.jobportal.repository.JobOfferRepository;
import com.jobportal.repository.CandidateOnboardingRepository;

@Service
@Transactional(readOnly = true)
public class AdminReportService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobOfferRepository jobOfferRepository;
    private final CandidateOnboardingRepository onboardingRepository;

    public AdminReportService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository,
            InterviewRepository interviewRepository,
            JobOfferRepository jobOfferRepository,
            CandidateOnboardingRepository onboardingRepository) {

        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.onboardingRepository = onboardingRepository;
    }

    // ========================================
    // PLATFORM OVERVIEW
    // ========================================

    public long getTotalCandidates() {
        return userRepository.countByRole("ROLE_CANDIDATE");
    }

    public long getTotalRecruiters() {
        return companyRepository.count();
    }

    public long getTotalJobs() {
        return jobRepository.count();
    }

    public long getTotalApplications() {
        return applicationRepository.count();
    }

    public long getTotalInterviews() {
        return interviewRepository.count();
    }

    public long getTotalOffers() {
        return jobOfferRepository.count();
    }

    public long getTotalOnboardings() {
        return onboardingRepository.count();
    }

    // ========================================
    // APPLICATION ANALYTICS
    // ========================================

    public long getApplicationCountByStatus(String status) {
        return applicationRepository.countByStatus(status);
    }

    // ========================================
    // INTERVIEW ANALYTICS
    // ========================================

    public long getInterviewCountByStatus(String status) {
        return interviewRepository.countByStatus(status);
    }

    public long getInterviewCountByResult(String result) {
        return interviewRepository.countByResult(result);
    }

    // ========================================
    // JOB OFFER ANALYTICS
    // ========================================

    public long getOfferCountByStatus(String status) {
        return jobOfferRepository.countByStatus(status);
    }

    // ========================================
    // ONBOARDING ANALYTICS
    // ========================================

    public long getOnboardingCountByStatus(String status) {
        return onboardingRepository.countByStatus(status);
    }

    // ========================================
    // OFFER ACCEPTANCE RATE
    // ========================================

    public double getOfferAcceptanceRate() {

        long totalOffers = jobOfferRepository.count();

        if (totalOffers == 0) {
            return 0.0;
        }

        long acceptedOffers =
                jobOfferRepository.countByStatus("ACCEPTED");

        return (acceptedOffers * 100.0) / totalOffers;
    }

    // ========================================
    // ONBOARDING APPROVAL RATE
    // ========================================

    public double getOnboardingApprovalRate() {

        long totalOnboardings = onboardingRepository.count();

        if (totalOnboardings == 0) {
            return 0.0;
        }

        long approvedOnboardings =
                onboardingRepository.countByStatus("APPROVED");

        return (approvedOnboardings * 100.0) / totalOnboardings;
    }

    // ========================================
    // DATE-WISE REPORTING
    // ========================================

    private LocalDateTime getStartDateTime(LocalDate date) {
        return date.atStartOfDay();
    }

    private LocalDateTime getEndDateTimeExclusive(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }

    public long getApplicationsBetweenDates(
            LocalDate startDate, LocalDate endDate) {

        return applicationRepository.countApplicationsBetweenDates(
                getStartDateTime(startDate),
                getEndDateTimeExclusive(endDate)
        );
    }

    public long getInterviewsBetweenDates(
            LocalDate startDate, LocalDate endDate) {

        return interviewRepository.countInterviewsBetweenDates(
                getStartDateTime(startDate),
                getEndDateTimeExclusive(endDate)
        );
    }

    public long getOffersBetweenDates(
            LocalDate startDate, LocalDate endDate) {

        return jobOfferRepository.countOffersBetweenDates(
                getStartDateTime(startDate),
                getEndDateTimeExclusive(endDate)
        );
    }

    public long getOnboardingsBetweenDates(
            LocalDate startDate, LocalDate endDate) {

        return onboardingRepository.countOnboardingsBetweenDates(
                getStartDateTime(startDate),
                getEndDateTimeExclusive(endDate)
        );
    }
}
