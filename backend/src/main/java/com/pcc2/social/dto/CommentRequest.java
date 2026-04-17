package com.pcc2.social.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private String content;
}
