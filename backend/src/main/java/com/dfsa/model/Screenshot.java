package com.dfsa.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Date;

@Entity
@Table(name = "screenshots", indexes = {
        @Index(columnList = "sha256"),
        @Index(columnList = "case_id"),
        @Index(columnList = "perceptual_hash")
})
public class Screenshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "case_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private com.dfsa.model.Case theCase;

    @NotBlank
    @Size(max = 255)
    private String originalFilename;

    @NotBlank
    @Size(max = 255)
    private String storedFilename; // UUID name

    @NotBlank
    @Size(max = 255)
    private String filePath; // relative to storage directory

    @NotBlank
    @Size(max = 64)
    private String md5;

    @NotBlank
    @Size(max = 64)
    private String sha256;

    @Column(length = 64)
    private String perceptualHash; // dHash or pHash as hex string

    private Long fileSize; // in bytes

    @Enumerated(EnumType.STRING)
    private Status status; // PENDING, PROCESSING, COMPLETED, FAILED

    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt;

    @Temporal(TemporalType.TIMESTAMP)
    private Date processedAt;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    // Constructors
    public Screenshot() {
        this.uploadedAt = new Date();
        this.status = Status.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public com.dfsa.model.Case getTheCase() { return theCase; }
    public void setTheCase(com.dfsa.model.Case theCase) { this.theCase = theCase; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getStoredFilename() { return storedFilename; }
    public void setStoredFilename(String storedFilename) { this.storedFilename = storedFilename; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getMd5() { return md5; }
    public void setMd5(String md5) { this.md5 = md5; }

    public String getSha256() { return sha256; }
    public void setSha256(String sha256) { this.sha256 = sha256; }

    public String getPerceptualHash() { return perceptualHash; }
    public void setPerceptualHash(String perceptualHash) { this.perceptualHash = perceptualHash; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Date getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Date uploadedAt) { this.uploadedAt = uploadedAt; }

    public Date getProcessedAt() { return processedAt; }
    public void setProcessedAt(Date processedAt) { this.processedAt = processedAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    // Enums
    public enum Status {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}