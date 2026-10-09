package com.dfsa.forensic;

import com.dfsa.model.AnalysisResult;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticityScorerTest {
    private final AuthenticityScorer scorer = new AuthenticityScorer();

    @Test
    void missingMetadataAndElaDoNotBecomeAuthenticEvidence() {
        Map<String, Object> metadata = Map.of("metadataStatus", "UNAVAILABLE", "editingSoftwareSignatures", List.of());
        Map<String, Object> result = scorer.computeScore(metadata, Map.of("status", "NOT_ASSESSED"), new HashMap<>(), true);
        assertEquals(AnalysisResult.AuthenticityVerdict.INCONCLUSIVE, result.get("verdict"));
        assertEquals(50, result.get("score"));
        assertTrue(result.get("explanation").toString().contains("not a verdict signal"));
    }

    @Test
    void editorSignatureIsReportedAsAnIndicatorRatherThanProof() {
        Map<String, Object> metadata = Map.of("hasEditingSoftware", true,
                "editingSoftwareSignatures", List.of("Software: ExampleEditor"));
        Map<String, Object> result = scorer.computeScore(metadata, null, null, false);
        assertEquals(AnalysisResult.AuthenticityVerdict.SUSPICIOUS, result.get("verdict"));
        assertTrue(result.get("explanation").toString().contains("not proof"));
    }
}
