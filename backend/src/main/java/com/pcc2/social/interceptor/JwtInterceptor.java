package com.pcc2.social.interceptor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class JwtInterceptor implements HandlerInterceptor {
    
    private final String jwtSecret = "pcc2-social-platform-secret-key-2024";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                
                Long userId = claims.get("userId", Long.class);
                String username = claims.getSubject();
                
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                
            } catch (Exception e) {
                response.setStatus(401);
                response.getWriter().write("{\"code\":401,\"message\":\"Token无效\",\"data\":null}");
                response.setContentType("application/json");
                return false;
            }
        }
        
        return true;
    }
}
