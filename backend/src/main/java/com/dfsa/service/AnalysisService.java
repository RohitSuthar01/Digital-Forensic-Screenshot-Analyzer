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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            try {
                elaImageFileName = errorLevelAnalysis.analyze(screenshot);
            } catch (IOException e) {
                // If ELA fails (e.g., not a JPEG), we can still continue with other analyses
                // We'll just log the error and set elaImageFileName to null
                System.err.println("ELA analysis failed: " + e.getMessage());
            }

            // 4. Tamper heuristics
            Map<String, Object> tamperResult = tamperHeuristics.analyze(screenshot);

            // 5. OCR and evidence extraction
            Map<String, Object> ocrResult = null;
            try {
                ocrResult = ocrAndEvidenceExtractor.analyze(screenshot);
            } catch (Throwable e) {
                // If OCR fails, we can still continue
                System.err.println("OCR analysis failed: " + e.getMessage());
                ocrResult = new HashMap<>();
                ocrResult.put("error", e.getMessage());
            }

            // 6. Compute authenticity score and verdict
            Map<String, Object> authenticityResult = authenticityScorer.computeScore(
                    metadataResult,
                    tamperResult,
                    ocrResult,
                    elaImageFileName != null
            );

            // 7. Save analysis result
            AnalysisResult analysisResult = new AnalysisResult();
            analysisResult.setScreenshot(screenshot);
            analysisResult.setVerdict((AnalysisResult.AuthenticityVerdict) authenticityResult.get("verdict"));
            analysisResult.setAuthenticityScore((Integer) authenticityResult.get("score"));
            analysisResult.setVerdictExplanation((String) authenticityResult.get("explanation"));
            analysisResult.setElaImageFileName(elaImageFileName);

            // We can store the detailed results as JSON in the analysis_results table if we want to keep them for debugging.
            // For now, we'll just store the summary. We can add JSON columns later if needed.
            // For the purpose of this task, we'll leave the JSON columns as null and focus on the score and verdict.

            analysisResultRepository.save(analysisResult);

            // 8. Update screenshot status to COMPLETED
            screenshot.setStatus(Screenshot.Status.COMPLETED);
            screenshot.setProcessedAt(new Date());
            screenshotRepository.save(screenshot);

        } catch (Throwable e) {
            // If any step fails, update status to FAILED and log the error
            screenshot.setStatus(Screenshot.Status.FAILED);
            screenshot.setProcessedAt(new Date());
            screenshot.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.toString());
            screenshotRepository.save(screenshot);
            System.err.println("Analysis failed for screenshot ID " + screenshotId + ": " + e.getMessage());
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