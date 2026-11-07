
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
@RequestMapping("/candidate/skills")
public class CandidateSkillsController {

    private final UserRepository userRepository;

    public CandidateSkillsController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String showSkillsPage(Principal principal, Model model) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Candidate not found"));

        model.addAttribute("user", user);

        return "candidate-skills";
    }

    @PostMapping
    public String updateSkills(
            @RequestParam("skills") String skills,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("Candidate not found"));

        if (skills.length() > 1000) {
            redirectAttributes.addFlashAttribute(
                    "error", "Skills must be within 1000 characters.");

            return "redirect:/candidate/skills";
        }

        user.setSkills(skills.trim());
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "success", "Your skills have been updated successfully!");

        return "redirect:/candidate/skills";
    }
}
