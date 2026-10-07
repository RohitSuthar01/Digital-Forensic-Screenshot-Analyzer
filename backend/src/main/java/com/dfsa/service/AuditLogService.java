package com.dfsa.service;

import com.dfsa.model.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import com.dfsa.service.UserService;
import com.dfsa.model.User;
import com.dfsa.repository.AuditLogRepository;
import com.dfsa.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserService userService; // We might need to get the user by id from the token, but we have the UserDetailsImpl

    public void logAction(HttpServletRequest request, String action, String entityType, Long entityId, String description) {
        // Get the current user from the security context
        UserDetailsImpl userDetails = (UserDetailsImpl) org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userDetails.getUser();

        // Get the IP address
        String ipAddress = request.getRemoteAddr();

        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLog.setIpAddress(ipAddress);
        auditLog.setTimestamp(new Date());

        auditLogRepository.save(auditLog);
    }

    // Overload for when we don't have an entity (e.g., login action)
    public void logAction(HttpServletRequest request, String action, String description) {
        logAction(request, action, null, null, description);
    }
}