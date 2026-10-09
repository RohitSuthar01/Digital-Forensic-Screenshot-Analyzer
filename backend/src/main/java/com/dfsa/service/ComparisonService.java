package com.dfsa.service;

import com.dfsa.model.ComparisonResult;
import com.dfsa.model.Screenshot;
import com.dfsa.repository.ComparisonResultRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ComparisonService {

    @Autowired
    private ComparisonResultRepository comparisonResultRepository;

    @Autowired
    private ScreenshotRepository screenshotRepository;

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    public ComparisonResult compare(Long targetId, Long referenceId) throws Exception {
        if (targetId.equals(referenceId)) {
            throw new IllegalArgumentException("Cannot compare a screenshot with itself");
        }

        Screenshot target = screenshotRepository.findById(targetId).orElseThrow(() -> new IllegalArgumentException("Target not found"));
        Screenshot reference = screenshotRepository.findById(referenceId).orElseThrow(() -> new IllegalArgumentException("Reference not found"));

        if (!target.getTheCase().getId().equals(reference.getTheCase().getId())) {
            throw new IllegalStateException("Screenshots belong to different cases");
        }

        File targetFile = new File(uploadDir, target.getStoredFilename());
        File referenceFile = new File(uploadDir, reference.getStoredFilename());

        if (!targetFile.exists() || !referenceFile.exists()) {
            throw new IllegalArgumentException("One or both screenshot files are missing from storage");
        }

        BufferedImage img1;
        BufferedImage img2;
        try {
            img1 = ImageIO.read(targetFile);
            img2 = ImageIO.read(referenceFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read image files: " + e.getMessage());
        }

        if (img1 == null || img2 == null) {
            throw new IllegalArgumentException("Could not decode one or both images. They may be unsupported or corrupted.");
        }

        ComparisonResult result = comparisonResultRepository.findByTargetScreenshotId(targetId).orElse(new ComparisonResult());
        result.setReferenceScreenshot(reference);
        result.setTargetScreenshot(target);
        result.setMethodVersion("PixelDiff-1.0");
        
        if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
            result.setVisualDifferenceDetected(true);
            result.setChangedRegionsDetails("Images have different dimensions. Target: " + img1.getWidth() + "x" + img1.getHeight() + ", Reference: " + img2.getWidth() + "x" + img2.getHeight() + ". Cannot determine which is original; human review needed.");
            result.setLimitations("Direct pixel comparison is unavailable without alignment. Registration/scaling is required for pixel-level differencing.");
            return comparisonResultRepository.save(result);
        }

        int width = img1.getWidth();
        int height = img1.getHeight();
        BufferedImage diffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        boolean differenceDetected = false;
        int diffPixels = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int p1 = img1.getRGB(x, y);
                int p2 = img2.getRGB(x, y);

                if (p1 != p2) {
                    // Difference!
                    Color c1 = new Color(p1, true);
                    Color c2 = new Color(p2, true);
                    int rDiff = Math.abs(c1.getRed() - c2.getRed());
                    int gDiff = Math.abs(c1.getGreen() - c2.getGreen());
                    int bDiff = Math.abs(c1.getBlue() - c2.getBlue());
                    
                    if (rDiff > 5 || gDiff > 5 || bDiff > 5) {
                        diffPixels++;
                        differenceDetected = true;
                        // Highlight in red on transparent background
                        diffImg.setRGB(x, y, new Color(255, 0, 0, 180).getRGB());
                    } else {
                        // Very minor diff
                        diffImg.setRGB(x, y, new Color(255, 200, 0, 100).getRGB());
                    }
                } else {
                    diffImg.setRGB(x, y, new Color(0, 0, 0, 0).getRGB());
                }
            }
        }

        result.setVisualDifferenceDetected(differenceDetected);
        
        // Analyze individual assessments
        String imageA_Findings = "";
        String imageB_Findings = "";
        
        java.util.Optional<com.dfsa.model.AnalysisResult> targetAnalysis = org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(), com.dfsa.repository.AnalysisResultRepository.class).findByScreenshotId(targetId);
        java.util.Optional<com.dfsa.model.AnalysisResult> refAnalysis = org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors(org.springframework.web.context.ContextLoader.getCurrentWebApplicationContext(), com.dfsa.repository.AnalysisResultRepository.class).findByScreenshotId(referenceId);
        
        boolean targetSus = targetAnalysis.map(a -> a.getVerdict() == com.dfsa.model.AnalysisResult.AuthenticityVerdict.SUSPICIOUS || a.getVerdict() == com.dfsa.model.AnalysisResult.AuthenticityVerdict.LIKELY_TAMPERED).orElse(false);
        boolean refSus = refAnalysis.map(a -> a.getVerdict() == com.dfsa.model.AnalysisResult.AuthenticityVerdict.SUSPICIOUS || a.getVerdict() == com.dfsa.model.AnalysisResult.AuthenticityVerdict.LIKELY_TAMPERED).orElse(false);
        
        String comparisonStatement;
        if (targetSus && !refSus) {
            comparisonStatement = "Target image has indicators consistent with editing. Cannot conclusively verify Reference is the true original based on this alone.";
        } else if (refSus && !targetSus) {
            comparisonStatement = "Reference image has indicators consistent with editing. Cannot conclusively verify Target is the true original based on this alone.";
        } else {
            comparisonStatement = "Cannot determine which is original; human review needed. Neither image provides definitive single-image evidence of being the original over the other.";
        }

        if (differenceDetected) {
            double percent = (diffPixels * 100.0) / (width * height);
            result.setChangedRegionsDetails(String.format("Found %d visually different pixels (%.2f%% of image). %s", diffPixels, percent, comparisonStatement));
            result.setLimitations("Simple pixel-wise comparison is sensitive to recompression/scaling shifts. Note: Never claim that the image with fewer differences or a higher score is certainly the original.");
            
            String diffFileName = "diff_" + UUID.randomUUID().toString() + ".png";
            File diffFile = new File(uploadDir, diffFileName);
            ImageIO.write(diffImg, "png", diffFile);
            result.setDifferenceMapFileName(diffFileName);
        } else {
            result.setChangedRegionsDetails("No visual differences detected (0.00%). " + comparisonStatement);
            result.setLimitations("No differences found at pixel level. Note: Never claim that the image with fewer differences or a higher score is certainly the original.");
        }

        return comparisonResultRepository.save(result);
    }

    public java.util.Optional<ComparisonResult> getComparisonResult(Long targetId) {
        return comparisonResultRepository.findByTargetScreenshotId(targetId);
    }
}
