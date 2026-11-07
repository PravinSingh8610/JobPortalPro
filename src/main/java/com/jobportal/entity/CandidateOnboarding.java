
package com.jobportal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "candidate_onboarding",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_onboarding_offer",
            columnNames = {"offer_id"}
        )
    }
)
public class CandidateOnboarding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One onboarding record for one accepted offer
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "offer_id",
        nullable = false,
        unique = true
    )
    private JobOffer jobOffer;

    // PENDING, UNDER_REVIEW, APPROVED, REJECTED
    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    @Column(length = 1000)
    private String recruiterRemarks;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime submittedAt;

    private LocalDateTime reviewedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null) {
            status = "PENDING";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public JobOffer getJobOffer() {
        return jobOffer;
    }

    public void setJobOffer(JobOffer jobOffer) {
        this.jobOffer = jobOffer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRecruiterRemarks() {
        return recruiterRemarks;
    }

    public void setRecruiterRemarks(String recruiterRemarks) {
        this.recruiterRemarks = recruiterRemarks;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
