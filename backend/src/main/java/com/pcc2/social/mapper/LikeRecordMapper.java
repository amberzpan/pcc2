package com.pcc2.social.mapper;

import com.pcc2.social.entity.LikeRecord;
import com.pcc2.social.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LikeRecordMapper {
    LikeRecord findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    int insert(LikeRecord likeRecord);
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);
    int countByPostId(Long postId);
    List<Post> findLikedPosts(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
}
