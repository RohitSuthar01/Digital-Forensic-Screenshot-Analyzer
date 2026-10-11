package com.dfsa.model;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Date;

@Entity
@Table(name = "analysis_results", indexes = {
        @Index(columnList = "screenshot_id")
})
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "screenshot_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private com.dfsa.model.Screenshot screenshot;

    @Enumerated(EnumType.STRING)
    private AuthenticityVerdict verdict; // AUTHENTIC, SUSPICIOUS, LIKELY_TAMPERED

    private Integer authenticityScore; // 0-100

    private String verdictExplanation;

    // New field for ELA image file name (stored in the upload directory)
    private String elaImageFileName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private java.util.Map<String, Object> metadataJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private java.util.Map<String, Object> tamperHeuristicsJson;

    @Column(columnDefinition = "TEXT")
    private String ocrText;

    @Temporal(TemporalType.TIMESTAMP)
    private Date analyzedAt;

    // Constructors
    public AnalysisResult() {
        this.analyzedAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public com.dfsa.model.Screenshot getScreenshot() { return screenshot; }
    public void setScreenshot(com.dfsa.model.Screenshot screenshot) { this.screenshot = screenshot; }

    public AuthenticityVerdict getVerdict() { return verdict; }
    public void setVerdict(AuthenticityVerdict verdict) { this.verdict = verdict; }

    public Integer getAuthenticityScore() { return authenticityScore; }
    public void setAuthenticityScore(Integer authenticityScore) { this.authenticityScore = authenticityScore; }

    public String getVerdictExplanation() { return verdictExplanation; }
    public void setVerdictExplanation(String verdictExplanation) { this.verdictExplanation = verdictExplanation; }

    public String getElaImageFileName() { return elaImageFileName; }
    public void setElaImageFileName(String elaImageFileName) { this.elaImageFileName = elaImageFileName; }

    public java.util.Map<String, Object> getMetadataJson() { return metadataJson; }
    public void setMetadataJson(java.util.Map<String, Object> metadataJson) { this.metadataJson = metadataJson; }

    public java.util.Map<String, Object> getTamperHeuristicsJson() { return tamperHeuristicsJson; }
    public void setTamperHeuristicsJson(java.util.Map<String, Object> tamperHeuristicsJson) { this.tamperHeuristicsJson = tamperHeuristicsJson; }

    public String getOcrText() { return ocrText; }
    public void setOcrText(String ocrText) { this.ocrText = ocrText; }

    public Date getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(Date analyzedAt) { this.analyzedAt = analyzedAt; }

    // Enums
    public enum AuthenticityVerdict {
        AUTHENTIC, SUSPICIOUS, LIKELY_TAMPERED, INCONCLUSIVE
    }
}
