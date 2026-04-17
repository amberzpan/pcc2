<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="user-info">
        <img :src="user?.avatar || 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2240%22 height=%2240%22><rect fill=%22%23ddd%22 width=%2240%22 height=%2240%22/><text fill=%22%23999%22 x=%2210%22 y=%2225%22 font-size=%2212%22>头像</text></svg>'" alt="avatar" class="avatar" />
        <div class="user-details">
          <h2>{{ user?.nickname || user?.username }}</h2>
          <p>@{{ user?.username }}</p>
        </div>
      </div>
    </div>
    
    <div class="post-list">
      <div v-if="loading" class="loading">加载中...</div>
      <div v-else-if="posts.length === 0" class="empty">暂无发布的动态</div>
      <div v-else>
        <div v-for="post in posts" :key="post.id" class="post-card">
          <div class="post-content">
            <p v-if="post.content">{{ post.content }}</p>
            <img v-if="post.mediaUrl && post.mediaType === 'image'" :src="post.mediaUrl" alt="post image" class="post-image" />
            <video v-if="post.mediaUrl && post.mediaType === 'video'" :src="post.mediaUrl" controls class="post-video"></video>
            <div v-if="post.originalContent" class="repost-original">
              <span class="repost-label">转发原文:</span>
              <p>{{ post.originalContent }}</p>
            </div>
          </div>
          <div class="post-meta">
            <span class="time">{{ formatTime(post.createdAt) }}</span>
            <span class="action-btn" :class="{ active: post.liked }" @click="handleLike(post)">
              <span class="icon">{{ post.liked ? '♥' : '♡' }}</span>
              {{ post.likeCount || 0 }}
            </span>
            <span class="action-btn" @click="showComments(post)">
              <span class="icon">💬</span>
              {{ post.commentCount || 0 }}
            </span>
            <span class="action-btn delete" @click="handleDelete(post.id)">删除</span>
          </div>
          
          <div v-if="post.showComments" class="comments-section">
            <div v-if="post.commentsLoading" class="loading">加载评论...</div>
            <div v-else-if="post.comments && post.comments.length > 0">
              <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
                <div class="comment-header">
                  <span class="comment-user">{{ comment.nickname || comment.username }}</span>
                  <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
              </div>
            </div>
            <div v-else class="no-comments">暂无评论</div>
            <div class="comment-input">
              <input v-model="post.newComment" placeholder="写评论..." @keyup.enter="submitComment(post)" />
              <button @click="submitComment(post)">发表</button>
            </div>
          </div>
        </div>
      </div>
      <div v-if="hasMore" class="load-more" @click="loadMore">加载更多</div>
    </div>
  </div>
</template>

<script setup>
import { ref, inject, onMounted } from 'vue'
import { getUserPosts, deletePost, toggleLike, getComments, createComment } from '@/api'

const user = inject('user')
const showToast = inject('showToast')

const posts = ref([])
const loading = ref(false)
const page = ref(1)
const size = 10
const hasMore = ref(true)

const loadPosts = async () => {
  if (!user.value?.id) return
  loading.value = true
  try {
    const res = await getUserPosts(user.value.id, page.value, size)
    if (res.code === 200) {
      posts.value = res.data.map(p => ({ ...p, showComments: false, comments: [], newComment: '' }))
      hasMore.value = res.data.length === size
    }
  } catch (e) {
    showToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!user.value?.id) return
  page.value++
  try {
    const res = await getUserPosts(user.value.id, page.value, size)
    if (res.code === 200) {
      const newPosts = res.data.map(p => ({ ...p, showComments: false, comments: [], newComment: '' }))
      posts.value.push(...newPosts)
      hasMore.value = res.data.length === size
    }
  } catch (e) {
    showToast('加载失败', 'error')
  }
}

const handleLike = async (post) => {
  try {
    const res = await toggleLike(post.id)
    if (res.code === 200) {
      post.liked = res.data.liked
      post.likeCount = (post.likeCount || 0) + (res.data.liked ? 1 : -1)
    }
  } catch (e) {
    showToast('操作失败', 'error')
  }
}

const showComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && !post.commentsLoaded) {
    post.commentsLoading = true
    try {
      const res = await getComments(post.id)
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

const submitComment = async (post) => {
  if (!post.newComment?.trim()) {
    showToast('请输入评论内容', 'error')
    return
  }
  try {
    const res = await createComment({ postId: post.id, content: post.newComment })
    if (res.code === 200) {
      if (!post.comments) post.comments = []
      post.comments.push(res.data)
      post.commentCount = (post.commentCount || 0) + 1
      post.newComment = ''
      showToast('评论成功')
    }
  } catch (e) {
    showToast('评论失败', 'error')
  }
}

const handleDelete = async (postId) => {
  if (!confirm('确定删除这条动态？')) return
  try {
    const res = await deletePost(postId)
    if (res.code === 200) {
      posts.value = posts.value.filter(p => p.id !== postId)
      showToast('删除成功')
    }
  } catch (e) {
    showToast('删除失败', 'error')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  return `${date.getMonth() + 1}-${date.getDate()} ${date.getHours()}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.profile-container {
  max-width: 700px;
  margin: 0 auto;
  padding: 20px 12px;
}

.profile-header {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.user-info {
  display: flex;
  align-items: center;
}

.avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #eee;
}

.user-details {
  margin-left: 20px;
}

.user-details h2 {
  font-size: 20px;
  margin-bottom: 4px;
}

.user-details p {
  color: #999;
  font-size: 14px;
}

.post-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.post-content p {
  font-size: 15px;
  line-height: 1.6;
  margin-bottom: 12px;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-image {
  max-width: 100%;
  border-radius: 8px;
}

.post-video {
  max-width: 100%;
  border-radius: 8px;
  margin-top: 8px;
  max-height: 400px;
}

.post-meta {
  display: flex;
  gap: 16px;
  align-items: center;
  font-size: 14px;
  color: #666;
}

.time {
  color: #999;
  font-size: 12px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.action-btn:hover {
  color: #e6162d;
}

.action-btn.active {
  color: #e6162d;
}

.action-btn.delete {
  margin-left: auto;
  color: #999;
}

.repost-original {
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
  margin-top: 8px;
}

.repost-label {
  font-size: 12px;
  color: #999;
}

.action-btn.delete:hover {
  color: #e6162d;
}

.comments-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.comment-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 4px;
  font-size: 12px;
}

.comment-user {
  color: #333;
  font-weight: 500;
}

.comment-time {
  color: #999;
  margin-left: 8px;
}

.comment-content {
  font-size: 14px;
  color: #333;
}

.comment-input {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.comment-input input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
}

.comment-input button {
  padding: 8px 16px;
  background: #e6162d;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
}

.loading, .empty, .load-more {
  text-align: center;
  padding: 20px;
  color: #999;
}

.load-more {
  cursor: pointer;
  color: #e6162d;
}

.no-comments {
  text-align: center;
  padding: 12px;
  color: #999;
  font-size: 14px;
}
</style>
