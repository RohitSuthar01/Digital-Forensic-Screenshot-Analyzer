package com.dfsa.service;

import com.dfsa.model.Case;
import com.dfsa.model.ComparisonResult;
import com.dfsa.model.Screenshot;
import com.dfsa.repository.AnalysisResultRepository;
import com.dfsa.repository.ComparisonResultRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComparisonServiceTest {
    @Mock ComparisonResultRepository comparisonRepository;
    @Mock AnalysisResultRepository analysisRepository;
    @Mock ScreenshotRepository screenshotRepository;
    @InjectMocks ComparisonService service;
    @TempDir Path tempDir;

    @Test
    void rejectsCrossCaseAndSelfComparisons() throws Exception {
        Screenshot target = screenshot(1L, 10L, "target.png");
        Screenshot otherCase = screenshot(2L, 11L, "reference.png");
        when(screenshotRepository.findById(1L)).thenReturn(Optional.of(target));
        when(screenshotRepository.findById(2L)).thenReturn(Optional.of(otherCase));
        assertThrows(IllegalStateException.class, () -> service.compare(1L, 2L));
        assertThrows(IllegalArgumentException.class, () -> service.compare(1L, 1L));
        verify(comparisonRepository, never()).save(any());
    }

    @Test
    void refusesMismatchedDimensionsAndReportsExactPairForSameSize() throws Exception {
        Screenshot target = screenshot(1L, 10L, "target.png");
        Screenshot reference = screenshot(2L, 10L, "reference.png");
        writeImage("target.png", 2, 2, 0xFF000000);
        writeImage("reference.png", 3, 2, 0xFFFFFFFF);
        when(screenshotRepository.findById(1L)).thenReturn(Optional.of(target));
        when(screenshotRepository.findById(2L)).thenReturn(Optional.of(reference));
        ReflectionTestUtils.setField(service, "uploadDir", tempDir.toString());

        IllegalArgumentException mismatch = assertThrows(IllegalArgumentException.class, () -> service.compare(1L, 2L));
        assertTrue(mismatch.getMessage().contains("No resizing was performed"));
        writeImage("reference.png", 2, 2, 0xFFFFFFFF);
        when(comparisonRepository.findByTargetScreenshotId(1L)).thenReturn(Optional.empty());
        when(analysisRepository.findByScreenshotId(anyLong())).thenReturn(Optional.empty());
        when(comparisonRepository.save(any(ComparisonResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComparisonResult result = service.compare(1L, 2L);
        assertSame(target, result.getTargetScreenshot());
        assertSame(reference, result.getReferenceScreenshot());
        assertTrue(result.getChangedRegionsDetails().contains("Target #1 (2×2)"));
        assertTrue(result.getChangedRegionsDetails().contains("reference #2 (2×2)"));
        assertTrue(result.getChangedRegionsDetails().contains("100.00%"));
        assertNotNull(result.getDifferenceMapFileName());
    }

    private Screenshot screenshot(long id, long caseId, String name) {
        Case theCase = new Case();
        theCase.setId(caseId);
        Screenshot screenshot = new Screenshot();
        screenshot.setId(id);
        screenshot.setTheCase(theCase);
        screenshot.setStoredFilename(name);
        return screenshot;
    }

    private void writeImage(String name, int width, int height, int color) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) image.setRGB(x, y, color);
        ImageIO.write(image, "png", tempDir.resolve(name).toFile());
    }
}
