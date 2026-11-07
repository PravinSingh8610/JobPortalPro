
package com.jobportal.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Interview;
import com.jobportal.entity.JobOffer;
import com.jobportal.entity.User;
import com.jobportal.repository.JobOfferRepository;
import com.jobportal.repository.UserRepository;

@Service
public class JobOfferService {

    private final JobOfferRepository jobOfferRepository;
    private final InterviewService interviewService;
    private final UserRepository userRepository;

    public JobOfferService(
            JobOfferRepository jobOfferRepository,
            InterviewService interviewService,
            UserRepository userRepository) {

        this.jobOfferRepository = jobOfferRepository;
        this.interviewService = interviewService;
        this.userRepository = userRepository;
    }

    // ========================================
    // 1. CREATE OFFER - RECRUITER
    // ========================================

    @Transactional
    public JobOffer createOffer(
            Long jobId,
            Long interviewId,
            String recruiterEmail,
            String designation,
            BigDecimal offeredSalary,
            LocalDate joiningDate,
            LocalDate offerExpiryDate,
            String offerMessage) {

        // Verify recruiter ownership
        Interview interview = interviewService.getRecruiterInterview(
                jobId, interviewId, recruiterEmail);

        // Only completed and selected interviews can receive offers
        if (!"COMPLETED".equalsIgnoreCase(interview.getStatus())
                || !"SELECTED".equalsIgnoreCase(interview.getResult())) {

            throw new IllegalArgumentException(
                    "Offer can only be created for a completed and selected interview");
        }

        // Prevent duplicate offers
        if (jobOfferRepository.existsByInterviewId(interviewId)) {
            throw new IllegalArgumentException(
                    "Offer already exists for this interview");
        }

        // Validate designation
        if (designation == null
                || designation.isBlank()
                || designation.trim().length() > 150) {

            throw new IllegalArgumentException(
                    "Please enter a valid designation (maximum 150 characters)");
        }

        // Validate salary
        if (offeredSalary == null
                || offeredSalary.compareTo(BigDecimal.ZERO) <= 0
                || offeredSalary.precision() - offeredSalary.scale() > 10
                || offeredSalary.scale() > 2) {

            throw new IllegalArgumentException(
                    "Please enter a valid salary with up to 2 decimal places");
        }

        LocalDate today = LocalDate.now();

        // Validate joining date
        if (joiningDate == null || !joiningDate.isAfter(today)) {
            throw new IllegalArgumentException(
                    "Joining date must be in the future");
        }

        // Validate offer expiry date
        if (offerExpiryDate == null
                || !offerExpiryDate.isAfter(today)) {

            throw new IllegalArgumentException(
                    "Offer expiry date must be in the future");
        }

        // Expiry must be before joining date
        if (!joiningDate.isAfter(offerExpiryDate)) {
            throw new IllegalArgumentException(
                    "Joining date must be after offer expiry date");
        }

        // Validate offer message
        String cleanedMessage = offerMessage == null
                ? null : offerMessage.trim();

        if (cleanedMessage != null
                && cleanedMessage.length() > 2000) {

            throw new IllegalArgumentException(
                    "Offer message cannot exceed 2000 characters");
        }

        // Create JobOffer entity
        JobOffer offer = new JobOffer();

        offer.setInterview(interview);
        offer.setDesignation(designation.trim());
        offer.setOfferedSalary(offeredSalary);
        offer.setJoiningDate(joiningDate);
        offer.setOfferExpiryDate(offerExpiryDate);

        offer.setOfferMessage(
                cleanedMessage == null || cleanedMessage.isBlank()
                        ? null : cleanedMessage);

        offer.setStatus("PENDING");

        return jobOfferRepository.save(offer);
    }

    // ========================================
    // 2. GET CANDIDATE OFFERS
    // ========================================

    @Transactional(readOnly = true)
    public List<JobOffer> getCandidateOffers(String candidateEmail) {

        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Candidate not found"));

        return jobOfferRepository
                .findByInterviewApplicationCandidateIdOrderByCreatedAtDesc(
                        candidate.getId());
    }

    // ========================================
    // 3. GET RECRUITER JOB OFFERS
    // ========================================

    @Transactional(readOnly = true)
    public List<JobOffer> getJobOffers(
            Long jobId,
            String recruiterEmail) {

        // Verify recruiter owns this job
        interviewService.getRecruiterJobInterviews(
                jobId, recruiterEmail);

        return jobOfferRepository
                .findByInterviewApplicationJobIdOrderByCreatedAtDesc(
                        jobId);
    }

    // ========================================
    // 4. CANDIDATE ACCEPT / REJECT OFFER
    // ========================================

    @Transactional
    public void respondToOffer(
            Long offerId,
            String candidateEmail,
            String decision) {

        JobOffer offer = jobOfferRepository.findById(offerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Offer not found"));

        // Verify candidate ownership
        String ownerEmail = offer.getInterview()
                .getApplication()
                .getCandidate()
                .getEmail();

        if (!ownerEmail.equalsIgnoreCase(candidateEmail)) {
            throw new AccessDeniedException(
                    "You are not authorized to respond to this offer");
        }

        // Check offer status
        if (!"PENDING".equalsIgnoreCase(offer.getStatus())) {
            throw new IllegalArgumentException(
                    "This offer has already been processed");
        }

        // Check offer expiry
        if (offer.getOfferExpiryDate() == null
                || offer.getOfferExpiryDate().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "This offer has expired");
        }

        // Normalize decision
        String normalizedDecision = decision == null
                ? ""
                : decision.trim().toUpperCase(Locale.ROOT);

        if (!"ACCEPTED".equals(normalizedDecision)
                && !"REJECTED".equals(normalizedDecision)) {

            throw new IllegalArgumentException(
                    "Invalid offer decision. Choose ACCEPTED or REJECTED");
        }

        // Save candidate response
        offer.setStatus(normalizedDecision);
        offer.setRespondedAt(LocalDateTime.now());

        jobOfferRepository.save(offer);
    }

    // ========================================
    // 5. UPDATE EXPIRED OFFERS
    // ========================================

    @Transactional
    public void expirePendingOffers() {

        jobOfferRepository.expirePendingOffers(LocalDate.now());
    }

    // ========================================
    // 6. CANDIDATE - SECURE OFFER PDF DOWNLOAD
    // ========================================

    @Transactional(readOnly = true)
    public JobOffer getCandidateOfferForDownload(
            Long offerId,
            String candidateEmail) {

        JobOffer offer = jobOfferRepository.findById(offerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Offer not found"));

        // Verify candidate ownership
        String ownerEmail = offer.getInterview()
                .getApplication()
                .getCandidate()
                .getEmail();

        if (!ownerEmail.equalsIgnoreCase(candidateEmail)) {
            throw new AccessDeniedException(
                    "You are not authorized to download this offer");
        }

        // Initialize PDF-required associations
        offer.getInterview()
                .getApplication()
                .getJob()
                .getCompany()
                .getCompanyName();

        return offer;
    }

    // ========================================
    // 7. RECRUITER - SECURE OFFER PDF DOWNLOAD
    // ========================================

    @Transactional(readOnly = true)
    public JobOffer getRecruiterOfferForDownload(
            Long jobId,
            Long offerId,
            String recruiterEmail) {

        // Verify recruiter owns this job
        interviewService.getRecruiterJobInterviews(
                jobId, recruiterEmail);

        JobOffer offer = jobOfferRepository.findById(offerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Offer not found"));

        // Verify offer belongs to requested job
        Long actualJobId = offer.getInterview()
                .getApplication()
                .getJob()
                .getId();

        if (!jobId.equals(actualJobId)) {
            throw new AccessDeniedException(
                    "You are not authorized to download this offer");
        }

        // Initialize PDF-required associations
        offer.getInterview()
                .getApplication()
                .getCandidate()
                .getFullName();

        offer.getInterview()
                .getApplication()
                .getCandidate()
                .getEmail();

        offer.getInterview()
                .getApplication()
                .getJob()
                .getCompany()
                .getCompanyName();

        return offer;
    }
}
