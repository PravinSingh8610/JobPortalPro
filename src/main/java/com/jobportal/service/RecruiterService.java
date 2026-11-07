
package com.jobportal.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.dto.RecruiterRequest;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;

@Service
public class RecruiterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RecruiterService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void registerRecruiter(RecruiterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Check whether email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email already registered!");
        }

        // Create recruiter account
        User recruiter = new User();

        recruiter.setFullName(request.getFullName().trim());
        recruiter.setEmail(email);

        // Encrypt password
        recruiter.setPassword(
                passwordEncoder.encode(request.getPassword()));

        recruiter.setRole("ROLE_RECRUITER");

        // Save recruiter only
        userRepository.save(recruiter);
    }
}
