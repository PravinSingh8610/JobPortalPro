
package com.jobportal.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.repository.JobApplicationRepository;
import com.jobportal.repository.InterviewRepository;
import com.jobportal.repository.JobOfferRepository;
import com.jobportal.repository.CandidateOnboardingRepository;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Service
@Transactional(readOnly = true)
public class AdminReportExportService {

    private final JobApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;
    private final JobOfferRepository offerRepository;
    private final CandidateOnboardingRepository onboardingRepository;

    public AdminReportExportService(
            JobApplicationRepository applicationRepository,
            InterviewRepository interviewRepository,
            JobOfferRepository offerRepository,
            CandidateOnboardingRepository onboardingRepository) {

        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.offerRepository = offerRepository;
        this.onboardingRepository = onboardingRepository;
    }

    private long[] getCounts(LocalDate startDate, LocalDate endDate) {

        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Invalid date range.");
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        return new long[] {
            applicationRepository.countApplicationsBetweenDates(start, endExclusive),
            interviewRepository.countInterviewsBetweenDates(start, endExclusive),
            offerRepository.countOffersBetweenDates(start, endExclusive),
            onboardingRepository.countOnboardingsBetweenDates(start, endExclusive)
        };
    }

    public byte[] generateCsv(LocalDate startDate, LocalDate endDate) {

        long[] counts = getCounts(startDate, endDate);

        StringBuilder csv = new StringBuilder();
        csv.append("JobPortalPro Admin Report\n");
        csv.append("Start Date,End Date\n");
        csv.append(startDate).append(",").append(endDate).append("\n\n");
        csv.append("Metric,Count\n");
        csv.append("Applications,").append(counts[0]).append("\n");
        csv.append("Interviews,").append(counts[1]).append("\n");
        csv.append("Job Offers,").append(counts[2]).append("\n");
        csv.append("Onboardings,").append(counts[3]).append("\n");

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generatePdf(LocalDate startDate, LocalDate endDate) {

        long[] counts = getCounts(startDate, endDate);

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            Document document = new Document();
            PdfWriter.getInstance(document, output);
            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 18);

            Paragraph title = new Paragraph(
                    "JobPortalPro - Admin Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Report Period: "
                    + startDate + " to " + endDate));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 3, 1 });

            table.addCell(new Phrase("Metric"));
            table.addCell(new Phrase("Count"));

            table.addCell("Applications");
            table.addCell(String.valueOf(counts[0]));

            table.addCell("Interviews");
            table.addCell(String.valueOf(counts[1]));

            table.addCell("Job Offers");
            table.addCell(String.valueOf(counts[2]));

            table.addCell("Onboardings");
            table.addCell(String.valueOf(counts[3]));

            document.add(table);
            document.close();

            return output.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF report generation failed.", e);
        }
    }
}
