package com.dfsa.mapper;

import com.dfsa.dto.AnalysisResultDTO;
import com.dfsa.model.AnalysisResult;

import java.util.Date;

public class AnalysisResultMapper {

    public static AnalysisResultDTO toDTO(AnalysisResult analysisResult) {
        AnalysisResultDTO dto = new AnalysisResultDTO();
        dto.setId(analysisResult.getId());
        dto.setScreenshotId(analysisResult.getScreenshot().getId());
        dto.setVerdict(analysisResult.getVerdict());
        dto.setAuthenticityScore(analysisResult.getAuthenticityScore());
        dto.setVerdictExplanation(analysisResult.getVerdictExplanation());
        dto.setElaImageFileName(analysisResult.getElaImageFileName());
        dto.setAnalyzedAt(analysisResult.getAnalyzedAt());
        return dto;
    }
}