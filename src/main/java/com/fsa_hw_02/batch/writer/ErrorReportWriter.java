package com.fsa_hw_02.batch.writer;

import com.fsa_hw_02.exception.PostProcessingException;
import com.fsa_hw_02.model.Post;
import com.fsa_hw_02.service.CsvFileGenerator;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ErrorReportWriter implements ItemWriter<Post> {

    private final CsvFileGenerator csvFileGenerator;
    private final List<Post> errorRecords = new ArrayList<>();
    private final ThreadLocal<ExecutionContext> executionContext = new ThreadLocal<>();

    @Autowired
    public ErrorReportWriter(CsvFileGenerator csvFileGenerator) {
        this.csvFileGenerator = csvFileGenerator;
    }

    @Override
    public void write(Chunk<? extends Post> chunk) throws Exception {
        ExecutionContext context = executionContext.get();

        if (context != null) {
            List<PostProcessingException> errors = (List<PostProcessingException>)
                    context.get("post.errors");

            if (errors != null) {
                errors.forEach(ex -> {
                    Post errorPost = ex.getFailedItem();
                    errorPost.setErrorReason(ex.getMessage());
                    errorRecords.add(errorPost);
                });

                context.remove("post.errors");
            }
        }

        // Generate error report
        if (!errorRecords.isEmpty()) {
            csvFileGenerator.generateErrorReport(errorRecords);
            errorRecords.clear();
        }
    }
}
