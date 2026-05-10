package com.pcc2.social.controller;

import com.pcc2.social.common.Result;
import com.pcc2.social.dto.PostRequest;
import com.pcc2.social.entity.Post;
import com.pcc2.social.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/post")
public class PostController {
    private final PostService postService;
    private final String uploadPath = "./uploads/images/";
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(".mp4", ".mov", ".webm", ".avi", ".mkv");
    
    public PostController(PostService postService) {
        this.postService = postService;
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

    @GetMapping("/search")
    public Result<?> searchPosts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.searchPosts(keyword, page, size, userId));
    }

    @GetMapping("/following")
    public Result<?> getFollowingPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(postService.getFollowingPosts(userId, page, size));
    }

    @GetMapping("/hot")
    public Result<?> getHotPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.getHotPosts(page, size, userId));
    }

    @GetMapping("/today-hot")
    public Result<?> getTodayHotPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(postService.getTodayHotPosts(page, size, userId));
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
        try {
            Post post = postService.createPost(userId, postRequest);
            return Result.success(post);
        } catch (RuntimeException ex) {
            return Result.error(ex.getMessage());
        }
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
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
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

        String extension = getSafeExtension(file.getOriginalFilename());
        if (!IMAGE_EXTENSIONS.contains(extension)) {
            return Result.error("不支持的图片格式");
        }
        
        try {
            File dir = new File(uploadPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
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
    public Result<Map<String, String>> uploadVideo(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
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

        String extension = getSafeExtension(file.getOriginalFilename());
        if (!VIDEO_EXTENSIONS.contains(extension)) {
            return Result.error("不支持的视频格式");
        }
        
        String videoPath = "./uploads/videos/";
        try {
            File dir = new File(videoPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
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

    private String getSafeExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot).toLowerCase(Locale.ROOT);
    }
}
