package com.fsa_hw_02.cache;

import com.fsa_hw_02.model.Post;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class PostCache {
    private final ConcurrentMap<Integer, CacheEntry> cache = new ConcurrentHashMap<>();

    public void put(Post post, long ttl) {
        cache.put(post.getId(), new CacheEntry(post, System.currentTimeMillis() + ttl));
    }

    public Post get(Integer id) {
        CacheEntry entry = cache.get(id);
        return (entry != null && entry.getExpirationTime() > System.currentTimeMillis()) ? entry.getPost() : null;
    }
}
