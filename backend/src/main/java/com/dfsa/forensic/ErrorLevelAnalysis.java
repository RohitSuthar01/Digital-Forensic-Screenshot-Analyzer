package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import com.dfsa.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ErrorLevelAnalysis {

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    /**
     * Perform Error Level Analysis on a screenshot.
     *
     * @param screenshot the screenshot entity
     * @return the file name of the generated ELA image (stored in the upload directory), or null if not applicable
     * @throws IOException if an error occurs during image processing
     */
    public String analyze(Screenshot screenshot) throws IOException {
        // Check if the file is a JPEG (based on extension or content type)
        String filePath = screenshot.getFilePath();
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        // ELA only evaluates JPEG recompression differences; converting another format
        // to JPEG first would create a misleading visualization.
        if (!"jpeg".equals(FileUtil.getImageFormat(file))) return null;
        BufferedImage originalImage = ImageIO.read(file);
        if (originalImage == null) {
            throw new IOException("Could not read image: " + filePath);
        }

        // We'll create a temporary file for the re-compressed JPEG
        File tempRecompressed = File.createTempFile("recompressed_", ".jpg");
        try {
            // Convert to RGB if it has alpha channel, to prevent ImageIO from failing to write JPEG
            BufferedImage rgbImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = rgbImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, Color.WHITE, null);
            g2d.dispose();
            originalImage = rgbImage;

            // Re-compress the image at 90% quality (default ImageIO.write uses default quality, which is ~75% usually)
            // But just writing it as JPEG introduces compression artifacts.
            boolean wrote = ImageIO.write(originalImage, "jpeg", tempRecompressed);
            if (!wrote) {
                throw new IOException("No appropriate writer found for format jpeg");
            }

            // Read the re-compressed image
            BufferedImage recompressedImage = ImageIO.read(tempRecompressed);
            if (recompressedImage == null) {
                throw new IOException("Could not read re-compressed image");
            }

            // Ensure both images have the same dimensions
            if (originalImage.getWidth() != recompressedImage.getWidth() ||
                    originalImage.getHeight() != recompressedImage.getHeight()) {
                throw new IOException("Original and re-compressed images have different dimensions");
            }

            // Compute the difference image
            BufferedImage diffImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_BYTE_GRAY);

            Graphics2D g = diffImage.createGraphics();

            int[] histogram = new int[256];

            // We'll compute the absolute difference for each channel and then convert to grayscale
            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    int rgb1 = originalImage.getRGB(x, y);
                    int rgb2 = recompressedImage.getRGB(x, y);

                    int r1 = (rgb1 >> 16) & 0xFF;
                    int g1 = (rgb1 >> 8) & 0xFF;
                    int b1 = rgb1 & 0xFF;

                    int r2 = (rgb2 >> 16) & 0xFF;
                    int g2 = (rgb2 >> 8) & 0xFF;
                    int b2 = rgb2 & 0xFF;

                    int diffR = Math.abs(r1 - r2);
                    int diffG = Math.abs(g1 - g2);
                    int diffB = Math.abs(b1 - b2);

                    // Convert to grayscale
                    int gray = (diffR + diffG + diffB) / 3;
                    histogram[gray]++;
                }
            }

            // Find 99th percentile for robust normalization
            int totalPixels = originalImage.getWidth() * originalImage.getHeight();
            int count = 0;
            int p99 = 255;
            for (int i = 0; i < 256; i++) {
                count += histogram[i];
                if (count >= totalPixels * 0.99) {
                    p99 = i;
                    break;
                }
            }

            // Ensure we don't amplify tiny noise too much
            if (p99 < 5) p99 = 5;
            double scale = 255.0 / p99;

            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    int rgb1 = originalImage.getRGB(x, y);
                    int rgb2 = recompressedImage.getRGB(x, y);
                    int gray = (Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
                            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
                            + Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF))) / 3;
                    int enhanced = (int) Math.min(255, gray * scale);
                    int grayRGB = (enhanced << 16) | (enhanced << 8) | enhanced;
                    diffImage.setRGB(x, y, grayRGB);
                }
            }

            g.dispose();

            // Save the ELA image to the upload directory
            String elaFileName = "ela_" + UUID.randomUUID().toString() + ".png";
            File elaFile = new File(uploadDir, elaFileName);
            ImageIO.write(diffImage, "png", elaFile);

            return elaFileName;
        } finally {
            // Delete the temporary file
            if (tempRecompressed.exists()) {
                tempRecompressed.delete();
            }
        }
    }
}
