
package com.jobportal.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.jobportal.entity.JobOffer;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

@Service
public class OfferLetterPdfService {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public byte[] generateOfferLetter(JobOffer offer) {

        if (offer == null) {
            throw new IllegalArgumentException("Offer not found!");
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);

        try {

            PdfWriter.getInstance(document, output);

            document.addTitle("JobPortalPro - Offer Letter");
            document.addAuthor("JobPortalPro");
            document.addSubject("Employment Offer Letter");

            document.open();

            // Fetch company details
            if (offer.getInterview() == null
                    || offer.getInterview().getApplication() == null
                    || offer.getInterview().getApplication().getJob() == null
                    || offer.getInterview().getApplication().getCandidate() == null) {

                throw new IllegalStateException(
                        "Required offer details are missing!");
            }

            String companyName = offer.getInterview()
                    .getApplication()
                    .getJob()
                    .getCompany()
                    .getCompanyName();

            String candidateName = offer.getInterview()
                    .getApplication()
                    .getCandidate()
                    .getFullName();

            String candidateEmail = offer.getInterview()
                    .getApplication()
                    .getCandidate()
                    .getEmail();

            if (companyName == null || companyName.isBlank()) {
                companyName = "JobPortalPro Company";
            }

            if (candidateName == null || candidateName.isBlank()) {
                candidateName = "Candidate";
            }

            // Fonts
            Font companyFont =
                    new Font(Font.HELVETICA, 22, Font.BOLD);

            Font titleFont =
                    new Font(Font.HELVETICA, 17, Font.BOLD);

            Font headingFont =
                    new Font(Font.HELVETICA, 12, Font.BOLD);

            Font normalFont =
                    new Font(Font.HELVETICA, 11, Font.NORMAL);

            Font footerFont =
                    new Font(Font.HELVETICA, 9, Font.ITALIC);

            // Company heading
            Paragraph companyHeading =
                    new Paragraph(companyName, companyFont);

            companyHeading.setAlignment(Element.ALIGN_CENTER);
            companyHeading.setSpacingAfter(10);

            document.add(companyHeading);

            Paragraph divider =
                    new Paragraph(
                            "________________________________________________________");

            divider.setAlignment(Element.ALIGN_CENTER);
            divider.setSpacingAfter(20);

            document.add(divider);

            // Offer letter title
            Paragraph title =
                    new Paragraph("OFFER LETTER", titleFont);

            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(25);

            document.add(title);

            // Offer reference
            addLine(document,
                    "Offer Reference: JPP-OFFER-" + offer.getId(),
                    normalFont);

            LocalDateTime createdAt = offer.getCreatedAt();

            String offerDate = createdAt != null
                    ? createdAt.format(DATE_TIME_FORMAT)
                    : LocalDate.now().format(DATE_FORMAT);

            addLine(document,
                    "Date: " + offerDate,
                    normalFont);

            document.add(new Paragraph(" "));

            // Candidate details
            addLine(document,
                    "To: " + candidateName,
                    headingFont);

            addLine(document,
                    "Email: " + safeText(candidateEmail),
                    normalFont);

            document.add(new Paragraph(" "));

            addLine(document,
                    "Subject: Offer of Employment",
                    headingFont);

            document.add(new Paragraph(" "));

            addLine(document,
                    "Dear " + candidateName + ",",
                    normalFont);

            document.add(new Paragraph(" "));

            addLine(document,
                    "We are pleased to offer you the position of "
                    + safeText(offer.getDesignation())
                    + " at " + companyName + ".",
                    normalFont);

            document.add(new Paragraph(" "));

            // Employment details
            addLine(document,
                    "EMPLOYMENT DETAILS",
                    headingFont);

            addLine(document,
                    "Designation: " + safeText(offer.getDesignation()),
                    normalFont);

            BigDecimal salary = offer.getOfferedSalary();

            if (salary == null || salary.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "Valid offered salary is required!");
            }

            DecimalFormatSymbols symbols =
                    DecimalFormatSymbols.getInstance(new Locale("en", "IN"));

            DecimalFormat salaryFormat =
                    new DecimalFormat("#,##,##0.00", symbols);

            String formattedSalary = salaryFormat.format(salary);

            addLine(document,
                    "Annual CTC: INR " + formattedSalary,
                    normalFont);

            LocalDate joiningDate = offer.getJoiningDate();
            LocalDate expiryDate = offer.getOfferExpiryDate();

            if (joiningDate == null || expiryDate == null) {
                throw new IllegalStateException(
                        "Joining date and offer expiry date are required!");
            }

            addLine(document,
                    "Joining Date: " + joiningDate.format(DATE_FORMAT),
                    normalFont);

            addLine(document,
                    "Offer Valid Until: " + expiryDate.format(DATE_FORMAT),
                    normalFont);

            document.add(new Paragraph(" "));

            // Offer terms
            if (offer.getOfferMessage() != null
                    && !offer.getOfferMessage().isBlank()) {

                addLine(document,
                        "OFFER TERMS AND CONDITIONS",
                        headingFont);

                addLine(document,
                        offer.getOfferMessage(),
                        normalFont);

                document.add(new Paragraph(" "));
            }

            // Offer status
            addLine(document,
                    "Offer Status: " + safeText(offer.getStatus()),
                    headingFont);

            document.add(new Paragraph(" "));

            addLine(document,
                    "We are excited about the possibility of you joining "
                    + "our team. We look forward to welcoming you.",
                    normalFont);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Signature
            addLine(document,
                    "Sincerely,",
                    normalFont);

            document.add(new Paragraph(" "));

            addLine(document,
                    "Human Resources Department",
                    headingFont);

            addLine(document,
                    companyName,
                    normalFont);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph footer = new Paragraph(
                    "This is a computer-generated offer letter.",
                    footerFont);

            footer.setAlignment(Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

            return output.toByteArray();

        } catch (Exception e) {

            if (document.isOpen()) {
                document.close();
            }

            throw new IllegalStateException(
                    "Unable to generate offer letter PDF", e);
        }
    }

    private void addLine(
            Document document,
            String text,
            Font font) throws Exception {

        Paragraph paragraph = new Paragraph(text, font);

        paragraph.setSpacingAfter(8);

        document.add(paragraph);
    }

    private String safeText(String value) {

        if (value == null || value.isBlank()) {
            return "Not Available";
        }

        return value.trim();
    }
}
