package com.dfsa.controller;

import com.dfsa.dto.AuditLogDTO;
import com.dfsa.dto.UserDTO;
import com.dfsa.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * GET /api/admin/users
     * Returns a list of all users (admin only).
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = adminService.getAllUsersList();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/{id}
     * Returns a single user by ID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    /**
     * PUT /api/admin/users/{id}/enable
     * Enables a user account.
     */
    @PutMapping("/users/{id}/enable")
    public ResponseEntity<UserDTO> enableUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, true));
    }

    /**
     * PUT /api/admin/users/{id}/disable
     * Disables a user account.
     */
    @PutMapping("/users/{id}/disable")
    public ResponseEntity<UserDTO> disableUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(adminService.updateUserStatus(id, false));
    }

    /**
     * PUT /api/admin/users/{id}/role
     * Updates a user's role.
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<UserDTO> updateRole(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (role == null || role.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(adminService.updateUserRole(id, role.toUpperCase()));
    }

    /**
     * DELETE /api/admin/users/{id}
     * Deletes a user account.
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/admin/audit-logs
     * Returns paginated audit logs.
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "sortBy", defaultValue = "timestamp") String sortBy,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction) {
        List<AuditLogDTO> logs = adminService.getAuditLogs(page, size, sortBy, direction);
        return ResponseEntity.ok(logs);
    }
}
