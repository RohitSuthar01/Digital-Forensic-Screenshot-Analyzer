import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        System.out.println("Testing password: " + password);
        System.out.println("Against hash: " + hash);
        boolean matches = encoder.matches(password, hash);
        System.out.println("Matches: " + matches);

        // Also test what the hash of "password" would be
        String generatedHash = encoder.encode(password);
        System.out.println("Generated hash for 'password': " + generatedHash);
        System.out.println("Does password match generated hash? " + encoder.matches(password, generatedHash));

        // Check if the stored hash is actually for "password" or something else
        System.out.println("\nChecking if hash is for 'Admin@123':");
        boolean matchesAdmin = encoder.matches("Admin@123", hash);
        System.out.println("'Admin@123' matches hash: " + matchesAdmin);

        System.out.println("\nChecking if hash is for 'Invest@123':");
        boolean matchesInvest = encoder.matches("Invest@123", hash);
        System.out.println("'Invest@123' matches hash: " + matchesInvest);

        System.out.println("\nChecking if hash is for 'Viewer@123':");
        boolean matchesViewer = encoder.matches("Viewer@123", hash);
        System.out.println("'Viewer@123' matches hash: " + matchesViewer);
    }
}