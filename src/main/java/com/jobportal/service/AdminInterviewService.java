
package com.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Interview;
import com.jobportal.repository.InterviewRepository;

@Service
public class AdminInterviewService {

    private final InterviewRepository interviewRepository;

    // Constructor Injection
    public AdminInterviewService(
            InterviewRepository interviewRepository) {

        this.interviewRepository = interviewRepository;
    }

    // ========================================
    // ADMIN: FETCH ALL INTERVIEWS
    // ========================================

    @Transactional(readOnly = true)
    public List<Interview> getAllInterviews() {

        return interviewRepository
                .findAllByOrderByInterviewDateTimeDesc();
    }

    // ========================================
    // ADMIN: COUNT ALL INTERVIEWS
    // ========================================

    @Transactional(readOnly = true)
    public long getTotalInterviews() {

        return interviewRepository.count();
    }
}
