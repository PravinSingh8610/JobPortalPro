
package com.jobportal.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jobportal.entity.Job;

public interface JobRepository extends JpaRepository<Job, Long> {

    // RECRUITER: GET ALL COMPANY JOBS
    List<Job> findByCompanyIdOrderByCreatedAtDesc(
            Long companyId
    );

    // RECRUITER: COUNT COMPANY JOBS
    long countByCompanyId(Long companyId);

    // GET JOBS BY STATUS
    List<Job> findByStatusOrderByCreatedAtDesc(
            String status
    );

    // CANDIDATE ACTIVE JOBS
    List<Job>
    findByStatusAndApplicationDeadlineGreaterThanEqualOrderByCreatedAtDesc(
            String status,
            LocalDate applicationDeadline
    );

    // CANDIDATE JOB PAGINATION
    @EntityGraph(attributePaths = {"company"})
    Page<Job> findByStatusAndApplicationDeadlineGreaterThanEqual(
            String status,
            LocalDate applicationDeadline,
            Pageable pageable
    );

    // SEARCH + FILTER + PAGINATION + SORTING
    @EntityGraph(attributePaths = {"company"})
    @Query(
        value = """
            SELECT j
            FROM Job j
            WHERE j.status = 'ACTIVE'
              AND j.applicationDeadline >= :today
              AND (
                    :keyword = ''
                    OR LOWER(j.title) LIKE
                       LOWER(CONCAT('%', :keyword, '%'))
                  )
              AND (
                    :location = ''
                    OR LOWER(j.location) LIKE
                       LOWER(CONCAT('%', :location, '%'))
                  )
              AND (
                    :employmentType = ''
                    OR j.employmentType = :employmentType
                  )
            """,
        countQuery = """
            SELECT COUNT(j)
            FROM Job j
            WHERE j.status = 'ACTIVE'
              AND j.applicationDeadline >= :today
              AND (
                    :keyword = ''
                    OR LOWER(j.title) LIKE
                       LOWER(CONCAT('%', :keyword, '%'))
                  )
              AND (
                    :location = ''
                    OR LOWER(j.location) LIKE
                       LOWER(CONCAT('%', :location, '%'))
                  )
              AND (
                    :employmentType = ''
                    OR j.employmentType = :employmentType
                  )
            """
    )
    Page<Job> searchActiveJobs(
            @Param("today") LocalDate today,
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("employmentType") String employmentType,
            Pageable pageable
    );

    // ACTIVE JOBS FOR RECOMMENDATIONS
    @EntityGraph(attributePaths = {"company"})
    @Query("""
        SELECT j
        FROM Job j
        WHERE j.status = 'ACTIVE'
          AND j.applicationDeadline >= :today
        ORDER BY j.createdAt DESC, j.id DESC
        """)
    List<Job> findActiveJobsForRecommendations(
            @Param("today") LocalDate today
    );

    // ADMIN: GET ALL JOBS WITH COMPANY DETAILS
    @EntityGraph(attributePaths = {"company"})
    List<Job> findAllByOrderByCreatedAtDesc();

    // GET ACTIVE JOBS OF A PARTICULAR COMPANY
    @EntityGraph(attributePaths = {"company"})
    List<Job> findByCompanyIdAndStatusAndApplicationDeadlineGreaterThanEqualOrderByCreatedAtDesc(
            Long companyId,
            String status,
            LocalDate applicationDeadline
    );

    // AUTOMATIC EXPIRY: FIND JOBS WHOSE DEADLINE HAS PASSED
    List<Job> findByStatusAndApplicationDeadlineBefore(
            String status,
            LocalDate today
    );
}
