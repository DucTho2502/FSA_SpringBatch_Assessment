package com.fsa_hw_02.service;

import com.fsa_hw_02.model.Post;

import java.util.List;

public interface PostService{
    Post createPost(Post post);
    List<Post> getAllPosts();
}
