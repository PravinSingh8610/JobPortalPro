
package com.jobportal.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jobportal.entity.CandidateResume;

@Repository
public interface CandidateResumeRepository
        extends JpaRepository<CandidateResume, Long> {

    // Find resume using candidate's User ID
    Optional<CandidateResume> findByCandidate_Id(Long candidateId);

    // Check whether candidate has uploaded a resume
    boolean existsByCandidate_Id(Long candidateId);

    // Delete resume using candidate's User ID
    void deleteByCandidate_Id(Long candidateId);

}
