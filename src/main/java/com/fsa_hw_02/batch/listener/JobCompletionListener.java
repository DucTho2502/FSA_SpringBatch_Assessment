package com.fsa_hw_02.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;

import java.time.LocalDateTime;
import java.util.Objects;

@Component
public class JobCompletionListener extends JobExecutionListenerSupport {
    private static final Logger logger = LoggerFactory.getLogger(JobCompletionListener.class);

    @Override
    public void beforeJob(JobExecution jobExecution) {
        logger.info("!!! BATCH JOB STARTED! Job ID: {}", jobExecution.getJobId());
        logger.debug("Job Parameters: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if (jobExecution.getStatus().isUnsuccessful()) {
            handleFailedJob(jobExecution);
        } else {
            handleSuccessfulJob(jobExecution);
        }
    }

    private void handleFailedJob(JobExecution jobExecution) {
        logger.error("!!! BATCH JOB FAILED! Status: {}", jobExecution.getStatus());
        logger.error("Exit Status: {}", jobExecution.getExitStatus().getExitDescription());

        // Log all exceptions
        jobExecution.getAllFailureExceptions().forEach(ex ->
                logger.error("Job failure exception: {}", ex.getMessage()));
    }

    private void handleSuccessfulJob(JobExecution jobExecution) {
        Date startTime = Date.from(Objects.requireNonNull(jobExecution.getStartTime()).atZone(ZoneId.systemDefault())
                .toInstant());
        Date endTime =  Date.from(Objects.requireNonNull(jobExecution.getEndTime()).atZone(ZoneId.systemDefault())
                .toInstant());

        long duration = startTime.getTime() - endTime.getTime();
        logger.info("!!! BATCH JOB COMPLETED SUCCESSFULLY! Time taken: {} ms", duration);

        generateFinalReport();
        cleanupTempFiles();
    }

    private void generateFinalReport() {
        try {
            Path reportPath = Paths.get("batch-report-" + System.currentTimeMillis() + ".txt");
            String content = "Batch Job Completed at: " + new Date() + "\n";
            content += "Total Items Processed: [TODO: Add metrics]";

            Files.write(reportPath, content.getBytes());
            logger.info("Generated final report: {}", reportPath);
        } catch (IOException e) {
            logger.error("Failed to generate final report: {}", e.getMessage());
        }
    }

    private void cleanupTempFiles() {
        try {
            Path tempDir = Paths.get("temp");
            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                logger.warn("Failed to delete temp file: {}", path);
                            }
                        });
                logger.info("Cleaned up temp directory: {}", tempDir);
            }
        } catch (IOException e) {
            logger.error("Temp directory cleanup failed: {}", e.getMessage());
        }
    }
}
