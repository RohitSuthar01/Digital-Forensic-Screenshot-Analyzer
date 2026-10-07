package com.dfsa.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class BCryptTest {

    @Test
    void testPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // The hash from data.sql
        String storedHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        // According to comments, this should be for Admin@123, Invest@123, Viewer@123
        // But let's test what it actually is

        System.out.println("Stored hash: " + storedHash);
        System.out.println();

        // Test the passwords mentioned in SUMMARY.md
        String[] summaryPasswords = {"password", "password", "password"}; // All should be "password" according to SUMMARY.md
        String[] summaryUsers = {"admin", "investigator1", "viewer1"};

        for (int i = 0; i < summaryUsers.length; i++) {
            boolean matches = encoder.matches(summaryPasswords[i], storedHash);
            System.out.println(summaryUsers[i] + " / " + summaryPasswords[i] + " matches hash: " + matches);
        }

        System.out.println();

        // Test the passwords mentioned in data.sql comments
        String[] commentPasswords = {"Admin@123", "Invest@123", "Viewer@123"};
        String[] commentUsers = {"admin", "investigator1", "viewer1"};

        for (int i = 0; i < commentUsers.length; i++) {
            boolean matches = encoder.matches(commentPasswords[i], storedHash);
            System.out.println(commentUsers[i] + " / " + commentPasswords[i] + " matches hash: " + matches);
        }

        System.out.println();

        // Generate hash for "password" to see what it actually is
        String generatedHash = encoder.encode("password");
        System.out.println("Generated hash for 'password': " + generatedHash);
        System.out.println("'password' matches generated hash: " + encoder.matches("password", generatedHash));

        System.out.println();
        System.out.println("Does stored hash equal generated hash for 'password'?");
        System.out.println(storedHash.equals(generatedHash));

        // The truth: the stored hash is actually for "password", not Admin@123/Invest@123/View@123
        assertTrue(encoder.matches("password", storedHash), "Stored hash should match 'password'");
        assertFalse(encoder.matches("Admin@123", storedHash), "Stored hash should NOT match 'Admin@123'");
        assertFalse(encoder.matches("Invest@123", storedHash), "Stored hash should NOT match 'Invest@123'");
        assertFalse(encoder.matches("Viewer@123", storedHash), "Stored hash should NOT match 'Viewer@123'");
    }
}