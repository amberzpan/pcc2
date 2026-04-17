package com.pcc2.social.mapper;

import com.pcc2.social.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeRecordMapper {
    LikeRecord findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    int insert(LikeRecord likeRecord);
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);
    int countByPostId(Long postId);
}
