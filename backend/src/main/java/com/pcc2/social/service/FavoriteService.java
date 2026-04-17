package com.pcc2.social.service;

import com.pcc2.social.entity.Favorite;
import com.pcc2.social.entity.Post;
import com.pcc2.social.mapper.FavoriteMapper;
import com.pcc2.social.mapper.LikeRecordMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FavoriteService {
    private final FavoriteMapper favoriteMapper;
    private final LikeRecordMapper likeRecordMapper;

    public FavoriteService(FavoriteMapper favoriteMapper, LikeRecordMapper likeRecordMapper) {
        this.favoriteMapper = favoriteMapper;
        this.likeRecordMapper = likeRecordMapper;
    }

    public boolean toggleFavorite(Long userId, Long postId) {
        Favorite existing = favoriteMapper.findByUserAndPost(userId, postId);
        if (existing != null) {
            favoriteMapper.delete(userId, postId);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setPostId(postId);
            favoriteMapper.insert(favorite);
            return true;
        }
    }

    public boolean getFavoriteStatus(Long userId, Long postId) {
        return favoriteMapper.findByUserAndPost(userId, postId) != null;
    }

    public List<Post> getFavorites(Long userId, int page, int limit, Long currentUserId) {
        int offset = (page - 1) * limit;
        List<Post> posts = favoriteMapper.findByUserId(userId, offset, limit);
        if (currentUserId != null) {
            for (Post post : posts) {
                post.setLiked(likeRecordMapper.findByPostIdAndUserId(post.getId(), currentUserId) != null);
            }
        }
        return posts;
    }

    public int getFavoritesCount(Long userId) {
        return favoriteMapper.countByUserId(userId);
    }
}