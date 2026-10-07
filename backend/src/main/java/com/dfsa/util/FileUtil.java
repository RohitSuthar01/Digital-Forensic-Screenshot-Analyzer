package com.dfsa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FileUtil {

    // Allowed MIME types for screenshots
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/webp",
            "image/bmp"
    );

    // Magic bytes for file type verification (first few bytes)
    private static final byte[] PNG_HEADER = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_HEADER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] WEBP_HEADER = {(byte) 0x52, 0x49, 0x46, 0x46}; // RIFF
    private static final byte[] BMP_HEADER = {(byte) 0x42, 0x4D}; // BM

    /**
     * Check if the content type is allowed.
     *
     * @param contentType the MIME type from the file upload
     * @return true if allowed, false otherwise
     */
    public static boolean isAllowedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
    }

    /**
     * Verify the file type by checking the magic bytes.
     *
     * @param file the file to check
     * @return true if the file type matches one of the allowed types, false otherwise
     * @throws IOException if an error occurs while reading the file
     */
    public static boolean isAllowedFileType(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            // Read the first 8 bytes (enough for all our checks)
            byte[] header = new byte[8];
            int bytesRead = is.read(header);
            if (bytesRead < 8) {
                return false; // File too small
            }

            // Check for PNG
            if (Arrays.equals(Arrays.copyOfRange(header, 0, PNG_HEADER.length), PNG_HEADER)) {
                return true;
            }

            // Check for JPEG
            if (Arrays.equals(Arrays.copyOfRange(header, 0, JPEG_HEADER.length), JPEG_HEADER)) {
                return true;
            }

            // Check for WEBP (first 4 bytes are RIFF, then we need to check for WEBP at offset 8)
            if (Arrays.equals(Arrays.copyOfRange(header, 0, 4), WEBP_HEADER)) {
                // We need to read a few more bytes to check for WEBP
                // Actually, the full header for WEBP is RIFF....WEBP
                // We'll read 12 bytes total and check for WEBP at offset 8
                byte[] more = new byte[4];
                is.read(more);
                if (Arrays.equals(more, new byte[]{0x57, 0x45, 0x42, 0x50})) { // WEBP
                    return true;
                }
                // If not WEBP, we can still check for other types? We'll just return false for now.
                // But note: we already read 12 bytes, we don't want to waste more.
                // We'll just return false if not WEBP.
                return false;
            }

            // Check for BMP
            if (Arrays.equals(Arrays.copyOfRange(header, 0, BMP_HEADER.length), BMP_HEADER)) {
                return true;
            }

            return false;
        }
    }

    /**
     * Calculate the MD5 checksum of a file.
     *
     * @param file the file to checksum
     * @return the MD5 checksum as a hexadecimal string
     * @throws IOException if an error occurs while reading the file
     * @throws NoSuchAlgorithmException if MD5 algorithm is not available
     */
    public static String getMD5Checksum(File file) throws IOException, NoSuchAlgorithmException {
        return getFileChecksum(file, "MD5");
    }

    /**
     * Calculate the SHA-256 checksum of a file.
     *
     * @param file the file to checksum
     * @return the SHA-256 checksum as a hexadecimal string
     * @throws IOException if an error occurs while reading the file
     * @throws NoSuchAlgorithmException if SHA-256 algorithm is not available
     */
    public static String getSHA256Checksum(File file) throws IOException, NoSuchAlgorithmException {
        return getFileChecksum(file, "SHA-256");
    }

    /**
     * Calculate the checksum of a file using the specified algorithm.
     *
     * @param file the file to checksum
     * @param algorithm the algorithm to use (e.g., "MD5", "SHA-256")
     * @return the checksum as a hexadecimal string
     * @throws IOException if an error occurs while reading the file
     * @throws NoSuchAlgorithmException if the algorithm is not available
     */
    private static String getFileChecksum(File file, String algorithm) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        byte[] hashBytes = digest.digest();
        return bytesToHex(hashBytes);
    }

    /**
     * Convert a byte array to a hexadecimal string.
     *
     * @param bytes the byte array to convert
     * @return the hexadecimal string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}