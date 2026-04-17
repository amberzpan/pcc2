package com.pcc2.social.service;

import com.pcc2.social.entity.LikeRecord;
import com.pcc2.social.mapper.LikeRecordMapper;
import com.pcc2.social.mapper.PostMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {
    private final LikeRecordMapper likeRecordMapper;
    private final PostMapper postMapper;
    
    public LikeService(LikeRecordMapper likeRecordMapper, PostMapper postMapper) {
        this.likeRecordMapper = likeRecordMapper;
        this.postMapper = postMapper;
    }
    
    @Transactional
    public boolean toggleLike(Long postId, Long userId) {
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
}
