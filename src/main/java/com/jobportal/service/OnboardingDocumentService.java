
package com.jobportal.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.entity.OnboardingDocument;
import com.jobportal.repository.OnboardingDocumentRepository;

@Service
public class OnboardingDocumentService {

    // ==========================================
    // CONSTANTS
    // ==========================================

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024;

    private static final Set<String> DOCUMENT_TYPES =
            Set.of(
                    "RESUME",
                    "MARKSHEET",
                    "ID_PROOF",
                    "CERTIFICATE"
            );

    // ==========================================
    // DEPENDENCIES
    // ==========================================

    private final OnboardingDocumentRepository documentRepository;
    private final CandidateOnboardingService onboardingService;
    private final Path uploadDirectory;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public OnboardingDocumentService(
            OnboardingDocumentRepository documentRepository,
            CandidateOnboardingService onboardingService,
            @Value("${app.onboarding.upload-dir}") String uploadDir) {

        this.documentRepository = documentRepository;
        this.onboardingService = onboardingService;

        this.uploadDirectory = Path.of(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    // ==========================================
    // UPLOAD DOCUMENT
    // ALSO SUPPORTS RE-UPLOAD
    // ==========================================

    @Transactional
    public OnboardingDocument uploadDocument(
            Long onboardingId,
            String candidateEmail,
            String documentType,
            MultipartFile file) {

        // 1. Verify candidate ownership
        CandidateOnboarding onboarding =
                onboardingService.getCandidateOnboarding(
                        onboardingId,
                        candidateEmail
                );

        // 2. Accepted offer required
        if (!"ACCEPTED".equals(
                onboarding.getJobOffer().getStatus())) {

            throw new IllegalArgumentException(
                    "An accepted job offer is required."
            );
        }

        // 3. Block uploads after completion
        if ("APPROVED".equals(onboarding.getStatus())) {

            throw new IllegalArgumentException(
                    "Onboarding is already approved. "
                    + "Document upload is disabled."
            );
        }

        // 4. Validate document type
        if (documentType == null
                || !DOCUMENT_TYPES.contains(documentType)) {

            throw new IllegalArgumentException(
                    "Invalid document type."
            );
        }

        // 5. Validate uploaded file
        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a file."
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new IllegalArgumentException(
                    "Maximum file size is 5 MB."
            );
        }

        // 6. Prepare original filename
        String originalName = file.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            originalName = "document";
        }

        originalName = originalName.replace('\\', '/');

        originalName = originalName.substring(
                originalName.lastIndexOf('/') + 1
        );

        if (originalName.isBlank()) {
            originalName = "document";
        }

        if (originalName.length() > 255) {

            originalName = originalName.substring(
                    originalName.length() - 255
            );
        }

        // 7. Validate file signature
        String extension;
        String contentType;

        try (InputStream input = file.getInputStream()) {

            byte[] header = input.readNBytes(8);

            boolean pdf =
                    header.length >= 5
                    && header[0] == '%'
                    && header[1] == 'P'
                    && header[2] == 'D'
                    && header[3] == 'F'
                    && header[4] == '-';

            boolean png =
                    header.length >= 8
                    && (header[0] & 0xFF) == 0x89
                    && header[1] == 'P'
                    && header[2] == 'N'
                    && header[3] == 'G'
                    && (header[4] & 0xFF) == 0x0D
                    && (header[5] & 0xFF) == 0x0A
                    && (header[6] & 0xFF) == 0x1A
                    && (header[7] & 0xFF) == 0x0A;

            boolean jpeg =
                    header.length >= 3
                    && (header[0] & 0xFF) == 0xFF
                    && (header[1] & 0xFF) == 0xD8
                    && (header[2] & 0xFF) == 0xFF;

            if (pdf) {

                extension = ".pdf";
                contentType = "application/pdf";

            } else if (png) {

                extension = ".png";
                contentType = "image/png";

            } else if (jpeg) {

                extension = ".jpg";
                contentType = "image/jpeg";

            } else {

                throw new IllegalArgumentException(
                        "Only PDF, PNG and JPG files are allowed."
                );
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to read uploaded file.",
                    e
            );
        }

        // 8. Generate unique stored filename
        String storedName =
                UUID.randomUUID().toString() + extension;

        Path destination =
                uploadDirectory.resolve(storedName).normalize();

        if (!destination.startsWith(uploadDirectory)) {

            throw new IllegalArgumentException(
                    "Invalid upload destination."
            );
        }

        // 9. Save file privately
        try {

            Files.createDirectories(uploadDirectory);

            try (InputStream input = file.getInputStream()) {

                // Do NOT use COPY_ATTRIBUTES here.
                Files.copy(input, destination);
            }

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Unable to save document.",
                    e
            );
        }

        // 10. Save document metadata
        try {

            OnboardingDocument document =
                    new OnboardingDocument();

            document.setOnboarding(onboarding);
            document.setDocumentType(documentType);
            document.setOriginalFileName(originalName);
            document.setStoredFileName(storedName);
            document.setContentType(contentType);
            document.setFileSize(file.getSize());

            // Every new upload requires fresh verification
            document.setVerificationStatus("PENDING");

            document.setRecruiterRemarks(null);
            document.setVerifiedAt(null);

            OnboardingDocument savedDocument =
                    documentRepository.save(document);

            // Re-open review when necessary
            if ("PENDING".equals(onboarding.getStatus())
                    || "REJECTED".equals(onboarding.getStatus())) {

                onboarding.setStatus("UNDER_REVIEW");
            }

            return savedDocument;

        } catch (RuntimeException e) {

            // Clean up newly saved physical file
            try {

                Files.deleteIfExists(destination);

            } catch (IOException ignored) {
                // Cleanup failure can be logged.
            }

            throw e;
        }
    }

    // ==========================================
    // STEP 73: SECURE DOCUMENT REPLACEMENT
    // ==========================================

    @Transactional
    public OnboardingDocument replaceDocument(
            Long onboardingId,
            Long oldDocumentId,
            String candidateEmail,
            MultipartFile newFile) {

        // 1. Verify candidate ownership
        CandidateOnboarding onboarding =
                onboardingService.getCandidateOnboarding(
                        onboardingId,
                        candidateEmail
                );

        // 2. Block replacement after completion
        if ("APPROVED".equals(onboarding.getStatus())) {

            throw new IllegalArgumentException(
                    "Completed onboarding documents "
                    + "cannot be replaced."
            );
        }

        // 3. Find the document belonging to this onboarding
        OnboardingDocument oldDocument =
                documentRepository.findByIdAndOnboardingId(
                        oldDocumentId,
                        onboardingId
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found."
                        )
                );

        // 4. Find the latest document of the same type
        List<OnboardingDocument> documents =
                documentRepository
                        .findByOnboardingIdOrderByUploadedAtDesc(
                                onboardingId
                        );

        OnboardingDocument latestDocument = null;

        for (OnboardingDocument document : documents) {

            if (!oldDocument.getDocumentType().equals(
                    document.getDocumentType())) {
                continue;
            }

            if (latestDocument == null
                    || document.getId() > latestDocument.getId()) {

                latestDocument = document;
            }
        }

        // 5. Reject replacement of previous versions
        if (latestDocument == null
                || !latestDocument.getId().equals(
                        oldDocument.getId())) {

            throw new IllegalArgumentException(
                    "This document is an older version. "
                    + "Only the latest uploaded document "
                    + "can be replaced."
            );
        }

        // 6. Keep original document type
        String documentType =
                oldDocument.getDocumentType();

        // 7. Save replacement as a new version
        // Previous files and records remain for history
        return uploadDocument(
                onboardingId,
                candidateEmail,
                documentType,
                newFile
        );
    }

    // ==========================================
    // GET ALL CANDIDATE DOCUMENTS
    // ==========================================

    @Transactional(readOnly = true)
    public List<OnboardingDocument> getCandidateDocuments(
            Long onboardingId,
            String candidateEmail) {

        onboardingService.getCandidateOnboarding(
                onboardingId,
                candidateEmail
        );

        return documentRepository
                .findByOnboardingIdOrderByUploadedAtDesc(
                        onboardingId
                );
    }

    // ==========================================
    // GET ONE AUTHORIZED CANDIDATE DOCUMENT
    // ==========================================

    @Transactional(readOnly = true)
    public OnboardingDocument getCandidateDocument(
            Long onboardingId,
            Long documentId,
            String candidateEmail) {

        onboardingService.getCandidateOnboarding(
                onboardingId,
                candidateEmail
        );

        return documentRepository
                .findByIdAndOnboardingId(
                        documentId,
                        onboardingId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Document not found."
                        )
                );
    }

    // ==========================================
    // GET LATEST DOCUMENT OF EACH TYPE
    // ==========================================

    @Transactional(readOnly = true)
    public List<OnboardingDocument> getLatestDocuments(
            Long onboardingId,
            String candidateEmail) {

        // Verify candidate ownership
        onboardingService.getCandidateOnboarding(
                onboardingId,
                candidateEmail
        );

        List<OnboardingDocument> documents =
                documentRepository
                        .findByOnboardingIdOrderByUploadedAtDesc(
                                onboardingId
                        );

        // Highest database ID = latest saved version
        Map<String, OnboardingDocument> latest =
                new HashMap<>();

        for (OnboardingDocument document : documents) {

            String type = document.getDocumentType();

            OnboardingDocument existing = latest.get(type);

            if (existing == null
                    || document.getId() > existing.getId()) {

                latest.put(type, document);
            }
        }

        // Return newest versions first
        List<OnboardingDocument> result =
                new ArrayList<>(latest.values());

        result.sort(
                Comparator.comparing(
                        OnboardingDocument::getId
                ).reversed()
        );

        return List.copyOf(result);
    }

    // ==========================================
    // GET PRIVATE STORED FILE PATH
    // ==========================================

    public Path getStoredFilePath(
            OnboardingDocument document) {

        if (document == null) {

            throw new IllegalArgumentException(
                    "Invalid document."
            );
        }

        String storedFileName =
                document.getStoredFileName();

        if (storedFileName == null
                || storedFileName.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid stored filename."
            );
        }

        Path filePath = uploadDirectory
                .resolve(storedFileName)
                .normalize();

        // Prevent path traversal
        if (!filePath.startsWith(uploadDirectory)) {

            throw new IllegalArgumentException(
                    "Invalid document path."
            );
        }

        // Prevent serving missing files
        if (!Files.isRegularFile(filePath)) {

            throw new IllegalArgumentException(
                    "Document file not found."
            );
        }

        return filePath;
    }
}
