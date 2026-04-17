package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.LoginResponse;
import com.pcc2.social.dto.RegisterRequest;
import com.pcc2.social.service.UserService;
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
        try {
            LoginResponse response = userService.register(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody RegisterRequest request) {
        try {
            LoginResponse response = userService.login(request.getUsername(), request.getPassword());
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/info")
    public Result<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return Result.error(401, "未登录");
        }
        try {
            token = token.substring(7);
            Long userId = userService.getUserIdFromToken(token);
            var user = userService.getCurrentUser(userId);
            return Result.success(user);
        } catch (Exception e) {
            return Result.error(401, "token无效");
        }
    }
}