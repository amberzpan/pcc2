package com.pcc2.social.controller;

import com.pcc2.social.dto.LoginRequest;
import com.pcc2.social.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerSecurityTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldNotContainRawPasswordInLoginErrorMessage() {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("my-secret-password");

        when(userService.login("demo", "my-secret-password")).thenThrow(new RuntimeException("failed"));

        var result = userController.login(request);

        assertFalse(result.getMessage().contains("my-secret-password"));
        verify(userService).login("demo", "my-secret-password");
    }

    @Test
    void shouldNotContainRawPasswordInRegisterErrorMessage() {
        com.pcc2.social.dto.RegisterRequest request = new com.pcc2.social.dto.RegisterRequest();
        String rawPassword = "my-secret-123";
        request.setUsername("demo");
        request.setPassword(rawPassword);

        when(userService.register(request)).thenThrow(new RuntimeException("failed"));

        var result = userController.register(request);

        assertTrue(result.getCode() == 400);
        assertFalse(result.getMessage().contains(rawPassword));
        verify(userService).register(request);
    }

    @Test
    void shouldRejectControlCharsInPasswordBeforeServiceCall() {
        LoginRequest request = new LoginRequest();
        request.setUsername("demo");
        request.setPassword("abc\n123");

        var result = userController.login(request);

        assertTrue(result.getCode() == 400);
        assertTrue(result.getMessage().contains("密码包含非法字符"));
        verify(userService, never()).login("demo", "abc\n123");
    }
}
