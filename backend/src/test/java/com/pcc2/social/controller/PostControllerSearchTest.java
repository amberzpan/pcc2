package com.pcc2.social.controller;

import com.pcc2.social.entity.Post;
import com.pcc2.social.service.PostService;
import com.pcc2.social.service.UserService;
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

    @Mock
    private UserService userService;

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
}
