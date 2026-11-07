
package com.jobportal.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.jobportal.entity.Job;
import com.jobportal.repository.JobRepository;

@Component
public class JobExpiryScheduler {

    private final JobRepository jobRepository;

    public JobExpiryScheduler(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    // AUTOMATICALLY RUN EVERY DAY AT MIDNIGHT
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void expireJobs() {

        LocalDate today = LocalDate.now();

        List<Job> expiredJobs =
                jobRepository.findByStatusAndApplicationDeadlineBefore(
                        "ACTIVE",
                        today
                );

        for (Job job : expiredJobs) {
            job.setStatus("EXPIRED");
        }

        if (!expiredJobs.isEmpty()) {
            jobRepository.saveAll(expiredJobs);
        }

        System.out.println(
                "Automatic Job Expiry: "
                + expiredJobs.size()
                + " jobs expired."
        );
    }
}
