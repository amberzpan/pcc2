package com.pcc2.social.mapper;

import com.pcc2.social.entity.Favorite;
import com.pcc2.social.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FavoriteMapper {
    int insert(Favorite favorite);
    int delete(@Param("userId") Long userId, @Param("postId") Long postId);
    Favorite findByUserAndPost(@Param("userId") Long userId, @Param("postId") Long postId);
    List<Post> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    int countByUserId(@Param("userId") Long userId);
}