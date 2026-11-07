
package com.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Job;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.JobApplicationRepository;

@Service
public class AdminJobService {

    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;

    // Constructor Injection
    public AdminJobService(
            JobRepository jobRepository,
            JobApplicationRepository applicationRepository) {

        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    // ========================================
    // ADMIN: FETCH ALL POSTED JOBS
    // ========================================

    @Transactional(readOnly = true)
    public List<Job> getAllJobs() {

        return jobRepository.findAllByOrderByCreatedAtDesc();
    }

    // ========================================
    // ADMIN: COUNT APPLICATIONS FOR A JOB
    // ========================================

    @Transactional(readOnly = true)
    public long getApplicationCount(Long jobId) {

        return applicationRepository.countByJobId(jobId);
    }

}
