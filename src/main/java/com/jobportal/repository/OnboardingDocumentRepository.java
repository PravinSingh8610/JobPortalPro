
package com.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jobportal.entity.OnboardingDocument;

public interface OnboardingDocumentRepository
        extends JpaRepository<OnboardingDocument, Long> {

    // Find all documents uploaded for an onboarding
    List<OnboardingDocument> findByOnboardingIdOrderByUploadedAtDesc(
            Long onboardingId);

    // Find documents by type
    List<OnboardingDocument> findByOnboardingIdAndDocumentType(
            Long onboardingId,
            String documentType);

    // Find a document belonging to a specific onboarding
    Optional<OnboardingDocument> findByIdAndOnboardingId(
            Long documentId,
            Long onboardingId);

    // Check whether a document type already exists
    boolean existsByOnboardingIdAndDocumentType(
            Long onboardingId,
            String documentType);

    // Count documents by verification status
    long countByOnboardingIdAndVerificationStatus(
            Long onboardingId,
            String verificationStatus);
}
