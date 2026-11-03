package com.example.help_bridge.common.config;

import com.example.help_bridge.users.systemadmin.entity.SystemAdmin;
import com.example.help_bridge.users.systemadmin.repository.SystemAdminRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("prod")
public class ProdAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProdAdminSeeder.class);

    private final PasswordEncoder passwordEncoder;
    private final SystemAdminRepository systemAdminRepository;

    @Value("${ADMIN_EMAIL:}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    public ProdAdminSeeder(PasswordEncoder passwordEncoder,
                           SystemAdminRepository systemAdminRepository) {
        this.passwordEncoder = passwordEncoder;
        this.systemAdminRepository = systemAdminRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_EMAIL or ADMIN_PASSWORD environment variable is not set. Skipping production admin creation.");
            return;
        }

        String email = adminEmail.trim();
        if (!systemAdminRepository.existsByEmail(email)) {
            String hash = passwordEncoder.encode(adminPassword);
            systemAdminRepository.save(new SystemAdmin(email, hash, "System", "Admin"));
            log.info("Initial production SystemAdmin created with email: {}", email);
        } else {
            log.info("SystemAdmin with email '{}' already exists. Skipping initialization.", email);
        }
    }
}
