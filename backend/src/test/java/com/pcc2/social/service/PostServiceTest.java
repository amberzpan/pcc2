package com.pcc2.social.service;

import com.pcc2.social.dto.PostRequest;
import com.pcc2.social.entity.Post;
import com.pcc2.social.mapper.FavoriteMapper;
import com.pcc2.social.mapper.FollowMapper;
import com.pcc2.social.mapper.LikeRecordMapper;
import com.pcc2.social.mapper.PostMapper;
import com.pcc2.social.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FollowMapper followMapper;

    @Mock
    private FavoriteMapper favoriteMapper;

    @InjectMocks
    private PostService postService;

    @Test
    void createPostShouldPersistImageUrlIntoMediaUrl() {
        Long userId = 1L;
        PostRequest request = new PostRequest();
        request.setContent("hello");
        request.setImageUrl("/images/a.png");
        request.setMediaType("image");

        Post created = new Post();
        created.setId(100L);

        when(postMapper.findById(100L)).thenReturn(created);
        when(postMapper.insert(any(Post.class))).thenAnswer(invocation -> {
            Post arg = invocation.getArgument(0);
            arg.setId(100L);
            return 1;
        });

        postService.createPost(userId, request);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        org.mockito.Mockito.verify(postMapper).insert(captor.capture());
        assertEquals("/images/a.png", captor.getValue().getMediaUrl());
    }

    @Test
    void createPostShouldPersistMultipleImagesIntoMediaUrlJson() {
        Long userId = 1L;
        PostRequest request = new PostRequest();
        request.setContent("hello");
        request.setMediaType("image");
        request.setMediaUrls(java.util.List.of("/images/a.png", "/images/b.png"));

        Post created = new Post();
        created.setId(101L);

        when(postMapper.findById(101L)).thenReturn(created);
        when(postMapper.insert(any(Post.class))).thenAnswer(invocation -> {
            Post arg = invocation.getArgument(0);
            arg.setId(101L);
            return 1;
        });

        postService.createPost(userId, request);

        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        org.mockito.Mockito.verify(postMapper).insert(captor.capture());
        assertEquals("[\"/images/a.png\",\"/images/b.png\"]", captor.getValue().getMediaUrl());
        assertEquals("image", captor.getValue().getMediaType());
    }

    @Test
    void getHotPostsShouldMarkStatusAsFalseWhenAnonymous() {
        Post post = new Post();
        post.setId(20L);
        post.setUserId(5L);
        when(postMapper.findHot(anyInt(), anyInt())).thenReturn(java.util.List.of(post));

        java.util.List<Post> result = postService.getHotPosts(1, 10, null);

        assertEquals(1, result.size());
        assertFalse(result.get(0).getLiked());
        assertFalse(result.get(0).getFavorited());
        assertFalse(result.get(0).getFollowed());
    }

    @Test
    void getTodayHotPostsShouldUseTodayHotMapperAndClampPageSize() {
        Post post = new Post();
        post.setId(21L);
        post.setUserId(6L);
        when(postMapper.findTodayHot(0, 50)).thenReturn(java.util.List.of(post));

        java.util.List<Post> result = postService.getTodayHotPosts(0, 60, null);

        assertEquals(1, result.size());
        org.mockito.Mockito.verify(postMapper).findTodayHot(0, 50);
        org.mockito.Mockito.verify(postMapper, org.mockito.Mockito.never()).findHot(anyInt(), anyInt());
    }

    @Test
    void getPostListShouldUseAllScoreOrderingFromMapper() {
        Post post = new Post();
        post.setId(66L);
        post.setUserId(3L);
        post.setLikeCount(10);
        post.setCommentCount(4);
        post.setRepostCount(2);
        post.setAllScore(8.6);
        when(postMapper.findAll(anyInt(), anyInt())).thenReturn(java.util.List.of(post));

        java.util.List<Post> result = postService.getPostList(1, 10, null);

        assertEquals(1, result.size());
        assertTrue(result.get(0).getAllScore() > 0);
    }
}
