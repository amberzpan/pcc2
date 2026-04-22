package com.pcc2.social.mapper;

import com.pcc2.social.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    User findById(Long id);
    java.util.List<User> searchByKeyword(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    int insert(User user);
    int update(User user);
    int updatePassword(@Param("id") Long id, @Param("password") String password);
    int incrementFollowersCount(Long id);
    int decrementFollowersCount(Long id);
    int incrementFollowingCount(Long id);
    int decrementFollowingCount(Long id);
}
