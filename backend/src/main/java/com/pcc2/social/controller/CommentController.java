package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.CommentRequest;
import com.pcc2.social.entity.Comment;
import com.pcc2.social.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;
    
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }
    
    @GetMapping("/post/{postId}")
    public Result<List<Comment>> getComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "time_desc") String sort) {
        List<Comment> comments = commentService.getCommentsByPostId(postId, sort);
        return Result.success(comments);
    }
    
    @PostMapping
    public Result<Comment> createComment(@RequestBody CommentRequest request, HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (request == null || request.getPostId() == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
            return Result.error("评论内容不能为空");
        }
        Comment comment = commentService.createComment(userId, request);
        return Result.success(comment);
    }
    
    @DeleteMapping("/{id}")
    public Result<?> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            boolean success = commentService.deleteComment(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("评论不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
