package com.pcc2.social.service;

import com.pcc2.social.dto.CommentRequest;
import com.pcc2.social.entity.Comment;
import com.pcc2.social.entity.Post;
import com.pcc2.social.entity.User;
import com.pcc2.social.mapper.CommentMapper;
import com.pcc2.social.mapper.CommentLikeMapper;
import com.pcc2.social.mapper.PostMapper;
import com.pcc2.social.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final CommentLikeMapper commentLikeMapper;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    
    public CommentService(CommentMapper commentMapper, CommentLikeMapper commentLikeMapper, PostMapper postMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.postMapper = postMapper;
        this.userMapper = userMapper;
    }
    
    public List<Comment> getCommentsByPostId(Long postId, String sort) {
        return getCommentsByPostId(postId, sort, null);
    }

    public List<Comment> getCommentsByPostId(Long postId, String sort, Long currentUserId) {
        List<Comment> comments = new java.util.ArrayList<>(commentMapper.findByPostId(postId));
        
        for (Comment comment : comments) {
            comment.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);
            if (currentUserId != null) {
                comment.setLiked(commentLikeMapper.exists(comment.getId(), currentUserId) > 0);
            } else {
                comment.setLiked(false);
            }
        }
        
        if ("hot".equals(sort)) {
            comments.sort((a, b) -> Integer.compare(b.getLikeCount(), a.getLikeCount()));
        } else if ("time_asc".equals(sort)) {
            comments.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
        } else {
            comments.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        }
        
        return comments;
    }

    @Transactional
    public boolean toggleCommentLike(Long commentId, Long userId) {
        Comment comment = commentMapper.findById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        boolean exists = commentLikeMapper.exists(commentId, userId) > 0;
        if (exists) {
            commentLikeMapper.delete(commentId, userId);
        } else {
            commentLikeMapper.insert(commentId, userId);
        }
        int count = commentLikeMapper.countByCommentId(commentId);
        commentMapper.updateLikeCount(commentId, count);
        return !exists;
    }

    public boolean getCommentLikeStatus(Long commentId, Long userId) {
        return commentLikeMapper.exists(commentId, userId) > 0;
    }
    
    @Transactional
    public Comment createComment(Long userId, CommentRequest request) {
        if (postMapper.findById(request.getPostId()) == null) {
            throw new RuntimeException("内容不存在");
        }
        Comment comment = new Comment();
        comment.setPostId(request.getPostId());
        comment.setUserId(userId);
        comment.setContent(request.getContent().trim());
        
        commentMapper.insert(comment);
        
        Post post = postMapper.findById(request.getPostId());
        if (post != null) {
            int count = commentMapper.countByPostId(request.getPostId());
            postMapper.updateCommentCount(request.getPostId(), count);
        }
        
        Comment createdComment = commentMapper.findById(comment.getId());
        User user = userMapper.findById(userId);
        if (user != null) {
            createdComment.setUsername(user.getUsername());
            createdComment.setNickname(user.getNickname());
            createdComment.setAvatar(user.getAvatar());
        }
        return createdComment;
    }
    
    @Transactional
    public boolean deleteComment(Long id, Long userId) {
        Comment comment = commentMapper.findById(id);
        if (comment == null) {
            return false;
        }
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除");
        }
        
        Long postId = comment.getPostId();
        commentMapper.delete(id);
        
        int count = commentMapper.countByPostId(postId);
        postMapper.updateCommentCount(postId, count);
        
        return true;
    }
}
