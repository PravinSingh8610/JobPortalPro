
package com.jobportal.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.security.Principal;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.jobportal.entity.OnboardingDocument;
import com.jobportal.service.OnboardingDocumentService;
import com.jobportal.service.RecruiterOnboardingService;

@Controller
public class OnboardingDocumentDownloadController {

    private final OnboardingDocumentService documentService;
    private final RecruiterOnboardingService recruiterService;

    public OnboardingDocumentDownloadController(
            OnboardingDocumentService documentService,
            RecruiterOnboardingService recruiterService) {

        this.documentService = documentService;
        this.recruiterService = recruiterService;
    }

    // ==========================================
    // CANDIDATE DOCUMENT DOWNLOAD
    // ==========================================

    @GetMapping(
        "/candidate/onboarding/{onboardingId}/documents/{documentId}/download"
    )
    public ResponseEntity<Resource> downloadCandidateDocument(
            @PathVariable Long onboardingId,
            @PathVariable Long documentId,
            Principal principal) throws IOException {

        if (principal == null) {
            throw new AccessDeniedException("Login required.");
        }

        // Candidate ownership check
        OnboardingDocument document =
                documentService.getCandidateDocument(
                        onboardingId,
                        documentId,
                        principal.getName()
                );

        return buildDownloadResponse(document);
    }

    // ==========================================
    // RECRUITER DOCUMENT DOWNLOAD
    // ==========================================

    @GetMapping(
        "/recruiter/onboarding/{onboardingId}/documents/{documentId}/download"
    )
    public ResponseEntity<Resource> downloadRecruiterDocument(
            @PathVariable Long onboardingId,
            @PathVariable Long documentId,
            Principal principal) throws IOException {

        if (principal == null) {
            throw new AccessDeniedException("Login required.");
        }

        // Verify recruiter belongs to the company
        recruiterService.getRecruiterOnboarding(
                onboardingId,
                principal.getName()
        );

        // Find document within authorized onboarding
        OnboardingDocument document =
                recruiterService.getDocuments(
                        onboardingId,
                        principal.getName()
                )
                .stream()
                .filter(d -> documentId.equals(d.getId()))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found."
                        )
                );

        return buildDownloadResponse(document);
    }

    // ==========================================
    // BUILD SECURE FILE RESPONSE
    // ==========================================

    private ResponseEntity<Resource> buildDownloadResponse(
            OnboardingDocument document) throws IOException {

        Path filePath = documentService.getStoredFilePath(document);

        Resource resource = new FileSystemResource(filePath);

        String safeFileName = document.getOriginalFileName();

        if (safeFileName == null || safeFileName.isBlank()) {
            safeFileName = "document";
        }

        // Remove control characters from filename
        safeFileName = safeFileName.replaceAll(
                "[\\r\\n\\x00-\\x1F\\x7F]", "_"
        );

        MediaType mediaType;

        try {
            mediaType = MediaType.parseMediaType(
                    document.getContentType()
            );
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(resource.contentLength())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        org.springframework.http.ContentDisposition
                                .attachment()
                                .filename(safeFileName,
                                        java.nio.charset.StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .header("X-Content-Type-Options", "nosniff")
                .body(resource);
    }
}
