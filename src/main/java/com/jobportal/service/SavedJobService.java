
package com.jobportal.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Job;
import com.jobportal.entity.SavedJob;
import com.jobportal.entity.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.SavedJobRepository;
import com.jobportal.repository.UserRepository;

@Service
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public SavedJobService(
            SavedJobRepository savedJobRepository,
            JobRepository jobRepository,
            UserRepository userRepository) {

        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    // ==========================================
    // 1. SAVE JOB
    // ==========================================

    @Transactional
    public void saveJob(Long jobId, String candidateEmail) {

        User candidate = getCandidate(candidateEmail);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Job not found."
                        )
                );

        // Only active and non-expired jobs can be saved
        if (!"ACTIVE".equals(job.getStatus())
                || job.getApplicationDeadline() == null
                || job.getApplicationDeadline()
                        .isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "This job is no longer available."
            );
        }

        // Prevent duplicate saved jobs
        boolean alreadySaved =
                savedJobRepository.existsByCandidateIdAndJobId(
                        candidate.getId(),
                        jobId
                );

        if (alreadySaved) {
            throw new IllegalArgumentException(
                    "You have already saved this job."
            );
        }

        SavedJob savedJob = new SavedJob();

        savedJob.setCandidate(candidate);
        savedJob.setJob(job);

        savedJobRepository.save(savedJob);
    }

    // ==========================================
    // 2. REMOVE SAVED JOB
    // ==========================================

    @Transactional
    public void removeSavedJob(
            Long jobId,
            String candidateEmail) {

        User candidate = getCandidate(candidateEmail);

        SavedJob savedJob = savedJobRepository
                .findByCandidateIdAndJobId(
                        candidate.getId(),
                        jobId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "This job is not in your saved jobs."
                        )
                );

        savedJobRepository.delete(savedJob);
    }

    // ==========================================
    // 3. GET ALL SAVED JOBS
    // ==========================================

    @Transactional(readOnly = true)
    public List<SavedJob> getSavedJobs(
            String candidateEmail) {

        User candidate = getCandidate(candidateEmail);

        return savedJobRepository
                .findByCandidateIdOrderBySavedAtDesc(
                        candidate.getId()
                );
    }

    // ==========================================
    // 4. CHECK IF JOB IS SAVED
    // ==========================================

    @Transactional(readOnly = true)
    public boolean isJobSaved(
            Long jobId,
            String candidateEmail) {

        User candidate = getCandidate(candidateEmail);

        return savedJobRepository
                .existsByCandidateIdAndJobId(
                        candidate.getId(),
                        jobId
                );
    }

    // ==========================================
    // 5. COUNT SAVED JOBS
    // ==========================================

    @Transactional(readOnly = true)
    public long countSavedJobs(String candidateEmail) {

        User candidate = getCandidate(candidateEmail);

        return savedJobRepository
                .countByCandidateId(candidate.getId());
    }

    // ==========================================
    // 6. GET LOGGED-IN CANDIDATE
    // ==========================================

    private User getCandidate(String candidateEmail) {

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate account not found."
                        )
                );

        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            throw new IllegalArgumentException(
                    "Only candidates can save jobs."
            );
        }

        return candidate;
    }
}
