package com.pcc2.social.controller;

import com.pcc2.social.entity.Post;
import com.pcc2.social.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostControllerSearchTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostController postController;

    @Test
    void getPostListShouldPassCurrentUserToService() {
        List<Post> posts = new ArrayList<>();
        Post post = new Post();
        post.setId(1L);
        posts.add(post);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 99L);

        when(postService.getPostList(1, 10, 99L)).thenReturn(posts);

        var result = postController.getPostList(1, 10, request);

        assertEquals(200, result.getCode());
        assertEquals(1, ((List<?>) result.getData()).size());
        verify(postService).getPostList(1, 10, 99L);
    }

    @Test
    void searchShouldUseServiceAndReturnSuccess() {
        List<Post> posts = new ArrayList<>();
        Post post = new Post();
        post.setId(2L);
        posts.add(post);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 7L);
        when(postService.searchPosts("hello", 1, 10, 7L)).thenReturn(posts);

        var result = postController.searchPosts("hello", 1, 10, request);

        assertEquals(200, result.getCode());
        assertEquals(1, ((List<?>) result.getData()).size());
        verify(postService).searchPosts("hello", 1, 10, 7L);
    }

    @Test
    void hotShouldUseServiceAndReturnSuccess() {
        List<Post> posts = new ArrayList<>();
        Post post = new Post();
        post.setId(3L);
        posts.add(post);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 8L);
        when(postService.getHotPosts(1, 10, 8L)).thenReturn(posts);

        var result = postController.getHotPosts(1, 10, request);

        assertEquals(200, result.getCode());
        verify(postService).getHotPosts(1, 10, 8L);
    }

    @Test
    void todayHotShouldUseServiceAndReturnSuccess() {
        List<Post> posts = new ArrayList<>();
        Post post = new Post();
        post.setId(4L);
        posts.add(post);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 8L);
        when(postService.getTodayHotPosts(1, 10, 8L)).thenReturn(posts);

        var result = postController.getTodayHotPosts(1, 10, request);

        assertEquals(200, result.getCode());
        verify(postService).getTodayHotPosts(1, 10, 8L);
    }

    @Test
    void followingShouldRejectAnonymousVisitor() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var result = postController.getFollowingPosts(1, 10, request);

        assertEquals(401, result.getCode());
    }
}
