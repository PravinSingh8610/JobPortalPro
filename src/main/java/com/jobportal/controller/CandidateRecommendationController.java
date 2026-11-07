
package com.jobportal.controller;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jobportal.entity.User;
import com.jobportal.repository.UserRepository;
import com.jobportal.service.JobRecommendationService;
import com.jobportal.service.JobRecommendationService.RecommendedJob;
import com.jobportal.service.SavedJobService;

@Controller
@RequestMapping("/candidate/recommendations")
public class CandidateRecommendationController {

    private final JobRecommendationService recommendationService;
    private final UserRepository userRepository;
    private final SavedJobService savedJobService;

    public CandidateRecommendationController(
            JobRecommendationService recommendationService,
            UserRepository userRepository,
            SavedJobService savedJobService) {

        this.recommendationService = recommendationService;
        this.userRepository = userRepository;
        this.savedJobService = savedJobService;
    }

    @GetMapping
    public String showRecommendations(
            Principal principal,
            Model model) {

        String candidateEmail = principal.getName();

        User candidate = userRepository
                .findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate not found"));

        List<RecommendedJob> recommendations =
                recommendationService.getRecommendedJobs(
                        candidateEmail);

        Set<Long> savedJobIds = savedJobService
                .getSavedJobs(candidateEmail)
                .stream()
                .map(savedJob -> savedJob.getJob().getId())
                .collect(Collectors.toSet());

        model.addAttribute("candidate", candidate);
        model.addAttribute("recommendations", recommendations);
        model.addAttribute("totalRecommendations",
                recommendations.size());
        model.addAttribute("savedJobIds", savedJobIds);

        return "candidate-recommended-jobs";
    }
}
