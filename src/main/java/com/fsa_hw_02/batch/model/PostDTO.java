package com.fsa_hw_02.batch.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class PostDTO {
    private Integer id;
    private String title;
    private String content;
    private String author;
}
