
package com.jobportal.service;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.jobportal.entity.CandidateResume;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.User;

import com.jobportal.repository.CandidateResumeRepository;
import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.UserRepository;

@Service
public class CandidateResumeService {

    private static final long MAX_FILE_SIZE =
            5L * 1024 * 1024;

    private final CandidateResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository applicationRepository;

    public CandidateResumeService(
            CandidateResumeRepository resumeRepository,
            UserRepository userRepository,
            JobApplicationRepository applicationRepository) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
    }

    // 1. UPLOAD OR REPLACE CANDIDATE RESUME
    @Transactional
    public void uploadResume(
            String candidateEmail,
            MultipartFile file) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate not found"
                        ));

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can upload resumes"
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a PDF resume"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "Resume must be 5 MB or smaller"
            );
        }

        // Get original uploaded filename
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException(
                    "Unable to identify uploaded file name"
            );
        }

        // Clean and validate filename
        String safeFileName = StringUtils.cleanPath(originalFileName);

        if (safeFileName.contains("..")
                || !safeFileName.toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Only PDF files are allowed"
            );
        }

        byte[] fileBytes;

        try {
            fileBytes = file.getBytes();
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Unable to read uploaded resume"
            );
        }

        // Validate actual PDF file signature
        if (fileBytes.length < 5
                || fileBytes[0] != '%'
                || fileBytes[1] != 'P'
                || fileBytes[2] != 'D'
                || fileBytes[3] != 'F'
                || fileBytes[4] != '-') {

            throw new IllegalArgumentException(
                    "Only valid PDF files are allowed"
            );
        }

        // Find existing resume or create new one
        CandidateResume resume = resumeRepository
                .findByCandidate_Id(candidate.getId())
                .orElseGet(CandidateResume::new);

        resume.setCandidate(candidate);

        // Save original filename instead of fixed filename
        resume.setFileName(safeFileName);

        resume.setContentType("application/pdf");
        resume.setFileData(fileBytes);

        resumeRepository.save(resume);
    }

    // 2. CHECK WHETHER CANDIDATE HAS RESUME
    @Transactional(readOnly = true)
    public boolean hasResume(String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate not found"
                        ));

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can access this feature"
            );
        }

        return resumeRepository
                .existsByCandidate_Id(candidate.getId());
    }

    // 3. GET CANDIDATE'S OWN RESUME
    @Transactional(readOnly = true)
    public CandidateResume getCandidateResume(
            String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate not found"
                        ));

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can access this feature"
            );
        }

        return resumeRepository
                .findByCandidate_Id(candidate.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Resume not uploaded yet"
                        ));
    }

    // 4. DELETE CANDIDATE'S OWN RESUME
    @Transactional
    public void deleteResume(String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate not found"
                        ));

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can delete resumes"
            );
        }

        resumeRepository.deleteByCandidate_Id(
                candidate.getId()
        );
    }

    // 5. CHECK RESUME AVAILABILITY FOR RECRUITER
    @Transactional(readOnly = true)
    public boolean hasResumeForApplication(
            Long applicationId,
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
                    "Only recruiters can access applicant resumes"
            );
        }

        boolean authorized = applicationRepository
                .existsByIdAndJobCompanyRecruiterEmail(
                        applicationId,
                        recruiterEmail
                );

        if (!authorized) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this applicant's resume"
            );
        }

        JobApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"
                        ));

        Long candidateId = application
                .getCandidate()
                .getId();

        return resumeRepository
                .existsByCandidate_Id(candidateId);
    }

    // 6. RECRUITER SECURE RESUME DOWNLOAD
    @Transactional(readOnly = true)
    public CandidateResume getResumeForRecruiter(
            Long applicationId,
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
                    "Only recruiters can download applicant resumes"
            );
        }

        boolean authorized = applicationRepository
                .existsByIdAndJobCompanyRecruiterEmail(
                        applicationId,
                        recruiterEmail
                );

        if (!authorized) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot access this applicant's resume"
            );
        }

        JobApplication application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Application not found"
                        ));

        Long candidateId = application
                .getCandidate()
                .getId();

        return resumeRepository
                .findByCandidate_Id(candidateId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate has not uploaded a resume"
                        ));
    }
}
