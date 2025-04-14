package com.fsa_hw_02.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.Collection;
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

        jobExecution.getExecutionContext().put("totalItemsProcessed", 0);
        jobExecution.getExecutionContext().put("validItems", 0);
        jobExecution.getExecutionContext().put("invalidItems", 0);
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

        int totalValid = 0;
        int totalInvalid = 0;

        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        for (StepExecution stepExecution : stepExecutions) {
            totalValid += stepExecution.getExecutionContext().getInt("validCount", 0);
            totalInvalid += stepExecution.getExecutionContext().getInt("invalidCount", 0);
        }

        long duration = startTime.getTime() - endTime.getTime();
        logger.info("!!! BATCH JOB COMPLETED SUCCESSFULLY! Time taken: {} ms", duration);

        generateFinalReport(totalValid + totalInvalid, totalValid, totalInvalid);
        cleanupTempFiles();
    }

    private void generateFinalReport(int totalItemsProcessed, int successfulItems, int failedItems) {
        try {
            // Create the report file with a timestamp in the filename
            Path reportPath = Paths.get("batch-report-" + System.currentTimeMillis() + ".txt");

            // Generate content for the report
            String content = "Batch Job Completed at: " + new Date() + "\n";
            content += "Total Records Processed: " + totalItemsProcessed + "\n";
            content += "Successful Records: " + successfulItems + "\n";
            content += "Failed Records: " + failedItems + "\n";

            // Write the content to the report file
            Files.write(reportPath, content.getBytes());

            // Log the generation of the report
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
