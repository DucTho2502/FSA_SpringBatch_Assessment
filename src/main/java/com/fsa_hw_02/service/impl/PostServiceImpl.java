package com.fsa_hw_02.service.impl;

import com.fsa_hw_02.cache.PostCache;
import com.fsa_hw_02.model.Post;
import com.fsa_hw_02.repository.PostRepository;
import com.fsa_hw_02.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository repository;
    private final PostCache cache;

    public PostServiceImpl(PostRepository repository, PostCache cache) {
        this.repository = repository;
        this.cache = cache;
    }

    @Override
    @Transactional
    public Post createPost(Post post) {
        Post savedPost = repository.save(post);
        cache.put(savedPost, 60_000); // 1 minute TTL
        return savedPost;
    }
}
