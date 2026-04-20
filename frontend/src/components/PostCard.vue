<template>
  <article class="post-card">
    <header class="post-head">
      <div class="author">
        <img class="avatar clickable" :src="post.avatar || defaultAvatar" alt="avatar" @click="openProfile" />
        <div class="clickable" @click="openProfile">
          <h3>{{ post.nickname || post.username || '匿名用户' }}</h3>
          <p>{{ formatTime(post.createdAt) }}</p>
        </div>
      </div>
      <div class="head-actions">
        <button v-if="showFollow" class="head-follow" @click="$emit('toggle-follow', post)">
          {{ post.followed ? '已关注' : '关注' }}
        </button>
        <button v-if="canDelete" class="icon-btn danger" @click="$emit('delete-post', post.id)">🗑</button>
      </div>
    </header>

    <p v-if="post.content" class="post-content">{{ post.content }}</p>
    <img v-if="post.mediaUrl && post.mediaType === 'image'" class="media" :src="post.mediaUrl" alt="post media" />
    <video v-if="post.mediaUrl && post.mediaType === 'video'" class="media" controls :src="post.mediaUrl"></video>

    <div v-if="post.originalContent" class="quote">转发原文：{{ post.originalContent }}</div>

    <footer class="post-actions">
      <button class="action" :class="post.liked ? 'active' : ''" @click="$emit('toggle-like', post)">
        <span>👍</span>
        <span>{{ post.likeCount || 0 }}</span>
      </button>
      <button class="action" @click="$emit('toggle-comments', post)">
        <span>💬</span>
        <span>{{ post.commentCount || 0 }}</span>
      </button>
      <button class="action" :class="post.favorited ? 'active' : ''" @click="$emit('toggle-favorite', post)">
        <span>⭐</span>
        <span>{{ post.favorited ? '已收藏' : '收藏' }}</span>
      </button>
      <button class="action" @click="$emit('repost', post)">
        <span>🔁</span>
        <span>{{ post.repostCount || 0 }}</span>
      </button>
    </footer>

    <div class="comments" v-if="post.showComments">
      <div class="comment-toolbar">
        <select v-model="post.commentSort" @change="$emit('load-comments', post)">
          <option value="time_desc">最新评论</option>
          <option value="time_asc">最早评论</option>
          <option value="hot">热门评论</option>
        </select>
      </div>

      <div v-if="post.commentsLoading" class="hint">评论加载中...</div>
      <template v-else>
        <div v-if="post.comments.length === 0" class="hint">暂无评论</div>
        <div class="comment-item" v-for="comment in post.comments" :key="comment.id">
          <div class="comment-meta">
            <strong>{{ comment.nickname || comment.username }}</strong>
            <span>{{ formatTime(comment.createdAt) }}</span>
            <button class="tiny-action" :class="comment.liked ? 'liked' : ''" @click="$emit('toggle-comment-like', { post, comment })">
              👍 {{ comment.likeCount || 0 }}
            </button>
            <button v-if="comment.userId === currentUserId" class="tiny-action danger" @click="$emit('delete-comment', { post, commentId: comment.id })">删除</button>
          </div>
          <p>{{ comment.content }}</p>
        </div>
      </template>

      <div class="comment-editor" v-if="isLoggedIn">
        <input v-model="post.newComment" placeholder="写评论..." @keyup.enter="$emit('submit-comment', post)" />
        <button @click="$emit('submit-comment', post)">发布</button>
      </div>
    </div>
  </article>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  currentUserId: {
    type: Number,
    default: null
  },
  isLoggedIn: {
    type: Boolean,
    default: false
  }
})

defineEmits([
  'delete-post',
  'toggle-like',
  'toggle-comments',
  'toggle-favorite',
  'toggle-follow',
  'repost',
  'load-comments',
  'submit-comment',
  'delete-comment',
  'toggle-comment-like'
])

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23f4e7d6" width="80" height="80"/><circle fill="%23d3b89d" cx="40" cy="30" r="12"/><rect fill="%23d3b89d" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const canDelete = computed(() => props.currentUserId && props.post.userId === props.currentUserId)
const showFollow = computed(() => props.currentUserId && props.post.userId !== props.currentUserId)
const router = useRouter()

const openProfile = () => {
  if (!props.post.userId) return
  router.push(`/profile/${props.post.userId}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', { hour12: false })
}
</script>

<style scoped>
.post-card {
  border: 1px solid #e8ddcf;
  border-radius: 20px;
  background: #fffef9;
  padding: 16px;
  box-shadow: 0 18px 35px rgba(66, 45, 17, 0.06);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.post-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 24px 42px rgba(66, 45, 17, 0.1);
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.head-follow {
  border: 1px solid #efcfb6;
  background: #fff4ec;
  color: #b64622;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
}

.head-follow:hover {
  border-color: #e9bfa3;
  background: #ffeadd;
}

.author {
  display: flex;
  gap: 10px;
  align-items: center;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f2e2cf;
}

.author h3 {
  margin: 0;
  font-size: 1rem;
}

.author p {
  margin: 2px 0 0;
  color: #8b7a63;
  font-size: 0.8rem;
}

.clickable {
  cursor: pointer;
}

.icon-btn {
  border: 0;
  background: transparent;
  cursor: pointer;
  font-size: 1rem;
}

.icon-btn.danger {
  color: #ca3434;
}

.post-content {
  margin: 12px 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.media {
  width: 100%;
  border-radius: 14px;
  margin-top: 6px;
  max-height: 460px;
  object-fit: cover;
}

.quote {
  margin-top: 10px;
  font-size: 0.9rem;
  color: #7a6d5a;
  background: linear-gradient(90deg, #fff1e5 0, #fffaf5 100%);
  border: 1px solid #f3dcc7;
  border-radius: 10px;
  padding: 8px 10px;
}

.post-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.action {
  border: 1px solid #eadfce;
  background: #fff;
  border-radius: 999px;
  padding: 6px 11px;
  cursor: pointer;
  font-weight: 700;
  color: #4f3f2c;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.action:hover {
  border-color: #dfc4ad;
  background: #fff8f1;
}

.action.active {
  background: #f25a29;
  border-color: #f25a29;
  color: #fff;
}

.comments {
  margin-top: 12px;
  border-top: 1px dashed #e9dccb;
  padding-top: 12px;
}

.comment-toolbar {
  display: flex;
  justify-content: flex-end;
}

.comment-toolbar select {
  border: 1px solid #eadfce;
  border-radius: 8px;
  padding: 6px;
  background: #fff;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px dashed #f0e6db;
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 0.84rem;
  color: #7a6d5a;
}

.comment-meta strong {
  color: #2d2418;
}

.tiny-action {
  border: 1px solid #ebdece;
  background: #fff;
  color: #5f4e3a;
  border-radius: 999px;
  font-size: 0.78rem;
  padding: 2px 8px;
  cursor: pointer;
}

.tiny-action.liked {
  border-color: #f25a29;
  color: #f25a29;
}

.tiny-action.danger {
  border-color: #f3c9c9;
  color: #cc2a2a;
}

.comment-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-top: 10px;
}

.comment-editor input {
  border: 1px solid #eadfce;
  border-radius: 10px;
  padding: 8px 10px;
}

.comment-editor button {
  border: 0;
  border-radius: 10px;
  background: #f25a29;
  color: #fff;
  font-weight: 700;
  padding: 0 12px;
}

.hint {
  text-align: center;
  color: #7a6d5a;
}
</style>
