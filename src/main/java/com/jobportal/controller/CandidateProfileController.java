
package com.jobportal.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;

@Controller
@RequestMapping("/candidate/profile")
public class CandidateProfileController {

    private final UserRepository userRepository;

    public CandidateProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showProfile(Principal principal, Model model) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        model.addAttribute("user", user);

        return "candidate-profile";
    }

    @PostMapping("/update")
    public String updateProfile(
            @RequestParam String fullName,
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) String location,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (fullName == null || fullName.trim().isEmpty()
                || fullName.trim().length() > 100) {
            redirectAttributes.addFlashAttribute("error",
                    "Please enter a valid name (maximum 100 characters).");
            return "redirect:/candidate/profile";
        }

        if (experienceYears != null
                && (experienceYears < 0 || experienceYears > 60)) {
            redirectAttributes.addFlashAttribute("error",
                    "Experience must be between 0 and 60 years.");
            return "redirect:/candidate/profile";
        }

        if (skills != null && skills.length() > 1000) {
            redirectAttributes.addFlashAttribute("error",
                    "Skills must be within 1000 characters.");
            return "redirect:/candidate/profile";
        }

        if (location != null && location.length() > 150) {
            redirectAttributes.addFlashAttribute("error",
                    "Location must be within 150 characters.");
            return "redirect:/candidate/profile";
        }

        user.setFullName(fullName.trim());
        user.setSkills(skills == null ? "" : skills.trim());
        user.setExperienceYears(experienceYears);
        user.setLocation(location == null ? "" : location.trim());

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success",
                "Profile updated successfully!");

        return "redirect:/candidate/profile";
    }
}
