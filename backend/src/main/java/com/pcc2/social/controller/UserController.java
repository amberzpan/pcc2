package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.LoginRequest;
import com.pcc2.social.dto.LoginResponse;
import com.pcc2.social.dto.RegisterRequest;
import com.pcc2.social.dto.UserVO;
import com.pcc2.social.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/register")
    public Result<LoginResponse> register(@RequestBody RegisterRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }
        try {
            LoginResponse response = userService.register(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }
        try {
            LoginResponse response = userService.login(request.getUsername(), request.getPassword());
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Result<?> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            var user = userService.getCurrentUser(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, "token无效");
        }
    }

    @PutMapping("/info")
    public Result<?> updateUserInfo(@RequestBody UserVO requestBody, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            UserVO updated = userService.updateCurrentUser(userId, requestBody);
            return Result.success(updated);
        } catch (RuntimeException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @GetMapping("/search")
    public Result<?> searchUsers(@RequestParam String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.searchUsers(keyword, page, size));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
