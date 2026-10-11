package com.dfsa.forensic;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LsbSteganographyAnalyzerTest {
    @TempDir Path tempDir;

    @Test
    void extractsReadableSequentialRedChannelMessage() throws Exception {
        String message = "secret note";
        byte[] payload = (message + '\0').getBytes(java.nio.charset.StandardCharsets.UTF_8);
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) image.setRGB(x, y, 0x00808080);
        }

        int bitPosition = 0;
        for (byte value : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                int x = bitPosition % image.getWidth();
                int y = bitPosition / image.getWidth();
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xFE | ((value >> bit) & 1);
                image.setRGB(x, y, (red << 16) | (pixel & 0xFFFF));
                bitPosition++;
            }
        }

        Path imagePath = tempDir.resolve("lsb-message.png");
        ImageIO.write(image, "png", imagePath.toFile());
        Map<String, Object> result = new LsbSteganographyAnalyzer().analyze(imagePath.toFile());

        assertEquals(Boolean.TRUE, result.get("messageDetected"));
        assertEquals(message, result.get("extractedText"));
        assertEquals("red channel", result.get("channelPattern"));
        assertTrue(((String) result.get("limitation")).contains("cannot reliably detect"));
    }

    @Test
    void doesNotClaimMessageForBlankLsbData() throws Exception {
        BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);
        Path imagePath = tempDir.resolve("no-message.png");
        ImageIO.write(image, "png", imagePath.toFile());

        Map<String, Object> result = new LsbSteganographyAnalyzer().analyze(imagePath.toFile());

        assertEquals(Boolean.FALSE, result.get("messageDetected"));
        assertFalse(result.containsKey("extractedText"));
    }
}
