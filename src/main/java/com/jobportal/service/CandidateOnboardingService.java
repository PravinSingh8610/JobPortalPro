
package com.jobportal.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.entity.JobOffer;
import com.jobportal.entity.OnboardingDocument;

import com.jobportal.repository.CandidateOnboardingRepository;
import com.jobportal.repository.JobOfferRepository;
import com.jobportal.repository.OnboardingDocumentRepository;

@Service
public class CandidateOnboardingService {

    private final CandidateOnboardingRepository onboardingRepository;

    private final JobOfferRepository jobOfferRepository;

    private final OnboardingDocumentRepository documentRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public CandidateOnboardingService(
            CandidateOnboardingRepository onboardingRepository,
            JobOfferRepository jobOfferRepository,
            OnboardingDocumentRepository documentRepository) {

        this.onboardingRepository = onboardingRepository;
        this.jobOfferRepository = jobOfferRepository;
        this.documentRepository = documentRepository;
    }

    // ==========================================
    // START ONBOARDING - ACCEPTED OFFER ONLY
    // ==========================================

    @Transactional
    public CandidateOnboarding startOnboarding(
            Long offerId,
            String candidateEmail) {

        JobOffer offer = jobOfferRepository.findById(offerId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Job offer not found"));

        String ownerEmail = offer.getInterview()
                .getApplication()
                .getCandidate()
                .getEmail();

        if (candidateEmail == null ||
                !ownerEmail.equalsIgnoreCase(candidateEmail)) {

            throw new AccessDeniedException(
                    "You cannot access this job offer");
        }

        if (!"ACCEPTED".equals(offer.getStatus())) {

            throw new IllegalArgumentException(
                    "Only accepted job offers can start onboarding");
        }

        Optional<CandidateOnboarding> existing =
                onboardingRepository.findByJobOfferId(offerId);

        if (existing.isPresent()) {
            return existing.get();
        }

        CandidateOnboarding onboarding =
                new CandidateOnboarding();

        onboarding.setJobOffer(offer);
        onboarding.setStatus("PENDING");

        return onboardingRepository.save(onboarding);
    }

    // ==========================================
    // VIEW OWN ONBOARDING
    // ==========================================

    @Transactional(readOnly = true)
    public CandidateOnboarding getCandidateOnboarding(
            Long onboardingId,
            String candidateEmail) {

        CandidateOnboarding onboarding =
                onboardingRepository.findById(onboardingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Onboarding record not found"));

        String ownerEmail = onboarding.getJobOffer()
                .getInterview()
                .getApplication()
                .getCandidate()
                .getEmail();

        if (candidateEmail == null ||
                !ownerEmail.equalsIgnoreCase(candidateEmail)) {

            throw new AccessDeniedException(
                    "You cannot view this onboarding record");
        }

        return onboarding;
    }

    // ==========================================
    // STEP 71: LATEST DOCUMENT BASED PROGRESS
    // ==========================================

    @Transactional(readOnly = true)
    public int getOnboardingProgress(
            Long onboardingId,
            String candidateEmail) {

        // STEP 1: Verify candidate ownership
        CandidateOnboarding onboarding =
                getCandidateOnboarding(
                        onboardingId,
                        candidateEmail
                );

        // STEP 2: Completed onboarding is always 100%
        if ("APPROVED".equals(onboarding.getStatus())) {
            return 100;
        }

        // STEP 3: Fetch all uploaded documents
        List<OnboardingDocument> documents =
                documentRepository
                        .findByOnboardingIdOrderByUploadedAtDesc(
                                onboardingId
                        );

        // STEP 4: Keep latest document of each type
        Map<String, OnboardingDocument> latestDocuments =
                new HashMap<>();

        for (OnboardingDocument document : documents) {

            String documentType = document.getDocumentType();

            OnboardingDocument existing =
                    latestDocuments.get(documentType);

            // Higher ID represents the newer saved record
            if (existing == null ||
                    document.getId() > existing.getId()) {

                latestDocuments.put(
                        documentType,
                        document
                );
            }
        }

        // STEP 5: Required document types
        Set<String> requiredTypes = Set.of(
                "RESUME",
                "MARKSHEET",
                "ID_PROOF"
        );

        // STEP 6: Count only latest approved documents
        long approvedCount = requiredTypes.stream()
                .filter(requiredType -> {

                    OnboardingDocument latestDocument =
                            latestDocuments.get(requiredType);

                    return latestDocument != null
                            && "APPROVED".equals(
                                    latestDocument.getVerificationStatus()
                            );
                })
                .count();

        // STEP 7: Calculate progress percentage
        return (int) (
                approvedCount * 100 / requiredTypes.size()
        );
    }

    // ==========================================
    // GET DOCUMENT STATUS LIST
    // ==========================================

    @Transactional(readOnly = true)
    public List<OnboardingDocument> getOnboardingDocuments(
            Long onboardingId,
            String candidateEmail) {

        // Verify candidate ownership
        getCandidateOnboarding(
                onboardingId,
                candidateEmail
        );

        // Return all documents, including older versions,
        // so candidate can view upload history.
        return documentRepository
                .findByOnboardingIdOrderByUploadedAtDesc(
                        onboardingId
                );
    }
}
