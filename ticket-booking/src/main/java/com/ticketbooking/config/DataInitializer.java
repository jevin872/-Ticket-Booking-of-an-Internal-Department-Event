package com.ticketbooking.config;

import com.ticketbooking.model.entity.User;
import com.ticketbooking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create default admin if not exists
        if (!userRepository.existsByEmail("admin@college.edu")) {
            User admin = User.builder()
                    .employeeId("ADMIN001")
                    .fullName("System Administrator")
                    .email("admin@college.edu")
                    .password(passwordEncoder.encode("Admin@1234"))
                    .department("IT Department")
                    .designation("System Admin")
                    .role(User.Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin created: admin@college.edu / Admin@1234");
        }

        // Create a demo organizer
        if (!userRepository.existsByEmail("organizer@college.edu")) {
            User organizer = User.builder()
                    .employeeId("ORG001")
                    .fullName("Event Organizer")
                    .email("organizer@college.edu")
                    .password(passwordEncoder.encode("Org@12345"))
                    .department("CSE Department")
                    .designation("Event Coordinator")
                    .role(User.Role.ORGANIZER)
                    .enabled(true)
                    .build();
            userRepository.save(organizer);
            log.info("Demo organizer created: organizer@college.edu / Org@12345");
        }
    }
}
