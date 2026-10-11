package com.dfsa.controller;

import com.dfsa.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5188"}, allowCredentials = "true")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // Generate and download PDF report for a single screenshot
    @GetMapping("/screenshot/{id}")
    @PreAuthorize("hasRole('INVESTIGATOR') or hasRole('ADMIN')")
    public ResponseEntity<Resource> getScreenshotReport(@PathVariable("id") Long id) {
        try {
            Resource report = reportService.generateScreenshotReport(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"screenshot_report_" + id + ".pdf\"")
                    .body(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Generate and download PDF report for a case
    @GetMapping("/case/{id}")
    @PreAuthorize("hasRole('INVESTIGATOR') or hasRole('ADMIN')")
    public ResponseEntity<Resource> getCaseReport(@PathVariable("id") Long id) {
        try {
            Resource report = reportService.generateCaseReport(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"case_report_" + id + ".pdf\"")
                    .body(report);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
