package com.dfsa.forensic;

import com.dfsa.model.Screenshot;
import net.sourceforge.tess4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OCRAndEvidenceExtractor {

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    /**
     * Perform OCR and extract evidence from a screenshot.
     *
     * @param screenshot the screenshot entity
     * @return a map containing the OCR text and extracted evidence (URLs, emails, phone numbers, etc.)
     * @throws IOException if an error occurs during image processing
     * @throws TesseractException if an error occurs during OCR
     */
    public Map<String, Object> analyze(Screenshot screenshot) throws IOException, TesseractException {
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

        // Initialize Tesseract
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(new File("tessdata").getAbsolutePath());
        tesseract.setLanguage("eng");

        // Do OCR
        String ocrText = tesseract.doOCR(image);

        // Extract evidence using regex
        List<String> urls = extractUrls(ocrText);
        List<String> emails = extractEmails(ocrText);
        List<String> phoneNumbers = extractPhoneNumbers(ocrText);
        List<String> ipAddresses = extractIPAddresses(ocrText);
        List<String> amounts = extractAmounts(ocrText);
        List<String> dates = extractDates(ocrText);

        result.put("ocr_text", ocrText);
        result.put("urls", urls);
        result.put("emails", emails);
        result.put("phone_numbers", phoneNumbers);
        result.put("ip_addresses", ipAddresses);
        result.put("amounts", amounts);
        result.put("dates", dates);

        return result;
    }

    private List<String> extractUrls(String text) {
        List<String> result = new ArrayList<>();
        // Simple URL regex (can be improved)
        String urlRegex = "\\b(?:https?://|www\\.)[^\\s]+\\b";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private List<String> extractEmails(String text) {
        List<String> result = new ArrayList<>();
        String emailRegex = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b";
        Pattern pattern = Pattern.compile(emailRegex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private List<String> extractPhoneNumbers(String text) {
        List<String> result = new ArrayList<>();
        // Simple phone number regex (can be improved for different formats)
        String phoneRegex = "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private List<String> extractIPAddresses(String text) {
        List<String> result = new ArrayList<>();
        String ipRegex = "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b";
        Pattern pattern = Pattern.compile(ipRegex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String ip = matcher.group();
            // Validate each octet is between 0 and 255
            if (isValidIP(ip)) {
                result.add(ip);
            }
        }
        return result;
    }

    private boolean isValidIP(String ip) {
        String[] octets = ip.split("\\.");
        if (octets.length != 4) {
            return false;
        }
        try {
            for (String octet : octets) {
                int val = Integer.parseInt(octet);
                if (val < 0 || val > 255) {
                    return false;
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private List<String> extractAmounts(String text) {
        List<String> result = new ArrayList<>();
        // Simple amount regex (with optional currency symbols and commas)
        String amountRegex = "(?:\\$|€|£)?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})";
        Pattern pattern = Pattern.compile(amountRegex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }

    private List<String> extractDates(String text) {
        List<String> result = new ArrayList<>();
        // Simple date regex (MM/DD/YYYY or DD/MM/YYYY or YYYY-MM-DD)
        String dateRegex = "\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b";
        Pattern pattern = Pattern.compile(dateRegex);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            result.add(matcher.group());
        }
        return result;
    }
}