package com.dfsa.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class BCryptTest {
    @Test
    void seedCredentialsMatchTheirDocumentedPasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("Admin@123", "$2a$10$/yt/eDo2YcwnE1MLv8bGtOEbgksACOX9LZLUyP7Iw2FidepAcXp4O"));
        assertTrue(encoder.matches("Invest@123", "$2a$10$uooSk41HvhQvJ346UfTMa.W0Gi.cuBrN6bzCSAEE33qpmCMbZvzV6"));
        assertTrue(encoder.matches("Viewer@123", "$2a$10$z7WmbRyL72AiFutwI9q5YOqqr1f.tetBOeJhSXCchy/Cf8mpeFdIm"));
        assertFalse(encoder.matches("wrong-password", "$2a$10$/yt/eDo2YcwnE1MLv8bGtOEbgksACOX9LZLUyP7Iw2FidepAcXp4O"));
    }
}
