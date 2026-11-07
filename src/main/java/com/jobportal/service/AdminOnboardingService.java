
package com.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.CandidateOnboarding;
import com.jobportal.repository.CandidateOnboardingRepository;

@Service
public class AdminOnboardingService {

    // ========================================
    // REPOSITORY DEPENDENCY
    // ========================================

    private final CandidateOnboardingRepository
            candidateOnboardingRepository;

    // ========================================
    // CONSTRUCTOR INJECTION
    // ========================================

    public AdminOnboardingService(
            CandidateOnboardingRepository
                    candidateOnboardingRepository) {

        this.candidateOnboardingRepository =
                candidateOnboardingRepository;
    }

    // ========================================
    // 1. GET ALL ONBOARDING RECORDS
    // ========================================

    @Transactional(readOnly = true)
    public List<CandidateOnboarding> getAllOnboardings() {

        return candidateOnboardingRepository
                .findAllByOrderByCreatedAtDesc();
    }

    // ========================================
    // 2. GET TOTAL ONBOARDINGS
    // ========================================

    @Transactional(readOnly = true)
    public long getTotalOnboardings() {

        return candidateOnboardingRepository.count();
    }

    // ========================================
    // 3. GET ONBOARDINGS BY STATUS
    // ========================================

    @Transactional(readOnly = true)
    public long getCountByStatus(String status) {

        return candidateOnboardingRepository
                .findAll()
                .stream()
                .filter(onboarding ->
                        status.equalsIgnoreCase(
                                onboarding.getStatus()
                        )
                )
                .count();
    }
}
