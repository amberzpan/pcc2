package com.pcc2.social.service;

import com.pcc2.social.dto.PostRequest;
import com.pcc2.social.entity.Post;
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
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = postMapper.findAll(offset, safeSize);
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
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = postMapper.findByUserId(userId, offset, safeSize);
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

    public List<Post> getFollowingPosts(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = postMapper.findByFollowing(userId, offset, safeSize);
        for (Post post : posts) {
            post.setLiked(likeRecordMapper.findByPostIdAndUserId(post.getId(), userId) != null);
            post.setFavorited(favoriteMapper.findByUserAndPost(userId, post.getId()) != null);
            if (!post.getUserId().equals(userId)) {
                post.setFollowed(followMapper.findByFollowerAndFollowing(userId, post.getUserId()) != null);
            }
        }
        return posts;
    }

    public List<Post> getHotPosts(int page, int size, Long currentUserId) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = postMapper.findHot(offset, safeSize);
        hydratePostStatus(posts, currentUserId);
        return posts;
    }

    public List<Post> getDiscoverPosts(int page, int size, Long currentUserId) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = postMapper.findDiscover(currentUserId, offset, safeSize);
        hydratePostStatus(posts, currentUserId);
        return posts;
    }

    public List<Post> searchPosts(String keyword, int page, int size, Long currentUserId) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return java.util.Collections.emptyList();
        }
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = postMapper.search(keyword.trim(), offset, safeSize);
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
        if (request == null) {
            throw new RuntimeException("请求不能为空");
        }

        boolean hasText = request.getContent() != null && !request.getContent().trim().isEmpty();
        boolean hasMedia = request.getMediaUrl() != null && !request.getMediaUrl().trim().isEmpty();
        boolean isRepost = request.getRepostId() != null;
        if (!hasText && !hasMedia && !isRepost) {
            throw new RuntimeException("内容不能为空");
        }

        Post post = new Post();
        post.setUserId(userId);
        post.setContent(request.getContent() == null ? "" : request.getContent().trim());
        if (request.getMediaUrl() != null && !request.getMediaUrl().trim().isEmpty()) {
            post.setMediaUrl(request.getMediaUrl());
        } else {
            post.setMediaUrl(request.getImageUrl());
        }
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
        
        return postMapper.findById(post.getId());
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

    private void hydratePostStatus(List<Post> posts, Long currentUserId) {
        if (currentUserId == null) {
            for (Post post : posts) {
                post.setLiked(false);
                post.setFavorited(false);
                post.setFollowed(false);
            }
            return;
        }
        for (Post post : posts) {
            post.setLiked(likeRecordMapper.findByPostIdAndUserId(post.getId(), currentUserId) != null);
            post.setFavorited(favoriteMapper.findByUserAndPost(currentUserId, post.getId()) != null);
            if (!post.getUserId().equals(currentUserId)) {
                post.setFollowed(followMapper.findByFollowerAndFollowing(currentUserId, post.getUserId()) != null);
            } else {
                post.setFollowed(false);
            }
        }
    }
}
