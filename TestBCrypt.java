import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password";
        String hash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        System.out.println("Password: " + password);
        System.out.println("Hash: " + hash);
        System.out.println("Matches: " + encoder.matches(password, hash));
    }
}