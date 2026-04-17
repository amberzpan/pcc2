package com.pcc2.social.service;

import com.pcc2.social.dto.PostRequest;
import com.pcc2.social.entity.Post;
import com.pcc2.social.entity.User;
import com.pcc2.social.mapper.FavoriteMapper;
import com.pcc2.social.mapper.FollowMapper;
import com.pcc2.social.mapper.LikeRecordMapper;
import com.pcc2.social.mapper.PostMapper;
import com.pcc2.social.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class PostService {
    private final PostMapper postMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final UserMapper userMapper;
    private final FollowMapper followMapper;
    private final FavoriteMapper favoriteMapper;
    
    public PostService(PostMapper postMapper, LikeRecordMapper likeRecordMapper, UserMapper userMapper, FollowMapper followMapper, FavoriteMapper favoriteMapper) {
        this.postMapper = postMapper;
        this.likeRecordMapper = likeRecordMapper;
        this.userMapper = userMapper;
        this.followMapper = followMapper;
        this.favoriteMapper = favoriteMapper;
    }
    
    public List<Post> getPostList(int page, int size, Long currentUserId) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.findAll(offset, size);
        if (currentUserId != null) {
            for (Post post : posts) {
                post.setLiked(likeRecordMapper.findByPostIdAndUserId(post.getId(), currentUserId) != null);
                post.setFavorited(favoriteMapper.findByUserAndPost(currentUserId, post.getId()) != null);
                if (!post.getUserId().equals(currentUserId)) {
                    post.setFollowed(followMapper.findByFollowerAndFollowing(currentUserId, post.getUserId()) != null);
                }
            }
        }
        return posts;
    }
    
    public List<Post> getUserPosts(Long userId, int page, int size, Long currentUserId) {
        int offset = (page - 1) * size;
        List<Post> posts = postMapper.findByUserId(userId, offset, size);
        if (currentUserId != null) {
            for (Post post : posts) {
                post.setLiked(likeRecordMapper.findByPostIdAndUserId(post.getId(), currentUserId) != null);
                post.setFavorited(favoriteMapper.findByUserAndPost(currentUserId, post.getId()) != null);
                if (!post.getUserId().equals(currentUserId)) {
                    post.setFollowed(followMapper.findByFollowerAndFollowing(currentUserId, post.getUserId()) != null);
                }
            }
        }
        return posts;
    }
    
    public Post getPostById(Long id, Long currentUserId) {
        Post post = postMapper.findById(id);
        if (post != null && currentUserId != null) {
            post.setLiked(likeRecordMapper.findByPostIdAndUserId(id, currentUserId) != null);
            post.setFavorited(favoriteMapper.findByUserAndPost(currentUserId, id) != null);
            if (!post.getUserId().equals(currentUserId)) {
                post.setFollowed(followMapper.findByFollowerAndFollowing(currentUserId, post.getUserId()) != null);
            }
        }
        return post;
    }
    
    @Transactional
    public Post createPost(Long userId, PostRequest request) {
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(request.getContent());
        post.setMediaUrl(request.getMediaUrl());
        post.setMediaType(request.getMediaType());
        
        if (request.getRepostId() != null) {
            Post originalPost = postMapper.findById(request.getRepostId());
            if (originalPost != null) {
                post.setRepostId(request.getRepostId());
                post.setOriginalContent(originalPost.getContent());
                if (request.getContent() == null || request.getContent().isEmpty()) {
                    post.setContent("");
                }
                postMapper.updateRepostCount(request.getRepostId(), originalPost.getRepostCount() + 1);
            }
        }
        
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setRepostCount(0);
        
        postMapper.insert(post);
        
        Post createdPost = postMapper.findById(post.getId());
        User user = userMapper.findById(userId);
        if (user != null) {
            createdPost.setUsername(user.getUsername());
            createdPost.setNickname(user.getNickname());
            createdPost.setAvatar(user.getAvatar());
        }
        return createdPost;
    }
    
    @Transactional
    public boolean deletePost(Long id, Long userId) {
        Post post = postMapper.findById(id);
        if (post == null) {
            return false;
        }
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除");
        }
        postMapper.delete(id);
        return true;
    }
    
    public void updateLikeCount(Long postId, int count) {
        postMapper.updateLikeCount(postId, count);
    }
    
    public void updateCommentCount(Long postId, int count) {
        postMapper.updateCommentCount(postId, count);
    }
}
