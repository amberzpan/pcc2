package com.pcc2.social.mapper;

import com.pcc2.social.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CommentMapper {
    Comment findById(Long id);
    List<Comment> findByPostId(@Param("postId") Long postId);
    int insert(Comment comment);
    int delete(Long id);
    int updateLikeCount(@Param("id") Long id, @Param("count") Integer count);
    int countByPostId(Long postId);
}
