package com.dfsa.mapper;

import com.dfsa.dto.ScreenshotDTO;
import com.dfsa.model.Screenshot;

import java.util.ArrayList;
import java.util.List;

public class ScreenshotMapper {

    public static ScreenshotDTO toDTO(Screenshot screenshot) {
        ScreenshotDTO dto = new ScreenshotDTO();
        dto.setId(screenshot.getId());
        dto.setCaseId(screenshot.getTheCase().getId());
        dto.setOriginalFilename(screenshot.getOriginalFilename());
        dto.setStoredFilename(screenshot.getStoredFilename());
        dto.setFilePath(screenshot.getFilePath());
        dto.setMd5(screenshot.getMd5());
        dto.setSha256(screenshot.getSha256());
        dto.setFileSize(screenshot.getFileSize());
        dto.setStatus(screenshot.getStatus());
        dto.setUploadedAt(screenshot.getUploadedAt());
        dto.setProcessedAt(screenshot.getProcessedAt());
        dto.setErrorMessage(screenshot.getErrorMessage());
        return dto;
    }

    public static List<ScreenshotDTO> toDTOList(List<Screenshot> screenshots) {
        List<ScreenshotDTO> dtos = new ArrayList<>();
        for (Screenshot screenshot : screenshots) {
            dtos.add(toDTO(screenshot));
        }
        return dtos;
    }
}