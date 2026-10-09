package com.dfsa.forensic;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Looks for readable text encoded in common sequential, least-significant-bit
 * image-channel patterns. This is a screening heuristic, not a general-purpose
 * steganography detector.
 */
@Service
public class LsbSteganographyAnalyzer {
    private static final int MAX_EXTRACTED_BYTES = 64 * 1024;
    private static final String LIMITATION = "Scans sequential MSB-first LSB text patterns in RGB-interleaved and individual color channels. It cannot reliably detect encrypted, compressed, non-text, length-prefixed, randomized, or non-sequential payloads; absence of a finding does not prove the image has no hidden data.";

    public Map<String, Object> analyze(File file) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "COMPLETED");
        result.put("messageDetected", false);
        result.put("method", "Sequential LSB printable-text scan (MSB-first)");
        result.put("limitation", LIMITATION);

        final BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            result.put("status", "UNAVAILABLE");
            result.put("reason", "Image could not be decoded for LSB analysis.");
            return result;
        }
        if (image == null) {
            result.put("status", "UNAVAILABLE");
            result.put("reason", "Image could not be decoded for LSB analysis.");
            return result;
        }

        Candidate best = null;
        for (ChannelMode mode : ChannelMode.values()) {
            Candidate candidate = extractCandidate(image, mode);
            if (candidate != null && candidate.isReadable() && (best == null || candidate.score > best.score)) {
                best = candidate;
            }
        }

        if (best == null) {
            result.put("finding", "No high-confidence readable text was found in the supported sequential LSB patterns.");
        } else {
            result.put("messageDetected", true);
            result.put("channelPattern", best.mode.label);
            result.put("extractedText", best.text);
            result.put("finding", "A readable text-like payload was found in a common sequential LSB pattern. Review it as a potential hidden message; this is not proof of malicious activity or authorship.");
        }
        return result;
    }

    private Candidate extractCandidate(BufferedImage image, ChannelMode mode) {
        long availableBits = (long) image.getWidth() * image.getHeight() * mode.channelsPerPixel;
        int limit = (int) Math.min(availableBits, MAX_EXTRACTED_BYTES * 8L);
        if (limit < 48) return null;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.min(limit / 8, MAX_EXTRACTED_BYTES));
        int currentByte = 0;
        int bitsInByte = 0;
        int consumedBits = 0;
        outer:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int pixel = image.getRGB(x, y);
                for (int channelIndex = 0; channelIndex < mode.channelsPerPixel; channelIndex++) {
                    int channel = mode.channelAt(channelIndex);
                    int shift = channel == 0 ? 16 : channel == 1 ? 8 : 0;
                    currentByte = (currentByte << 1) | ((pixel >> shift) & 1);
                    bitsInByte++;
                    consumedBits++;
                    if (bitsInByte == 8) {
                        if (currentByte == 0) break outer;
                        bytes.write(currentByte);
                        currentByte = 0;
                        bitsInByte = 0;
                        if (bytes.size() >= MAX_EXTRACTED_BYTES) break outer;
                    }
                    if (consumedBits >= limit) break outer;
                }
            }
        }

        String text = decodeUtf8(bytes.toByteArray());
        if (text == null || text.length() < 6) return null;
        int readable = 0;
        int useful = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (!Character.isISOControl(ch) || Character.isWhitespace(ch)) readable++;
            if (Character.isLetterOrDigit(ch)) useful++;
        }
        double score = (double) readable / text.length();
        return new Candidate(mode, text, score, useful);
    }

    private String decodeUtf8(byte[] bytes) {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            return null;
        }
    }

    private enum ChannelMode {
        RGB("RGB-interleaved", 3, new int[]{0, 1, 2}),
        RED("red channel", 1, new int[]{0}),
        GREEN("green channel", 1, new int[]{1}),
        BLUE("blue channel", 1, new int[]{2});

        private final String label;
        private final int channelsPerPixel;
        private final int[] channels;

        ChannelMode(String label, int channelsPerPixel, int[] channels) {
            this.label = label;
            this.channelsPerPixel = channelsPerPixel;
            this.channels = channels;
        }

        int channelAt(int index) { return channels[index]; }
    }

    private static final class Candidate {
        private final ChannelMode mode;
        private final String text;
        private final double score;
        private final int usefulCharacters;

        private Candidate(ChannelMode mode, String text, double score, int usefulCharacters) {
            this.mode = mode;
            this.text = text;
            this.score = score;
            this.usefulCharacters = usefulCharacters;
        }

        private boolean isReadable() {
            return score >= 0.95 && usefulCharacters >= 4;
        }
    }
}
