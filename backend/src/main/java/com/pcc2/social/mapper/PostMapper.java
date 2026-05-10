package com.pcc2.social.mapper;

import com.pcc2.social.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper {
    List<Post> findAll(@Param("offset") int offset, @Param("limit") int limit);
    List<Post> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    List<Post> findByFollowing(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    List<Post> findHot(@Param("offset") int offset, @Param("limit") int limit);
    List<Post> findTodayHot(@Param("offset") int offset, @Param("limit") int limit);
    List<Post> search(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    Post findById(Long id);
    int countByUserId(@Param("userId") Long userId);
    int insert(Post post);
    int delete(Long id);
    int updateLikeCount(@Param("id") Long id, @Param("count") Integer count);
    int updateCommentCount(@Param("id") Long id, @Param("count") Integer count);
    int updateRepostCount(@Param("id") Long id, @Param("count") Integer count);
}
