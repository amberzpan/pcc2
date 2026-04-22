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
        <button v-if="canDelete" class="icon-btn danger" @click="$emit('delete-post', post.id)" title="删除动态">
          <Trash2 :size="15" />
        </button>
      </div>
    </header>

    <p v-if="post.content" class="post-content allow-selection">{{ post.content }}</p>
    <img v-if="post.mediaUrl && post.mediaType === 'image'" class="media" :src="post.mediaUrl" alt="post media" />
    <video v-if="post.mediaUrl && post.mediaType === 'video'" class="media" controls :src="post.mediaUrl"></video>

    <button v-if="post.repostId && post.originalContent" class="quote" @click="openOriginalPost">
      <ArrowRightLeft :size="14" />
      <span>转发原文：{{ post.originalContent }}</span>
    </button>

    <footer class="post-actions">
      <button class="action" :class="post.liked ? 'active' : ''" @click="$emit('toggle-like', post)">
        <ThumbsUp :size="14" />
        <span>{{ post.likeCount || 0 }}</span>
      </button>
      <button class="action" @click="$emit('toggle-comments', post)">
        <MessageCircle :size="14" />
        <span>{{ post.commentCount || 0 }}</span>
      </button>
      <button
        class="action favorite-action"
        :class="post.favorited ? 'active' : ''"
        :title="post.favorited ? '已标记' : '标记'"
        :aria-label="post.favorited ? '已标记' : '标记'"
        @click="$emit('toggle-favorite', post)"
      >
        <Bookmark :size="14" />
      </button>
      <button class="action" @click="$emit('repost', post)">
        <Repeat2 :size="14" />
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
              <ThumbsUp :size="12" />
              {{ comment.likeCount || 0 }}
            </button>
            <button v-if="comment.userId === currentUserId" class="tiny-action danger" @click="$emit('delete-comment', { post, commentId: comment.id })">
              删除
            </button>
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
import { ArrowRightLeft, Bookmark, MessageCircle, Repeat2, ThumbsUp, Trash2 } from 'lucide-vue-next'
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

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23d8dde6" width="80" height="80"/><circle fill="%239aa3b3" cx="40" cy="30" r="12"/><rect fill="%239aa3b3" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const canDelete = computed(() => props.currentUserId && props.post.userId === props.currentUserId)
const showFollow = computed(() => props.currentUserId && props.post.userId !== props.currentUserId)
const router = useRouter()

const openProfile = () => {
  if (!props.post.userId) return
  router.push(`/profile/${props.post.userId}`)
}

const openOriginalPost = () => {
  if (!props.post.repostId) {
    return
  }
  router.push(`/post/${props.post.repostId}`)
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
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--paper);
  padding: 14px;
  box-shadow: 0 12px 22px color-mix(in srgb, var(--line) 35%, transparent);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.post-card:hover {
  transform: translateY(-2px);
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
}

.head-actions {
  display: flex;
  gap: 8px;
}

.head-follow {
  border: 1px solid var(--line);
  background: transparent;
  border-radius: 999px;
  padding: 5px 10px;
  color: var(--ink);
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
}

.author {
  display: flex;
  gap: 10px;
  align-items: center;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.author h3 {
  margin: 0;
  font-size: 0.98rem;
}

.author p {
  margin: 2px 0 0;
  color: var(--muted);
  font-size: 0.78rem;
}

.clickable {
  cursor: pointer;
}

.icon-btn {
  border: 1px solid transparent;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: grid;
  place-items: center;
}

.icon-btn.danger {
  color: var(--error);
}

.post-content {
  margin: 12px 0 0;
  line-height: 1.6;
  white-space: pre-wrap;
}

.media {
  width: 100%;
  border-radius: 12px;
  margin-top: 10px;
  max-height: 460px;
  object-fit: cover;
}

.quote {
  margin-top: 10px;
  width: 100%;
  display: inline-flex;
  gap: 6px;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: transparent;
  color: var(--muted);
  padding: 8px 10px;
  text-align: left;
  cursor: pointer;
}

.post-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.action {
  border: 1px solid var(--line);
  background: transparent;
  border-radius: 999px;
  padding: 6px 10px;
  color: var(--ink);
  display: inline-flex;
  gap: 6px;
  align-items: center;
  font-weight: 700;
  cursor: pointer;
}

.action.active {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

.comments {
  margin-top: 12px;
  border-top: 1px solid var(--line);
  padding-top: 12px;
}

.comment-toolbar {
  display: flex;
  justify-content: flex-end;
}

.comment-toolbar select {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 6px;
  background: transparent;
  color: var(--ink);
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid color-mix(in srgb, var(--line) 70%, transparent);
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--muted);
  font-size: 0.82rem;
}

.comment-meta strong {
  color: var(--ink);
}

.tiny-action {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--muted);
  border-radius: 999px;
  display: inline-flex;
  gap: 4px;
  align-items: center;
  font-size: 0.76rem;
  padding: 2px 7px;
  cursor: pointer;
}

.tiny-action.liked {
  color: var(--ink);
}

.tiny-action.danger {
  color: var(--error);
}

.comment-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-top: 10px;
}

.comment-editor input {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: transparent;
  color: var(--ink);
  padding: 8px 10px;
  font: inherit;
}

.comment-editor button {
  border: 1px solid var(--accent);
  border-radius: 10px;
  background: var(--accent);
  color: var(--paper);
  font-weight: 700;
  padding: 0 12px;
}

.hint {
  text-align: center;
  color: var(--muted);
}
</style>
