package com.dfsa.service;

import com.dfsa.dto.AnalysisResultDTO;
import com.dfsa.dto.CaseDTO;
import com.dfsa.dto.ScreenshotDTO;
import com.dfsa.model.*;
import com.dfsa.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private ScreenshotRepository screenshotRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExtractedEvidenceRepository extractedEvidenceRepository;

    @Autowired
    private CaseNoteRepository caseNoteRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    /**
     * Generate a PDF report for a single screenshot.
     *
     * @param screenshotId the ID of the screenshot
     * @return a ByteArrayResource containing the PDF
     * @throws DocumentException if there is an error in the PDF generation
     * @throws IOException if there is an error reading files
     */
    public ByteArrayResource generateScreenshotReport(Long screenshotId) throws DocumentException, IOException {
        // Fetch the screenshot
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found"));

        // Fetch the analysis result
        AnalysisResult analysisResult = analysisResultRepository.findByScreenshotId(screenshotId)
                .orElseThrow(() -> new RuntimeException("Analysis result not found for screenshot"));

        // Fetch the case
        Case theCase = screenshot.getTheCase();

        // Fetch the investigator (user who owns the case)
        User investigator = theCase.getInvestigator();

        // Fetch extracted evidence
        List<ExtractedEvidence> extractedEvidenceList = extractedEvidenceRepository.findByScreenshot_Id(screenshotId);

        // Fetch case notes for this case
        List<CaseNote> caseNotesList = caseNoteRepository.findByTheCase_Id(theCase.getId());

        // Fetch audit logs for this screenshot (optional)
        List<AuditLog> auditLogList = auditLogRepository.findByEntityIdAndEntityType(screenshotId, "Screenshot");

        // Create a PDF document
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        // Set up fonts
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, java.awt.Color.BLACK);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.BLACK);

        // Title
        Paragraph title = new Paragraph("Forensic Screenshot Analysis Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Case Information
        Paragraph caseInfoHeading = new Paragraph("Case Information", headingFont);
        document.add(caseInfoHeading);
        document.add(Chunk.NEWLINE);

        PdfPTable caseInfoTable = new PdfPTable(2);
        caseInfoTable.setWidthPercentage(100);
        caseInfoTable.setSpacingBefore(10f);
        caseInfoTable.setSpacingAfter(10f);

        caseInfoTable.addCell("Case Number:");
        caseInfoTable.addCell(theCase.getCaseNumber());

        caseInfoTable.addCell("Case Title:");
        caseInfoTable.addCell(theCase.getTitle());

        caseInfoTable.addCell("Investigator:");
        caseInfoTable.addCell(investigator.getFirstName() + " " + investigator.getLastName());

        caseInfoTable.addCell("Case Status:");
        caseInfoTable.addCell(theCase.getStatus().toString());

        caseInfoTable.addCell("Case Priority:");
        caseInfoTable.addCell(theCase.getPriority().toString());

        document.add(caseInfoTable);
        document.add(Chunk.NEWLINE);

        // Screenshot Information
        Paragraph screenshotInfoHeading = new Paragraph("Screenshot Information", headingFont);
        document.add(screenshotInfoHeading);
        document.add(Chunk.NEWLINE);

        PdfPTable screenshotInfoTable = new PdfPTable(2);
        screenshotInfoTable.setWidthPercentage(100);
        screenshotInfoTable.setSpacingBefore(10f);
        screenshotInfoTable.setSpacingAfter(10f);

        screenshotInfoTable.addCell("Original Filename:");
        screenshotInfoTable.addCell(screenshot.getOriginalFilename());

        screenshotInfoTable.addCell("Stored Filename:");
        screenshotInfoTable.addCell(screenshot.getStoredFilename());

        screenshotInfoTable.addCell("MD5 Hash:");
        screenshotInfoTable.addCell(screenshot.getMd5());

        screenshotInfoTable.addCell("SHA-256 Hash:");
        screenshotInfoTable.addCell(screenshot.getSha256());

        screenshotInfoTable.addCell("Perceptual Hash (dHash):");
        screenshotInfoTable.addCell(screenshot.getPerceptualHash());

        screenshotInfoTable.addCell("File Size (bytes):");
        screenshotInfoTable.addCell(screenshot.getFileSize().toString());

        screenshotInfoTable.addCell("Uploaded At:");
        screenshotInfoTable.addCell(screenshot.getUploadedAt().toString());

        screenshotInfoTable.addCell("Processed At:");
        screenshotInfoTable.addCell(screenshot.getProcessedAt() != null ? screenshot.getProcessedAt().toString() : "N/A");

        document.add(screenshotInfoTable);
        document.add(Chunk.NEWLINE);

        // Analysis Results
        Paragraph analysisHeading = new Paragraph("Analysis Results", headingFont);
        document.add(analysisHeading);
        document.add(Chunk.NEWLINE);

        PdfPTable analysisTable = new PdfPTable(2);
        analysisTable.setWidthPercentage(100);
        analysisTable.setSpacingBefore(10f);
        analysisTable.setSpacingAfter(10f);

        analysisTable.addCell("Authenticity Score:");
        analysisTable.addCell(analysisResult.getAuthenticityScore().toString() + "/100");

        analysisTable.addCell("Verdict:");
        analysisTable.addCell(analysisResult.getVerdict().toString());

        analysisTable.addCell("Explanation:");
        analysisTable.addCell(analysisResult.getVerdictExplanation());

        analysisTable.addCell("Analyzed At:");
        analysisTable.addCell(analysisResult.getAnalyzedAt().toString());

        document.add(analysisTable);
        document.add(Chunk.NEWLINE);

        // ELA Image (if available)
        if (analysisResult.getElaImageFileName() != null && !analysisResult.getElaImageFileName().isEmpty()) {
            Paragraph elaHeading = new Paragraph("Error Level Analysis (ELA) Image", headingFont);
            document.add(elaHeading);
            document.add(Chunk.NEWLINE);

            File elaFile = new File(uploadDir, analysisResult.getElaImageFileName());
            if (elaFile.exists()) {
                try {
                    Image elaImage = Image.getInstance(elaFile.getAbsolutePath());
                    // Scale the image to fit within the page width (with margins)
                    float width = elaImage.getScaledWidth();
                    float height = elaImage.getScaledHeight();
                    float maxWidth = PageSize.A4.getWidth() - 100; // 50pt margin on each side
                    if (width > maxWidth) {
                        elaImage.scalePercent((maxWidth / width) * 100);
                    }
                    document.add(elaImage);
                } catch (BadElementException e) {
                    // If the image cannot be added, we'll just note that
                    Paragraph elaError = new Paragraph("Could not load ELA image: " + e.getMessage(), normalFont);
                    document.add(elaError);
                }
            } else {
                Paragraph elaError = new Paragraph("ELA image file not found.", normalFont);
                document.add(elaError);
            }
            document.add(Chunk.NEWLINE);
        }

        // Extracted Evidence
        if (!extractedEvidenceList.isEmpty()) {
            Paragraph evidenceHeading = new Paragraph("Extracted Evidence", headingFont);
            document.add(evidenceHeading);
            document.add(Chunk.NEWLINE);

            PdfPTable evidenceTable = new PdfPTable(2);
            evidenceTable.setWidthPercentage(100);
            evidenceTable.setSpacingBefore(10f);
            evidenceTable.setSpacingAfter(10f);

            evidenceTable.addCell("Evidence Type");
            evidenceTable.addCell("Evidence Value");

            for (ExtractedEvidence evidence : extractedEvidenceList) {
                evidenceTable.addCell(evidence.getEvidenceType() != null ? evidence.getEvidenceType() : "");
                // Use rawText as the evidence value since ExtractedEvidence has no getEvidenceValue()
                String val = evidence.getRawText();
                if (val == null) val = evidence.getUrl() != null ? evidence.getUrl() :
                    evidence.getEmail() != null ? evidence.getEmail() :
                    evidence.getPhoneNumber() != null ? evidence.getPhoneNumber() :
                    evidence.getIpAddress() != null ? evidence.getIpAddress() : "";
                evidenceTable.addCell(val);
            }

            document.add(evidenceTable);
            document.add(Chunk.NEWLINE);
        }

        // Case Notes (if any)
        if (!caseNotesList.isEmpty()) {
            Paragraph notesHeading = new Paragraph("Case Notes", headingFont);
            document.add(notesHeading);
            document.add(Chunk.NEWLINE);

            for (CaseNote note : caseNotesList) {
                Paragraph notePara = new Paragraph(
                        "[" + note.getCreatedAt() + "] " + note.getUser().getFirstName() + " " + note.getUser().getLastName() + ": " + note.getNote(),
                        normalFont);
                document.add(notePara);
                document.add(Chunk.NEWLINE);
            }
            document.add(Chunk.NEWLINE);
        }

        // Audit Logs for this screenshot (if any)
        if (!auditLogList.isEmpty()) {
            Paragraph auditHeading = new Paragraph("Audit Log (for this screenshot)", headingFont);
            document.add(auditHeading);
            document.add(Chunk.NEWLINE);

            PdfPTable auditTable = new PdfPTable(4);
            auditTable.setWidthPercentage(100);
            auditTable.setSpacingBefore(10f);
            auditTable.setSpacingAfter(10f);

            auditTable.addCell("Timestamp");
            auditTable.addCell("Action");
            auditTable.addCell("User");
            auditTable.addCell("Description");

            for (AuditLog log : auditLogList) {
                auditTable.addCell(log.getTimestamp().toString());
                auditTable.addCell(log.getAction());
                auditTable.addCell(log.getUser() != null ? log.getUser().getUsername() : "System");
                auditTable.addCell(log.getDescription());
            }

            document.add(auditTable);
            document.add(Chunk.NEWLINE);
        }

        // Footer
        Paragraph footer = new Paragraph("Report generated by Digital Forensic Screenshot Analyzer", normalFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();

        return new ByteArrayResource(baos.toByteArray());
    }

    /**
     * Generate a PDF report for a case (aggregate of all screenshots in the case).
     *
     * @param caseId the ID of the case
     * @return a ByteArrayResource containing the PDF
     * @throws DocumentException if there is an error in the PDF generation
     * @throws IOException if there is an error reading files
     */
    public ByteArrayResource generateCaseReport(Long caseId) throws DocumentException, IOException {
        // Fetch the case
        Case theCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Fetch the investigator
        User investigator = theCase.getInvestigator();

        // Fetch all screenshots for this case
        List<Screenshot> screenshotsList = screenshotRepository.findByTheCase_Id(caseId);

        // We'll create a report that lists each screenshot and its analysis.
        // For brevity, we'll generate a summary and then for each screenshot we can include the key points.

        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, java.awt.Color.BLACK);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, java.awt.Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, java.awt.Color.BLACK);

        // Title
        Paragraph title = new Paragraph("Forensic Case Analysis Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(Chunk.NEWLINE);

        // Case Information
        Paragraph caseInfoHeading = new Paragraph("Case Information", headingFont);
        document.add(caseInfoHeading);
        document.add(Chunk.NEWLINE);

        PdfPTable caseInfoTable = new PdfPTable(2);
        caseInfoTable.setWidthPercentage(100);
        caseInfoTable.setSpacingBefore(10f);
        caseInfoTable.setSpacingAfter(10f);

        caseInfoTable.addCell("Case Number:");
        caseInfoTable.addCell(theCase.getCaseNumber());

        caseInfoTable.addCell("Case Title:");
        caseInfoTable.addCell(theCase.getTitle());

        caseInfoTable.addCell("Investigator:");
        caseInfoTable.addCell(investigator.getFirstName() + " " + investigator.getLastName());

        caseInfoTable.addCell("Case Status:");
        caseInfoTable.addCell(theCase.getStatus().toString());

        caseInfoTable.addCell("Case Priority:");
        caseInfoTable.addCell(theCase.getPriority().toString());

        caseInfoTable.addCell("Created At:");
        caseInfoTable.addCell(theCase.getCreatedAt().toString());

        caseInfoTable.addCell("Updated At:");
        caseInfoTable.addCell(theCase.getUpdatedAt().toString());

        document.add(caseInfoTable);
        document.add(Chunk.NEWLINE);

        // Screenshots Summary
        Paragraph screenshotsHeading = new Paragraph("Screenshots Summary", headingFont);
        document.add(screenshotsHeading);
        document.add(Chunk.NEWLINE);

        PdfPTable screenshotsTable = new PdfPTable(4);
        screenshotsTable.setWidthPercentage(100);
        screenshotsTable.setSpacingBefore(10f);
        screenshotsTable.setSpacingAfter(10f);

        screenshotsTable.addCell("Screenshot");
        screenshotsTable.addCell("MD5");
        screenshotsTable.addCell("SHA-256");
        screenshotsTable.addCell("Score/Verdict");

        int index = 1;
        for (Screenshot screenshot : screenshotsList) {
            AnalysisResult analysisResult = analysisResultRepository.findByScreenshotId(screenshot.getId())
                    .orElse(null); // It's possible that analysis is not done yet

            screenshotsTable.addCell(String.valueOf(index));
            screenshotsTable.addCell(screenshot.getMd5());
            screenshotsTable.addCell(screenshot.getSha256());

            if (analysisResult != null) {
                screenshotsTable.addCell(analysisResult.getAuthenticityScore() + "/100 (" + analysisResult.getVerdict() + ")");
            } else {
                screenshotsTable.addCell("Not analyzed");
            }
            index++;
        }

        document.add(screenshotsTable);
        document.add(Chunk.NEWLINE);

        // For each screenshot, we can add a detailed section (optional, but we'll do it for the first few to avoid too large a PDF)
        // We'll limit to 5 screenshots for the detailed section.
        int detailedCount = Math.min(5, screenshotsList.size());
        if (detailedCount > 0) {
            Paragraph detailedHeading = new Paragraph("Detailed Analysis (First " + detailedCount + " Screenshots)", headingFont);
            document.add(detailedHeading);
            document.add(Chunk.NEWLINE);

            for (int i = 0; i < detailedCount; i++) {
                Screenshot screenshot = screenshotsList.get(i);
                document.add(new Paragraph("Screenshot #" + (i+1) + ": " + screenshot.getOriginalFilename(), headingFont));
                document.add(Chunk.NEWLINE);

                // We'll reuse the screenshot report generation but we don't want to duplicate code.
                // Instead, we'll create a small section with the key points.

                AnalysisResult analysisResult = analysisResultRepository.findByScreenshotId(screenshot.getId())
                        .orElse(null);

                if (analysisResult != null) {
                    PdfPTable detailTable = new PdfPTable(2);
                    detailTable.setWidthPercentage(100);
                    detailTable.setSpacingBefore(5f);
                    detailTable.setSpacingAfter(5f);

                    detailTable.addCell("Authenticity Score:");
                    detailTable.addCell(analysisResult.getAuthenticityScore().toString() + "/100");

                    detailTable.addCell("Verdict:");
                    detailTable.addCell(analysisResult.getVerdict().toString());

                    detailTable.addCell("Explanation:");
                    detailTable.addCell(analysisResult.getVerdictExplanation());

                    document.add(detailTable);

                    // ELA image (if available) - we'll try to add it but limit the size
                    if (analysisResult.getElaImageFileName() != null && !analysisResult.getElaImageFileName().isEmpty()) {
                        File elaFile = new File(uploadDir, analysisResult.getElaImageFileName());
                        if (elaFile.exists()) {
                            try {
                                Image elaImage = Image.getInstance(elaFile.getAbsolutePath());
                                // Scale to width of 200px
                                elaImage.scaleAbsoluteWidth(200);
                                document.add(elaImage);
                            } catch (BadElementException e) {
                                // Ignore
                            }
                        }
                    }
                } else {
                    document.add(new Paragraph("Analysis not yet performed.", normalFont));
                }

                document.add(Chunk.NEWLINE);
            }
        }

        document.close();

        return new ByteArrayResource(baos.toByteArray());
    }
}