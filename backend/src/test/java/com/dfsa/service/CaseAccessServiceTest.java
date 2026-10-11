package com.dfsa.service;

import com.dfsa.model.Case;
import com.dfsa.model.Screenshot;
import com.dfsa.model.User;
import com.dfsa.repository.CaseRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseAccessServiceTest {
    @Mock CaseRepository caseRepository;
    @Mock ScreenshotRepository screenshotRepository;
    @InjectMocks CaseAccessService caseAccessService;

    @Test
    void caseOwnerCanAccessCase() {
        when(caseRepository.findById(7L)).thenReturn(Optional.of(aCase(12L)));
        assertDoesNotThrow(() -> caseAccessService.requireCaseAccess(7L, 12L, false));
    }

    @Test
    void nonOwnerCannotAccessCase() {
        when(caseRepository.findById(7L)).thenReturn(Optional.of(aCase(12L)));
        assertThrows(AccessDeniedException.class,
                () -> caseAccessService.requireCaseAccess(7L, 99L, false));
    }

    @Test
    void adminCanAccessAnotherInvestigatorsCase() {
        when(caseRepository.findById(7L)).thenReturn(Optional.of(aCase(12L)));
        assertDoesNotThrow(() -> caseAccessService.requireCaseAccess(7L, 99L, true));
    }

    @Test
    void screenshotUsesItsCaseOwnership() {
        Case theCase = aCase(12L);
        Screenshot screenshot = new Screenshot();
        screenshot.setTheCase(theCase);
        when(screenshotRepository.findById(21L)).thenReturn(Optional.of(screenshot));
        assertThrows(AccessDeniedException.class,
                () -> caseAccessService.requireScreenshotAccess(21L, 99L, false));
    }

    private Case aCase(Long investigatorId) {
        User investigator = new User();
        investigator.setId(investigatorId);
        Case theCase = new Case();
        theCase.setInvestigator(investigator);
        return theCase;
    }
}
