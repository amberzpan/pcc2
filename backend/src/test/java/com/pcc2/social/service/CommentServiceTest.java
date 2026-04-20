package com.pcc2.social.service;

import com.pcc2.social.entity.Comment;
import com.pcc2.social.mapper.CommentLikeMapper;
import com.pcc2.social.mapper.CommentMapper;
import com.pcc2.social.mapper.PostMapper;
import com.pcc2.social.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CommentLikeMapper commentLikeMapper;

    @InjectMocks
    private CommentService commentService;

    @Test
    void toggleCommentLikeShouldInsertAndUpdateCountWhenUserHasNotLiked() {
        Comment comment = new Comment();
        comment.setId(10L);
        when(commentMapper.findById(10L)).thenReturn(comment);
        when(commentLikeMapper.exists(10L, 3L)).thenReturn(0);
        when(commentLikeMapper.countByCommentId(10L)).thenReturn(1);

        boolean liked = commentService.toggleCommentLike(10L, 3L);

        assertTrue(liked);
        verify(commentLikeMapper).insert(10L, 3L);
        verify(commentMapper).updateLikeCount(10L, 1);
        verify(commentLikeMapper, never()).delete(anyLong(), anyLong());
    }

    @Test
    void getCommentsByPostIdShouldSortByHotAndAttachLikedState() {
        Comment c1 = new Comment();
        c1.setId(1L);
        c1.setLikeCount(3);
        c1.setCreatedAt(LocalDateTime.now().minusMinutes(10));

        Comment c2 = new Comment();
        c2.setId(2L);
        c2.setLikeCount(9);
        c2.setCreatedAt(LocalDateTime.now().minusMinutes(20));

        when(commentMapper.findByPostId(99L)).thenReturn(List.of(c1, c2));
        when(commentLikeMapper.exists(1L, 7L)).thenReturn(0);
        when(commentLikeMapper.exists(2L, 7L)).thenReturn(1);

        List<Comment> comments = commentService.getCommentsByPostId(99L, "hot", 7L);

        assertEquals(2L, comments.get(0).getId());
        assertTrue(comments.get(0).getLiked());
    }

    @Test
    void toggleCommentLikeShouldDeleteAndUpdateCountWhenAlreadyLiked() {
        Comment comment = new Comment();
        comment.setId(11L);
        when(commentMapper.findById(11L)).thenReturn(comment);
        when(commentLikeMapper.exists(11L, 5L)).thenReturn(1);
        when(commentLikeMapper.countByCommentId(11L)).thenReturn(4);

        boolean liked = commentService.toggleCommentLike(11L, 5L);

        org.junit.jupiter.api.Assertions.assertFalse(liked);
        verify(commentLikeMapper).delete(11L, 5L);
        verify(commentMapper).updateLikeCount(11L, 4);
        verify(commentLikeMapper, never()).insert(anyLong(), anyLong());
    }
}
