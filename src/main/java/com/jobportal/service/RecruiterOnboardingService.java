
package com.jobportal.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.entity.OnboardingDocument;
import com.jobportal.repository.CandidateOnboardingRepository;
import com.jobportal.repository.OnboardingDocumentRepository;

@Service
public class RecruiterOnboardingService {

    private final CandidateOnboardingRepository onboardingRepository;
    private final OnboardingDocumentRepository documentRepository;

    private static final Set<String> REQUIRED_TYPES =
            Set.of("RESUME", "MARKSHEET", "ID_PROOF");

    public RecruiterOnboardingService(
            CandidateOnboardingRepository onboardingRepository,
            OnboardingDocumentRepository documentRepository) {

        this.onboardingRepository = onboardingRepository;
        this.documentRepository = documentRepository;
    }

    // ==========================================
    // VERIFY RECRUITER OWNERSHIP
    // ==========================================

    @Transactional(readOnly = true)
    public CandidateOnboarding getRecruiterOnboarding(
            Long onboardingId,
            String recruiterEmail) {

        if (recruiterEmail == null || recruiterEmail.isBlank()) {
            throw new AccessDeniedException(
                    "Recruiter login is required.");
        }

        CandidateOnboarding onboarding =
                onboardingRepository.findById(onboardingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Onboarding record not found."));
        
        String ownerEmail = onboarding
                .getJobOffer()
                .getInterview()
                .getApplication()
                .getJob()
                .getCompany()
                .getRecruiter()
                .getEmail();

        if (ownerEmail == null ||
                !ownerEmail.equalsIgnoreCase(recruiterEmail)) {

            throw new AccessDeniedException(
                    "You cannot access this candidate onboarding.");
        }

        return onboarding;
    }

    // ==========================================
    // GET ALL DOCUMENTS (INCLUDING HISTORY)
    // ==========================================

    @Transactional(readOnly = true)
    public List<OnboardingDocument> getDocuments(
            Long onboardingId,
            String recruiterEmail) {

        getRecruiterOnboarding(onboardingId, recruiterEmail);

        return documentRepository
                .findByOnboardingIdOrderByUploadedAtDesc(
                        onboardingId);
    }

    // ==========================================
    // GET LATEST DOCUMENT OF EACH TYPE
    // ==========================================

    @Transactional(readOnly = true)
    public List<OnboardingDocument> getLatestDocuments(
            Long onboardingId,
            String recruiterEmail) {

        getRecruiterOnboarding(onboardingId, recruiterEmail);

        List<OnboardingDocument> documents =
                documentRepository
                        .findByOnboardingIdOrderByUploadedAtDesc(
                                onboardingId);

        Map<String, OnboardingDocument> latestDocuments =
                findLatestDocuments(documents);

        return List.copyOf(latestDocuments.values());
    }

    // ==========================================
    // HELPER: FIND LATEST DOCUMENT PER TYPE
    // ==========================================

    private Map<String, OnboardingDocument> findLatestDocuments(
            List<OnboardingDocument> documents) {

        Map<String, OnboardingDocument> latestDocuments =
                new HashMap<>();

        for (OnboardingDocument document : documents) {

            String documentType = document.getDocumentType();

            OnboardingDocument existing =
                    latestDocuments.get(documentType);

            // Use highest database ID as newest version
            if (existing == null ||
                    document.getId() > existing.getId()) {

                latestDocuments.put(documentType, document);
            }
        }

        return latestDocuments;
    }

    // ==========================================
    // APPROVE OR REJECT LATEST DOCUMENT
    // ==========================================

    @Transactional
    public void verifyDocument(
            Long onboardingId,
            Long documentId,
            String recruiterEmail,
            String decision,
            String remarks) {

        CandidateOnboarding onboarding =
                getRecruiterOnboarding(
                        onboardingId,
                        recruiterEmail);

        if ("APPROVED".equals(onboarding.getStatus())) {
            throw new IllegalArgumentException(
                    "Onboarding is already approved.");
        }

        if (!"APPROVED".equals(decision)
                && !"REJECTED".equals(decision)) {

            throw new IllegalArgumentException(
                    "Invalid verification decision.");
        }

        if ("REJECTED".equals(decision)
                && (remarks == null || remarks.isBlank())) {

            throw new IllegalArgumentException(
                    "Please provide a reason for rejection.");
        }

        OnboardingDocument document =
                documentRepository.findByIdAndOnboardingId(
                        documentId,
                        onboardingId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Document not found."));

        // Only the latest version may be verified
        List<OnboardingDocument> documents =
                documentRepository
                        .findByOnboardingIdOrderByUploadedAtDesc(
                                onboardingId);

        Map<String, OnboardingDocument> latestDocuments =
                findLatestDocuments(documents);

        OnboardingDocument latestDocument =
                latestDocuments.get(document.getDocumentType());

        if (latestDocument == null ||
                !latestDocument.getId().equals(document.getId())) {

            throw new IllegalArgumentException(
                    "This document has been replaced. "
                    + "Please verify the latest uploaded version.");
        }

        LocalDateTime now = LocalDateTime.now();

        document.setVerificationStatus(decision);
        document.setRecruiterRemarks(
                remarks == null || remarks.isBlank()
                        ? null : remarks.trim());
        document.setVerifiedAt(now);

        if ("REJECTED".equals(decision)) {
            onboarding.setStatus("REJECTED");
        } else {
            onboarding.setStatus("UNDER_REVIEW");
        }

        onboarding.setReviewedAt(now);
    }

    // ==========================================
    // GET ALL RECRUITER ONBOARDINGS
    // ==========================================

    @Transactional(readOnly = true)
    public List<CandidateOnboarding> getAllRecruiterOnboardings(
            String recruiterEmail) {

        if (recruiterEmail == null ||
                recruiterEmail.isBlank()) {

            throw new AccessDeniedException(
                    "Recruiter login is required.");
        }

        return onboardingRepository.findAllByRecruiterEmail(
                recruiterEmail);
    }

    // ==========================================
    // COMPLETE ONBOARDING - LATEST DOCUMENTS
    // ==========================================

    @Transactional
    public void completeOnboarding(
            Long onboardingId,
            String recruiterEmail) {

        CandidateOnboarding onboarding =
                getRecruiterOnboarding(
                        onboardingId,
                        recruiterEmail);

        if ("APPROVED".equals(onboarding.getStatus())) {
            throw new IllegalArgumentException(
                    "This onboarding is already completed.");
        }

        List<OnboardingDocument> documents =
                documentRepository
                        .findByOnboardingIdOrderByUploadedAtDesc(
                                onboardingId);

        if (documents.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot complete onboarding. No documents uploaded.");
        }

        Map<String, OnboardingDocument> latestDocuments =
                findLatestDocuments(documents);

        // Check all required document types
        for (String requiredType : REQUIRED_TYPES) {

            OnboardingDocument latestDocument =
                    latestDocuments.get(requiredType);

            if (latestDocument == null) {
                throw new IllegalArgumentException(
                        "Required document missing: " + requiredType);
            }

            if (!"APPROVED".equals(
                    latestDocument.getVerificationStatus())) {

                throw new IllegalArgumentException(
                        "Latest " + requiredType
                        + " document must be approved "
                        + "before completing onboarding.");
            }
        }

        // Check optional uploaded documents too
        for (OnboardingDocument latestDocument :
                latestDocuments.values()) {

            if (!"APPROVED".equals(
                    latestDocument.getVerificationStatus())) {

                throw new IllegalArgumentException(
                        "Document verification pending or rejected: "
                        + latestDocument.getDocumentType());
            }
        }

        LocalDateTime now = LocalDateTime.now();

        onboarding.setStatus("APPROVED");
        onboarding.setReviewedAt(now);
        onboarding.setCompletedAt(now);
    }
}
