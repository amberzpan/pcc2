package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.entity.Follow;
import com.pcc2.social.service.FollowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/follow")
public class FollowController {
    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{userId}")
    public Result<Map<String, Object>> toggleFollow(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        if (currentUserId == null) {
            return Result.error(401, "未登录");
        }
        boolean followed = followService.toggleFollow(currentUserId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("followed", followed);
        return Result.success(result);
    }

    @GetMapping("/status/{userId}")
    public Result<Map<String, Boolean>> getFollowStatus(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        boolean followed = false;
        if (currentUserId != null) {
            followed = followService.getFollowStatus(currentUserId, userId);
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("followed", followed);
        return Result.success(result);
    }

    @GetMapping("/followers/{userId}")
    public Result<List<Follow>> getFollowers(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        List<Follow> followers = followService.getFollowers(userId, page, limit);
        return Result.success(followers);
    }

    @GetMapping("/following/{userId}")
    public Result<List<Follow>> getFollowing(@PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit) {
        List<Follow> following = followService.getFollowing(userId, page, limit);
        return Result.success(following);
    }
}