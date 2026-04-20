package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.CommentRequest;
import com.pcc2.social.entity.Comment;
import com.pcc2.social.service.CommentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
            @RequestParam(defaultValue = "time_desc") String sort,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Comment> comments = commentService.getCommentsByPostId(postId, sort, userId);
        return Result.success(comments);
    }

    @PostMapping("/{commentId}/like")
    public Result<Map<String, Object>> toggleCommentLike(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        boolean liked = commentService.toggleCommentLike(commentId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }

    @GetMapping("/{commentId}/like/status")
    public Result<Map<String, Boolean>> getCommentLikeStatus(@PathVariable Long commentId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = userId != null && commentService.getCommentLikeStatus(commentId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
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
