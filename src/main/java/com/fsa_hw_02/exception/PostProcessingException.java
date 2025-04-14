package com.fsa_hw_02.exception;

import com.fsa_hw_02.model.Post;

public class PostProcessingException extends Exception  {
    private final Post failedItem;

    public PostProcessingException(String message, Post failedItem) {
        super(message);
        this.failedItem = failedItem;
    }

    public Post getFailedItem() { return failedItem; }
}
