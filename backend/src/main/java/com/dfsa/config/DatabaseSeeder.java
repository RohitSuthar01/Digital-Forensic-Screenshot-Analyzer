package com.dfsa.config;

import com.dfsa.model.Case;
import com.dfsa.model.Role;
import com.dfsa.model.User;
import com.dfsa.repository.CaseRepository;
import com.dfsa.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Order(1)
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("[DFSA] Verifying initial seed data in database...");

        // 1. Seed admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setFirstName("System");
            admin.setLastName("Admin");
            admin.setEmail("admin@dfsa.com");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setCreatedAt(new Date());
            admin.setUpdatedAt(new Date());
            userRepository.save(admin);
            log.info("[DFSA] Created default user 'admin'");
        }

        // 2. Seed investigator1
        User investigator = userRepository.findByUsername("investigator1").orElse(null);
        if (investigator == null) {
            investigator = new User();
            investigator.setFirstName("John");
            investigator.setLastName("Investigator");
            investigator.setEmail("investigator1@dfsa.com");
            investigator.setUsername("investigator1");
            investigator.setPassword(passwordEncoder.encode("Invest@123"));
            investigator.setRole(Role.INVESTIGATOR);
            investigator.setEnabled(true);
            investigator.setCreatedAt(new Date());
            investigator.setUpdatedAt(new Date());
            investigator = userRepository.save(investigator);
            log.info("[DFSA] Created default user 'investigator1'");
        }

        // 3. Seed viewer1
        if (userRepository.findByUsername("viewer1").isEmpty()) {
            User viewer = new User();
            viewer.setFirstName("Jane");
            viewer.setLastName("Viewer");
            viewer.setEmail("viewer1@dfsa.com");
            viewer.setUsername("viewer1");
            viewer.setPassword(passwordEncoder.encode("Viewer@123"));
            viewer.setRole(Role.VIEWER);
            viewer.setEnabled(true);
            viewer.setCreatedAt(new Date());
            viewer.setUpdatedAt(new Date());
            userRepository.save(viewer);
            log.info("[DFSA] Created default user 'viewer1'");
        }

        // 4. Seed sample cases if none exist
        if (caseRepository.count() == 0 && investigator != null) {
            Case case1 = new Case();
            case1.setCaseNumber("DFSA-2026-0001");
            case1.setTitle("Sample Case 1");
            case1.setDescription("This is a sample case for demonstration purposes.");
            case1.setPriority(Case.Priority.HIGH);
            case1.setStatus(Case.Status.OPEN);
            case1.setInvestigator(investigator);
            case1.setCreatedAt(new Date());
            case1.setUpdatedAt(new Date());
            caseRepository.save(case1);

            Case case2 = new Case();
            case2.setCaseNumber("DFSA-2026-0002");
            case2.setTitle("Sample Case 2");
            case2.setDescription("Another sample case for testing.");
            case2.setPriority(Case.Priority.MEDIUM);
            case2.setStatus(Case.Status.IN_PROGRESS);
            case2.setInvestigator(investigator);
            case2.setCreatedAt(new Date());
            case2.setUpdatedAt(new Date());
            caseRepository.save(case2);

            log.info("[DFSA] Seeded sample cases DFSA-2026-0001 and DFSA-2026-0002");
        }

        log.info("[DFSA] Database initialization check complete.");
    }
}
