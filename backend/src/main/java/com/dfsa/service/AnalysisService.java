package com.dfsa.service;

import com.dfsa.dto.AnalysisResultDTO;
import com.dfsa.mapper.AnalysisResultMapper;
import com.dfsa.model.AnalysisResult;
import com.dfsa.model.Screenshot;
import com.dfsa.forensic.*;
import com.dfsa.repository.AnalysisResultRepository;
import com.dfsa.repository.ScreenshotRepository;
import com.dfsa.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class AnalysisService {

    @Autowired
    private ScreenshotRepository screenshotRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private MetadataAnalyzer metadataAnalyzer;

    @Autowired
    private ErrorLevelAnalysis errorLevelAnalysis;

    @Autowired
    private TamperHeuristics tamperHeuristics;

    @Autowired
    private OCRAndEvidenceExtractor ocrAndEvidenceExtractor;

    @Autowired
    private PerceptualHash perceptualHash;

    @Autowired
    private AuthenticityScorer authenticityScorer;

    /**
     * Perform forensic analysis on a screenshot asynchronously.
     *
     * @param screenshotId the ID of the screenshot to analyze
     */
    @Async
    @Transactional
    public void analyzeScreenshot(Long screenshotId) {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found"));

        // Update status to PROCESSING
        screenshot.setStatus(Screenshot.Status.PROCESSING);
        screenshotRepository.save(screenshot);

        try {
            // 1. Compute and store perceptual hash
            String perceptualHashHex = perceptualHash.getHashHex(screenshot);
            screenshot.setPerceptualHash(perceptualHashHex);

            // 2. Metadata analysis
            Map<String, Object> metadataResult = metadataAnalyzer.analyze(screenshot);

            // 3. Error Level Analysis (if applicable)
            String elaImageFileName = null;
            Map<String, Object> moduleStatuses = new LinkedHashMap<>();
            moduleStatuses.put("metadata", metadataResult.getOrDefault("metadataStatus", "UNAVAILABLE"));
            try {
                elaImageFileName = errorLevelAnalysis.analyze(screenshot);
                moduleStatuses.put("ela", elaImageFileName == null ? "NOT_APPLICABLE" : "COMPLETED");
            } catch (IOException e) {
                moduleStatuses.put("ela", "FAILED");
            }

            // 4. Tamper heuristics
            Map<String, Object> tamperResult;
            try {
                tamperResult = tamperHeuristics.analyze(screenshot);
                moduleStatuses.put("tamperAssessment", tamperResult.getOrDefault("status", "COMPLETED"));
            } catch (Exception e) {
                tamperResult = new HashMap<>();
                tamperResult.put("status", "FAILED");
                tamperResult.put("limitation", "Tamper assessment failed; no inference was made from it.");
                moduleStatuses.put("tamperAssessment", "FAILED");
            }

            // 5. OCR and evidence extraction
            Map<String, Object> ocrResult = null;
            try {
                ocrResult = ocrAndEvidenceExtractor.analyze(screenshot);
            } catch (Exception e) {
                ocrResult = new HashMap<>();
                ocrResult.put("error", "OCR module failed; other analysis results are still available.");
            }
            moduleStatuses.put("ocr", ocrResult.containsKey("error") ? "FAILED" : "COMPLETED");
            tamperResult.put("moduleStatuses", moduleStatuses);

            // 6. Compute authenticity score and verdict
            Map<String, Object> authenticityResult = authenticityScorer.computeScore(
                    metadataResult,
                    tamperResult,
                    ocrResult,
                    elaImageFileName != null
            );

            // 7. Save analysis result
            AnalysisResult analysisResult = analysisResultRepository.findByScreenshotId(screenshotId)
                    .orElseGet(AnalysisResult::new);
            analysisResult.setScreenshot(screenshot);
            analysisResult.setVerdict((AnalysisResult.AuthenticityVerdict) authenticityResult.get("verdict"));
            analysisResult.setAuthenticityScore((Integer) authenticityResult.get("score"));
            analysisResult.setVerdictExplanation((String) authenticityResult.get("explanation"));
            analysisResult.setElaImageFileName(elaImageFileName);
            analysisResult.setMetadataJson(metadataResult);
            analysisResult.setTamperHeuristicsJson(tamperResult);
            analysisResult.setOcrText(ocrResult.get("ocr_text") instanceof String text ? text : null);

            // We can store the detailed results as JSON in the analysis_results table if we want to keep them for debugging.
            // For now, we'll just store the summary. We can add JSON columns later if needed.
            // For the purpose of this task, we'll leave the JSON columns as null and focus on the score and verdict.

            analysisResultRepository.save(analysisResult);

            // 8. Update screenshot status to COMPLETED
            screenshot.setStatus(Screenshot.Status.COMPLETED);
            screenshot.setProcessedAt(new Date());
            screenshotRepository.save(screenshot);

        } catch (Exception e) {
            screenshot.setStatus(Screenshot.Status.FAILED);
            screenshot.setProcessedAt(new Date());
            screenshot.setErrorMessage("Analysis failed. Check server diagnostics and retry.");
            screenshotRepository.save(screenshot);
            System.err.println("Analysis failed for screenshot ID " + screenshotId + " (" + e.getClass().getSimpleName() + ")");
        }
    }

    /**
     * Get the analysis result for a screenshot.
     *
     * @param screenshotId the ID of the screenshot
     * @return the analysis result DTO
     */
    public AnalysisResultDTO getAnalysisResult(Long screenshotId) {
        AnalysisResult analysisResult = analysisResultRepository.findByScreenshotId(screenshotId)
                .orElseThrow(() -> new RuntimeException("Analysis result not found for screenshot ID " + screenshotId));
        return AnalysisResultMapper.toDTO(analysisResult);
    }
}
