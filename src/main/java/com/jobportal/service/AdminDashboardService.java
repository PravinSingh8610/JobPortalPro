
package com.jobportal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.repository.UserRepository;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.JobApplicationRepository;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            CompanyRepository companyRepository,
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository) {

        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    // Count registered candidates
    @Transactional(readOnly = true)
    public long getTotalCandidates() {

        return userRepository.countByRole("ROLE_CANDIDATE");
    }

    // Count registered company/recruiter records
    @Transactional(readOnly = true)
    public long getTotalRecruiters() {

        return companyRepository.count();
    }

    // Count all posted jobs
    @Transactional(readOnly = true)
    public long getTotalJobs() {

        return jobRepository.count();
    }

    // Count all job applications
    @Transactional(readOnly = true)
    public long getTotalApplications() {

        return applicationRepository.count();
    }
}
