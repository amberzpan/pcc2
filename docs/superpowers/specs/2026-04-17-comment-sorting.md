# 评论排序功能实现

> **目标：** 实现评论排序功能，支持按时间正序/倒序、按热度排序，热门评论置顶

**问题分析：**
- 当前 CommentController 的 getComments 接口不支持排序参数
- CommentService 查询评论时使用默认排序
- 需要添加 sort 参数支持

**解决方案：**
1. 后端添加 sort 参数支持
2. 前端添加排序选项 UI
3. 热门评论（点赞数最多）置顶

**涉及文件：**
- backend/src/main/java/com/pcc2/social/controller/CommentController.java
- backend/src/main/java/com/pcc2/social/service/CommentService.java
- backend/src/main/java/com/pcc2/social/mapper/CommentMapper.java
- frontend/src/views/HomeView.vue