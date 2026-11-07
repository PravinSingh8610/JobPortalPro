
package com.jobportal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "job_applications",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_job_candidate",
            columnNames = {"job_id", "candidate_id"}
        )
    }
)
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Job for which candidate applied
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    // Candidate who submitted application
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidate_id", nullable = false)
    private User candidate;

    // Application Status
    @Column(nullable = false, length = 30)
    private String status = "APPLIED";

    // Application Submission Date
    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    // ========================================
    // FINAL HIRING DECISION
    // ========================================

    // SELECTED, REJECTED, ON_HOLD
    // Null means final decision not declared
    @Column(name = "final_decision", length = 30)
    private String finalDecision;

    // Date and time of final hiring decision
    private LocalDateTime decisionDeclaredAt;

    // Final recruiter feedback
    @Column(length = 2000)
    private String recruiterFeedback;

    // ========================================
    // PRE PERSIST
    // ========================================

    @PrePersist
    public void onCreate() {

        if (status == null) {
            status = "APPLIED";
        }

        if (appliedAt == null) {
            appliedAt = LocalDateTime.now();
        }
    }

    // ========================================
    // GETTERS AND SETTERS
    // ========================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public LocalDateTime getDecisionDeclaredAt() {
        return decisionDeclaredAt;
    }

    public void setDecisionDeclaredAt(
            LocalDateTime decisionDeclaredAt) {
        this.decisionDeclaredAt = decisionDeclaredAt;
    }

    public String getRecruiterFeedback() {
        return recruiterFeedback;
    }

    public void setRecruiterFeedback(String recruiterFeedback) {
        this.recruiterFeedback = recruiterFeedback;
    }
}
