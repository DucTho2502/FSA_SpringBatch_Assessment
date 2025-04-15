package com.fsa_hw_02.controller;

import com.fsa_hw_02.model.Post;
import com.fsa_hw_02.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping()
    public ResponseEntity<?> getCustomerProfile() {
        List<Post> customerFormDto = postService.getAllPosts();
        return ResponseEntity.ok(customerFormDto);
    }
}
