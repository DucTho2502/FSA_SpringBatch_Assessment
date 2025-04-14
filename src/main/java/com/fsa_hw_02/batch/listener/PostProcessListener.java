package com.fsa_hw_02.batch.listener;

import com.fsa_hw_02.batch.model.PostDTO;
import com.fsa_hw_02.model.Post;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Component
public class PostProcessListener implements ItemProcessListener<PostDTO, Post> {
    @Override
    public void beforeProcess(PostDTO item) {
        // Có thể thêm logic pre-processing
    }

    @Override
    public void afterProcess(PostDTO item, Post result) {
        // Xử lý sau khi process thành công
    }

    @Override
    public void onProcessError(PostDTO item, Exception e) {
        // Ghi log hoặc xử lý lỗi
        System.err.println("Error processing item: " + item);
        System.err.println("Error message: " + e.getMessage());
    }
}
