package com.pcc2.social.controller;

import com.pcc2.social.dto.UserVO;
import com.pcc2.social.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerProfileTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void getUserProfileShouldPassCurrentUserIdToService() {
        UserVO profile = new UserVO();
        profile.setId(6L);
        profile.setUsername("target");
        profile.setFollowersCount(10);
        profile.setFollowingCount(8);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 2L);
        when(userService.getUserProfile(6L, 2L)).thenReturn(profile);

        var result = userController.getUserProfile(6L, request);

        assertEquals(200, result.getCode());
        verify(userService).getUserProfile(6L, 2L);
    }

    @Test
    void getUserProfileShouldAllowAnonymousVisitor() {
        UserVO profile = new UserVO();
        profile.setId(7L);
        when(userService.getUserProfile(7L, null)).thenReturn(profile);

        MockHttpServletRequest request = new MockHttpServletRequest();
        var result = userController.getUserProfile(7L, request);

        assertEquals(200, result.getCode());
        verify(userService).getUserProfile(7L, null);
    }
}
