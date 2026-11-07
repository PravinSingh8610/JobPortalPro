
package com.jobportal.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;

@Service
public class AdminCandidateService {

    private final UserRepository userRepository;

    // Constructor Injection
    public AdminCandidateService(
            UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    // Get all registered candidates
    @Transactional(readOnly = true)
    public List<User> getAllCandidates() {

        return userRepository
                .findByRoleOrderByIdDesc("ROLE_CANDIDATE");
    }
}
