package com.pcc2.social.mapper;

import com.pcc2.social.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FollowMapper {
    int insert(Follow follow);
    int delete(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    Follow findByFollowerAndFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
    List<Follow> findFollowersByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    List<Follow> findFollowingByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    int countFollowers(@Param("userId") Long userId);
    int countFollowing(@Param("userId") Long userId);
}