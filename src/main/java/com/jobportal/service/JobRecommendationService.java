
package com.jobportal.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jobportal.entity.Job;
import com.jobportal.entity.User;
import com.jobportal.repository.JobRepository;
import com.jobportal.repository.UserRepository;

@Service
public class JobRecommendationService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public JobRecommendationService(
            JobRepository jobRepository,
            UserRepository userRepository) {

        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public List<RecommendedJob> getRecommendedJobs(
            String candidateEmail) {

        User candidate = userRepository.findByEmail(candidateEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate not found"));

        Set<String> candidateSkills = parseSkills(
                candidate.getSkills());

        List<Job> activeJobs =
                jobRepository.findActiveJobsForRecommendations(
                        LocalDate.now());

        List<RecommendedJob> recommendations =
                new ArrayList<>();

        for (Job job : activeJobs) {

            Set<String> requiredSkills =
                    parseSkills(job.getSkills());

            List<String> matchedSkills =
                    requiredSkills.stream()
                            .filter(candidateSkills::contains)
                            .collect(Collectors.toList());

            int matchPercentage = requiredSkills.isEmpty()
                    ? 0
                    : (int) Math.round(
                            matchedSkills.size() * 100.0
                                    / requiredSkills.size());

            recommendations.add(
                    new RecommendedJob(
                            job,
                            matchPercentage,
                            matchedSkills.size(),
                            requiredSkills.size()
                    )
            );
        }

        recommendations.sort(
                Comparator.comparingInt(
                        RecommendedJob::getMatchPercentage)
                        .reversed()
                        .thenComparing(
                                r -> r.getJob().getId(),
                                Comparator.reverseOrder())
        );

        return recommendations;
    }

    private Set<String> parseSkills(String skills) {

        if (skills == null || skills.isBlank()) {
            return new LinkedHashSet<>();
        }

        return Arrays.stream(skills.split(","))
                .map(String::trim)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(
                        LinkedHashSet::new));
    }

    public static class RecommendedJob {

        private final Job job;
        private final int matchPercentage;
        private final int matchedSkillsCount;
        private final int requiredSkillsCount;

        public RecommendedJob(
                Job job,
                int matchPercentage,
                int matchedSkillsCount,
                int requiredSkillsCount) {

            this.job = job;
            this.matchPercentage = matchPercentage;
            this.matchedSkillsCount = matchedSkillsCount;
            this.requiredSkillsCount = requiredSkillsCount;
        }

        public Job getJob() {
            return job;
        }

        public int getMatchPercentage() {
            return matchPercentage;
        }

        public int getMatchedSkillsCount() {
            return matchedSkillsCount;
        }

        public int getRequiredSkillsCount() {
            return requiredSkillsCount;
        }
    }
}
