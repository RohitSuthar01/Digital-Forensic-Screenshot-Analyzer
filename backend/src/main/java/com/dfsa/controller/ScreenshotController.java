package com.dfsa.controller;

import com.dfsa.dto.AnalysisResultDTO;
import com.dfsa.dto.ScreenshotDTO;
import com.dfsa.model.Screenshot;
import com.dfsa.service.AnalysisService;
import com.dfsa.service.ScreenshotService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/api/screenshots")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ScreenshotController {

    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private AnalysisService analysisService;

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    // Upload screenshot(s) for a case
    @PostMapping("/upload/{caseId}")
    @PreAuthorize("hasRole('INVESTIGATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> uploadScreenshot(
            @PathVariable("caseId") Long caseId,
            @RequestParam("file") MultipartFile file) {
        try {
            ScreenshotDTO dto = screenshotService.uploadScreenshot(caseId, file);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (IOException | java.security.NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // Get screenshot file by ID (to serve the image or file)
    @GetMapping("/{id}/file")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getScreenshotFile(@PathVariable("id") Long id) {
        try {
            Resource file = screenshotService.getScreenshotFile(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // We don't know the actual type, but we can let Spring determine it or set to application/octet-stream
                    .body(file);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Get all screenshots for a case
    @GetMapping("/case/{caseId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ScreenshotDTO>> getScreenshotsByCase(@PathVariable("caseId") Long caseId) {
        List<ScreenshotDTO> dtos = screenshotService.getScreenshotsByCaseId(caseId);
        return ResponseEntity.ok(dtos);
    }

    // Get a screenshot by ID (metadata)
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ScreenshotDTO> getScreenshotById(@PathVariable("id") Long id) {
        ScreenshotDTO dto = screenshotService.getScreenshotById(id);
        return ResponseEntity.ok(dto);
    }

    // Update screenshot status (e.g., after analysis)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('INVESTIGATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateScreenshotStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Screenshot.Status status) {
        try {
            screenshotService.updateScreenshotStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Trigger analysis for a screenshot
    @PostMapping("/{id}/analyze")
    @PreAuthorize("hasRole('INVESTIGATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> triggerAnalysis(@PathVariable("id") Long id) {
        try {
            // This will run asynchronously
            analysisService.analyzeScreenshot(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Get the analysis result for a screenshot
    @GetMapping("/{id}/analysis")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AnalysisResultDTO> getAnalysisResult(@PathVariable("id") Long id) {
        try {
            AnalysisResultDTO dto = analysisService.getAnalysisResult(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // Get the ELA image for a screenshot (if available)
    @GetMapping("/{id}/ela-image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getELaImage(@PathVariable("id") Long id) {
        try {
            // Get the analysis result to see if there is an ELA image file name
            AnalysisResultDTO dto = analysisService.getAnalysisResult(id);
            String elaImageFileName = dto.getElaImageFileName();
            if (elaImageFileName == null || elaImageFileName.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            // Construct the file path in the upload directory
            File elaFile = new File(uploadDir, elaImageFileName);
            if (!elaFile.exists() || !elaFile.isFile()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            Resource resource = new UrlResource(elaFile.toURI());
            if (resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG) // ELA images are saved as PNG
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}