
package com.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jobportal.entity.Interview;

@Repository
public interface InterviewRepository
        extends JpaRepository<Interview, Long> {

    // ========================================
    // 1. GET ALL INTERVIEWS FOR CANDIDATE
    // ========================================

    // UPDATED: Fetch application with interviews
    @EntityGraph(attributePaths = {
            "application"
    })
    List<Interview>
        findByApplicationCandidateIdOrderByInterviewDateTimeAsc(
                Long candidateId
        );

    // ========================================
    // 2. GET INTERVIEWS FOR PARTICULAR JOB
    // ========================================

    List<Interview>
        findByApplicationJobIdOrderByInterviewDateTimeAsc(
                Long jobId
        );

    // ========================================
    // 3. GET INTERVIEWS FOR APPLICATION
    // ========================================

    List<Interview>
        findByApplicationIdOrderByInterviewDateTimeDesc(
                Long applicationId
        );

    // ========================================
    // 4. COUNT INTERVIEWS FOR CANDIDATE
    // ========================================

    long countByApplicationCandidateId(
            Long candidateId
    );

    // ========================================
    // 5. COUNT INTERVIEWS BY CANDIDATE AND STATUS
    // ========================================

    long countByApplicationCandidateIdAndStatus(
            Long candidateId,
            String status
    );

    // ========================================
    // 6. COUNT INTERVIEWS FOR RECRUITER
    // ========================================

    long countByApplicationJobCompanyRecruiterId(
            Long recruiterId
    );

    // ========================================
    // 7. ADMIN: GET ALL INTERVIEWS
    // ========================================

    @EntityGraph(attributePaths = {
            "application",
            "application.candidate",
            "application.job",
            "application.job.company"
    })
    List<Interview> findAllByOrderByInterviewDateTimeDesc();

    // ========================================
    // 8. ADMIN REPORTS & ANALYTICS
    // COUNT INTERVIEWS BY STATUS
    // ========================================

    long countByStatus(String status);

    // ========================================
    // 9. ADMIN REPORTS & ANALYTICS
    // COUNT INTERVIEWS BY RESULT
    // ========================================

    long countByResult(String result);

    // ========================================
    // 10. ADVANCED DATE-WISE ANALYTICS
    // ========================================

    @Query("""
        SELECT COUNT(i)
        FROM Interview i
        WHERE i.interviewDateTime >= :startDate
          AND i.interviewDateTime < :endDateExclusive
        """)
    long countInterviewsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDateExclusive") LocalDateTime endDateExclusive
    );
}
