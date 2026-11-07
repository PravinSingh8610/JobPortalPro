
package com.jobportal.config;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminAccountInitializer {

    @Bean
    public CommandLineRunner initializeAdminAccount(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            String adminEmail =
                    System.getenv("JOBPORTAL_ADMIN_EMAIL");

            String adminPassword =
                    System.getenv("JOBPORTAL_ADMIN_PASSWORD");

            String resetPassword =
                    System.getenv("JOBPORTAL_ADMIN_RESET");

            if (adminEmail == null || adminEmail.isBlank()
                    || adminPassword == null || adminPassword.isBlank()) {
                System.out.println("Admin environment variables not configured.");
                return;
            }

            adminEmail = adminEmail.trim();

            if (adminPassword.length() < 12) {
                throw new IllegalStateException(
                        "Admin password must contain at least 12 characters."
                );
            }

            var existingUser =
                    userRepository.findByEmail(adminEmail);

            if (existingUser.isPresent()) {

                User admin = existingUser.get();

                if ("true".equalsIgnoreCase(resetPassword)
                        && "ROLE_ADMIN".equals(admin.getRole())) {

                    admin.setPassword(
                            passwordEncoder.encode(adminPassword)
                    );

                    userRepository.save(admin);

                    System.out.println(
                            "Existing admin password reset successfully."
                    );

                } else {
                    System.out.println(
                            "Admin initialization skipped: email already exists."
                    );
                }

                return;
            }

            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole("ROLE_ADMIN");

            userRepository.save(admin);

            System.out.println("Admin account created successfully.");
        };
    }
}
