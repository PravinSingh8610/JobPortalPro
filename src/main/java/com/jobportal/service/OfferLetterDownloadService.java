
package com.jobportal.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.JobOffer;

@Service
public class OfferLetterDownloadService {

    private final JobOfferService jobOfferService;
    private final OfferLetterPdfService offerLetterPdfService;

    public OfferLetterDownloadService(
            JobOfferService jobOfferService,
            OfferLetterPdfService offerLetterPdfService) {

        this.jobOfferService = jobOfferService;
        this.offerLetterPdfService = offerLetterPdfService;
    }

    // Candidate can download only their own offer letter
    @Transactional(readOnly = true)
    public byte[] downloadCandidateOffer(
            Long offerId,
            String candidateEmail) {

        JobOffer offer = jobOfferService
                .getCandidateOfferForDownload(
                        offerId, candidateEmail);

        return generatePdf(offer);
    }

    // Recruiter can download only offers for their own job
    @Transactional(readOnly = true)
    public byte[] downloadRecruiterOffer(
            Long jobId,
            Long offerId,
            String recruiterEmail) {

        JobOffer offer = jobOfferService
                .getRecruiterOfferForDownload(
                        jobId, offerId, recruiterEmail);

        return generatePdf(offer);
    }

    // Common PDF generation method
    private byte[] generatePdf(JobOffer offer) {

        if (offer == null) {
            throw new IllegalArgumentException(
                    "Offer not found!");
        }

        byte[] pdf = offerLetterPdfService
                .generateOfferLetter(offer);

        if (pdf == null || pdf.length == 0) {
            throw new IllegalStateException(
                    "Offer letter PDF could not be generated!");
        }

        return pdf;
    }
}
