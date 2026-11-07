
package com.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.JobOffer;
import com.jobportal.repository.JobOfferRepository;

@Service
public class AdminOfferService {

    private final JobOfferRepository jobOfferRepository;

    // ========================================
    // CONSTRUCTOR INJECTION
    // ========================================

    public AdminOfferService(
            JobOfferRepository jobOfferRepository) {

        this.jobOfferRepository = jobOfferRepository;
    }

    // ========================================
    // ADMIN: GET ALL JOB OFFERS
    // ========================================

    @Transactional(readOnly = true)
    public List<JobOffer> getAllOffers() {

        return jobOfferRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // ========================================
    // ADMIN: COUNT TOTAL JOB OFFERS
    // ========================================

    @Transactional(readOnly = true)
    public long getTotalOffers() {

        return jobOfferRepository.count();
    }
}
