package com.pcc2.social.controller;

import com.pcc2.social.entity.Post;
import com.pcc2.social.service.LikeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LikeControllerListTest {

    @Mock
    private LikeService likeService;

    @InjectMocks
    private LikeController likeController;

    @Test
    void likedListShouldRejectAnonymousUser() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var result = likeController.getLikedPosts(1, 10, request);

        assertEquals(401, result.getCode());
    }

    @Test
    void likedListShouldReturnPostsWhenLoggedIn() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 6L);
        Post post = new Post();
        post.setId(101L);
        when(likeService.getLikedPosts(6L, 1, 10)).thenReturn(List.of(post));

        var result = likeController.getLikedPosts(1, 10, request);

        assertEquals(200, result.getCode());
        verify(likeService).getLikedPosts(6L, 1, 10);
    }
}
