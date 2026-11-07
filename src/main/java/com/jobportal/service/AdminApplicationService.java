
package com.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.JobApplication;
import com.jobportal.repository.JobApplicationRepository;

@Service
public class AdminApplicationService {

    private final JobApplicationRepository applicationRepository;

    // Constructor Injection
    public AdminApplicationService(
            JobApplicationRepository applicationRepository) {

        this.applicationRepository = applicationRepository;
    }

    // ========================================
    // ADMIN: FETCH ALL JOB APPLICATIONS
    // ========================================

    @Transactional(readOnly = true)
    public List<JobApplication> getAllApplications() {

        return applicationRepository
                .findAllByOrderByAppliedAtDesc();
    }

    // ========================================
    // ADMIN: COUNT ALL APPLICATIONS
    // ========================================

    @Transactional(readOnly = true)
    public long getTotalApplications() {

        return applicationRepository.count();
    }
}
