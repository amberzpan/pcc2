package com.pcc2.social.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentLikeMapper {
    int insert(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int delete(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int exists(@Param("commentId") Long commentId, @Param("userId") Long userId);

    int countByCommentId(@Param("commentId") Long commentId);
}
