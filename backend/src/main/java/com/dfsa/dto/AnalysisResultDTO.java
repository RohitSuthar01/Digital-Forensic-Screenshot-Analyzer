package com.dfsa.dto;

import com.dfsa.model.AnalysisResult;

import java.util.Date;

public class AnalysisResultDTO {

    private Long id;
    private Long screenshotId;
    private AnalysisResult.AuthenticityVerdict verdict;
    private Integer authenticityScore;
    private String verdictExplanation;
    private String elaImageFileName; // We can use this to construct a URL for the ELA image
    private Date analyzedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getScreenshotId() {
        return screenshotId;
    }

    public void setScreenshotId(Long screenshotId) {
        this.screenshotId = screenshotId;
    }

    public AnalysisResult.AuthenticityVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(AnalysisResult.AuthenticityVerdict verdict) {
        this.verdict = verdict;
    }

    public Integer getAuthenticityScore() {
        return authenticityScore;
    }

    public void setAuthenticityScore(Integer authenticityScore) {
        this.authenticityScore = authenticityScore;
    }

    public String getVerdictExplanation() {
        return verdictExplanation;
    }

    public void setVerdictExplanation(String verdictExplanation) {
        this.verdictExplanation = verdictExplanation;
    }

    public String getElaImageFileName() {
        return elaImageFileName;
    }

    public void setElaImageFileName(String elaImageFileName) {
        this.elaImageFileName = elaImageFileName;
    }

    public Date getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(Date analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}