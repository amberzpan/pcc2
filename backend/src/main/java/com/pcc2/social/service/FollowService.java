package com.pcc2.social.service;

import com.pcc2.social.entity.Follow;
import com.pcc2.social.mapper.FollowMapper;
import com.pcc2.social.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FollowService {
    private final FollowMapper followMapper;
    private final UserMapper userMapper;

    public FollowService(FollowMapper followMapper, UserMapper userMapper) {
        this.followMapper = followMapper;
        this.userMapper = userMapper;
    }

    @Transactional
    public boolean toggleFollow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("不能关注自己");
        }

        if (userMapper.findById(followingId) == null) {
            throw new RuntimeException("目标用户不存在");
        }

        Follow existing = followMapper.findByFollowerAndFollowing(followerId, followingId);
        if (existing != null) {
            followMapper.delete(followerId, followingId);
            userMapper.decrementFollowingCount(followerId);
            userMapper.decrementFollowersCount(followingId);
            return false;
        } else {
            Follow follow = new Follow();
            follow.setFollowerId(followerId);
            follow.setFollowingId(followingId);
            followMapper.insert(follow);
            userMapper.incrementFollowingCount(followerId);
            userMapper.incrementFollowersCount(followingId);
            return true;
        }
    }

    public boolean getFollowStatus(Long followerId, Long followingId) {
        return followMapper.findByFollowerAndFollowing(followerId, followingId) != null;
    }

    public List<Follow> getFollowers(Long userId, int page, int limit) {
        int offset = (page - 1) * limit;
        return followMapper.findFollowersByUserId(userId, offset, limit);
    }

    public List<Follow> getFollowing(Long userId, int page, int limit) {
        int offset = (page - 1) * limit;
        return followMapper.findFollowingByUserId(userId, offset, limit);
    }

    public int getFollowersCount(Long userId) {
        return followMapper.countFollowers(userId);
    }

    public int getFollowingCount(Long userId) {
        return followMapper.countFollowing(userId);
    }
}
