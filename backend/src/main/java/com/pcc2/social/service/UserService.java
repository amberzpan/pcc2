package com.pcc2.social.service;

import com.pcc2.social.dto.LoginResponse;
import com.pcc2.social.dto.RegisterRequest;
import com.pcc2.social.dto.UserVO;
import com.pcc2.social.entity.User;
import com.pcc2.social.mapper.FollowMapper;
import com.pcc2.social.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public UserService(UserMapper userMapper, FollowMapper followMapper) {
        this.userMapper = userMapper;
        this.followMapper = followMapper;
    }
    
    public LoginResponse register(RegisterRequest request) {
        validateRegisterRequest(request);
        User existUser = userMapper.findByUsername(request.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        
        userMapper.insert(user);
        
        String token = generateToken(user.getId(), user.getUsername());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(toUserVO(user));
        return response;
    }
    
    public LoginResponse login(String username, String password) {
        if (isBlank(username) || isBlank(password)) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        
        String token = generateToken(user.getId(), user.getUsername());
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(toUserVO(user));
        return response;
    }
    
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }
    
    public UserVO getCurrentUser(Long userId) {
        User user = userMapper.findById(userId);
        UserVO vo = toUserVO(user);
        if (vo != null) {
            vo.setFollowed(false);
        }
        return vo;
    }

    public UserVO getUserProfile(Long targetUserId, Long currentUserId) {
        User user = userMapper.findById(targetUserId);
        UserVO vo = toUserVO(user);
        if (vo == null) {
            throw new RuntimeException("用户不存在");
        }

        boolean followed = currentUserId != null
                && !currentUserId.equals(targetUserId)
                && followMapper.findByFollowerAndFollowing(currentUserId, targetUserId) != null;
        vo.setFollowed(followed);
        return vo;
    }

    public UserVO updateCurrentUser(Long userId, UserVO updateRequest) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!isBlank(updateRequest.getNickname())) {
            user.setNickname(updateRequest.getNickname().trim());
        }
        if (updateRequest.getAvatar() != null) {
            user.setAvatar(updateRequest.getAvatar().trim());
        }
        if (updateRequest.getBio() != null) {
            user.setBio(updateRequest.getBio().trim());
        }
        userMapper.update(user);
        return toUserVO(userMapper.findById(userId));
    }

    public List<User> searchUsers(String keyword, int page, int size) {
        if (isBlank(keyword)) {
            return java.util.Collections.emptyList();
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        return userMapper.searchByKeyword(keyword.trim(), offset, safeSize);
    }
    
    private UserVO toUserVO(User user) {
        if (user == null) return null;
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setBio(user.getBio());
        vo.setFollowersCount(user.getFollowersCount() == null ? 0 : user.getFollowersCount());
        vo.setFollowingCount(user.getFollowingCount() == null ? 0 : user.getFollowingCount());
        return vo;
    }
    
    private String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        String username = request.getUsername().trim();
        String password = request.getPassword();
        if (username.length() < 3 || username.length() > 20) {
            throw new RuntimeException("用户名长度需在3到20个字符之间");
        }
        if (password.length() < 8 || password.length() > 64) {
            throw new RuntimeException("密码长度需在8到64个字符之间");
        }
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new RuntimeException("密码需同时包含字母和数字");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    public Long getUserIdFromToken(String token) {
        var claims = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.get("userId", Long.class);
    }
}
