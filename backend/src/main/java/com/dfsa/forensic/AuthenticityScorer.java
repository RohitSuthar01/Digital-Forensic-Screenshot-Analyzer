package com.dfsa.forensic;

import org.springframework.stereotype.Service;
import com.dfsa.model.AnalysisResult.AuthenticityVerdict;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
public class AuthenticityScorer {

    /**
     * Compute the authenticity score and verdict based on the results from various forensic analyses.
     *
     * @param metadataResult     result from metadata analysis
     * @param tamperResult       result from tamper heuristics
     * @param ocrResult          result from OCR and evidence extraction
     * @param elaAvailable       whether an ELA image was generated (indicating the image was JPEG and ELA could be performed)
     * @return a map containing the score (0-100), verdict, and explanation
     */
    public Map<String, Object> computeScore(Map<String, Object> metadataResult,
                                            Map<String, Object> tamperResult,
                                            Map<String, Object> ocrResult,
                                            boolean elaAvailable) {
        int score = 50;
        StringBuilder explanation = new StringBuilder();
        boolean editorSignature = metadataResult != null && Boolean.TRUE.equals(metadataResult.get("hasEditingSoftware"));
        @SuppressWarnings("unchecked")
        List<String> signatures = metadataResult == null ? List.of() :
                (List<String>) metadataResult.getOrDefault("editingSoftwareSignatures", List.of());
        AuthenticityVerdict verdict = AuthenticityVerdict.INCONCLUSIVE;
        if (editorSignature && !signatures.isEmpty()) {
            verdict = AuthenticityVerdict.SUSPICIOUS;
            score = 35;
            explanation.append("Metadata editor signature observed: ").append(String.join(", ", signatures))
                    .append(". This is an indicator for review, not proof that image content was altered. ");
        } else {
            explanation.append("No verified editing indicator was established by the available checks. ");
            if (metadataResult == null || !"AVAILABLE".equals(metadataResult.get("metadataStatus"))) {
                explanation.append("Metadata is unavailable; missing metadata alone is not evidence of editing. ");
            }
            explanation.append("The current heuristic modules cannot determine authenticity or identify an original without trusted reference evidence.");
        }
        if (tamperResult != null && tamperResult.get("limitation") != null) {
            explanation.append(" ").append(tamperResult.get("limitation"));
        }
        if (ocrResult != null && ocrResult.get("error") != null) {
            explanation.append(" OCR module: ").append(ocrResult.get("error")).append(".");
        }
        explanation.append(elaAvailable
                ? " ELA visualization is available for human review only; it is not a verdict signal."
                : " ELA is unavailable or not applicable for this file; it is not a verdict signal.");

        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("verdict", verdict);
        result.put("explanation", explanation.toString().trim());

        return result;
    }
}
