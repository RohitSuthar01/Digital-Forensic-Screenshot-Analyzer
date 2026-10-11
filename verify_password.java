import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class verify_password {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        System.out.println("Testing password: " + password);
        System.out.println("Against hash: " + hash);
        System.out.println("Result: " + encoder.matches(password, hash));

        // Also test what the hash of "password" would be
        String generatedHash = encoder.encode(password);
        System.out.println("Generated hash for 'password': " + generatedHash);
        System.out.println("Does generated hash match stored hash? " + encoder.matches(password, hash));
    }
}