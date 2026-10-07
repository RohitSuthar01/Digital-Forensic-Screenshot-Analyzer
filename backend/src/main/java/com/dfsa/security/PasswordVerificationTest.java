package com.dfsa.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordVerificationTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        System.out.println("Testing password: password");
        System.out.println("Against hash: " + hash);
        System.out.println("Matches: " + encoder.matches("password", hash));

        System.out.println("\nTesting password: Admin@123");
        System.out.println("Matches: " + encoder.matches("Admin@123", hash));

        System.out.println("\nTesting password: Invest@123");
        System.out.println("Matches: " + encoder.matches("Invest@123", hash));

        System.out.println("\nTesting password: Viewer@123");
        System.out.println("Matches: " + encoder.matches("Viewer@123", hash));

        // Generate hash for "password" to see what it should be
        String generatedHash = encoder.encode("password");
        System.out.println("\nGenerated hash for 'password': " + generatedHash);
        System.out.println("Does 'password' match generated hash? " + encoder.matches("password", generatedHash));
    }
}