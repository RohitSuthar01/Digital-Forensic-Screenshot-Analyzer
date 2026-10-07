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
        int score = 100; // Start with perfect score and deduct for issues
        StringBuilder explanation = new StringBuilder();
        
        // 1. Metadata analysis (Weight: 20)
        if (metadataResult != null) {
            Boolean hasEditingSoftware = (Boolean) metadataResult.get("hasEditingSoftware");
            if (hasEditingSoftware != null && hasEditingSoftware) {
                score -= 20; // Moderate deduction
                explanation.append("Metadata contains signatures of editing software. ");
                @SuppressWarnings("unchecked")
                List<String> signatures = (List<String>) metadataResult.get("editingSoftwareSignatures");
                if (signatures != null && !signatures.isEmpty()) {
                    explanation.append("Software found: ").append(String.join(", ", signatures)).append(". ");
                }
            } else {
                explanation.append("No common editing software signatures found in metadata. ");
            }
        }

        // 2. Tamper heuristics (Weight: 40)
        // Combines noise inconsistency, grid mismatch, sharp edges, and duplicated regions
        if (tamperResult != null) {
            Double tamperLikelihood = (Double) tamperResult.get("tampering_likelihood");
            if (tamperLikelihood != null) {
                int deduct = (int) Math.round(tamperLikelihood * 40);
                score -= deduct;
                if (deduct > 5) {
                    explanation.append("Tampering heuristics strongly suggest manipulation (likelihood: ")
                            .append(String.format("%.2f", tamperLikelihood)).append("). ");
                } else {
                    explanation.append("Tampering heuristics are normal. ");
                }
            }
        }

        // 3. ELA Analysis (Weight: 20)
        // If ELA failed despite it being a valid image, we note it.
        // If it succeeded, we assume there's an ELA image. 
        // Real ELA interpretation needs human review or ML model, but for now we note its status.
        if (!elaAvailable) {
            explanation.append("ELA could not be generated (possible unsupported format or corruption). ");
            score -= 5; // small penalty for opaque/unsupported formats that resist ELA
        } else {
            explanation.append("ELA image generated successfully for human review. ");
        }
        // 4. OCR and evidence extraction: we don't deduct for OCR, but we can note if OCR failed.
        if (ocrResult != null && ocrResult.containsKey("error")) {
            explanation.append("OCR encountered an error: ").append(ocrResult.get("error")).append(". ");
        }

        // Ensure score is within bounds
        if (score < 0) {
            score = 0;
        }
        if (score > 100) {
            score = 100;
        }

        // Determine verdict based on score
        AuthenticityVerdict verdict;
        if (score >= 90) {
            verdict = AuthenticityVerdict.AUTHENTIC;
        } else if (score >= 70) {
            verdict = AuthenticityVerdict.SUSPICIOUS;
        } else {
            verdict = AuthenticityVerdict.LIKELY_TAMPERED;
        }

        // Build the result map
        Map<String, Object> result = new HashMap<>();
        result.put("score", score);
        result.put("verdict", verdict);
        result.put("explanation", explanation.toString().trim());

        return result;
    }
}