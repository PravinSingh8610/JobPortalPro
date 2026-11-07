
package com.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jobportal.entity.JobApplication;

@Repository
public interface JobApplicationRepository
        extends JpaRepository<JobApplication, Long> {

    // ========================================
    // 1. CHECK WHETHER CANDIDATE ALREADY APPLIED
    // ========================================

    boolean existsByJobIdAndCandidateId(
            Long jobId,
            Long candidateId
    );

    // ========================================
    // 2. GET CANDIDATE APPLICATIONS
    // ========================================

    List<JobApplication> findByCandidateIdOrderByAppliedAtDesc(
            Long candidateId
    );

    // ========================================
    // 3. GET APPLICATIONS FOR A PARTICULAR JOB
    // ========================================

    List<JobApplication> findByJobIdOrderByAppliedAtDesc(
            Long jobId
    );

    // ========================================
    // 4. COUNT APPLICATIONS FOR A JOB
    // ========================================

    long countByJobId(Long jobId);

    // ========================================
    // 5. VERIFY APPLICATION BELONGS TO RECRUITER
    // ========================================

    boolean existsByIdAndJobCompanyRecruiterEmail(
            Long applicationId,
            String recruiterEmail
    );

    // ========================================
    // 6. ADMIN: GET ALL APPLICATIONS
    // ========================================

    @EntityGraph(attributePaths = {
            "candidate",
            "job",
            "job.company"
    })
    List<JobApplication> findAllByOrderByAppliedAtDesc();

    // ========================================
    // 7. ADMIN REPORTS & ANALYTICS
    // COUNT APPLICATIONS BY STATUS
    // ========================================

    long countByStatus(String status);

    // ========================================
    // 8. NEW: DATE-WISE APPLICATION REPORT
    // ========================================

    @Query("""
        SELECT COUNT(a)
        FROM JobApplication a
        WHERE a.appliedAt >= :startDate
          AND a.appliedAt < :endDateExclusive
        """)
    long countApplicationsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDateExclusive") LocalDateTime endDateExclusive
    );

}
