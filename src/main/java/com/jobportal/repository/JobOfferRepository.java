
package com.jobportal.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jobportal.entity.JobOffer;

@Repository
public interface JobOfferRepository
        extends JpaRepository<JobOffer, Long> {

    // 1. CHECK IF OFFER ALREADY EXISTS
    boolean existsByInterviewId(Long interviewId);

    // 2. FIND OFFER BY INTERVIEW ID
    Optional<JobOffer> findByInterviewId(Long interviewId);

    // 3. GET ALL OFFERS FOR A CANDIDATE
    @EntityGraph(attributePaths = {
            "interview",
            "interview.application",
            "interview.application.job",
            "interview.application.job.company"
    })
    List<JobOffer>
        findByInterviewApplicationCandidateIdOrderByCreatedAtDesc(
                Long candidateId);

    // 4. GET ALL OFFERS FOR A JOB
    @EntityGraph(attributePaths = {
            "interview",
            "interview.application",
            "interview.application.candidate",
            "interview.application.job",
            "interview.application.job.company"
    })
    List<JobOffer>
        findByInterviewApplicationJobIdOrderByCreatedAtDesc(
                Long jobId);

    // 5. ADMIN: GET ALL JOB OFFERS
    @EntityGraph(attributePaths = {
            "interview",
            "interview.application",
            "interview.application.candidate",
            "interview.application.job",
            "interview.application.job.company"
    })
    List<JobOffer> findAllByOrderByCreatedAtDesc();

    // 6. ADMIN: COUNT JOB OFFERS BY STATUS
    long countByStatus(String status);

    // 7. DATE-WISE JOB OFFERS COUNT
    @Query("""
        SELECT COUNT(o)
        FROM JobOffer o
        WHERE o.createdAt >= :startDate
          AND o.createdAt < :endDateExclusive
        """)
    long countOffersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDateExclusive") LocalDateTime endDateExclusive);

    // 8. FIND EXPIRED PENDING OFFERS
    List<JobOffer> findByStatusAndOfferExpiryDateBefore(
            String status,
            LocalDate expiryDate);

    // 9. BULK UPDATE EXPIRED OFFERS
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE JobOffer o
        SET o.status = 'EXPIRED'
        WHERE o.status = 'PENDING'
          AND o.offerExpiryDate < :today
        """)
    int expirePendingOffers(
            @Param("today") LocalDate today);
}
