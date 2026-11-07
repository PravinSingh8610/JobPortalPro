
package com.jobportal.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobApplicationService;
import com.jobportal.service.SavedJobService;

@Controller
@RequestMapping("/candidate/jobs")
public class CandidateJobController {

    private static final int PAGE_SIZE = 10;

    private static final Set<String> SORT_OPTIONS = Set.of(
            "newest",
            "oldest",
            "salaryHigh",
            "salaryLow",
            "deadline"
    );

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobApplicationService applicationService;
    private final SavedJobService savedJobService;

    public CandidateJobController(
            JobRepository jobRepository,
            UserRepository userRepository,
            JobApplicationService applicationService,
            SavedJobService savedJobService) {

        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.applicationService = applicationService;
        this.savedJobService = savedJobService;
    }

    // ==========================================
    // 1. JOB SEARCH, FILTER, SORT AND PAGINATION
    // ==========================================

    @GetMapping
    public String showAvailableJobs(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "") String location,
            @RequestParam(defaultValue = "") String employmentType,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            Principal principal,
            Model model) {

        int currentPage = Math.max(page, 0);

        String searchKeyword =
                keyword.trim().toLowerCase(Locale.ROOT);

        String searchLocation =
                location.trim().toLowerCase(Locale.ROOT);

        String searchEmploymentType =
                employmentType.trim().toUpperCase(Locale.ROOT);

        String selectedSort = SORT_OPTIONS.contains(sortBy)
                ? sortBy
                : "newest";

        Sort sort = getJobSort(selectedSort);

        Pageable pageable = PageRequest.of(
                currentPage,
                PAGE_SIZE,
                sort
        );

        LocalDate today = LocalDate.now();

        Page<Job> jobPage = jobRepository.searchActiveJobs(
                today,
                searchKeyword,
                searchLocation,
                searchEmploymentType,
                pageable
        );

        // HANDLE OUT-OF-RANGE PAGES
        if (jobPage.getTotalPages() > 0
                && currentPage >= jobPage.getTotalPages()) {

            currentPage = jobPage.getTotalPages() - 1;

            pageable = PageRequest.of(
                    currentPage,
                    PAGE_SIZE,
                    sort
            );

            jobPage = jobRepository.searchActiveJobs(
                    today,
                    searchKeyword,
                    searchLocation,
                    searchEmploymentType,
                    pageable
            );
        }

        model.addAttribute("jobs", jobPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        model.addAttribute("employmentType", employmentType);
        model.addAttribute("sortBy", selectedSort);
        model.addAttribute("totalJobs", jobPage.getTotalElements());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", jobPage.getTotalPages());
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("hasPrevious", jobPage.hasPrevious());
        model.addAttribute("hasNext", jobPage.hasNext());

        // ======================================
        // 2. IDENTIFY CANDIDATE'S SAVED JOBS
        // ======================================

        Set<Long> savedJobIds = savedJobService
                .getSavedJobs(principal.getName())
                .stream()
                .map(savedJob -> savedJob.getJob().getId())
                .collect(Collectors.toSet());

        model.addAttribute("savedJobIds", savedJobIds);

        // ======================================
        // 3. SMART JOB RECOMMENDATIONS
        // ======================================

        User candidate = userRepository
                .findByEmail(principal.getName())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Candidate not found"
                        )
                );

        String candidateSkills = candidate.getSkills() == null
                ? ""
                : candidate.getSkills();

        String candidateLocation = candidate.getLocation() == null
                ? ""
                : candidate.getLocation().trim();

        List<Job> allActiveJobs =
                jobRepository.findActiveJobsForRecommendations(today);

        List<Map<String, Object>> recommendedJobs = new ArrayList<>();

        for (Job job : allActiveJobs) {

            int matchPercentage = calculateMatchPercentage(
                    candidateSkills,
                    candidateLocation,
                    job
            );

            if (matchPercentage > 0) {
                recommendedJobs.add(
                        Map.of(
                                "job", job,
                                "matchPercentage", matchPercentage
                        )
                );
            }
        }

        // FIXED: Explicit Map<String, Object> type
        recommendedJobs.sort(
                Comparator.comparingInt(
                        (Map<String, Object> recommendation) ->
                                (Integer) recommendation.get("matchPercentage")
                ).reversed()
        );

        model.addAttribute("recommendedJobs", recommendedJobs);

        return "candidate-jobs";
    }

    // ==========================================
    // 4. CALCULATE JOB MATCH PERCENTAGE
    // ==========================================

    private int calculateMatchPercentage(
            String candidateSkills,
            String candidateLocation,
            Job job) {

        int skillScore = 0;
        int locationScore = 0;

        // SKILL MATCHING

        Set<String> skills = java.util.Arrays.stream(
                candidateSkills.toLowerCase(Locale.ROOT)
                        .split("\\s*,\\s*")
        )
        .map(String::trim)
        .filter(skill -> !skill.isBlank())
        .collect(Collectors.toSet());

        String jobSkills = job.getSkills() == null
                ? ""
                : job.getSkills().toLowerCase(Locale.ROOT);

        int totalSkills = skills.size();
        int matchedSkills = 0;

        for (String skill : skills) {

            if (jobSkills.contains(skill)) {
                matchedSkills++;
            }
        }

        if (totalSkills > 0) {
            skillScore = (matchedSkills * 80) / totalSkills;
        }

        // LOCATION MATCHING

        if (candidateLocation != null
                && !candidateLocation.isBlank()
                && job.getLocation() != null
                && job.getLocation().toLowerCase(Locale.ROOT)
                        .contains(candidateLocation.toLowerCase(Locale.ROOT))) {

            locationScore = 20;
        }

        return skillScore + locationScore;
    }

    // ==========================================
    // 5. SORTING OPTIONS
    // ==========================================

    private Sort getJobSort(String sortBy) {

        Sort primarySort;

        switch (sortBy) {

            case "oldest":
                primarySort = Sort.by(
                        Sort.Direction.ASC,
                        "createdAt"
                );
                break;

            case "salaryHigh":
                primarySort = Sort.by(
                        Sort.Direction.DESC,
                        "maxSalary"
                );
                break;

            case "salaryLow":
                primarySort = Sort.by(
                        Sort.Direction.ASC,
                        "minSalary"
                );
                break;

            case "deadline":
                primarySort = Sort.by(
                        Sort.Direction.ASC,
                        "applicationDeadline"
                );
                break;

            case "newest":
            default:
                primarySort = Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                );
                break;
        }

        return primarySort.and(
                Sort.by(Sort.Direction.DESC, "id")
        );
    }

    // ==========================================
    // 6. DISPLAY COMPLETE JOB DETAILS
    // ==========================================

    @GetMapping("/{jobId}")
    public String showJobDetails(
            @PathVariable Long jobId,
            Principal principal,
            Model model) {

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Job not found"
                        )
                );

        if (!"ACTIVE".equals(job.getStatus())
                || job.getApplicationDeadline() == null
                || job.getApplicationDeadline()
                        .isBefore(LocalDate.now())) {

            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This job is no longer available"
            );
        }

        model.addAttribute("job", job);

        boolean isJobSaved = savedJobService.isJobSaved(
                jobId,
                principal.getName()
        );

        model.addAttribute("isJobSaved", isJobSaved);

        return "candidate-job-details";
    }

    // ==========================================
    // 7. CANDIDATE APPLY FOR JOB
    // ==========================================

    @PostMapping("/{jobId}/apply")
    public String applyForJob(
            @PathVariable Long jobId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {

            applicationService.applyForJob(
                    jobId,
                    principal.getName()
            );

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Your application has been submitted successfully!"
            );

        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    ex.getMessage()
            );
        }

        return "redirect:/candidate/jobs/" + jobId;
    }
}
