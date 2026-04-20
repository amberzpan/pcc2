package com.pcc2.social.controller;

import com.pcc2.social.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UserControllerValidationTest {

    @Test
    void registerShouldRejectBlankUsername() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"abc12345\",\"nickname\":\"n\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verify(userService, never()).register(any());
    }

    @Test
    void loginShouldRejectBlankPassword() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400));

        verify(userService, never()).login(anyString(), anyString());
    }

    @Test
    void loginShouldUseLoginRequestAndCallService() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"abc12345\"}"))
                .andExpect(status().isOk());

        verify(userService).login("demo", "abc12345");
    }

    @Test
    void loginShouldReturnFallbackMessageWhenExceptionMessageIsNull() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(userService.login("demo", "abc12345")).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"abc12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("登录失败"));
    }

    @Test
    void registerShouldReturnFallbackMessageWhenExceptionMessageIsNull() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        when(userService.register(any())).thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"abc12345\",\"nickname\":\"d\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("注册失败"));
    }

    @Test
    void registerShouldRejectWeakPasswordMissingLetters() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo_user\",\"password\":\"12345678\",\"nickname\":\"d\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码需同时包含字母和数字"));

        verify(userService, never()).register(any());
    }

    @Test
    void registerShouldRejectWeakPasswordMissingDigits() throws Exception {
        UserService userService = mock(UserService.class);
        UserController controller = new UserController(userService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo_user\",\"password\":\"abcdefgh\",\"nickname\":\"d\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("密码需同时包含字母和数字"));

        verify(userService, never()).register(any());
    }
}
