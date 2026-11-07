
package com.jobportal.controller;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobportal.service.AdminPdfReportService;
import com.jobportal.service.AdminReportService;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportExportController {

    private final AdminReportService adminReportService;
    private final AdminPdfReportService adminPdfReportService;

    public AdminReportExportController(
            AdminReportService adminReportService,
            AdminPdfReportService adminPdfReportService) {

        this.adminReportService = adminReportService;
        this.adminPdfReportService = adminPdfReportService;
    }

    // =====================================
    // CSV EXPORT
    // =====================================

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportReportsCsv(
            Principal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if ((startDate == null) != (endDate == null)
                || (startDate != null && startDate.isAfter(endDate))) {
            return ResponseEntity.badRequest().build();
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Category,Metric,Count\n");

        // PLATFORM OVERVIEW
        addRow(csv, "Platform", "Total Candidates",
                adminReportService.getTotalCandidates());

        addRow(csv, "Platform", "Total Recruiters",
                adminReportService.getTotalRecruiters());

        addRow(csv, "Platform", "Total Jobs",
                adminReportService.getTotalJobs());

        addRow(csv, "Platform", "Total Applications",
                adminReportService.getTotalApplications());

        addRow(csv, "Platform", "Total Interviews",
                adminReportService.getTotalInterviews());

        addRow(csv, "Platform", "Total Offers",
                adminReportService.getTotalOffers());

        addRow(csv, "Platform", "Total Onboardings",
                adminReportService.getTotalOnboardings());

        // APPLICATION ANALYTICS
        addRow(csv, "Applications", "Applied",
                adminReportService.getApplicationCountByStatus("APPLIED"));

        addRow(csv, "Applications", "Shortlisted",
                adminReportService.getApplicationCountByStatus("SHORTLISTED"));

        addRow(csv, "Applications", "Rejected",
                adminReportService.getApplicationCountByStatus("REJECTED"));

        // INTERVIEW ANALYTICS
        addRow(csv, "Interviews", "Scheduled",
                adminReportService.getInterviewCountByStatus("SCHEDULED"));

        addRow(csv, "Interviews", "Completed",
                adminReportService.getInterviewCountByStatus("COMPLETED"));

        addRow(csv, "Interviews", "Cancelled",
                adminReportService.getInterviewCountByStatus("CANCELLED"));

        // INTERVIEW RESULTS
        addRow(csv, "Results", "Selected",
                adminReportService.getInterviewCountByResult("SELECTED"));

        addRow(csv, "Results", "Rejected",
                adminReportService.getInterviewCountByResult("REJECTED"));

        addRow(csv, "Results", "On Hold",
                adminReportService.getInterviewCountByResult("ON_HOLD"));

        // JOB OFFERS
        addRow(csv, "Offers", "Pending",
                adminReportService.getOfferCountByStatus("PENDING"));

        addRow(csv, "Offers", "Accepted",
                adminReportService.getOfferCountByStatus("ACCEPTED"));

        addRow(csv, "Offers", "Rejected",
                adminReportService.getOfferCountByStatus("REJECTED"));

        addRow(csv, "Offers", "Expired",
                adminReportService.getOfferCountByStatus("EXPIRED"));

        // ONBOARDING
        addRow(csv, "Onboarding", "Pending",
                adminReportService.getOnboardingCountByStatus("PENDING"));

        addRow(csv, "Onboarding", "Under Review",
                adminReportService.getOnboardingCountByStatus("UNDER_REVIEW"));

        addRow(csv, "Onboarding", "Approved",
                adminReportService.getOnboardingCountByStatus("APPROVED"));

        addRow(csv, "Onboarding", "Rejected",
                adminReportService.getOnboardingCountByStatus("REJECTED"));

        // SELECTED DATE RANGE ANALYTICS
        if (startDate != null && endDate != null) {

            csv.append("\nDate Range,Start Date,")
                    .append(startDate).append("\n");

            csv.append("Date Range,End Date,")
                    .append(endDate).append("\n");

            addRow(csv, "Selected Period", "Applications",
                    adminReportService.getApplicationsBetweenDates(
                            startDate, endDate));

            addRow(csv, "Selected Period", "Interviews",
                    adminReportService.getInterviewsBetweenDates(
                            startDate, endDate));

            addRow(csv, "Selected Period", "Offers",
                    adminReportService.getOffersBetweenDates(
                            startDate, endDate));

            addRow(csv, "Selected Period", "Onboardings",
                    adminReportService.getOnboardingsBetweenDates(
                            startDate, endDate));
        }

        byte[] csvBytes = csv.toString()
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"jobportal-admin-report.csv\""
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    // =====================================
    // PDF EXPORT
    // =====================================

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportReportsPdf(
            Principal principal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if ((startDate == null) != (endDate == null)
                || (startDate != null && startDate.isAfter(endDate))) {
            return ResponseEntity.badRequest().build();
        }

        byte[] pdfBytes;

        if (startDate != null && endDate != null) {

            pdfBytes = adminPdfReportService.generatePdfReport(
                    startDate, endDate);

        } else {

            pdfBytes = adminPdfReportService.generatePdfReport();
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"jobportal-admin-report.pdf\""
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // =====================================
    // CSV HELPER METHOD
    // =====================================

    private void addRow(
            StringBuilder csv,
            String category,
            String metric,
            long count) {

        csv.append(category)
                .append(",")
                .append(metric)
                .append(",")
                .append(count)
                .append("\n");
    }
}
