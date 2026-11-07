
package com.jobportal.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.dto.JobRequest;
import com.jobportal.entity.Company;
import com.jobportal.entity.Job;
import com.jobportal.repository.CompanyRepository;
import com.jobportal.repository.JobRepository;

@Service
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public JobService(
            JobRepository jobRepository,
            CompanyRepository companyRepository) {

        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
    }

    // CREATE NEW JOB
    @Transactional
    public Job createJob(JobRequest request, String recruiterEmail) {

        Company company = getRecruiterCompany(recruiterEmail);

        validateSalary(request);

        Job job = new Job();

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setSkills(request.getSkills().trim());
        job.setLocation(request.getLocation().trim());

        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());

        job.setExperienceYears(request.getExperienceYears());
        job.setEmploymentType(request.getEmploymentType());

        job.setApplicationDeadline(request.getApplicationDeadline());

        job.setStatus("ACTIVE");
        job.setCompany(company);

        return jobRepository.save(job);
    }

    // GET ALL JOBS OF LOGGED-IN RECRUITER
    @Transactional(readOnly = true)
    public List<Job> getRecruiterJobs(String recruiterEmail) {

        Company company = getRecruiterCompany(recruiterEmail);

        return jobRepository
                .findByCompanyIdOrderByCreatedAtDesc(company.getId());
    }

    // COUNT JOBS OF LOGGED-IN RECRUITER
    @Transactional(readOnly = true)
    public long countRecruiterJobs(String recruiterEmail) {

        Company company = getRecruiterCompany(recruiterEmail);

        return jobRepository.countByCompanyId(company.getId());
    }

    // GET ONE JOB AND VERIFY OWNERSHIP
    @Transactional(readOnly = true)
    public Job getRecruiterJob(Long jobId, String recruiterEmail) {

        Company company = getRecruiterCompany(recruiterEmail);

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Job not found"));

        if (!job.getCompany().getId().equals(company.getId())) {

            throw new IllegalArgumentException(
                    "You are not allowed to access this job");
        }

        return job;
    }

    // UPDATE EXISTING JOB
    @Transactional
    public Job updateJob(
            Long jobId,
            JobRequest request,
            String recruiterEmail) {

        Job job = getRecruiterJob(jobId, recruiterEmail);

        validateSalary(request);

        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setSkills(request.getSkills().trim());
        job.setLocation(request.getLocation().trim());

        job.setMinSalary(request.getMinSalary());
        job.setMaxSalary(request.getMaxSalary());

        job.setExperienceYears(request.getExperienceYears());
        job.setEmploymentType(request.getEmploymentType());

        job.setApplicationDeadline(request.getApplicationDeadline());

        // Keep original company, status and creation date
        return jobRepository.save(job);
    }

    // CLOSE AN ACTIVE JOB
    @Transactional
    public void closeJob(Long jobId, String recruiterEmail) {

        Job job = getRecruiterJob(jobId, recruiterEmail);

        if (!"ACTIVE".equals(job.getStatus())) {

            throw new IllegalArgumentException(
                    "Only active jobs can be closed");
        }

        job.setStatus("CLOSED");

        jobRepository.save(job);
    }

    // REOPEN A CLOSED JOB
    @Transactional
    public void reopenJob(Long jobId, String recruiterEmail) {

        Job job = getRecruiterJob(jobId, recruiterEmail);

        if (!"CLOSED".equals(job.getStatus())) {

            throw new IllegalArgumentException(
                    "Only closed jobs can be reopened");
        }

        // Deadline must not be expired
        if (job.getApplicationDeadline() == null
                || job.getApplicationDeadline().isBefore(LocalDate.now())) {

            throw new IllegalArgumentException(
                    "Please edit the application deadline before reopening this job");
        }

        job.setStatus("ACTIVE");

        jobRepository.save(job);
    }

    // GET LOGGED-IN RECRUITER'S COMPANY
    private Company getRecruiterCompany(String recruiterEmail) {

        return companyRepository
                .findByRecruiterEmail(recruiterEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recruiter company not found"));
    }

    // SALARY VALIDATION
    private void validateSalary(JobRequest request) {

        if (request.getMinSalary() == null
                || request.getMaxSalary() == null) {

            throw new IllegalArgumentException(
                    "Minimum and maximum salary are required");
        }

        if (request.getMinSalary()
                .compareTo(request.getMaxSalary()) > 0) {

            throw new IllegalArgumentException(
                    "Minimum salary cannot exceed maximum salary");
        }
    }
}
