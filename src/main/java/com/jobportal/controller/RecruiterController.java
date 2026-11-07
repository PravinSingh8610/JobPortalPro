
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.jobportal.dto.RecruiterRequest;
import com.jobportal.entity.Company;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;
import com.jobportal.service.CompanyService;
import com.jobportal.service.JobApplicationService;
import com.jobportal.service.RecruiterService;

import jakarta.validation.Valid;

@Controller
public class RecruiterController {

    private final RecruiterService recruiterService;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobApplicationService applicationService;
    private final CompanyService companyService;

    public RecruiterController(
            RecruiterService recruiterService,
            CompanyRepository companyRepository,
            JobRepository jobRepository,
            JobApplicationService applicationService,
            CompanyService companyService) {

        this.recruiterService = recruiterService;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationService = applicationService;
        this.companyService = companyService;
    }

    // 1. RECRUITER REGISTRATION PAGE
    @GetMapping("/recruiter/register")
    public String registerPage(Model model) {

        model.addAttribute(
                "recruiterRequest",
                new RecruiterRequest()
        );

        return "recruiter-register";
    }

    // 2. REGISTER NEW RECRUITER
    @PostMapping("/recruiter/register")
    public String registerRecruiter(
            @Valid @ModelAttribute("recruiterRequest")
            RecruiterRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "recruiter-register";
        }

        try {

            recruiterService.registerRecruiter(request);

        } catch (IllegalArgumentException ex) {

            model.addAttribute("error", ex.getMessage());
            return "recruiter-register";

        } catch (DataIntegrityViolationException ex) {

            model.addAttribute(
                    "error",
                    "Registration failed. Please check your details."
            );

            return "recruiter-register";
        }

        return "redirect:/login?registered=true";
    }

    // 3. RECRUITER DASHBOARD
    @GetMapping("/recruiter/dashboard")
    public String dashboard(
            Principal principal,
            Model model) {

        String recruiterEmail = principal.getName();

        // Check whether company profile exists
        Company company = companyRepository
                .findByRecruiterEmail(recruiterEmail)
                .orElse(null);

        // If profile does not exist, redirect to profile setup
        if (company == null) {
            return "redirect:/recruiter/company-profile";
        }

        // Fetch recruiter's jobs
        List<Job> jobs = jobRepository
                .findByCompanyIdOrderByCreatedAtDesc(
                        company.getId()
                );

        long totalJobs = jobs.size();

        long activeJobs = jobs.stream()
                .filter(job ->
                        "ACTIVE".equals(job.getStatus()))
                .count();

        long totalApplications = 0;
        long shortlistedCount = 0;

        // Count applications
        for (Job job : jobs) {

            List<JobApplication> applications =
                    applicationService.getRecruiterJobApplications(
                            job.getId(),
                            recruiterEmail
                    );

            totalApplications += applications.size();

            shortlistedCount += applications.stream()
                    .filter(application ->
                            "SHORTLISTED".equals(
                                    application.getStatus()
                            ))
                    .count();
        }

        model.addAttribute("company", company);
        model.addAttribute("totalJobs", totalJobs);
        model.addAttribute("activeJobs", activeJobs);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("shortlistedCount", shortlistedCount);

        return "recruiter-dashboard";
    }

    // 4. OPEN COMPANY PROFILE PAGE
    @GetMapping("/recruiter/company-profile")
    public String companyProfile(
            Principal principal,
            Model model) {

        Company company = companyService.getCompanyProfile(
                principal.getName()
        );

        model.addAttribute("company", company);

        return "company-profile";
    }

    // 5. UPDATE COMPANY PROFILE
    @PostMapping("/recruiter/company-profile")
    public String updateCompanyProfile(
            @ModelAttribute("company") Company company,
            Principal principal,
            Model model) {

        try {

            companyService.saveOrUpdateCompany(
                    principal.getName(),
                    company
            );

            return "redirect:/recruiter/company-profile?success";

        } catch (RuntimeException ex) {

            Company existingCompany =
                    companyService.getCompanyProfile(
                            principal.getName()
                    );

            model.addAttribute("company", existingCompany);
            model.addAttribute("error", ex.getMessage());

            return "company-profile";
        }
    }
}
