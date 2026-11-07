
package com.jobportal.controller;

import java.security.Principal;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.dto.JobRequest;
import com.jobportal.entity.Job;
import com.jobportal.service.JobService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/recruiter/jobs")
public class JobController {

    private static final Set<String> EMPLOYMENT_TYPES =
            Set.of(
                "FULL_TIME",
                "PART_TIME",
                "INTERNSHIP",
                "CONTRACT"
            );

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    // DISPLAY NEW JOB FORM
    @GetMapping("/new")
    public String showJobForm(Model model) {

        model.addAttribute("jobRequest", new JobRequest());

        return "post-job";
    }

    // SAVE NEW JOB
    @PostMapping("/new")
    public String submitJob(
            @Valid @ModelAttribute("jobRequest") JobRequest request,
            BindingResult result,
            Model model,
            Principal principal) {

        validateJobRequest(request, result);

        if (result.hasErrors()) {
            return "post-job";
        }

        try {

            jobService.createJob(
                    request,
                    principal.getName()
            );

            return "redirect:/recruiter/dashboard?jobPosted";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("error", ex.getMessage());

            return "post-job";
        }
    }

    // DISPLAY ALL JOBS OF LOGGED-IN RECRUITER
    @GetMapping
    public String manageJobs(
            Principal principal,
            Model model) {

        model.addAttribute(
                "jobs",
                jobService.getRecruiterJobs(principal.getName())
        );

        return "manage-jobs";
    }

    // DISPLAY EDIT JOB FORM
    @GetMapping("/{jobId}/edit")
    public String showEditJobForm(
            @PathVariable Long jobId,
            Principal principal,
            Model model) {

        Job job = jobService.getRecruiterJob(
                jobId,
                principal.getName()
        );

        JobRequest request = new JobRequest();

        request.setTitle(job.getTitle());
        request.setDescription(job.getDescription());
        request.setSkills(job.getSkills());
        request.setLocation(job.getLocation());

        request.setMinSalary(job.getMinSalary());
        request.setMaxSalary(job.getMaxSalary());

        request.setExperienceYears(job.getExperienceYears());
        request.setEmploymentType(job.getEmploymentType());

        request.setApplicationDeadline(
                job.getApplicationDeadline()
        );

        model.addAttribute("jobRequest", request);
        model.addAttribute("jobId", jobId);

        return "edit-job";
    }

    // UPDATE EXISTING JOB
    @PostMapping("/{jobId}/edit")
    public String updateJob(
            @PathVariable Long jobId,
            @Valid @ModelAttribute("jobRequest") JobRequest request,
            BindingResult result,
            Model model,
            Principal principal) {

        // Verify job ownership
        jobService.getRecruiterJob(
                jobId,
                principal.getName()
        );

        validateJobRequest(request, result);

        if (result.hasErrors()) {

            model.addAttribute("jobId", jobId);

            return "edit-job";
        }

        try {

            jobService.updateJob(
                    jobId,
                    request,
                    principal.getName()
            );

            return "redirect:/recruiter/jobs?updated";

        } catch (IllegalArgumentException ex) {

            model.addAttribute("jobId", jobId);
            model.addAttribute("error", ex.getMessage());

            return "edit-job";
        }
    }

    // CLOSE ACTIVE JOB
    @PostMapping("/{jobId}/close")
    public String closeJob(
            @PathVariable Long jobId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            jobService.closeJob(
                    jobId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Job closed successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs";
    }

    // REOPEN CLOSED JOB
    @PostMapping("/{jobId}/reopen")
    public String reopenJob(
            @PathVariable Long jobId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            jobService.reopenJob(
                    jobId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Job reopened successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/recruiter/jobs";
    }

    // COMMON VALIDATION FOR CREATE AND UPDATE
    private void validateJobRequest(
            JobRequest request,
            BindingResult result) {

        // Employment type validation
        if (request.getEmploymentType() == null
                || !EMPLOYMENT_TYPES.contains(
                        request.getEmploymentType())) {

            result.rejectValue(
                    "employmentType",
                    "invalid",
                    "Please select a valid employment type"
            );
        }

        // Salary range validation
        if (request.getMinSalary() != null
                && request.getMaxSalary() != null
                && request.getMinSalary()
                    .compareTo(request.getMaxSalary()) > 0) {

            result.rejectValue(
                    "maxSalary",
                    "invalid",
                    "Maximum salary must be greater than minimum salary"
            );
        }
    }
}
