package com.pcc2.social.mapper;

import com.pcc2.social.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByUsername(String username);
    User findById(Long id);
    int insert(User user);
    int update(User user);
    int incrementFollowersCount(Long id);
    int decrementFollowersCount(Long id);
    int incrementFollowingCount(Long id);
    int decrementFollowingCount(Long id);
}
