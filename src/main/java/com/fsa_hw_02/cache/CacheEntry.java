package com.fsa_hw_02.cache;

import com.fsa_hw_02.model.Post;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheEntry {
    private Post post;
    private long expiryTime;  // Expiration time in milliseconds

    public CacheEntry(Post post, long expiryTime) {
        this.post = post;
        this.expiryTime = expiryTime;
    }

    public Post getPost() {
        return post;
    }

    public long getExpirationTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
