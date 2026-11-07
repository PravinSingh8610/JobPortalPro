
package com.jobportal.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobportal.service.AdminDateWiseReportService;

@Controller
public class AdminDateWiseReportController {

    private final AdminDateWiseReportService dateWiseReportService;

    // ==========================================
    // CONSTRUCTOR INJECTION
    // ==========================================

    public AdminDateWiseReportController(
            AdminDateWiseReportService dateWiseReportService) {

        this.dateWiseReportService = dateWiseReportService;
    }

    // ==========================================
    // ADMIN DATE-WISE REPORTS
    // ==========================================

    @GetMapping("/admin/reports/date-wise")
    public String showDateWiseReport(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            Model model) {

        // ======================================
        // PRESERVE SELECTED DATES
        // ======================================

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        // ======================================
        // DEFAULT COUNTS
        // ======================================

        model.addAttribute("totalFilteredApplications", 0L);
        model.addAttribute("totalFilteredInterviews", 0L);
        model.addAttribute("totalFilteredOffers", 0L);

        // NEW: ONBOARDINGS
        model.addAttribute("totalFilteredOnboardings", 0L);

        model.addAttribute("filterApplied", false);

        // ======================================
        // DATE FILTER VALIDATION
        // ======================================

        if (fromDate != null && toDate != null) {

            if (fromDate.isAfter(toDate)) {

                model.addAttribute(
                        "errorMessage",
                        "From Date cannot be after To Date."
                );

            } else {

                // ==================================
                // 1. APPLICATIONS
                // ==================================

                long totalApplications =
                        dateWiseReportService
                                .getApplicationsBetweenDates(
                                        fromDate,
                                        toDate
                                );

                // ==================================
                // 2. INTERVIEWS
                // ==================================

                long totalInterviews =
                        dateWiseReportService
                                .getInterviewsBetweenDates(
                                        fromDate,
                                        toDate
                                );

                // ==================================
                // 3. JOB OFFERS
                // ==================================

                long totalOffers =
                        dateWiseReportService
                                .getOffersBetweenDates(
                                        fromDate,
                                        toDate
                                );

                // ==================================
                // 4. NEW: ONBOARDINGS
                // ==================================

                long totalOnboardings =
                        dateWiseReportService
                                .getOnboardingsBetweenDates(
                                        fromDate,
                                        toDate
                                );

                // ==================================
                // SEND RESULTS TO THYMELEAF
                // ==================================

                model.addAttribute(
                        "totalFilteredApplications",
                        totalApplications
                );

                model.addAttribute(
                        "totalFilteredInterviews",
                        totalInterviews
                );

                model.addAttribute(
                        "totalFilteredOffers",
                        totalOffers
                );

                model.addAttribute(
                        "totalFilteredOnboardings",
                        totalOnboardings
                );

                model.addAttribute("filterApplied", true);
            }

        } else if (fromDate != null || toDate != null) {

            model.addAttribute(
                    "errorMessage",
                    "Please select both From Date and To Date."
            );
        }

        return "admin-date-wise-reports";
    }
}
