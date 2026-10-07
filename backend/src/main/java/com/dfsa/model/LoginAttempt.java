package com.dfsa.model;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "login_attempts", indexes = {
        @Index(columnList = "username"),
        @Index(columnList = "ip_address"),
        @Index(columnList = "timestamp")
})
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private boolean success;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    // Constructors
    public LoginAttempt() {
        this.timestamp = new Date();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}