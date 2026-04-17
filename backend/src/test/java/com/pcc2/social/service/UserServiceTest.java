package com.pcc2.social.service;

import com.pcc2.social.dto.LoginResponse;
import com.pcc2.social.dto.RegisterRequest;
import com.pcc2.social.entity.User;
import com.pcc2.social.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private void configureJwtFields() {
        try {
            Field secret = UserService.class.getDeclaredField("jwtSecret");
            secret.setAccessible(true);
            secret.set(userService, "pcc2-social-platform-secret-key-2024");

            Field expiration = UserService.class.getDeclaredField("jwtExpiration");
            expiration.setAccessible(true);
            expiration.setLong(userService, 604800000L);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void registerShouldRejectBlankUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("   ");
        request.setPassword("123456");

        assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    void registerShouldRejectShortPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("u");
        request.setPassword("123");

        assertThrows(RuntimeException.class, () -> userService.register(request));
    }

    @Test
    void loginShouldRejectBlankPassword() {
        assertThrows(RuntimeException.class, () -> userService.login("demo", "  "));
    }

    @Test
    void registerShouldReturnTokenWhenValid() {
        configureJwtFields();
        RegisterRequest request = new RegisterRequest();
        request.setUsername("userA");
        request.setPassword("123456");
        request.setNickname("A");

        when(userMapper.findByUsername("userA")).thenReturn(null);
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return 1;
        });

        LoginResponse response = userService.register(request);

        assertNotNull(response.getToken());
        assertNotNull(response.getUser());
    }

    @Test
    void updateCurrentUserShouldPersistProfileFields() {
        User existing = new User();
        existing.setId(5L);
        existing.setUsername("u5");
        existing.setNickname("old");

        User updated = new User();
        updated.setId(5L);
        updated.setUsername("u5");
        updated.setNickname("new-name");
        updated.setBio("new-bio");

        com.pcc2.social.dto.UserVO req = new com.pcc2.social.dto.UserVO();
        req.setNickname("new-name");
        req.setBio("new-bio");

        when(userMapper.findById(5L)).thenReturn(existing).thenReturn(updated);

        com.pcc2.social.dto.UserVO response = userService.updateCurrentUser(5L, req);

        assertNotNull(response);
        org.junit.jupiter.api.Assertions.assertEquals("new-name", response.getNickname());
        org.junit.jupiter.api.Assertions.assertEquals("new-bio", response.getBio());
        verify(userMapper).update(any(User.class));
    }
}
