
package com.jobportal.controller;

import com.jobportal.dto.RegisterRequest;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobApplicationService;
import com.jobportal.service.InterviewService;
import com.jobportal.service.JobRecommendationService;
import com.jobportal.service.JobRecommendationService.RecommendedJob;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JobApplicationService applicationService;
    private final InterviewService interviewService;
    private final JobRecommendationService recommendationService;

    public AuthController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JobApplicationService applicationService,
            InterviewService interviewService,
            JobRecommendationService recommendationService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.applicationService = applicationService;
        this.interviewService = interviewService;
        this.recommendationService = recommendationService;
    }

    // 1. ACCOUNT TYPE SELECTION PAGE
    @GetMapping("/register")
    public String showRegisterOptions() {
        return "register";
    }

    // 2. CANDIDATE REGISTRATION PAGE
    @GetMapping("/candidate/register")
    public String showCandidateRegister(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "candidate-register";
    }

    // 3. REGISTER NEW CANDIDATE
    @PostMapping("/candidate/register")
    public String registerUser(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "candidate-register";
        }

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            model.addAttribute("error", "Email already registered!");
            return "candidate-register";
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_CANDIDATE");

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            model.addAttribute(
                    "error",
                    "Unable to register. Email may already exist."
            );
            return "candidate-register";
        }

        return "redirect:/login?registered=true";
    }

    // 4. LOGIN PAGE
    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    // 5. CANDIDATE DASHBOARD
    @GetMapping("/candidate/dashboard")
    public String candidateDashboard(Principal principal, Model model) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please login first"
            );
        }

        String candidateEmail = principal.getName();

        User user = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Candidate account not found"
                ));

        if (!"ROLE_CANDIDATE".equals(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only candidates can access this dashboard"
            );
        }

        List<JobApplication> applications =
                applicationService.getCandidateApplications(candidateEmail);

        long totalApplications = applications.size();

        long shortlistedCount = applications.stream()
                .filter(jobApp -> "SHORTLISTED".equals(jobApp.getStatus()))
                .count();

        long interviewCount = interviewService
                .getCandidateInterviews(candidateEmail)
                .size();

        List<RecommendedJob> topRecommendedJobs =
                recommendationService.getRecommendedJobs(candidateEmail)
                        .stream()
                        .filter(job -> job.getMatchPercentage() > 0)
                        .limit(3)
                        .toList();

        model.addAttribute("user", user);
        model.addAttribute("totalApplications", totalApplications);
        model.addAttribute("shortlistedCount", shortlistedCount);
        model.addAttribute("interviewCount", interviewCount);
        model.addAttribute("topRecommendedJobs", topRecommendedJobs);

        return "candidate-dashboard";
    }
}
