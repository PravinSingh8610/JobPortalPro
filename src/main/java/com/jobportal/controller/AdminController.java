
package com.jobportal.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.entity.Company;
import com.jobportal.entity.Interview;
import com.jobportal.entity.Job;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.JobOffer;

import com.jobportal.service.AdminApplicationService;
import com.jobportal.service.AdminCandidateService;
import com.jobportal.service.AdminDashboardService;
import com.jobportal.service.AdminInterviewService;
import com.jobportal.service.AdminJobService;
import com.jobportal.service.AdminOfferService;
import com.jobportal.service.AdminOnboardingService;
import com.jobportal.service.AdminRecruiterService;
import com.jobportal.service.AdminReportService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // ========================================
    // SERVICE DEPENDENCIES
    // ========================================

    private final AdminDashboardService adminDashboardService;
    private final AdminCandidateService adminCandidateService;
    private final AdminRecruiterService adminRecruiterService;
    private final AdminJobService adminJobService;
    private final AdminApplicationService adminApplicationService;
    private final AdminInterviewService adminInterviewService;
    private final AdminOfferService adminOfferService;
    private final AdminOnboardingService adminOnboardingService;
    private final AdminReportService adminReportService;

    // ========================================
    // CONSTRUCTOR INJECTION
    // ========================================

    public AdminController(
            AdminDashboardService adminDashboardService,
            AdminCandidateService adminCandidateService,
            AdminRecruiterService adminRecruiterService,
            AdminJobService adminJobService,
            AdminApplicationService adminApplicationService,
            AdminInterviewService adminInterviewService,
            AdminOfferService adminOfferService,
            AdminOnboardingService adminOnboardingService,
            AdminReportService adminReportService) {

        this.adminDashboardService = adminDashboardService;
        this.adminCandidateService = adminCandidateService;
        this.adminRecruiterService = adminRecruiterService;
        this.adminJobService = adminJobService;
        this.adminApplicationService = adminApplicationService;
        this.adminInterviewService = adminInterviewService;
        this.adminOfferService = adminOfferService;
        this.adminOnboardingService = adminOnboardingService;
        this.adminReportService = adminReportService;
    }

    // ========================================
    // 1. ADMIN DASHBOARD
    // URL: /admin/dashboard
    // ========================================

    @GetMapping("/dashboard")
    public String adminDashboard(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("adminEmail", principal.getName());

        model.addAttribute("totalCandidates",
                adminDashboardService.getTotalCandidates());

        model.addAttribute("totalRecruiters",
                adminDashboardService.getTotalRecruiters());

        model.addAttribute("totalJobs",
                adminDashboardService.getTotalJobs());

        model.addAttribute("totalApplications",
                adminDashboardService.getTotalApplications());

        return "admin-dashboard";
    }

    // ========================================
    // 2. ADMIN CANDIDATE MANAGEMENT
    // URL: /admin/candidates
    // ========================================

    @GetMapping("/candidates")
    public String manageCandidates(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        model.addAttribute("adminEmail", principal.getName());

        model.addAttribute("candidates",
                adminCandidateService.getAllCandidates());

        return "admin-candidates";
    }

    // ========================================
    // 3. ADMIN RECRUITER MANAGEMENT
    // URL: /admin/recruiters
    // ========================================

    @GetMapping("/recruiters")
    public String manageRecruiters(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<Company> recruiters =
                adminRecruiterService.getAllRecruiters();

        model.addAttribute("adminEmail", principal.getName());
        model.addAttribute("recruiters", recruiters);

        return "admin-recruiters";
    }

    // ========================================
    // 3A. ADMIN RECRUITER VERIFICATION
    // URL: POST /admin/recruiters/{id}/verification
    // ========================================

    @PostMapping("/recruiters/{id}/verification")
    public String updateRecruiterVerification(
            Principal principal,
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String remarks,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {

            adminRecruiterService.updateVerificationStatus(
                    id, status, remarks);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Recruiter verification status updated successfully!");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage());
        }

        return "redirect:/admin/recruiters";
    }

    // ========================================
    // 4. ADMIN JOB MANAGEMENT
    // URL: /admin/jobs
    // ========================================

    @GetMapping("/jobs")
    public String manageJobs(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<Job> jobs = adminJobService.getAllJobs();

        Map<Long, Long> applicationCounts =
                new LinkedHashMap<>();

        for (Job job : jobs) {

            long count =
                    adminJobService.getApplicationCount(job.getId());

            applicationCounts.put(job.getId(), count);
        }

        model.addAttribute("adminEmail", principal.getName());
        model.addAttribute("jobs", jobs);
        model.addAttribute("applicationCounts", applicationCounts);

        return "admin-jobs";
    }

    // ========================================
    // 5. ADMIN APPLICATION MANAGEMENT
    // URL: /admin/applications
    // ========================================

    @GetMapping("/applications")
    public String manageApplications(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<JobApplication> applications =
                adminApplicationService.getAllApplications();

        model.addAttribute("adminEmail", principal.getName());
        model.addAttribute("applications", applications);
        model.addAttribute("totalApplications", applications.size());

        return "admin-applications";
    }

    // ========================================
    // 6. ADMIN INTERVIEW MANAGEMENT
    // URL: /admin/interviews
    // ========================================

    @GetMapping("/interviews")
    public String manageInterviews(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<Interview> interviews =
                adminInterviewService.getAllInterviews();

        model.addAttribute("adminEmail", principal.getName());
        model.addAttribute("interviews", interviews);
        model.addAttribute("totalInterviews", interviews.size());

        return "admin-interviews";
    }

    // ========================================
    // 7. ADMIN JOB OFFER MANAGEMENT
    // URL: /admin/offers
    // ========================================

    @GetMapping("/offers")
    public String manageOffers(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<JobOffer> offers =
                adminOfferService.getAllOffers();

        model.addAttribute("adminEmail", principal.getName());
        model.addAttribute("offers", offers);
        model.addAttribute("totalOffers", offers.size());

        model.addAttribute("pendingOffers",
                offers.stream()
                        .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()))
                        .count());

        model.addAttribute("acceptedOffers",
                offers.stream()
                        .filter(o -> "ACCEPTED".equalsIgnoreCase(o.getStatus()))
                        .count());

        model.addAttribute("rejectedOffers",
                offers.stream()
                        .filter(o -> "REJECTED".equalsIgnoreCase(o.getStatus()))
                        .count());

        model.addAttribute("expiredOffers",
                offers.stream()
                        .filter(o -> "EXPIRED".equalsIgnoreCase(o.getStatus()))
                        .count());

        return "admin-offers";
    }

    // ========================================
    // 8. ADMIN ONBOARDING MANAGEMENT
    // URL: /admin/onboarding
    // ========================================

    @GetMapping("/onboarding")
    public String manageOnboarding(
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        List<CandidateOnboarding> onboardings =
                adminOnboardingService.getAllOnboardings();

        model.addAttribute("adminEmail", principal.getName());
        model.addAttribute("onboardings", onboardings);
        model.addAttribute("totalOnboardings", onboardings.size());

        model.addAttribute("pendingOnboardings",
                onboardings.stream()
                        .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()))
                        .count());

        model.addAttribute("underReviewOnboardings",
                onboardings.stream()
                        .filter(o -> "UNDER_REVIEW".equalsIgnoreCase(o.getStatus()))
                        .count());

        model.addAttribute("approvedOnboardings",
                onboardings.stream()
                        .filter(o -> "APPROVED".equalsIgnoreCase(o.getStatus()))
                        .count());

        model.addAttribute("rejectedOnboardings",
                onboardings.stream()
                        .filter(o -> "REJECTED".equalsIgnoreCase(o.getStatus()))
                        .count());

        return "admin-onboarding";
    }

    // ========================================
    // 9. ADMIN REPORTS & ANALYTICS
    // URL: /admin/reports
    // ========================================

    @GetMapping("/reports")
    public String adminReports(
            Principal principal,
            Model model,

            @RequestParam(required = false)
            String filter,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        if (principal == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();

        if (filter == null || filter.isBlank()) {
            filter = "THIS_MONTH";
        }

        // ========================================
        // DATE FILTER LOGIC
        // ========================================

        switch (filter) {

            case "TODAY":
                startDate = today;
                endDate = today;
                break;

            case "YESTERDAY":
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;

            case "THIS_WEEK":
                startDate = today.with(DayOfWeek.MONDAY);
                endDate = today;
                break;

            case "THIS_MONTH":
                startDate = today.withDayOfMonth(1);
                endDate = today;
                break;

            case "CUSTOM":

                if (startDate == null || endDate == null) {

                    model.addAttribute("dateError",
                            "Please select both start date and end date.");

                    filter = "THIS_MONTH";
                    startDate = today.withDayOfMonth(1);
                    endDate = today;
                }
                break;

            default:
                filter = "THIS_MONTH";
                startDate = today.withDayOfMonth(1);
                endDate = today;
                break;
        }

        // ========================================
        // DATE VALIDATION
        // ========================================

        if (startDate == null || endDate == null
                || startDate.isAfter(endDate)) {

            model.addAttribute("dateError",
                    "Start date cannot be after end date.");

            filter = "THIS_MONTH";
            startDate = today.withDayOfMonth(1);
            endDate = today;
        }

        // ========================================
        // ADMIN DETAILS
        // ========================================

        model.addAttribute("adminEmail", principal.getName());

        // ========================================
        // PLATFORM OVERVIEW
        // ========================================

        model.addAttribute("totalCandidates",
                adminReportService.getTotalCandidates());

        model.addAttribute("totalRecruiters",
                adminReportService.getTotalRecruiters());

        model.addAttribute("totalJobs",
                adminReportService.getTotalJobs());

        model.addAttribute("totalApplications",
                adminReportService.getTotalApplications());

        model.addAttribute("totalInterviews",
                adminReportService.getTotalInterviews());

        model.addAttribute("totalOffers",
                adminReportService.getTotalOffers());

        model.addAttribute("totalOnboardings",
                adminReportService.getTotalOnboardings());

        // ========================================
        // APPLICATION ANALYTICS
        // ========================================

        model.addAttribute("appliedApplications",
                adminReportService.getApplicationCountByStatus("APPLIED"));

        model.addAttribute("shortlistedApplications",
                adminReportService.getApplicationCountByStatus("SHORTLISTED"));

        model.addAttribute("rejectedApplications",
                adminReportService.getApplicationCountByStatus("REJECTED"));

        // ========================================
        // INTERVIEW ANALYTICS
        // ========================================

        model.addAttribute("scheduledInterviews",
                adminReportService.getInterviewCountByStatus("SCHEDULED"));

        model.addAttribute("completedInterviews",
                adminReportService.getInterviewCountByStatus("COMPLETED"));

        model.addAttribute("cancelledInterviews",
                adminReportService.getInterviewCountByStatus("CANCELLED"));

        model.addAttribute("selectedResults",
                adminReportService.getInterviewCountByResult("SELECTED"));

        model.addAttribute("rejectedResults",
                adminReportService.getInterviewCountByResult("REJECTED"));

        model.addAttribute("onHoldResults",
                adminReportService.getInterviewCountByResult("ON_HOLD"));

        // ========================================
        // JOB OFFER ANALYTICS
        // ========================================

        model.addAttribute("pendingOffers",
                adminReportService.getOfferCountByStatus("PENDING"));

        model.addAttribute("acceptedOffers",
                adminReportService.getOfferCountByStatus("ACCEPTED"));

        model.addAttribute("rejectedOffers",
                adminReportService.getOfferCountByStatus("REJECTED"));

        model.addAttribute("expiredOffers",
                adminReportService.getOfferCountByStatus("EXPIRED"));

        // ========================================
        // ONBOARDING ANALYTICS
        // ========================================

        model.addAttribute("pendingOnboardings",
                adminReportService.getOnboardingCountByStatus("PENDING"));

        model.addAttribute("underReviewOnboardings",
                adminReportService.getOnboardingCountByStatus("UNDER_REVIEW"));

        model.addAttribute("approvedOnboardings",
                adminReportService.getOnboardingCountByStatus("APPROVED"));

        model.addAttribute("rejectedOnboardings",
                adminReportService.getOnboardingCountByStatus("REJECTED"));

        // ========================================
        // PERFORMANCE METRICS
        // ========================================

        model.addAttribute("offerAcceptanceRate",
                adminReportService.getOfferAcceptanceRate());

        model.addAttribute("onboardingApprovalRate",
                adminReportService.getOnboardingApprovalRate());

        // ========================================
        // DATE-WISE REPORT DATA
        // ========================================

        model.addAttribute("filter", filter);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        model.addAttribute("dateApplications",
                adminReportService.getApplicationsBetweenDates(
                        startDate, endDate));

        model.addAttribute("dateInterviews",
                adminReportService.getInterviewsBetweenDates(
                        startDate, endDate));

        model.addAttribute("dateOffers",
                adminReportService.getOffersBetweenDates(
                        startDate, endDate));

        model.addAttribute("dateOnboardings",
                adminReportService.getOnboardingsBetweenDates(
                        startDate, endDate));

        return "admin-reports";
    }
}
