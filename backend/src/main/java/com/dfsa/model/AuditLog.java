package com.dfsa.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(columnList = "timestamp")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // nullable because some actions might be system actions or anonymous (but we want to track user if possible)

    @Column(length = 50)
    private String action; // e.g., LOGIN, LOGOUT, UPLOAD, VIEW, ANALYZE, EXPORT, DELETE, ROLE_CHANGE, etc.

    @Column(length = 50)
    private String entityType; // e.g., User, Case, Screenshot, etc.

    private Long entityId; // ID of the entity, if applicable

    @Column(length = 255)
    private String description;

    @Column(length = 45) // IPv6 address length
    private String ipAddress;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // Constructors
    public AuditLog() {
        this.timestamp = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}