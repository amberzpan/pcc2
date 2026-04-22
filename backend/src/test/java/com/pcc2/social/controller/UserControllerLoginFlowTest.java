package com.pcc2.social.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pcc2.social.dto.LoginRequest;
import com.pcc2.social.dto.LoginResponse;
import com.pcc2.social.dto.RegisterRequest;
import com.pcc2.social.dto.UserVO;
import com.pcc2.social.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerLoginFlowTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerShouldReturnTokenWhenServiceReturnsLoginResponse() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        UserVO vo = new UserVO();
        vo.setId(7L);
        vo.setUsername("new_user");
        vo.setNickname("New User");

        LoginResponse response = new LoginResponse();
        response.setToken("token-register-123");
        response.setUser(vo);
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        RegisterRequest req = new RegisterRequest();
        req.setUsername("new_user");
        req.setPassword("abc12345");
        req.setNickname("New User");

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token-register-123"))
                .andExpect(jsonPath("$.data.user.username").value("new_user"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void loginShouldReturnTokenWhenServiceReturnsLoginResponse() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        UserVO vo = new UserVO();
        vo.setId(8L);
        vo.setUsername("aaa");
        vo.setNickname("aaa");

        LoginResponse response = new LoginResponse();
        response.setToken("token-login-456");
        response.setUser(vo);
        when(userService.login(eq("aaa"), eq("aaa12345"))).thenReturn(response);

        LoginRequest req = new LoginRequest();
        req.setUsername("aaa");
        req.setPassword("aaa12345");

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").value("token-login-456"))
                .andExpect(jsonPath("$.data.user.id").value(8));

        verify(userService).login("aaa", "aaa12345");
    }

    @Test
    void changePasswordShouldCallServiceWhenPayloadValid() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put("/api/user/password")
                        .requestAttr("userId", 8L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"oldPassword\":\"aaa12345\",\"newPassword\":\"abc12345\",\"confirmPassword\":\"abc12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(userService).changePassword(8L, "aaa12345", "abc12345");
    }
}
