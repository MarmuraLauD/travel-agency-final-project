package com.epam.finaltask.config.bootstrap;

import com.epam.finaltask.model.Role; // Убедись, что импортируешь свой Enum Role
import com.epam.finaltask.model.User;
import com.epam.finaltask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.default.username}")
    private String defaultUsername;

    @Value("${admin.default.password}")
    private String defaultPassword;

    @Value("${admin.default.phone}")
    private String defaultPhone;

    @Override
    public void run(String... args) {
        if (userRepository.findUserByUsername("superadmin").isEmpty()) {
            User admin = new User();
            admin.setUsername(defaultUsername);
            admin.setPassword(passwordEncoder.encode(defaultPassword));
            admin.setPhoneNumber(defaultPhone);
            admin.setRole(Role.ADMIN);
            admin.setActive(true);

            userRepository.save(admin);
            log.info("Default SUPERADMIN account created.");
        } else {
            log.info("Admin account already exists. Skipping initialization.");
        }
    }
}