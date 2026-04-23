package com.pcc2.social.service;

import com.pcc2.social.dto.LoginResponse;
import com.pcc2.social.dto.RegisterRequest;
import com.pcc2.social.entity.User;
import com.pcc2.social.mapper.FollowMapper;
import com.pcc2.social.mapper.PostMapper;
import com.pcc2.social.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserSchemaCompatibilityService schemaCompatibilityService;

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
        request.setUsername("user1");
        request.setPassword("123");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));

        assertTrue(ex.getMessage().contains("密码长度"));
        verify(userMapper, never()).findByUsername(anyString());
    }

    @Test
    void registerShouldRejectPasswordWithoutLetter() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user2");
        request.setPassword("12345678");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));

        assertTrue(ex.getMessage().contains("字母和数字"));
        verify(userMapper, never()).findByUsername(anyString());
    }

    @Test
    void registerShouldRejectPasswordWithoutDigit() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user3");
        request.setPassword("abcdefgh");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.register(request));

        assertTrue(ex.getMessage().contains("字母和数字"));
        verify(userMapper, never()).findByUsername(anyString());
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
        request.setPassword("abc12345");
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
        verify(schemaCompatibilityService, atLeastOnce()).apply();
        verify(userMapper).update(any(User.class));
    }

    @Test
    void getUserProfileShouldExposeFollowCountsAndFollowStatus() {
        User target = new User();
        target.setId(11L);
        target.setUsername("target");
        target.setFollowersCount(9);
        target.setFollowingCount(3);

        when(userMapper.findById(11L)).thenReturn(target);
        when(followMapper.findByFollowerAndFollowing(2L, 11L)).thenReturn(new com.pcc2.social.entity.Follow());

        com.pcc2.social.dto.UserVO profile = userService.getUserProfile(11L, 2L);

        org.junit.jupiter.api.Assertions.assertEquals(9, profile.getFollowersCount());
        org.junit.jupiter.api.Assertions.assertEquals(3, profile.getFollowingCount());
        org.junit.jupiter.api.Assertions.assertTrue(profile.getFollowed());
    }

    @Test
    void loginTokenShouldNotContainRawPassword() {
        configureJwtFields();
        User user = new User();
        user.setId(20L);
        user.setUsername("safe-user");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("abc12345"));

        when(userMapper.findByUsername("safe-user")).thenReturn(user);

        LoginResponse response = userService.login("safe-user", "abc12345");

        assertNotNull(response.getToken());
        assertFalse(response.getToken().contains("abc12345"));
    }

    @Test
    void updateCurrentUserShouldRejectBlankNickname() {
        User existing = new User();
        existing.setId(3L);
        existing.setUsername("u3");
        existing.setNickname("old");

        com.pcc2.social.dto.UserVO req = new com.pcc2.social.dto.UserVO();
        req.setNickname("  ");

        when(userMapper.findById(3L)).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.updateCurrentUser(3L, req));

        assertTrue(ex.getMessage().contains("昵称不能为空"));
        verify(userMapper, never()).update(any(User.class));
    }

    @Test
    void changePasswordShouldPersistEncodedPasswordWhenOldPasswordMatches() {
        User existing = new User();
        existing.setId(9L);
        existing.setUsername("demo9");
        existing.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("abc12345"));

        when(userMapper.findById(9L)).thenReturn(existing);

        userService.changePassword(9L, "abc12345", "new12345");

        verify(userMapper).updatePassword(eq(9L), anyString());
    }

    @Test
    void changePasswordShouldRejectOverlongPassword() {
        User existing = new User();
        existing.setId(10L);
        existing.setUsername("demo10");
        existing.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("abc12345"));

        when(userMapper.findById(10L)).thenReturn(existing);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.changePassword(10L, "abc12345", "ab12345678901234567890123X"));

        assertTrue(ex.getMessage().contains("8-24"));
        verify(userMapper, never()).updatePassword(eq(10L), anyString());
    }
}
