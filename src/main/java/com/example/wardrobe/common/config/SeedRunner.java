package com.example.wardrobe.common.config;

import com.example.wardrobe.auth.entity.User;
import com.example.wardrobe.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public SeedRunner(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      AppProperties appProperties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appProperties = appProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Seed seed = appProperties.getSeed();
        if (!seed.isEnabled()) {
            return;
        }
        if (seed.getUsername() == null || seed.getUsername().isBlank()
                || seed.getPassword() == null || seed.getPassword().isBlank()) {
            log.warn("Seed is enabled but SEED_ADMIN_USERNAME or SEED_ADMIN_PASSWORD is missing");
            return;
        }
        if (userRepository.existsByUsername(seed.getUsername())) {
            log.info("Seed user '{}' already exists, skipping", seed.getUsername());
            return;
        }
        User user = new User(
                seed.getUsername(),
                passwordEncoder.encode(seed.getPassword()),
                seed.getDisplayName() != null ? seed.getDisplayName() : seed.getUsername()
        );
        userRepository.save(user);
        log.info("Seeded user '{}'", seed.getUsername());
    }
}
