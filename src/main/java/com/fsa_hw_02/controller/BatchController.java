package com.fsa_hw_02.controller;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/batch")
public class BatchController {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Job importPostJob;

    @Autowired
    public BatchController(
            JobLauncher jobLauncher,
            JobExplorer jobExplorer,
            @Qualifier("importPostJob") Job importPostJob // Inject bằng qualifier
    ) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.importPostJob = importPostJob;
    }

    @PostMapping("/start")
    public ResponseEntity<String> startJob() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importPostJob, params);
        return ResponseEntity.ok("Job started");
    }

    @GetMapping("/jobs")
    public List<JobInstance> listJobs() {
        return jobExplorer.getJobInstances("importPostJob", 0, 10);
    }
}
