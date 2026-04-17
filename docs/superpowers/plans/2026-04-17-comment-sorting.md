# 评论排序功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现评论排序功能，支持按时间正序/倒序、按热度排序，热门评论置顶

**Architecture:** 后端添加排序参数支持，前端添加排序选项UI，热门评论（点赞数最多）置顶显示

**Tech Stack:** Spring Boot, MyBatis, Vue 3

---

### Task 1: 后端 - 修改 CommentController 添加排序参数

**Files:**
- Modify: `E:\vscode\code\pcc2\backend\src\main\java\com\pcc2\social\controller\CommentController.java:21-25`

- [ ] **Step 1: 修改 getComments 接口添加 sort 参数**

```java
@GetMapping("/post/{postId}")
public Result<List<Comment>> getComments(
        @PathVariable Long postId,
        @RequestParam(defaultValue = "time_desc") String sort) {
    List<Comment> comments = commentService.getCommentsByPostId(postId, sort);
    return Result.success(comments);
}
```

- [ ] **Step 2: 验证编译**

运行: `C:\maven\apache-maven-3.9.14\bin\mvn.cmd -f E:\vscode\code\pcc2\backend\pom.xml compile`
预期: BUILD SUCCESS

---

### Task 2: 后端 - 修改 CommentService 实现排序逻辑

**Files:**
- Modify: `E:\vscode\code\pcc2\backend\src\main\java\com\pcc2\social\service\CommentService.java:26`

- [ ] **Step 1: 修改 getCommentsByPostId 方法签名添加 sort 参数**

```java
public List<Comment> getCommentsByPostId(Long postId, String sort) {
    // sort 参数: time_asc(时间正序), time_desc(时间倒序), hot(热度)
    // 热门评论置顶: 按点赞数降序排在最前面
    List<Comment> comments = commentMapper.selectByPostId(postId);
    
    if ("hot".equals(sort)) {
        // 热度排序: 点赞数多的在前
        comments.sort((a, b) -> Integer.compare(b.getLikeCount() != null ? b.getLikeCount() : 0, 
                                                a.getLikeCount() != null ? a.getLikeCount() : 0));
    } else if ("time_asc".equals(sort)) {
        // 时间正序
        comments.sort((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()));
    } else {
        // 默认时间倒序
        comments.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    }
    
    return comments;
}
```

- [ ] **Step 2: 验证编译**

运行: `C:\maven\apache-maven-3.9.14\bin\mvn.cmd -f E:\vscode\code\pcc2\backend\pom.xml compile`
预期: BUILD SUCCESS

---

### Task 3: 前端 - 添加评论排序选项 UI

**Files:**
- Modify: `E:\vscode\code\pcc2\frontend\src\views\HomeView.vue:51-69`

- [ ] **Step 1: 添加排序选项下拉框**

在评论区域添加排序选项:

```html
<div v-if="post.showComments" class="comments-section">
    <div class="comments-sort">
        <select v-model="post.commentSort" @change="loadComments(post)">
            <option value="time_desc">最新评论</option>
            <option value="time_asc">最早评论</option>
            <option value="hot">热门评论</option>
        </select>
    </div>
    <!-- 原有评论列表代码 -->
</div>
```

- [ ] **Step 2: 修改 showComments 方法传递排序参数**

```javascript
const showComments = async (post) => {
    post.showComments = !post.showComments
    if (post.showComments && !post.commentsLoaded) {
        post.commentsLoading = true
        post.commentSort = post.commentSort || 'time_desc'
        try {
            const res = await getComments(post.id, post.commentSort)
            if (res.code === 200) {
                post.comments = res.data
                post.commentsLoaded = true
            }
        } catch (e) {
            showToast('加载评论失败', 'error')
        } finally {
            post.commentsLoading = false
        }
    }
}
```

- [ ] **Step 3: 修改 getComments API 调用添加 sort 参数**

检查 frontend/src/api/index.js 中的 getComments 函数，添加 sort 参数支持

---

### Task 4: 验证测试

**验证命令:**
1. 后端编译: `C:\maven\apache-maven-3.9.14\bin\mvn.cmd -f E:\vscode\code\pcc2\backend\pom.xml compile`
2. API 测试: 使用 Node.js 测试评论接口带 sort 参数

预期: 编译成功，API 返回正确排序的评论