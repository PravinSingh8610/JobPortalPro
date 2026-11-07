package com.jobportal.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "candidate_resumes")
public class CandidateResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Each candidate has one resume
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "candidate_id",
            nullable = false,
            unique = true
    )
    private User candidate;

    // Original uploaded file name
    @Column(nullable = false, length = 255)
    private String fileName;

    // File content type
    @Column(nullable = false, length = 100)
    private String contentType;

    // Resume file data stored in MySQL
    @Lob
    @Column(
            name = "file_data",
            nullable = false,
            columnDefinition = "LONGBLOB"
    )
    private byte[] fileData;

    // Upload timestamp
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    public CandidateResume() {
    }

    @PrePersist
    @PreUpdate
    public void updateTimestamp() {
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User candidate) {
        this.candidate = candidate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}