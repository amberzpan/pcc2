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
import static org.mockito.ArgumentMatchers.any;
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
}
