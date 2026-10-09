package com.dfsa.service;

import com.dfsa.dto.ScreenshotDTO;
import com.dfsa.mapper.ScreenshotMapper;
import com.dfsa.model.Screenshot;
import com.dfsa.repository.AnalysisResultRepository;
import com.dfsa.repository.CaseRepository;
import com.dfsa.repository.ScreenshotRepository;
import com.dfsa.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ScreenshotService {

    @Autowired
    private ScreenshotRepository screenshotRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    @Transactional
    public ScreenshotDTO uploadScreenshot(Long caseId, MultipartFile file)
            throws IOException, NoSuchAlgorithmException {
        // Validate the case exists
        var theCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("A non-empty filename is required");
        }
        if (file.isEmpty()) throw new RuntimeException("File is empty");
        long maxSize = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds 10 MB limit");
        }

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
        Path temporaryFile = Files.createTempFile(uploadRoot, "upload-", ".tmp");
        Path storedPath = null;
        try {
            file.transferTo(temporaryFile.toFile());
            if (!FileUtil.isAllowedFileType(temporaryFile.toFile())) {
                throw new RuntimeException("Unsupported or corrupt image. Supported formats: PNG, JPEG, BMP.");
            }
            String extension = switch (FileUtil.getImageFormat(temporaryFile.toFile())) {
                case "png" -> ".png";
                case "jpeg" -> ".jpg";
                case "bmp" -> ".bmp";
                default -> throw new RuntimeException("Unsupported image format");
            };
            String storedFilename = UUID.randomUUID() + extension;
            storedPath = uploadRoot.resolve(storedFilename).normalize();
            if (!storedPath.startsWith(uploadRoot)) throw new RuntimeException("Invalid storage path");
            Files.copy(temporaryFile, storedPath);
            Files.delete(temporaryFile);

            Screenshot screenshot = new Screenshot();
            screenshot.setTheCase(theCase);
            screenshot.setOriginalFilename(sanitizeDisplayFilename(originalFilename));
            screenshot.setStoredFilename(storedPath.getFileName().toString());
            screenshot.setFilePath(storedPath.toString());
            screenshot.setMd5(FileUtil.getMD5Checksum(storedPath.toFile()));
            screenshot.setSha256(FileUtil.getSHA256Checksum(storedPath.toFile()));
            screenshot.setFileSize(file.getSize());
            screenshot.setStatus(Screenshot.Status.PENDING);
            screenshot.setUploadedAt(new Date());
            return toDTOWithAnalysis(screenshotRepository.save(screenshot));
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            if (storedPath != null) Files.deleteIfExists(storedPath);
            throw e;
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private String sanitizeDisplayFilename(String filename) {
        String basename = filename.replace('\\', '/');
        basename = basename.substring(basename.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").trim();
        if (basename.isEmpty()) return "uploaded-image";
        return basename.length() > 255 ? basename.substring(basename.length() - 255) : basename;
    }

    public Resource getScreenshotFile(Long screenshotId) throws MalformedURLException {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found"));

        Path filePath = Paths.get(screenshot.getFilePath());
        Resource resource = new UrlResource(filePath.toUri());
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("File not found or cannot be read.");
        }
    }

    public List<ScreenshotDTO> getScreenshotsByCaseId(Long caseId) {
        List<Screenshot> screenshots = screenshotRepository.findByTheCase_Id(caseId);
        return screenshots.stream().map(this::toDTOWithAnalysis).toList();
    }

    public void updateScreenshotStatus(Long screenshotId, Screenshot.Status status) {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found"));
        screenshot.setStatus(status);
        screenshot.setProcessedAt(new Date());
        screenshotRepository.save(screenshot);
    }

    public ScreenshotDTO getScreenshotById(Long screenshotId) {
        Screenshot screenshot = screenshotRepository.findById(screenshotId)
                .orElseThrow(() -> new RuntimeException("Screenshot not found"));
        return toDTOWithAnalysis(screenshot);
    }

    private ScreenshotDTO toDTOWithAnalysis(Screenshot screenshot) {
        ScreenshotDTO dto = ScreenshotMapper.toDTO(screenshot);
        dto.setFilePath(null);
        dto.setStoredFilename(null);
        analysisResultRepository.findByScreenshotId(screenshot.getId()).ifPresent(result -> {
            dto.setVerdict(result.getVerdict());
            dto.setAuthenticityScore(result.getAuthenticityScore());
        });
        return dto;
    }
}
