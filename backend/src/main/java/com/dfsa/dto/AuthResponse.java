package com.dfsa.dto;

public class AuthResponse {
    private String username;
    private String role;

    public AuthResponse() {}

    public AuthResponse(String username, String role) {
        this.username = username;
        // clean up role string like "[ROLE_ADMIN]" -> "ADMIN"
        this.role = role.replace("[ROLE_", "").replace("]", "").trim();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
