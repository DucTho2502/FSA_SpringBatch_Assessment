package com.fsa_hw_02.batch.processor;

import com.fsa_hw_02.batch.model.PostDTO;
import com.fsa_hw_02.enums.PostStatus;
import com.fsa_hw_02.exception.PostProcessingException;
import com.fsa_hw_02.model.Post;
import com.fsa_hw_02.service.CsvFileGenerator;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PostItemProcessor implements ItemProcessor<PostDTO, Post> {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final String ERROR_TITLE_REQUIRED = "Title is required";
    private static final String ERROR_TITLE_TOO_LONG = "Title exceeds " + MAX_TITLE_LENGTH + " characters";
    private static final String ERROR_CONTENT_REQUIRED = "Content is required";
    private static final String ERROR_AUTHOR_REQUIRED = "Author is required";
    private static final String ERROR_AUTHOR_NOT_EXIST = "Author does not exist";

    private final CsvFileGenerator csvFileGenerator;

    // Inject CsvFileGenerator
    @Autowired
    public PostItemProcessor(CsvFileGenerator csvFileGenerator) {
        this.csvFileGenerator = csvFileGenerator;
    }

    @Override
    public Post process(PostDTO item) throws PostProcessingException {
        List<String> validationErrors = validatePost(item);

        if (!validationErrors.isEmpty()) {
            // Create a failed post with error reason
            Post failedPost = convertToFailedPost(String.join("; ", validationErrors));

            // Call the CSV file generator to log the errors
            csvFileGenerator.generateErrorReport(List.of(failedPost));

            // Throw exception as intended
            return null;
        }

        return convertToPost(item);
    }

    private List<String> validatePost(PostDTO item) {
        List<String> errors = new ArrayList<>();

        validateTitle(item.getTitle(), errors);
        validateContent(item.getContent(), errors);
        /*validateAuthor(item.getAuthor(), errors);*/

        return errors;
    }

    private void validateTitle(String title, List<String> errors) {
        if (title == null || title.isEmpty()) {
            errors.add(ERROR_TITLE_REQUIRED);
        } else if (title.length() > MAX_TITLE_LENGTH) {
            errors.add(ERROR_TITLE_TOO_LONG);
        }
    }

    private void validateContent(String content, List<String> errors) {
        if (content == null || content.isEmpty()) {
            errors.add(ERROR_CONTENT_REQUIRED);
        }
    }

    /*private void validateAuthor(String author, List<String> errors) {
        if (author == null || author.isEmpty()) {
            errors.add(ERROR_AUTHOR_REQUIRED);
        } else if (!userService.existsByUsername(author)) {
            errors.add(ERROR_AUTHOR_NOT_EXIST);
        }
    }*/

    private Post convertToPost(PostDTO dto) {
        Post post = new Post();
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setAuthor(dto.getAuthor());
        post.setStatus(PostStatus.OK);
        return post;
    }

    private Post convertToFailedPost(String error) {
        Post post = new Post();
        post.setTitle("error");
        post.setAuthor("error");
        post.setContent("error");
        post.setErrorReason(error);
        post.setStatus(PostStatus.FAILED);
        return post;
    }
}
