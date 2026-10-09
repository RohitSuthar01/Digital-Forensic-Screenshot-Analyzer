package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ErrorLevelAnalysisTest {
    @TempDir Path tempDir;

    @Test
    void createsElaForJpegAndReturnsNotApplicableForPng() throws Exception {
        ErrorLevelAnalysis ela = new ErrorLevelAnalysis();
        ReflectionTestUtils.setField(ela, "uploadDir", tempDir.toString());
        BufferedImage image = new BufferedImage(24, 24, BufferedImage.TYPE_INT_RGB);
        Path jpeg = tempDir.resolve("photo.jpg");
        Path png = tempDir.resolve("photo.png");
        ImageIO.write(image, "jpeg", jpeg.toFile());
        ImageIO.write(image, "png", png.toFile());

        Screenshot jpegEvidence = screenshot(jpeg);
        String output = ela.analyze(jpegEvidence);
        assertNotNull(output);
        assertTrue(Files.exists(tempDir.resolve(output)));
        assertNull(ela.analyze(screenshot(png)));
    }

    private Screenshot screenshot(Path path) {
        Screenshot screenshot = new Screenshot();
        screenshot.setFilePath(path.toString());
        screenshot.setOriginalFilename("user-provided-name");
        return screenshot;
    }
}
