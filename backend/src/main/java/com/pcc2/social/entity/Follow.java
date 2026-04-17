package com.pcc2.social.entity;

import java.time.LocalDateTime;

public class Follow {
    private Long id;
    private Long followerId;
    private Long followingId;
    private LocalDateTime createdAt;

    private String followerUsername;
    private String followerNickname;
    private String followerAvatar;
    private String followingUsername;
    private String followingNickname;
    private String followingAvatar;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Long followerId) {
        this.followerId = followerId;
    }

    public Long getFollowingId() {
        return followingId;
    }

    public void setFollowingId(Long followingId) {
        this.followingId = followingId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFollowerUsername() {
        return followerUsername;
    }

    public void setFollowerUsername(String followerUsername) {
        this.followerUsername = followerUsername;
    }

    public String getFollowerNickname() {
        return followerNickname;
    }

    public void setFollowerNickname(String followerNickname) {
        this.followerNickname = followerNickname;
    }

    public String getFollowerAvatar() {
        return followerAvatar;
    }

    public void setFollowerAvatar(String followerAvatar) {
        this.followerAvatar = followerAvatar;
    }

    public String getFollowingUsername() {
        return followingUsername;
    }

    public void setFollowingUsername(String followingUsername) {
        this.followingUsername = followingUsername;
    }

    public String getFollowingNickname() {
        return followingNickname;
    }

    public void setFollowingNickname(String followingNickname) {
        this.followingNickname = followingNickname;
    }

    public String getFollowingAvatar() {
        return followingAvatar;
    }

    public void setFollowingAvatar(String followingAvatar) {
        this.followingAvatar = followingAvatar;
    }
}