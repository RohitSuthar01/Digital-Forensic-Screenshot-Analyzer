package com.dfsa.dto;

import com.dfsa.model.AuditLog;
import java.util.Date;

public class AuditLogDTO {

    private Long id;
    private String username;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private String ipAddress;
    private Date timestamp;

    // Constructors
    public AuditLogDTO() {}

    public AuditLogDTO(AuditLog auditLog) {
        this.id = auditLog.getId();
        this.username = auditLog.getUser() != null ? auditLog.getUser().getUsername() : null;
        this.action = auditLog.getAction();
        this.entityType = auditLog.getEntityType();
        this.entityId = auditLog.getEntityId();
        this.details = auditLog.getDescription();
        this.ipAddress = auditLog.getIpAddress();
        this.timestamp = auditLog.getTimestamp();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
