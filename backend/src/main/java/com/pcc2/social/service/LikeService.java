package com.pcc2.social.service;

import com.pcc2.social.entity.LikeRecord;
import com.pcc2.social.entity.Post;
import com.pcc2.social.mapper.FavoriteMapper;
import com.pcc2.social.mapper.FollowMapper;
import com.pcc2.social.mapper.LikeRecordMapper;
import com.pcc2.social.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LikeService {
    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    private final FavoriteMapper favoriteMapper;
    private final FollowMapper followMapper;
    
    public LikeService(LikeRecordMapper likeRecordMapper,
                       PostMapper postMapper,
                       FavoriteMapper favoriteMapper,
                       FollowMapper followMapper) {
        this.likeRecordMapper = likeRecordMapper;
        this.postMapper = postMapper;
        this.favoriteMapper = favoriteMapper;
        this.followMapper = followMapper;
    }
    
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
        if (postMapper.findById(postId) == null) {
            throw new RuntimeException("内容不存在");
        }
        LikeRecord existing = likeRecordMapper.findByPostIdAndUserId(postId, userId);
        
        if (existing != null) {
            likeRecordMapper.delete(postId, userId);
            int count = likeRecordMapper.countByPostId(postId);
            postMapper.updateLikeCount(postId, count);
            return false;
        } else {
            LikeRecord likeRecord = new LikeRecord();
            likeRecord.setPostId(postId);
            likeRecord.setUserId(userId);
            likeRecordMapper.insert(likeRecord);
            int count = likeRecordMapper.countByPostId(postId);
            postMapper.updateLikeCount(postId, count);
            return true;
        }
    }
    
    public boolean getLikeStatus(Long postId, Long userId) {
        return likeRecordMapper.findByPostIdAndUserId(postId, userId) != null;
    }

    public List<Post> getLikedPosts(Long userId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 50);
        int offset = (safePage - 1) * safeSize;
        List<Post> posts = likeRecordMapper.findLikedPosts(userId, offset, safeSize);
        for (Post post : posts) {
            post.setLiked(true);
            post.setFavorited(favoriteMapper.findByUserAndPost(userId, post.getId()) != null);
            if (!post.getUserId().equals(userId)) {
                post.setFollowed(followMapper.findByFollowerAndFollowing(userId, post.getUserId()) != null);
            } else {
                post.setFollowed(false);
            }
        }
        return posts;
    }
}
