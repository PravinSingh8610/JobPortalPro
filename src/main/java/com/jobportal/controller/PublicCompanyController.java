
package com.jobportal.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.jobportal.entity.Company;
import com.jobportal.entity.Job;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;

@Controller
@RequestMapping("/companies")
public class PublicCompanyController {

    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;

    public PublicCompanyController(
            CompanyRepository companyRepository,
            JobRepository jobRepository) {

        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
    }

    // DISPLAY PUBLIC COMPANY PROFILE WITH ACTIVE JOBS
    @GetMapping("/{companyId}")
    public String showCompanyProfile(
            @PathVariable Long companyId,
            Model model) {

        // FIND COMPANY
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Company not found"
                    )
                );

        // FIND ACTIVE AND NON-EXPIRED JOBS OF THIS COMPANY
        List<Job> activeJobs = jobRepository
                .findByCompanyIdAndStatusAndApplicationDeadlineGreaterThanEqualOrderByCreatedAtDesc(
                        companyId,
                        "ACTIVE",
                        LocalDate.now()
                );

        // SEND DATA TO THYMELEAF
        model.addAttribute("company", company);
        model.addAttribute("activeJobs", activeJobs);

        return "public-company-profile";
    }
}
