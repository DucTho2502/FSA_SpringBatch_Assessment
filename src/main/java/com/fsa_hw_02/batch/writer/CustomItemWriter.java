package com.fsa_hw_02.batch.writer;

import com.fsa_hw_02.cache.PostCache;
import com.fsa_hw_02.model.Post;
import com.fsa_hw_02.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CustomItemWriter implements ItemWriter<Post> {
    private final PostRepository postRepository;
    private final PostCache postCache;

    public CustomItemWriter(PostRepository postRepository, PostCache postCache) {
        this.postRepository = postRepository;
        this.postCache = postCache;
    }

    @Override
    @Transactional
    public void write(Chunk<? extends Post> chunk) throws Exception {
        List<Post> posts = new ArrayList<>(chunk.getItems());

        // Batch insert to database
        List<Post> savedPosts = postRepository.saveAll(posts);

        // Update cache with TTL 1 hour
        savedPosts.forEach(post ->
                postCache.put(post, 60 * 60 * 1000)
        );
    }
}
