package com.dfsa.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "extracted_evidence", indexes = {
        @Index(columnList = "screenshot_id")
})
public class ExtractedEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "screenshot_id", nullable = false)
    private com.dfsa.model.Screenshot screenshot;

    // Evidence types extracted by OCR and regex
    private String url;           // URLs found
    private String email;         // Email addresses found
    private String phoneNumber;   // Phone numbers found
    private String ipAddress;     // IP addresses found
    private String amount;        // Monetary amounts found
    private String date;          // Dates found

    // Additional metadata
    private String evidenceType;  // Type of evidence (URL, EMAIL, PHONE, etc.)
    private String confidence;    // Confidence level of extraction
    private String rawText;       // Original text where evidence was found

    @Temporal(TemporalType.TIMESTAMP)
    private Date extractedAt;

    // Constructors
    public ExtractedEvidence() {
        this.extractedAt = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public com.dfsa.model.Screenshot getScreenshot() { return screenshot; }
    public void setScreenshot(com.dfsa.model.Screenshot screenshot) { this.screenshot = screenshot; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getEvidenceType() { return evidenceType; }
    public void setEvidenceType(String evidenceType) { this.evidenceType = evidenceType; }

    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public Date getExtractedAt() { return extractedAt; }
    public void setExtractedAt(Date extractedAt) { this.extractedAt = extractedAt; }
}
