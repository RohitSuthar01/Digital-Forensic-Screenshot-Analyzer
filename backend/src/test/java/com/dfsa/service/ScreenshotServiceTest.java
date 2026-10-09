package com.dfsa.service;

import com.dfsa.model.Case;
import com.dfsa.model.Screenshot;
import com.dfsa.repository.AnalysisResultRepository;
import com.dfsa.repository.CaseRepository;
import com.dfsa.repository.ScreenshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScreenshotServiceTest {
    @Mock ScreenshotRepository screenshotRepository;
    @Mock AnalysisResultRepository analysisResultRepository;
    @Mock CaseRepository caseRepository;
    @InjectMocks ScreenshotService screenshotService;
    @TempDir Path tempDir;

    @Test
    void uploadGeneratesSafeStorageNameAndSanitizesTraversalFilename() throws Exception {
        Case theCase = new Case();
        theCase.setId(1L);
        when(caseRepository.findById(1L)).thenReturn(Optional.of(theCase));
        when(screenshotRepository.save(any(Screenshot.class))).thenAnswer(invocation -> {
            Screenshot saved = invocation.getArgument(0);
            saved.setId(12L);
            return saved;
        });
        when(analysisResultRepository.findByScreenshotId(anyLong())).thenReturn(Optional.empty());
        ReflectionTestUtils.setField(screenshotService, "uploadDir", tempDir.toString());

        MockMultipartFile upload = new MockMultipartFile("file", "../../evidence.png", "text/plain", pngBytes());
        var result = screenshotService.uploadScreenshot(1L, upload);

        var captor = org.mockito.ArgumentCaptor.forClass(Screenshot.class);
        verify(screenshotRepository).save(captor.capture());
        Screenshot saved = captor.getValue();
        assertEquals("evidence.png", saved.getOriginalFilename());
        assertTrue(saved.getStoredFilename().matches("[0-9a-f-]{36}\\.png"));
        assertTrue(Path.of(saved.getFilePath()).normalize().startsWith(tempDir));
        assertTrue(Files.exists(Path.of(saved.getFilePath())));
        assertEquals("evidence.png", result.getOriginalFilename());
        assertNull(result.getFilePath(), "API DTO must not expose local storage paths");
        assertNull(result.getStoredFilename(), "API DTO must not expose internal storage names");
    }

    @Test
    void rejectsCorruptImageDespiteImageExtensionAndMime() throws Exception {
        when(caseRepository.findById(1L)).thenReturn(Optional.of(new Case()));
        ReflectionTestUtils.setField(screenshotService, "uploadDir", tempDir.toString());
        MockMultipartFile upload = new MockMultipartFile("file", "fake.png", "image/png", new byte[]{1, 2, 3, 4});
        assertThrows(RuntimeException.class, () -> screenshotService.uploadScreenshot(1L, upload));
        verify(screenshotRepository, never()).save(any());
    }

    private byte[] pngBytes() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return bytes.toByteArray();
    }
}
