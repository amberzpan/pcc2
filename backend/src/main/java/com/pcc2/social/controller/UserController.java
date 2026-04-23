package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.ChangePasswordRequest;
import com.pcc2.social.dto.ForgotPasswordRequest;
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
        if (containsSensitiveChars(request.getPassword())) {
            return Result.error("密码包含非法字符");
        }
        String passwordPolicyError = validateRegisterPasswordPolicy(request.getPassword());
        if (passwordPolicyError != null) {
            return Result.error(passwordPolicyError);
        }
        try {
            LoginResponse response = userService.register(request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(isBlank(e.getMessage()) ? "注册失败" : e.getMessage());
        }
    }
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return Result.error("用户名和密码不能为空");
        }
        if (containsSensitiveChars(request.getPassword())) {
            return Result.error("密码包含非法字符");
        }
        try {
            LoginResponse response = userService.login(request.getUsername(), request.getPassword());
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(isBlank(e.getMessage()) ? "登录失败" : e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public Result<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        String validateMessage = userService.validateForgotPasswordRequest(request);
        if (validateMessage != null) {
            return Result.error(validateMessage);
        }
        if (containsSensitiveChars(request.getNewPassword())) {
            return Result.error("密码包含非法字符");
        }
        try {
            userService.resetPassword(request);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(isBlank(e.getMessage()) ? "重置失败" : e.getMessage());
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
        if (requestBody == null || isBlank(requestBody.getNickname())) {
            return Result.error("昵称不能为空");
        }
        try {
            UserVO updated = userService.updateCurrentUser(userId, requestBody);
            return Result.success(updated);
        } catch (RuntimeException ex) {
            return Result.error(ex.getMessage());
        }
    }

    @PutMapping("/password")
    public Result<?> changePassword(@RequestBody ChangePasswordRequest requestBody, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        String validateMessage = userService.validatePasswordRequest(requestBody);
        if (validateMessage != null) {
            return Result.error(validateMessage);
        }
        try {
            userService.changePassword(userId, requestBody.getOldPassword(), requestBody.getNewPassword());
            return Result.success();
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

    @GetMapping("/{userId}/profile")
    public Result<?> getUserProfile(@PathVariable Long userId, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        try {
            return Result.success(userService.getUserProfile(userId, currentUserId));
        } catch (RuntimeException ex) {
            return Result.error(ex.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean containsSensitiveChars(String value) {
        if (value == null) {
            return false;
        }
        return value.contains("\n") || value.contains("\r") || value.contains("\t");
    }

    private String validateRegisterPasswordPolicy(String password) {
        if (password == null) {
            return "用户名和密码不能为空";
        }
        if (password.length() < 8 || password.length() > 24) {
            return "密码长度需在8到24个字符之间";
        }
        boolean hasLetter = password.matches(".*[A-Za-z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        if (!hasLetter || !hasDigit) {
            return "密码需同时包含字母和数字";
        }
        return null;
    }
}
