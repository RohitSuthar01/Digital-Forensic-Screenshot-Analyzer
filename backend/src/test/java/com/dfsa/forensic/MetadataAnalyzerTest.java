package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetadataAnalyzerTest {
    @TempDir Path tempDir;

    @Test
    void reportsDecodedFormatColorModeAndResolutionWhenExifIsAbsent() throws Exception {
        Path imagePath = tempDir.resolve("metadata-free.png");
        ImageIO.write(new BufferedImage(32, 24, BufferedImage.TYPE_INT_RGB), "png", imagePath.toFile());
        Screenshot screenshot = new Screenshot();
        screenshot.setFilePath(imagePath.toString());

        Map<String, Object> result = new MetadataAnalyzer().analyze(screenshot);

        assertEquals("png", result.get("format"));
        assertEquals("RGB", result.get("colorMode"));
        assertEquals("32 × 24 pixels", result.get("resolution"));
        assertEquals(32, result.get("width"));
        assertEquals(24, result.get("height"));
    }
}
