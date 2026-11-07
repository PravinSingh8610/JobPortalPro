
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;

@Controller
@RequestMapping("/recruiter/candidates")
public class RecruiterCandidateController {

    private final UserRepository userRepository;

    public RecruiterCandidateController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. SEARCH AND FILTER CANDIDATES
    @GetMapping
    public String searchCandidates(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            Principal principal,
            Model model) {

        User recruiter = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Recruiter not found"));

        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            return "redirect:/candidate/dashboard";
        }

        if (minExperience != null && minExperience < 0) {
            minExperience = null;
        }

        if (maxExperience != null && maxExperience < 0) {
            maxExperience = null;
        }

        if (minExperience != null && maxExperience != null
                && minExperience > maxExperience) {

            model.addAttribute("error",
                    "Minimum experience cannot be greater than maximum experience.");

            minExperience = null;
            maxExperience = null;
        }

        keyword = normalize(keyword);
        skills = normalize(skills);
        location = normalize(location);

        List<User> candidates = userRepository.searchCandidates(
                keyword,
                skills,
                location,
                minExperience,
                maxExperience
        );

        model.addAttribute("candidates", candidates);
        model.addAttribute("keyword", keyword);
        model.addAttribute("skills", skills);
        model.addAttribute("location", location);
        model.addAttribute("minExperience", minExperience);
        model.addAttribute("maxExperience", maxExperience);

        return "recruiter-candidates";
    }

    // 2. VIEW COMPLETE CANDIDATE PROFILE
    @GetMapping("/profile/{candidateId}")
    public String viewCandidateProfile(
            @PathVariable Long candidateId,
            Principal principal,
            Model model) {

        User recruiter = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Recruiter not found"));

        // Verify recruiter role
        if (!"ROLE_RECRUITER".equals(recruiter.getRole())) {
            return "redirect:/candidate/dashboard";
        }

        // Find candidate
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Candidate not found"));

        // Verify candidate role
        if (!"ROLE_CANDIDATE".equals(candidate.getRole())) {
            return "redirect:/recruiter/candidates";
        }

        model.addAttribute("candidate", candidate);

        return "recruiter-candidate-profile";
    }

    // 3. NORMALIZE SEARCH INPUT
    private String normalize(String value) {

        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }
}
