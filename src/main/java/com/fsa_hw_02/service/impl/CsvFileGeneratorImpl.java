package com.fsa_hw_02.service.impl;

import com.fsa_hw_02.model.Post;
import com.fsa_hw_02.service.CsvFileGenerator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CsvFileGeneratorImpl implements CsvFileGenerator {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Override
    public void generateErrorReport(List<Post> errorPosts) {
        String fileName = "error_report_" + LocalDateTime.now().format(DATE_FORMATTER) + ".csv";

        try (FileWriter out = new FileWriter(fileName);
             CSVPrinter printer = new CSVPrinter(out, CSVFormat.DEFAULT
                     .withHeader("ID", "Title", "Content", "Author", "Error"))) {

            for (Post post : errorPosts) {
                printer.printRecord(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getAuthor(),
                        "Error in this record" // Cần thêm logic lưu thông tin lỗi cụ thể
                );
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to generate error report", e);
        }
    }
}
