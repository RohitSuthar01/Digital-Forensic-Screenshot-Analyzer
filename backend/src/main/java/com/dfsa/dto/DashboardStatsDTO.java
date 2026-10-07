package com.dfsa.dto;

import java.util.*;

public class DashboardStatsDTO {

    private long totalCases;
    private long totalScreenshots;
    private long totalScreenshotsAnalyzed;
    private long totalTamperedDetected;
    private long pendingAnalyses;
    private Map<String, Long> verdictDistribution;
    private List<ChartDataDTO> uploadsOverTime;
    private Map<String, Long> casesByPriority;
    private List<ActivityDTO> recentActivity;

    // Getters and Setters
    public long getTotalCases() {
        return totalCases;
    }

    public void setTotalCases(long totalCases) {
        this.totalCases = totalCases;
    }

    public long getTotalScreenshots() {
        return totalScreenshots;
    }

    public void setTotalScreenshots(long totalScreenshots) {
        this.totalScreenshots = totalScreenshots;
    }

    public long getTotalScreenshotsAnalyzed() {
        return totalScreenshotsAnalyzed;
    }

    public void setTotalScreenshotsAnalyzed(long totalScreenshotsAnalyzed) {
        this.totalScreenshotsAnalyzed = totalScreenshotsAnalyzed;
    }

    public long getTotalTamperedDetected() {
        return totalTamperedDetected;
    }

    public void setTotalTamperedDetected(long totalTamperedDetected) {
        this.totalTamperedDetected = totalTamperedDetected;
    }

    public long getPendingAnalyses() {
        return pendingAnalyses;
    }

    public void setPendingAnalyses(long pendingAnalyses) {
        this.pendingAnalyses = pendingAnalyses;
    }

    public Map<String, Long> getVerdictDistribution() {
        return verdictDistribution;
    }

    public void setVerdictDistribution(Map<String, Long> verdictDistribution) {
        this.verdictDistribution = verdictDistribution;
    }

    public List<ChartDataDTO> getUploadsOverTime() {
        return uploadsOverTime;
    }

    public void setUploadsOverTime(List<ChartDataDTO> uploadsOverTime) {
        this.uploadsOverTime = uploadsOverTime;
    }

    public Map<String, Long> getCasesByPriority() {
        return casesByPriority;
    }

    public void setCasesByPriority(Map<String, Long> casesByPriority) {
        this.casesByPriority = casesByPriority;
    }

    public List<ActivityDTO> getRecentActivity() {
        return recentActivity;
    }

    public void setRecentActivity(List<ActivityDTO> recentActivity) {
        this.recentActivity = recentActivity;
    }
}