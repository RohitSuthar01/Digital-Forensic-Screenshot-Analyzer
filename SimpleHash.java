import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class SimpleHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String dbHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";
        String password = "password";

        System.out.println("Testing if '" + password + "' matches hash: " + dbHash);
        System.out.println("Result: " + encoder.matches(password, dbHash));

        System.out.println("\nGenerating hash for '" + password + "':");
        String generatedHash = encoder.encode(password);
        System.out.println("Generated hash: " + generatedHash);
        System.out.println("Does password match generated hash? " + encoder.matches(password, generatedHash));
    }
}