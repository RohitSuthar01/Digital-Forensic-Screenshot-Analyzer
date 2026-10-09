package com.dfsa.controller;

import com.dfsa.dto.CaseDTO;
import com.dfsa.dto.ScreenshotDTO;
import com.dfsa.model.User;
import com.dfsa.security.UserDetailsImpl;
import com.dfsa.service.CaseService;
import com.dfsa.service.ScreenshotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping("/api/cases")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5188"}, allowCredentials = "true")
public class CaseController {

    @Autowired
    private CaseService caseService;

    @Autowired
    private ScreenshotService screenshotService;

    // Create a new case
    @PostMapping
    public ResponseEntity<?> createCase(@Valid @RequestBody CaseDTO caseDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long investigatorId = userDetails.getId();

        // Override the investigatorId from the DTO with the current user's id
        caseDTO.setInvestigatorId(investigatorId);

        CaseDTO createdCase = caseService.createCase(caseDTO, investigatorId);
        return new ResponseEntity<>(createdCase, HttpStatus.CREATED);
    }

    // Get a case by ID (service will check authorization)
    @GetMapping("/{id}")
    public ResponseEntity<?> getCaseById(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        CaseDTO caseDTO = caseService.getCaseById(id, currentUserId);
        return ResponseEntity.ok(caseDTO);
    }

    // Get all cases with pagination (service will filter based on current user)
    @GetMapping
    public ResponseEntity<Page<CaseDTO>> getAllCases(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<CaseDTO> casePage = caseService.getAllCases(page, size, sortBy, direction, currentUserId, isAdmin);
        return ResponseEntity.ok(casePage);
    }

    // Update a case (service will check authorization)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCase(@PathVariable("id") Long id, @Valid @RequestBody CaseDTO caseDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        CaseDTO updatedCase = caseService.updateCase(id, caseDTO, currentUserId);
        return ResponseEntity.ok(updatedCase);
    }

    // Delete a case (service will check authorization)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCase(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.getId();

        caseService.deleteCase(id, currentUserId);
        return ResponseEntity.ok().build();
    }

    // Get all screenshots for a case
    @GetMapping("/{id}/screenshots")
    public ResponseEntity<List<ScreenshotDTO>> getScreenshotsForCase(@PathVariable("id") Long id) {
        List<ScreenshotDTO> screenshots = screenshotService.getScreenshotsByCaseId(id);
        return ResponseEntity.ok(screenshots);
    }

    // Upload a screenshot for a case
    @PostMapping("/{id}/screenshots")
    public ResponseEntity<?> uploadScreenshotForCase(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            ScreenshotDTO dto = screenshotService.uploadScreenshot(id, file);
            return new ResponseEntity<>(dto, HttpStatus.CREATED);
        } catch (IOException | NoSuchAlgorithmException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
