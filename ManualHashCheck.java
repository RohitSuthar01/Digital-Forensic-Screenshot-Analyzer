import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class ManualHashCheck {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // The hash from the database
        String dbHash = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.";

        System.out.println("Database hash: " + dbHash);
        System.out.println();

        // Test common passwords
        String[] testPasswords = {
            "password",
            "Admin@123",
            "Invest@123",
            "Viewer@123",
            "admin",
            "investigator1",
            "viewer1"
        };

        for (String testPass : testPasswords) {
            boolean matches = encoder.matches(testPass, dbHash);
            System.out.println("'" + testPass + "' matches DB hash: " + matches);
        }

        System.out.println();
        System.out.println("What is the hash of 'password'?");
        String hashOfPassword = encoder.encode("password");
        System.out.println("hashOfPassword = " + hashOfPassword);
        System.out.println("Does hashOfPassword match dbHash? " + hashOfPassword.equals(dbHash));
    }
}