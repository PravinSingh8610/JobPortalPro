
package com.jobportal.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jobportal.entity.CandidateOnboarding;

@Repository
public interface CandidateOnboardingRepository
        extends JpaRepository<CandidateOnboarding, Long> {

    // ==========================================
    // 1. FIND ONBOARDING BY JOB OFFER ID
    // ==========================================

    Optional<CandidateOnboarding> findByJobOfferId(
            Long offerId
    );

    // ==========================================
    // 2. CHECK IF ONBOARDING ALREADY EXISTS
    // ==========================================

    boolean existsByJobOfferId(
            Long offerId
    );

    // ==========================================
    // 3. GET ALL ONBOARDING RECORDS
    // FOR LOGGED-IN RECRUITER'S COMPANY
    // ==========================================

    @Query("""
        SELECT co
        FROM CandidateOnboarding co
        JOIN FETCH co.jobOffer jo
        JOIN FETCH jo.interview i
        JOIN FETCH i.application a
        JOIN FETCH a.candidate
        JOIN FETCH a.job j
        JOIN FETCH j.company c
        JOIN FETCH c.recruiter r
        WHERE LOWER(r.email) = LOWER(:recruiterEmail)
        ORDER BY co.createdAt DESC
        """)
    List<CandidateOnboarding> findAllByRecruiterEmail(
            @Param("recruiterEmail") String recruiterEmail
    );

    // ==========================================
    // 4. ADMIN ONBOARDING MANAGEMENT
    // GET ALL RECORDS, LATEST FIRST
    // ==========================================

    @EntityGraph(attributePaths = {
            "jobOffer",
            "jobOffer.interview",
            "jobOffer.interview.application",
            "jobOffer.interview.application.candidate",
            "jobOffer.interview.application.job",
            "jobOffer.interview.application.job.company"
    })
    List<CandidateOnboarding> findAllByOrderByCreatedAtDesc();

    // ==========================================
    // 5. ADMIN REPORTS & ANALYTICS
    // COUNT ONBOARDINGS BY STATUS
    // ==========================================

    long countByStatus(String status);

    // ==========================================
    // 6. NEW: DATE-WISE ONBOARDINGS COUNT
    // ==========================================

    @Query("""
        SELECT COUNT(co)
        FROM CandidateOnboarding co
        WHERE co.createdAt >= :startDate
          AND co.createdAt < :endDateExclusive
        """)
    long countOnboardingsBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDateExclusive") LocalDateTime endDateExclusive
    );
}
