package com.dfsa.mapper;

import com.dfsa.dto.AuditLogDTO;
import com.dfsa.model.AuditLog;

import java.util.ArrayList;
import java.util.List;

public class AuditLogMapper {

    public static AuditLogDTO toDTO(AuditLog auditLog) {
        if (auditLog == null) {
            return null;
        }
        return new AuditLogDTO(auditLog);
    }

    public static AuditLog toEntity(AuditLogDTO auditLogDTO) {
        if (auditLogDTO == null) {
            return null;
        }
        AuditLog auditLog = new AuditLog();
        auditLog.setId(auditLogDTO.getId());
        // Note: user must be set separately by fetching from UserRepository
        auditLog.setAction(auditLogDTO.getAction());
        auditLog.setEntityType(auditLogDTO.getEntityType());
        auditLog.setEntityId(auditLogDTO.getEntityId());
        auditLog.setDescription(auditLogDTO.getDetails());
        auditLog.setIpAddress(auditLogDTO.getIpAddress());
        auditLog.setTimestamp(auditLogDTO.getTimestamp());
        return auditLog;
    }

    public static List<AuditLogDTO> toDTOList(List<AuditLog> auditLogs) {
        if (auditLogs == null) {
            return null;
        }
        List<AuditLogDTO> dtoList = new ArrayList<>();
        for (AuditLog auditLog : auditLogs) {
            dtoList.add(toDTO(auditLog));
        }
        return dtoList;
    }
}
