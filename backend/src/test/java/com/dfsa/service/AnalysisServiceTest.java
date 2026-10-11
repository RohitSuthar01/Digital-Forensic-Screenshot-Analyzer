package com.dfsa.service;

import com.dfsa.forensic.*;
import com.dfsa.model.Screenshot;
import com.dfsa.repository.AnalysisResultRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceTest {
    @Mock ScreenshotRepository screenshotRepository;
    @Mock AnalysisResultRepository analysisResultRepository;
    @Mock MetadataAnalyzer metadataAnalyzer;
    @Mock ErrorLevelAnalysis errorLevelAnalysis;
    @Mock TamperHeuristics tamperHeuristics;
    @Mock OCRAndEvidenceExtractor ocrAndEvidenceExtractor;
    @Mock PerceptualHash perceptualHash;
    @Mock AuthenticityScorer authenticityScorer;
    @InjectMocks AnalysisService service;

    @Test
    void moduleFailureMarksAnalysisFailedWithSafeRetryMessage() throws Exception {
        Screenshot screenshot = new Screenshot();
        screenshot.setId(5L);
        when(screenshotRepository.findById(5L)).thenReturn(Optional.of(screenshot));
        when(screenshotRepository.save(any(Screenshot.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IOException("private local path" )).when(perceptualHash).getHashHex(screenshot);

        service.analyzeScreenshot(5L);

        assertEquals(Screenshot.Status.FAILED, screenshot.getStatus());
        assertEquals("Analysis failed. Check server diagnostics and retry.", screenshot.getErrorMessage());
        verify(screenshotRepository, times(2)).save(screenshot);
        verifyNoInteractions(metadataAnalyzer, errorLevelAnalysis, tamperHeuristics,
                ocrAndEvidenceExtractor, analysisResultRepository);
    }
}
