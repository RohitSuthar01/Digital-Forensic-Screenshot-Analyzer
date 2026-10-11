package com.dfsa.service;

import com.dfsa.dto.DashboardStatsDTO;
import com.dfsa.model.*;
import java.util.Arrays;
import com.dfsa.dto.ActivityDTO;
import com.dfsa.dto.ChartDataDTO;
import com.dfsa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private ScreenshotRepository screenshotRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Get dashboard statistics.
     *
     * @return a DTO containing various statistics for the dashboard
     */
    public DashboardStatsDTO getDashboardStats(Long userId, boolean admin) {
        if (admin) return getDashboardStats();

        DashboardStatsDTO stats = new DashboardStatsDTO();
        List<Case> cases = caseRepository.findByInvestigatorId(userId);
        List<Screenshot> screenshots = screenshotRepository.findByTheCase_Investigator_Id(userId);
        List<Long> screenshotIds = screenshots.stream().map(Screenshot::getId).filter(Objects::nonNull).toList();
        List<AnalysisResult> results = screenshotIds.isEmpty()
                ? List.of() : analysisResultRepository.findByScreenshot_IdIn(screenshotIds);

        stats.setTotalCases(cases.size());
        stats.setTotalScreenshots(screenshots.size());
        stats.setTotalScreenshotsAnalyzed(results.size());
        stats.setTotalTamperedDetected(results.stream()
                .filter(result -> result.getVerdict() == AnalysisResult.AuthenticityVerdict.LIKELY_TAMPERED).count());
        stats.setPendingAnalyses(screenshots.stream()
                .filter(screenshot -> screenshot.getStatus() == Screenshot.Status.PENDING
                        || screenshot.getStatus() == Screenshot.Status.PROCESSING).count());

        Map<String, Long> verdicts = new HashMap<>();
        for (AnalysisResult.AuthenticityVerdict verdict : AnalysisResult.AuthenticityVerdict.values()) {
            verdicts.put(verdict.name(), results.stream().filter(result -> result.getVerdict() == verdict).count());
        }
        stats.setVerdictDistribution(verdicts);

        java.time.LocalDate today = java.time.LocalDate.now();
        Map<java.time.LocalDate, Long> uploadsByDate = new TreeMap<>();
        screenshots.stream().map(Screenshot::getUploadedAt).filter(Objects::nonNull)
                .map(date -> date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                .filter(date -> !date.isBefore(today.minusDays(7)) && !date.isAfter(today))
                .forEach(date -> uploadsByDate.merge(date, 1L, Long::sum));
        stats.setUploadsOverTime(uploadsByDate.entrySet().stream()
                .map(entry -> new ChartDataDTO(entry.getKey().toString(), entry.getValue())).toList());

        Map<String, Long> priorities = new HashMap<>();
        for (Case.Priority priority : Case.Priority.values()) {
            priorities.put(priority.name(), cases.stream().filter(item -> item.getPriority() == priority).count());
        }
        stats.setCasesByPriority(priorities);

        List<ActivityDTO> activity = auditLogRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(AuditLog::getTimestamp,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(10)
                .map(log -> new ActivityDTO(log.getId(), log.getAction(), log.getEntityType(), log.getEntityId(),
                        log.getDescription(), log.getUser() != null ? log.getUser().getUsername() : "System",
                        log.getTimestamp()))
                .toList();
        stats.setRecentActivity(activity);
        return stats;
    }

    public DashboardStatsDTO getDashboardStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        // Total cases
        stats.setTotalCases(caseRepository.count());

        // Total screenshots
        stats.setTotalScreenshots(screenshotRepository.count());

        // Total screenshots analyzed (status COMPLETED in analysis_results)
        stats.setTotalScreenshotsAnalyzed(analysisResultRepository.count());

        // Total tampered detected (verdict LIKELY_TAMPERED)
        stats.setTotalTamperedDetected(analysisResultRepository.countByVerdict(AnalysisResult.AuthenticityVerdict.LIKELY_TAMPERED));

        // Pending analyses (screenshots with status PENDING or PROCESSING)
        long pending = screenshotRepository.countByStatusIn(Arrays.asList(
                Screenshot.Status.PENDING,
                Screenshot.Status.PROCESSING));
        stats.setPendingAnalyses(pending);

        // Verdict distribution (for the pie chart)
        Map<String, Long> verdictDistribution = new HashMap<>();
        verdictDistribution.put("AUTHENTIC", analysisResultRepository.countByVerdict(AnalysisResult.AuthenticityVerdict.AUTHENTIC));
        verdictDistribution.put("SUSPICIOUS", analysisResultRepository.countByVerdict(AnalysisResult.AuthenticityVerdict.SUSPICIOUS));
        verdictDistribution.put("LIKELY_TAMPERED", analysisResultRepository.countByVerdict(AnalysisResult.AuthenticityVerdict.LIKELY_TAMPERED));
        verdictDistribution.put("INCONCLUSIVE", analysisResultRepository.countByVerdict(AnalysisResult.AuthenticityVerdict.INCONCLUSIVE));
        stats.setVerdictDistribution(verdictDistribution);

        // Uploads over time (last 30 days, grouped by day) - we'll simplify and just return the last 7 days for the line chart
        // We'll get the uploads per day for the last 7 days
        List<Object[]> uploadsOverTime = screenshotRepository.getUploadsPerDayLast7Days();
        List<ChartDataDTO> uploadsOverTimeList = uploadsOverTime.stream()
                .map(row -> new ChartDataDTO(row[0].toString(), ((Number) row[1]).longValue()))
                .collect(Collectors.toList());
        stats.setUploadsOverTime(uploadsOverTimeList);

        // Cases by priority (bar chart)
        Map<String, Long> casesByPriority = new HashMap<>();
        casesByPriority.put("LOW", caseRepository.countByPriority(Case.Priority.LOW));
        casesByPriority.put("MEDIUM", caseRepository.countByPriority(Case.Priority.MEDIUM));
        casesByPriority.put("HIGH", caseRepository.countByPriority(Case.Priority.HIGH));
        casesByPriority.put("CRITICAL", caseRepository.countByPriority(Case.Priority.CRITICAL));
        stats.setCasesByPriority(casesByPriority);

        // Recent activity feed (last 10 audit logs)
        List<AuditLog> recentActivity = auditLogRepository.findTop10ByOrderByTimestampDesc();
        List<ActivityDTO> activityList = recentActivity.stream()
                .map(log -> new ActivityDTO(
                        log.getId(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getDescription(),
                        log.getUser() != null ? log.getUser().getUsername() : "System",
                        log.getTimestamp()))
                .collect(Collectors.toList());
        stats.setRecentActivity(activityList);

        return stats;
    }
}
