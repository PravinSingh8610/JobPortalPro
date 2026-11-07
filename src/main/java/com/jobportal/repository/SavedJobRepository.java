
package com.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jobportal.entity.SavedJob;

public interface SavedJobRepository
        extends JpaRepository<SavedJob, Long> {

    // Check whether candidate has already saved this job
    boolean existsByCandidateIdAndJobId(
            Long candidateId,
            Long jobId
    );

    // Find a particular saved job
    Optional<SavedJob> findByCandidateIdAndJobId(
            Long candidateId,
            Long jobId
    );

    // Get all saved jobs of a candidate
    // Fetch job and company details together
    @EntityGraph(attributePaths = {"job", "job.company"})
    List<SavedJob> findByCandidateIdOrderBySavedAtDesc(
            Long candidateId
    );

    // Count total saved jobs of a candidate
    long countByCandidateId(Long candidateId);
}
