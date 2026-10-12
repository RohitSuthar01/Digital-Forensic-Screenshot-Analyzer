package com.dfsa.service;

import com.dfsa.dto.AnalysisResultDTO;
import com.dfsa.mapper.AnalysisResultMapper;
import com.dfsa.model.AnalysisResult;
import com.dfsa.model.Screenshot;
import com.dfsa.forensic.*;
import com.dfsa.repository.AnalysisResultRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
public class AnalysisService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    @Autowired
    private ScreenshotRepository screenshotRepository;
    @Autowired
    private AnalysisResultRepository analysisResultRepository;
    @Autowired
    private MetadataAnalyzer metadataAnalyzer;
    @Autowired
    private LsbSteganographyAnalyzer lsbSteganographyAnalyzer;
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

    /** Perform forensic analysis on a screenshot asynchronously. */
    @Async
    public void analyzeScreenshot(Long screenshotId) {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found"));

        screenshot.setStatus(Screenshot.Status.PROCESSING);
        screenshotRepository.saveAndFlush(screenshot);

        try {
            String perceptualHashHex = perceptualHash.getHashHex(screenshot);
            screenshot.setPerceptualHash(perceptualHashHex);

            Map<String, Object> metadataResult = metadataAnalyzer.analyze(screenshot);

            Map<String, Object> lsbResult;
            try {
                lsbResult = lsbSteganographyAnalyzer.analyze(new java.io.File(screenshot.getFilePath()));
            } catch (RuntimeException e) {
                lsbResult = new HashMap<>();
                lsbResult.put("status", "FAILED");
                lsbResult.put("messageDetected", false);
                lsbResult.put("reason", "LSB analysis failed; no conclusion was made.");
                lsbResult.put("limitation", "LSB scanning only covers common sequential readable-text patterns.");
            }
            metadataResult.put("lsbSteganography", lsbResult);

            String elaImageFileName = null;
            Map<String, Object> moduleStatuses = new LinkedHashMap<>();
            moduleStatuses.put("metadata", metadataResult.getOrDefault("metadataStatus", "UNAVAILABLE"));
            moduleStatuses.put("lsbSteganography", lsbResult.getOrDefault("status", "UNAVAILABLE"));
            try {
                elaImageFileName = errorLevelAnalysis.analyze(screenshot);
                moduleStatuses.put("ela", elaImageFileName == null ? "NOT_APPLICABLE" : "COMPLETED");
            } catch (IOException e) {
                moduleStatuses.put("ela", "FAILED");
            }

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

            Map<String, Object> ocrResult;
            try {
                ocrResult = ocrAndEvidenceExtractor.analyze(screenshot);
            } catch (Exception e) {
                ocrResult = new HashMap<>();
                ocrResult.put("error", "OCR module failed; other analysis results are still available.");
            }
            moduleStatuses.put("ocr", ocrResult.containsKey("error") ? "FAILED" : "COMPLETED");
            tamperResult.put("moduleStatuses", moduleStatuses);

            Map<String, Object> authenticityResult = authenticityScorer.computeScore(
                    metadataResult, tamperResult, ocrResult, elaImageFileName != null);

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
            analysisResultRepository.saveAndFlush(analysisResult);

            screenshot.setStatus(Screenshot.Status.COMPLETED);
            screenshot.setProcessedAt(new Date());
            screenshotRepository.saveAndFlush(screenshot);
        } catch (Exception e) {
            screenshot.setStatus(Screenshot.Status.FAILED);
            screenshot.setProcessedAt(new Date());
            screenshot.setErrorMessage("Analysis failed. Check server diagnostics and retry.");
            try {
                screenshotRepository.saveAndFlush(screenshot);
            } catch (Exception statusUpdateError) {
                log.error("Could not persist FAILED status for screenshot {}", screenshotId, statusUpdateError);
            }
            log.error("Analysis failed for screenshot {}", screenshotId, e);
        }
    }

    public AnalysisResultDTO getAnalysisResult(Long screenshotId) {
        AnalysisResult analysisResult = analysisResultRepository.findByScreenshotId(screenshotId)
                .orElseThrow(() -> new RuntimeException("Analysis result not found for screenshot ID " + screenshotId));
        return AnalysisResultMapper.toDTO(analysisResult);
    }
}
