
package com.jobportal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jobportal.entity.Company;

public interface CompanyRepository
        extends JpaRepository<Company, Long> {

    // 1. Existing: Find company by recruiter email
    Optional<Company> findByRecruiterEmail(String email);

    // 2. NEW: Fetch all companies with recruiter details
    // Latest company first
    @EntityGraph(attributePaths = {"recruiter"})
    List<Company> findAllByOrderByIdDesc();

}
