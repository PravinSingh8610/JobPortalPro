
package com.jobportal.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class AdminPdfReportService {

    private final AdminReportService reportService;

    public AdminPdfReportService(AdminReportService reportService) {
        this.reportService = reportService;
    }

    // Overall platform report
    public byte[] generatePdfReport() {
        return generatePdfReportInternal(null, null);
    }

    // Date-wise platform report
    public byte[] generatePdfReport(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null
                || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                    "Invalid report date range");
        }

        return generatePdfReportInternal(startDate, endDate);
    }

    // Common PDF generation method
    private byte[] generatePdfReportInternal(
            LocalDate startDate,
            LocalDate endDate) {

        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            Document document = new Document();

            PdfWriter.getInstance(document, outputStream);

            document.open();

            Font titleFont = new Font(
                    Font.HELVETICA,
                    20,
                    Font.BOLD
            );

            Paragraph title = new Paragraph(
                    "JobPortalPro - Admin Analytics Report",
                    titleFont
            );

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            String generatedDate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern(
                            "dd-MM-yyyy HH:mm:ss"
                    ));

            document.add(new Paragraph(
                    "Generated On: " + generatedDate
            ));

            if (startDate != null && endDate != null) {
                document.add(new Paragraph(
                        "Selected Period: " + startDate
                        + " to " + endDate
                ));
            } else {
                document.add(new Paragraph(
                        "Report Type: Overall Platform Report"
                ));
            }

            document.add(new Paragraph(" "));

            // Overall platform analytics
            PdfPTable table = new PdfPTable(2);

            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            addHeader(table, "Metric");
            addHeader(table, "Count");

            addRow(table, "Total Candidates",
                    reportService.getTotalCandidates());

            addRow(table, "Total Recruiters",
                    reportService.getTotalRecruiters());

            addRow(table, "Total Jobs",
                    reportService.getTotalJobs());

            addRow(table, "Total Applications",
                    reportService.getTotalApplications());

            addRow(table, "Total Interviews",
                    reportService.getTotalInterviews());

            addRow(table, "Total Offers",
                    reportService.getTotalOffers());

            addRow(table, "Total Onboardings",
                    reportService.getTotalOnboardings());

            // Application analytics
            addRow(table, "Applied Applications",
                    reportService.getApplicationCountByStatus("APPLIED"));

            addRow(table, "Shortlisted Applications",
                    reportService.getApplicationCountByStatus("SHORTLISTED"));

            addRow(table, "Rejected Applications",
                    reportService.getApplicationCountByStatus("REJECTED"));

            // Interview analytics
            addRow(table, "Scheduled Interviews",
                    reportService.getInterviewCountByStatus("SCHEDULED"));

            addRow(table, "Completed Interviews",
                    reportService.getInterviewCountByStatus("COMPLETED"));

            addRow(table, "Cancelled Interviews",
                    reportService.getInterviewCountByStatus("CANCELLED"));

            // Interview results
            addRow(table, "Selected Results",
                    reportService.getInterviewCountByResult("SELECTED"));

            addRow(table, "Rejected Results",
                    reportService.getInterviewCountByResult("REJECTED"));

            addRow(table, "On Hold Results",
                    reportService.getInterviewCountByResult("ON_HOLD"));

            // Job offers
            addRow(table, "Pending Offers",
                    reportService.getOfferCountByStatus("PENDING"));

            addRow(table, "Accepted Offers",
                    reportService.getOfferCountByStatus("ACCEPTED"));

            addRow(table, "Rejected Offers",
                    reportService.getOfferCountByStatus("REJECTED"));

            addRow(table, "Expired Offers",
                    reportService.getOfferCountByStatus("EXPIRED"));

            // Onboarding analytics
            addRow(table, "Pending Onboardings",
                    reportService.getOnboardingCountByStatus("PENDING"));

            addRow(table, "Under Review Onboardings",
                    reportService.getOnboardingCountByStatus("UNDER_REVIEW"));

            addRow(table, "Approved Onboardings",
                    reportService.getOnboardingCountByStatus("APPROVED"));

            addRow(table, "Rejected Onboardings",
                    reportService.getOnboardingCountByStatus("REJECTED"));

            document.add(table);

            // Date-wise analytics
            if (startDate != null && endDate != null) {

                document.add(new Paragraph(" "));
                document.add(new Paragraph(
                        "Selected Period Analytics"
                ));

                PdfPTable dateTable = new PdfPTable(2);

                dateTable.setWidthPercentage(100);
                dateTable.setSpacingBefore(10);

                addHeader(dateTable, "Metric");
                addHeader(dateTable, "Count");

                addRow(dateTable, "Applications in Selected Period",
                        reportService.getApplicationsBetweenDates(
                                startDate, endDate));

                addRow(dateTable, "Interviews in Selected Period",
                        reportService.getInterviewsBetweenDates(
                                startDate, endDate));

                addRow(dateTable, "Offers in Selected Period",
                        reportService.getOffersBetweenDates(
                                startDate, endDate));

                addRow(dateTable, "Onboardings in Selected Period",
                        reportService.getOnboardingsBetweenDates(
                                startDate, endDate));

                document.add(dateTable);
            }

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Offer Acceptance Rate: "
                    + String.format("%.2f",
                            reportService.getOfferAcceptanceRate())
                    + "%"
            ));

            document.add(new Paragraph(
                    "Onboarding Approval Rate: "
                    + String.format("%.2f",
                            reportService.getOnboardingApprovalRate())
                    + "%"
            ));

            document.close();

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to generate Admin PDF Report",
                    e
            );
        }
    }

    // Table header
    private void addHeader(PdfPTable table, String text) {

        Font font = new Font(
                Font.HELVETICA,
                12,
                Font.BOLD
        );

        PdfPCell cell = new PdfPCell(
                new Phrase(text, font)
        );

        cell.setPadding(10);

        table.addCell(cell);
    }

    // Table row
    private void addRow(
            PdfPTable table,
            String metric,
            long count) {

        table.addCell(metric);
        table.addCell(String.valueOf(count));
    }
}
