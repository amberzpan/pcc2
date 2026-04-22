-- 创建数据库
CREATE DATABASE IF NOT EXISTS social_platform DEFAULT CHARSET utf8mb4;
USE social_platform;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码(加密存储)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '背景图URL',
    `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介',
    `followers_count` INT DEFAULT 0 COMMENT '粉丝数',
    `following_count` INT DEFAULT 0 COMMENT '关注数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE `user` ADD COLUMN IF NOT EXISTS `cover_url` VARCHAR(255) DEFAULT NULL COMMENT '背景图URL';

-- 内容表
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '发布者ID',
    `content` TEXT NOT NULL COMMENT '文字内容',
    `media_url` VARCHAR(255) DEFAULT NULL COMMENT '媒体URL(图片/视频)',
    `media_type` VARCHAR(20) DEFAULT NULL COMMENT '媒体类型(image/video)',
    `repost_id` BIGINT DEFAULT NULL COMMENT '原动态ID(转发)',
    `original_content` TEXT DEFAULT NULL COMMENT '原动态内容(转发)',
    `like_count` INT DEFAULT 0 COMMENT '点赞数量',
    `comment_count` INT DEFAULT 0 COMMENT '评论数量',
    `repost_count` INT DEFAULT 0 COMMENT '转发数量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (`repost_id`) REFERENCES `post`(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL COMMENT '所属内容ID',
    `user_id` BIGINT NOT NULL COMMENT '评论者ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(id) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `comment_id` BIGINT NOT NULL COMMENT '被点赞评论ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`comment_id`) REFERENCES `comment`(id) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 点赞表
CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL COMMENT '被点赞内容ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞者ID',
    `type` VARCHAR(20) DEFAULT 'post' COMMENT '类型(post/comment)',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`post_id`) REFERENCES `post`(id) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 关注表
CREATE TABLE IF NOT EXISTS `follow` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `follower_id` BIGINT NOT NULL COMMENT '粉丝ID',
    `following_id` BIGINT NOT NULL COMMENT '关注者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`follower_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (`following_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '收藏者ID',
    `post_id` BIGINT NOT NULL COMMENT '被收藏内容ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`user_id`) REFERENCES `user`(id) ON DELETE CASCADE,
    FOREIGN KEY (`post_id`) REFERENCES `post`(id) ON DELETE CASCADE,
    UNIQUE KEY `uk_user_post` (`user_id`, `post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 添加索引
CREATE INDEX idx_post_user_id ON post(user_id);
CREATE INDEX idx_post_created_at ON post(created_at);
CREATE INDEX idx_comment_post_id ON comment(post_id);
CREATE INDEX idx_comment_like_comment_id ON comment_like(comment_id);
CREATE INDEX idx_follow_follower_id ON follow(follower_id);
CREATE INDEX idx_follow_following_id ON follow(following_id);
CREATE INDEX idx_favorite_user_id ON favorite(user_id);
