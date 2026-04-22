package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.service.LikeService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/like")
public class LikeController {
    private final LikeService likeService;
    
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }
    
    @PostMapping("/{postId}")
    public Result<Map<String, Object>> toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        
        boolean liked = likeService.toggleLike(postId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }
    
    @GetMapping("/status/{postId}")
    public Result<Map<String, Boolean>> getLikeStatus(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            Map<String, Boolean> result = new HashMap<>();
            result.put("liked", false);
            return Result.success(result);
        }
        
        boolean liked = likeService.getLikeStatus(postId, userId);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<?> getLikedPosts(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(likeService.getLikedPosts(userId, page, size));
    }
}
