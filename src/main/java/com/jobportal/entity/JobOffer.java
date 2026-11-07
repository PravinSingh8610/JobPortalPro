
package com.jobportal.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "job_offers",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_offer_interview",
            columnNames = {"interview_id"}
        )
    }
)
public class JobOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // INTERVIEW RELATIONSHIP
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "interview_id",
        nullable = false,
        unique = true
    )
    private Interview interview;

    // JOB OFFER DETAILS
    @Column(nullable = false, length = 150)
    private String designation;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal offeredSalary;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Column(nullable = false)
    private LocalDate offerExpiryDate;

    @Column(length = 2000)
    private String offerMessage;

    // PENDING, ACCEPTED, REJECTED, EXPIRED
    @Column(nullable = false, length = 30)
    private String status = "PENDING";

    // OFFER TIMESTAMPS
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime respondedAt;

    // BEFORE INSERT
    @PrePersist
    public void onCreate() {

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (status == null || status.isBlank()) {
            status = "PENDING";
        } else {
            status = status.trim().toUpperCase();
        }

        if (designation != null) {
            designation = designation.trim();
        }

        if (offerMessage != null && offerMessage.isBlank()) {
            offerMessage = null;
        }
    }

    // BEFORE UPDATE
    @PreUpdate
    public void onUpdate() {

        if (status != null) {
            status = status.trim().toUpperCase();
        }

        if (designation != null) {
            designation = designation.trim();
        }
    }

    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Interview getInterview() {
        return interview;
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public BigDecimal getOfferedSalary() {
        return offeredSalary;
    }

    public void setOfferedSalary(BigDecimal offeredSalary) {
        this.offeredSalary = offeredSalary;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDate getOfferExpiryDate() {
        return offerExpiryDate;
    }

    public void setOfferExpiryDate(LocalDate offerExpiryDate) {
        this.offerExpiryDate = offerExpiryDate;
    }

    public String getOfferMessage() {
        return offerMessage;
    }

    public void setOfferMessage(String offerMessage) {
        this.offerMessage = offerMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }
}
