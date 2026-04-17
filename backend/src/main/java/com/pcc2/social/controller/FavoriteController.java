package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.entity.Post;
import com.pcc2.social.service.FavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{postId}")
    public Result<Map<String, Object>> toggleFavorite(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        boolean favorited = favoriteService.toggleFavorite(userId, postId);
        Map<String, Object> result = new HashMap<>();
        result.put("favorited", favorited);
        return Result.success(result);
    }

    @GetMapping("/status/{postId}")
    public Result<Map<String, Boolean>> getFavoriteStatus(@PathVariable Long postId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorited = false;
        if (userId != null) {
            favorited = favoriteService.getFavoriteStatus(userId, postId);
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", favorited);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<Post>> getFavorites(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        List<Post> posts = favoriteService.getFavorites(userId, page, limit, userId);
        return Result.success(posts);
    }
}