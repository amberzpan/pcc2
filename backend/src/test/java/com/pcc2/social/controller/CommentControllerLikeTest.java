package com.pcc2.social.controller;

import com.pcc2.social.service.CommentService;
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
class CommentControllerLikeTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @Test
    void toggleCommentLikeShouldReturnLikedState() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 9L);
        when(commentService.toggleCommentLike(33L, 9L)).thenReturn(true);

        var result = commentController.toggleCommentLike(33L, request);

        assertEquals(200, result.getCode());
        @SuppressWarnings("unchecked")
        var data = (java.util.Map<String, Object>) result.getData();
        assertEquals(true, data.get("liked"));
        verify(commentService).toggleCommentLike(33L, 9L);
    }

    @Test
    void getCommentsShouldPassCurrentUserToService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userId", 5L);
        when(commentService.getCommentsByPostId(7L, "hot", 5L)).thenReturn(java.util.List.of());

        var result = commentController.getComments(7L, "hot", request);

        assertEquals(200, result.getCode());
        verify(commentService).getCommentsByPostId(7L, "hot", 5L);
    }

    @Test
    void toggleCommentLikeShouldRejectWhenNotLoggedIn() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        var result = commentController.toggleCommentLike(12L, request);

        assertEquals(401, result.getCode());
    }
}
