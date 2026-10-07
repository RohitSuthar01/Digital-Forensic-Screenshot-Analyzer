package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class TamperHeuristics {

    /**
     * Analyze tampering heuristics for a screenshot.
     *
     * @param screenshot the screenshot entity
     * @return a map of tampering heuristic results
     * @throws IOException if an error occurs during image processing
     */
    public Map<String, Object> analyze(Screenshot screenshot) throws IOException {
        Map<String, Object> result = new HashMap<>();
        File file = new File(screenshot.getFilePath());
        if (!file.exists()) {
            result.put("error", "File not found");
            return result;
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            result.put("error", "Could not read image");
            return result;
        }

        // We'll implement a few simple heuristics:
        // 1. Noise inconsistency (by dividing the image into blocks and comparing noise levels)
        // 2. Compression grid mismatch (for JPEG, we can check for grid artifacts)
        // 3. Duplicated region (copy-move) by block matching (simplified)
        // 4. Abnormal edge sharpness (by comparing edge strength in blocks)

        // For simplicity, we'll return placeholder values and implement a few basic checks.

        // 1. Noise inconsistency (placeholder)
        double noiseInconsistency = calculateNoiseInconsistency(image);
        result.put("noise_inconsistency", noiseInconsistency);

        // 2. Compression grid mismatch (placeholder)
        double compressionGridMismatch = calculateCompressionGridMismatch(image);
        result.put("compression_grid_mismatch", compressionGridMismatch);

        // 3. Duplicated region (placeholder)
        double duplicatedRegionScore = calculateDuplicatedRegionScore(image);
        result.put("duplicated_region_score", duplicatedRegionScore);

        // 4. Abnormal edge sharpness (placeholder)
        double abnormalEdgeSharpness = calculateAbnormalEdgeSharpness(image);
        result.put("abnormal_edge_sharpness", abnormalEdgeSharpness);

        // Overall tampering likelihood (we can combine these scores)
        double tamperingLikelihood = (noiseInconsistency + compressionGridMismatch + duplicatedRegionScore + abnormalEdgeSharpness) / 4.0;
        result.put("tampering_likelihood", tamperingLikelihood);

        return result;
    }

    private double calculateNoiseInconsistency(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width < 32 || height < 32) return 0.0;
        
        long totalDiff = 0;
        int count = 0;
        for (int y = 0; y < height - 1; y += 4) {
            for (int x = 0; x < width - 1; x += 4) {
                int p1 = image.getRGB(x, y) & 0xFF;
                int p2 = image.getRGB(x+1, y+1) & 0xFF;
                totalDiff += Math.abs(p1 - p2);
                count++;
            }
        }
        double avgNoise = (double) totalDiff / count;
        // Map typical average noise (e.g. 0 to 20) to 0.0-1.0 score where extreme high or low could be suspicious,
        // but for a heuristic, let's just return a bounded value
        return Math.min(1.0, avgNoise / 50.0);
    }

    private double calculateCompressionGridMismatch(BufferedImage image) {
        // Simple 8x8 block boundary check for JPEG artifacts
        int width = image.getWidth();
        int height = image.getHeight();
        if (width < 16 || height < 16) return 0.0;
        
        long boundaryDiff = 0;
        long nonBoundaryDiff = 0;
        int boundaryCount = 0;
        int nonBoundaryCount = 0;
        
        for (int y = 0; y < height - 1; y++) {
            for (int x = 0; x < width - 1; x++) {
                int p1 = image.getRGB(x, y) & 0xFF;
                int p2 = image.getRGB(x+1, y) & 0xFF;
                int diff = Math.abs(p1 - p2);
                if (x % 8 == 7) {
                    boundaryDiff += diff;
                    boundaryCount++;
                } else {
                    nonBoundaryDiff += diff;
                    nonBoundaryCount++;
                }
            }
        }
        
        double avgBoundary = boundaryCount > 0 ? (double) boundaryDiff / boundaryCount : 0;
        double avgNonBoundary = nonBoundaryCount > 0 ? (double) nonBoundaryDiff / nonBoundaryCount : 0;
        
        // If boundary diff is significantly higher, indicates heavy JPEG blocking
        if (avgNonBoundary == 0) return 0.0;
        double ratio = avgBoundary / avgNonBoundary;
        if (ratio < 1.0) return 0.0;
        return Math.min(1.0, (ratio - 1.0) / 2.0);
    }

    private double calculateDuplicatedRegionScore(BufferedImage image) {
        // Simplified check: usually very complex. We return 0.1 as a safe baseline instead of random.
        // A real copy-move detection requires robust hashing of blocks.
        return 0.1;
    }

    private double calculateAbnormalEdgeSharpness(BufferedImage image) {
        // High contrast edge ratio
        int width = image.getWidth();
        int height = image.getHeight();
        long edgePixels = 0;
        
        for (int y = 1; y < height - 1; y += 2) {
            for (int x = 1; x < width - 1; x += 2) {
                int p = image.getRGB(x, y) & 0xFF;
                int right = image.getRGB(x+1, y) & 0xFF;
                int down = image.getRGB(x, y+1) & 0xFF;
                if (Math.abs(p - right) > 50 || Math.abs(p - down) > 50) {
                    edgePixels++;
                }
            }
        }
        
        double edgeRatio = (double) edgePixels / ((width/2) * (height/2));
        // Unusually high amount of sharp edges could indicate text overlay or synthetic elements
        return Math.min(1.0, edgeRatio * 2.0);
    }
}