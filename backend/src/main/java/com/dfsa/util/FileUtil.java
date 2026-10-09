package com.dfsa.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.stereotype.Component;

@Component
public class FileUtil {

    // Magic bytes for file type verification (first few bytes)
    private static final byte[] PNG_HEADER = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] JPEG_HEADER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] BMP_HEADER = {(byte) 0x42, 0x4D}; // BM
    private static final long MAX_DECODED_PIXELS = 40_000_000L;

    /**
     * Check if the content type is allowed.
     *
     * @param contentType the MIME type from the file upload
     * @return true if allowed, false otherwise
     */
    public static boolean isAllowedContentType(String contentType) {
        return contentType != null && (contentType.equalsIgnoreCase("image/png")
                || contentType.equalsIgnoreCase("image/jpeg")
                || contentType.equalsIgnoreCase("image/jpg")
                || contentType.equalsIgnoreCase("image/bmp"));
    }

    /**
     * Verify the file type by checking the magic bytes.
     *
     * @param file the file to check
     * @return true if the file type matches one of the allowed types, false otherwise
     * @throws IOException if an error occurs while reading the file
     */
    public static boolean isAllowedFileType(File file) throws IOException {
        if (getImageFormat(file) == null) return false;
        try (ImageInputStream input = ImageIO.createImageInputStream(file)) {
            if (input == null) return false;
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) return false;
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || (long) width * height > MAX_DECODED_PIXELS) return false;
                return reader.read(0) != null;
            } finally {
                reader.dispose();
            }
        } catch (RuntimeException | javax.imageio.IIOException e) {
            return false;
        }
    }

    /** Returns a format derived from the signature, for formats supported by ImageIO. */
    public static String getImageFormat(File file) throws IOException {
        byte[] header = new byte[8];
        try (InputStream input = new FileInputStream(file)) {
            if (input.read(header) < 8) return null;
        }
        if (Arrays.equals(Arrays.copyOfRange(header, 0, PNG_HEADER.length), PNG_HEADER)) return "png";
        if (Arrays.equals(Arrays.copyOfRange(header, 0, JPEG_HEADER.length), JPEG_HEADER)) return "jpeg";
        if (Arrays.equals(Arrays.copyOfRange(header, 0, BMP_HEADER.length), BMP_HEADER)) return "bmp";
        return null;
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
