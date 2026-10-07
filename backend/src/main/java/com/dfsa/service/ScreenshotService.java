package com.dfsa.service;

import com.dfsa.dto.ScreenshotDTO;
import com.dfsa.mapper.ScreenshotMapper;
import com.dfsa.model.Screenshot;
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

import java.io.File;
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
    private CaseRepository caseRepository;

    @Autowired
    private FileUtil fileUtil;

    @Value("${screenshot.upload.dir}")
    private String uploadDir;

    @Transactional
    public ScreenshotDTO uploadScreenshot(Long caseId, MultipartFile file) throws IOException, NoSuchAlgorithmException {
        // Validate the case exists
        var theCase = caseRepository.findById(caseId)
                .orElseThrow(() -> new RuntimeException("Case not found"));

        // Validate file is not empty
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // Validate file type (MIME and magic bytes)
        String contentType = file.getContentType();
        if (!fileUtil.isAllowedContentType(contentType)) {
            throw new RuntimeException("Invalid file type. Allowed types: PNG, JPG, JPEG, WEBP, BMP");
        }

        // Validate file size (already limited by Spring Boot properties, but double-check)
        long maxSize = 10 * 1024 * 1024; // 10 MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("File size exceeds 10 MB limit");
        }

        // Generate a unique filename for storage
        String storedFilename = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFilename.substring(dotIndex);
        }
        storedFilename = storedFilename + fileExtension;

        // Ensure upload directory exists
        File uploadDirectory = new File(uploadDir);
        if (!uploadDirectory.exists()) {
            uploadDirectory.mkdirs();
        }

        // Save the file to the upload directory
        Path filePath = Paths.get(uploadDir, storedFilename);
        file.transferTo(filePath.toFile());

        // Validate magic bytes after saving
        if (!FileUtil.isAllowedFileType(filePath.toFile())) {
            Files.deleteIfExists(filePath);
            throw new RuntimeException("File signature mismatch. The file is corrupt or has an invalid extension/format.");
        }

        // Calculate hashes
        String md5 = FileUtil.getMD5Checksum(filePath.toFile());
        String sha256 = FileUtil.getSHA256Checksum(filePath.toFile());

        // Create and save the screenshot entity
        Screenshot screenshot = new Screenshot();
        screenshot.setTheCase(theCase);
        screenshot.setOriginalFilename(originalFilename);
        screenshot.setStoredFilename(storedFilename);
        screenshot.setFilePath(filePath.toString());
        screenshot.setMd5(md5);
        screenshot.setSha256(sha256);
        screenshot.setFileSize(file.getSize());
        screenshot.setStatus(Screenshot.Status.PENDING);
        screenshot.setUploadedAt(new Date());

        Screenshot savedScreenshot = screenshotRepository.save(screenshot);

        // Return DTO
        return ScreenshotMapper.toDTO(savedScreenshot);
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
        return ScreenshotMapper.toDTOList(screenshots);
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
        return ScreenshotMapper.toDTO(screenshot);
    }
}