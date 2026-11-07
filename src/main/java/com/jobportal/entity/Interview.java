
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

@Entity
@Table(name = "interviews")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ========================================
    // JOB APPLICATION RELATIONSHIP
    // ========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private JobApplication application;

    // ========================================
    // INTERVIEW SCHEDULING DETAILS
    // ========================================

    @Column(nullable = false)
    private LocalDateTime interviewDateTime;

    @Column(nullable = false, length = 20)
    private String interviewMode;

    @Column(length = 500)
    private String meetingLink;

    @Column(length = 255)
    private String interviewLocation;

    // SCHEDULED, COMPLETED, CANCELLED
    @Column(nullable = false, length = 30)
    private String status = "SCHEDULED";

    @Column(length = 1000)
    private String notes;

    // ========================================
    // INTERVIEW RESULT DETAILS
    // ========================================

    // SELECTED, REJECTED, ON_HOLD
    // Null means result not declared
    @Column(length = 30)
    private String result;

    @Column(length = 2000)
    private String feedback;

    private LocalDateTime resultDeclaredAt;

    // ========================================
    // RECORD CREATION DATE
    // ========================================

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {

        if (status == null) {
            status = "SCHEDULED";
        }

        if (createdAt == null) {
            createdAt = LocalDateTime.now();
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

    public JobApplication getApplication() {
        return application;
    }

    public void setApplication(JobApplication application) {
        this.application = application;
    }

    public LocalDateTime getInterviewDateTime() {
        return interviewDateTime;
    }

    public void setInterviewDateTime(
            LocalDateTime interviewDateTime) {
        this.interviewDateTime = interviewDateTime;
    }

    public String getInterviewMode() {
        return interviewMode;
    }

    public void setInterviewMode(String interviewMode) {
        this.interviewMode = interviewMode;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public String getInterviewLocation() {
        return interviewLocation;
    }

    public void setInterviewLocation(String interviewLocation) {
        this.interviewLocation = interviewLocation;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getResultDeclaredAt() {
        return resultDeclaredAt;
    }

    public void setResultDeclaredAt(
            LocalDateTime resultDeclaredAt) {
        this.resultDeclaredAt = resultDeclaredAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
