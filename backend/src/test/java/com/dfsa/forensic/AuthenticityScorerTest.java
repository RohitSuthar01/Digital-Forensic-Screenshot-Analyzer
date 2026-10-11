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
        assertNull(result.get("score"));
        assertTrue(result.get("explanation").toString().contains("not a verdict signal"));
    }

    @Test
    void metadataWithoutEditorSignatureAndElaDoNotCreateAuthenticityVerdictOrScore() {
        Map<String, Object> metadata = Map.of("metadataStatus", "AVAILABLE",
                "hasEditingSoftware", false, "editingSoftwareSignatures", List.of());

        Map<String, Object> withEla = scorer.computeScore(metadata, Map.of("status", "NOT_ASSESSED"), Map.of(), true);
        Map<String, Object> withoutEla = scorer.computeScore(metadata, Map.of("status", "NOT_ASSESSED"), Map.of(), false);

        assertEquals(AnalysisResult.AuthenticityVerdict.INCONCLUSIVE, withEla.get("verdict"));
        assertEquals(AnalysisResult.AuthenticityVerdict.INCONCLUSIVE, withoutEla.get("verdict"));
        assertNull(withEla.get("score"));
        assertNull(withoutEla.get("score"));
    }

    @Test
    void editorSignatureIsReportedAsAnIndicatorRatherThanProof() {
        Map<String, Object> metadata = Map.of("hasEditingSoftware", true,
                "editingSoftwareSignatures", List.of("Software: ExampleEditor"));
        Map<String, Object> result = scorer.computeScore(metadata, null, null, false);
        assertEquals(AnalysisResult.AuthenticityVerdict.SUSPICIOUS, result.get("verdict"));
        assertNull(result.get("score"));
        assertTrue(result.get("explanation").toString().contains("not proof"));
    }
}
