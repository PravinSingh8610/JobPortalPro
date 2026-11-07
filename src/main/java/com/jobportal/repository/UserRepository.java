
package com.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.jobportal.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // 1. Find user by email
    Optional<User> findByEmail(String email);

    // 2. Check if email already exists
    boolean existsByEmail(String email);

    // 3. Count users according to role
    long countByRole(String role);

    // 4. Get all candidates ordered by latest ID
    List<User> findByRoleOrderByIdDesc(String role);

    // 5. Advanced Candidate Search and Filtering
    @Query("""
        SELECT u FROM User u
        WHERE u.role = 'ROLE_CANDIDATE'
        AND (
            :keyword IS NULL
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
        AND (
            :skills IS NULL
            OR LOWER(u.skills) LIKE LOWER(CONCAT('%', :skills, '%'))
        )
        AND (
            :location IS NULL
            OR LOWER(u.location) LIKE LOWER(CONCAT('%', :location, '%'))
        )
        AND (:minExperience IS NULL OR u.experienceYears >= :minExperience)
        AND (:maxExperience IS NULL OR u.experienceYears <= :maxExperience)
        ORDER BY u.id DESC
        """)
    List<User> searchCandidates(
            @Param("keyword") String keyword,
            @Param("skills") String skills,
            @Param("location") String location,
            @Param("minExperience") Integer minExperience,
            @Param("maxExperience") Integer maxExperience
    );
}
