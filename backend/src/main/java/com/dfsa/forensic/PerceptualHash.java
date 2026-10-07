package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Service
public class PerceptualHash {

    /**
     * Compute the dHash (difference hash) of an image.
     * The dHash is computed by:
     * 1. Resizing the image to 9x8 (or 8x8, but we use 9x8 to compute differences between adjacent columns)
     * 2. Converting to grayscale
     * 3. For each row, compare adjacent pixels: if the left pixel is greater than the right pixel, set bit to 1, else 0.
     * 4. Compute the hash as a 64-bit integer (8 rows * 8 bits per row = 64 bits).
     *
     * @param screenshot the screenshot entity
     * @return a 64-bit integer representing the hash, or -1 if an error occurs
     * @throws IOException if an error occurs during image processing
     */
    public long computeHash(Screenshot screenshot) throws IOException {
        File file = new File(screenshot.getFilePath());
        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new IOException("Could not read image: " + file.getAbsolutePath());
        }

        // Step 1: Resize to 9x8 (width 9, height 8)
        BufferedImage resized = new BufferedImage(9, 8, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, 9, 8, null);
        g.dispose();

        // Step 2: Compute the hash
        long hash = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int leftPixel = resized.getRGB(x, y) & 0xFF; // grayscale value
                int rightPixel = resized.getRGB(x + 1, y) & 0xFF;
                int bit = (leftPixel > rightPixel) ? 1 : 0;
                // We are building the hash from left to right, top to bottom.
                // Position: (y * 8) + x
                hash = (hash << 1) | bit;
            }
        }
        return hash;
    }

    /**
     * Compute the Hamming distance between two 64-bit hashes.
     *
     * @param hash1 first hash
     * @param hash2 second hash
     * @return the number of differing bits
     */
    public int hammingDistance(long hash1, long hash2) {
        long xor = hash1 ^ hash2;
        int distance = 0;
        while (xor != 0) {
            distance += xor & 1;
            xor >>>= 1;
        }
        return distance;
    }

    /**
     * Find near-duplicate screenshots for a given screenshot in the same user's cases.
     * We'll leave the implementation to the AnalysisService that will call this method for each screenshot.
     * This method just computes the hash.
     *
     * @param screenshot the screenshot to compute hash for
     * @return the hash as a hexadecimal string (16 hex characters for 64 bits)
     * @throws IOException if an error occurs
     */
    public String getHashHex(Screenshot screenshot) throws IOException {
        long hash = computeHash(screenshot);
        return String.format("%016x", hash);
    }
}