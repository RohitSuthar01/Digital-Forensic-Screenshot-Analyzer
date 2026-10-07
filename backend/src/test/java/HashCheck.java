import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class HashCheck {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        System.out.println("Database hash: " + dbHash);
        System.out.println();

        // Test the passwords from SUMMARY.md
        String[] testPasswords = {"password", "Admin@123", "Invest@123", "Viewer@123"};

        for (String testPass : testPasswords) {
            boolean matches = encoder.matches(testPass, dbHash);
            System.out.println("'" + testPass + "' matches DB hash: " + matches);
        }

        System.out.println();
        System.out.println("Generating hash for 'password':");
        String generatedHash = encoder.encode("password");
        System.out.println("Generated hash: " + generatedHash);
        System.out.println("'password' matches generated hash: " + encoder.matches("password", generatedHash));
    }
}