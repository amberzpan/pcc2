package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.PostRequest;
import com.pcc2.social.entity.Post;
import com.pcc2.social.service.PostService;
import com.pcc2.social.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;
    private final UserService userService;
    private final String uploadPath = "./uploads/images/";
    
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }
    
    @GetMapping("/list")
    public Result<?> getPostList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        var posts = postService.getPostList(page, size, userId);
        return Result.success(posts);
    }
    
    @GetMapping("/user/{userId}")
    public Result<?> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        var posts = postService.getUserPosts(userId, page, size, currentUserId);
        return Result.success(posts);
    }
    
    @GetMapping("/{id}")
    public Result<?> getPost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Post post = postService.getPostById(id, userId);
        if (post == null) {
            return Result.error("内容不存在");
        }
        return Result.success(post);
    }
    
    @PostMapping
    public Result<Post> createPost(@RequestBody PostRequest postRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        Post post = postService.createPost(userId, postRequest);
        return Result.success(post);
    }
    
    @DeleteMapping("/{id}")
    public Result<?> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        try {
            boolean success = postService.deletePost(id, userId);
            if (success) {
                return Result.success();
            } else {
                return Result.error("内容不存在");
            }
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.error("文件大小不能超过5MB");
        }
        
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path path = Paths.get(uploadPath + newFilename);
            Files.write(path, file.getBytes());
            
            Map<String, String> result = new HashMap<>();
            result.put("url", "/images/" + newFilename);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/uploadVideo")
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件为空");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return Result.error("只能上传视频文件");
        }
        
        if (file.getSize() > 50 * 1024 * 1024) {
            return Result.error("视频大小不能超过50MB");
        }
        
        String videoPath = "./uploads/videos/";
        try {
            File dir = new File(videoPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".mp4";
            String newFilename = UUID.randomUUID().toString() + extension;
            
            Path path = Paths.get(videoPath + newFilename);
            Files.write(path, file.getBytes());
            
            Map<String, String> result = new HashMap<>();
            result.put("url", "/videos/" + newFilename);
            return Result.success(result);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
