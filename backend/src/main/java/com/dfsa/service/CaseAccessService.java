package com.dfsa.service;

import com.dfsa.model.Case;
import com.dfsa.model.Screenshot;
import com.dfsa.repository.CaseRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

/** Centralizes case-level access checks for evidence and reports. */
@Service
public class CaseAccessService {
    @Autowired private CaseRepository caseRepository;
    @Autowired private ScreenshotRepository screenshotRepository;

    public void requireCaseAccess(Long caseId, Long userId, boolean admin) {
        Case theCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new IllegalArgumentException("Case not found"));
        if (!canAccess(theCase, userId, admin)) {
            throw new AccessDeniedException("You are not authorized to access this case");
        }
    }

    public void requireScreenshotAccess(Long screenshotId, Long userId, boolean admin) {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new IllegalArgumentException("Screenshot not found"));
        if (!canAccess(screenshot.getTheCase(), userId, admin)) {
            throw new AccessDeniedException("You are not authorized to access this screenshot");
        }
    }

    public void requireScreenshotsAccess(Long firstId, Long secondId, Long userId, boolean admin) {
        requireScreenshotAccess(firstId, userId, admin);
        requireScreenshotAccess(secondId, userId, admin);
    }

    private boolean canAccess(Case theCase, Long userId, boolean admin) {
        return admin || (theCase.getInvestigator() != null
                && theCase.getInvestigator().getId() != null
                && theCase.getInvestigator().getId().equals(userId));
    }
}
