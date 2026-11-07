
package com.jobportal.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Company;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;

@Service
public class AdminRecruiterService {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    public AdminRecruiterService(
            CompanyRepository companyRepository,
            JobRepository jobRepository) {

        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional(readOnly = true)
    public List<Company> getAllRecruiters() {
        return companyRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public long getCompanyJobCount(Long companyId) {
        return jobRepository.countByCompanyId(companyId);
    }

    // Update recruiter verification status
    @Transactional
    public void updateVerificationStatus(
            Long companyId,
            String status,
            String remarks) {

        Set<String> allowedStatuses =
                Set.of("PENDING", "VERIFIED", "REJECTED");

        if (status == null ||
                !allowedStatuses.contains(status.toUpperCase())) {

            throw new IllegalArgumentException(
                    "Invalid verification status");
        }

        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Company not found"));

        company.setVerificationStatus(status.toUpperCase());
        company.setVerificationRemarks(remarks);

        companyRepository.save(company);
    }
}
