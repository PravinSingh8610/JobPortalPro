
package com.jobportal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Company;
import com.jobportal.entity.User;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.UserRepository;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(
            CompanyRepository companyRepository,
            UserRepository userRepository) {

        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    // Get existing profile or create a blank profile object
    @Transactional(readOnly = true)
    public Company getCompanyProfile(String email) {

        return companyRepository.findByRecruiterEmail(email)
                .orElseGet(() -> {
                    Company company = new Company();

                    User recruiter = userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException("Recruiter not found"));

                    company.setRecruiter(recruiter);

                    return company;
                });
    }

    // Save or update company profile
    @Transactional
    public Company saveOrUpdateCompany(
            String email, Company companyDetails) {

        User recruiter = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Recruiter not found"));

        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            throw new RuntimeException("Only recruiters can update company profile");
        }

        Company company = companyRepository
                .findByRecruiterEmail(email)
                .orElseGet(Company::new);

        company.setRecruiter(recruiter);
        company.setCompanyName(companyDetails.getCompanyName());
        company.setDescription(companyDetails.getDescription());
        company.setIndustry(companyDetails.getIndustry());
        company.setWebsite(companyDetails.getWebsite());
        company.setLocation(companyDetails.getLocation());
        company.setLogoUrl(companyDetails.getLogoUrl());

        return companyRepository.save(company);
    }
}
