<template>
  <section class="feed-page">
    <div class="panel switcher" v-if="isLoggedIn">
      <button :class="['switch-btn', mode === 'all' ? 'active' : '']" @click="changeMode('all')">全部动态</button>
      <button :class="['switch-btn', mode === 'following' ? 'active' : '']" @click="changeMode('following')">关注动态</button>
    </div>

    <article v-for="post in posts" :key="post.id" class="panel post-card">
      <header class="post-head">
        <div>
          <h3>{{ post.nickname || post.username || '匿名用户' }}</h3>
          <p>{{ formatTime(post.createdAt) }}</p>
        </div>
        <button v-if="post.userId === currentUserId" class="danger-link" @click="removePost(post.id)">删除</button>
      </header>

      <p v-if="post.content" class="post-content">{{ post.content }}</p>
      <img v-if="post.mediaUrl && post.mediaType === 'image'" class="media" :src="post.mediaUrl" alt="post media" />
      <video v-if="post.mediaUrl && post.mediaType === 'video'" class="media" controls :src="post.mediaUrl"></video>

      <div v-if="post.originalContent" class="quote">转发原文：{{ post.originalContent }}</div>

      <footer class="post-actions">
        <button class="action" :class="post.liked ? 'active' : ''" @click="togglePostLike(post)">赞 {{ post.likeCount || 0 }}</button>
        <button class="action" @click="toggleComments(post)">评论 {{ post.commentCount || 0 }}</button>
        <button class="action" :class="post.favorited ? 'active' : ''" @click="togglePostFavorite(post)">收藏</button>
        <button class="action" v-if="post.userId !== currentUserId" @click="togglePostFollow(post)">{{ post.followed ? '取消关注' : '关注' }}</button>
        <button class="action" @click="repost(post)">转发</button>
      </footer>

      <div class="comments" v-if="post.showComments">
        <div class="comment-toolbar">
          <select v-model="post.commentSort" @change="loadComments(post)">
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
              <button v-if="comment.userId === currentUserId" class="danger-link" @click="removeComment(post, comment.id)">删除</button>
            </div>
            <p>{{ comment.content }}</p>
          </div>
        </template>

        <div class="comment-editor" v-if="isLoggedIn">
          <input v-model="post.newComment" placeholder="写评论..." @keyup.enter="submitComment(post)" />
          <button @click="submitComment(post)">发布</button>
        </div>
      </div>
    </article>

    <div class="panel empty" v-if="!loading && posts.length === 0">当前没有内容</div>

    <div class="panel load-more" v-if="hasMore" @click="loadMore">加载更多</div>
    <div class="panel hint" v-if="loading">加载中...</div>
  </section>
</template>

<script setup>
import { computed, inject, onMounted, ref } from 'vue'
import {
  createComment,
  createPost,
  deleteComment as apiDeleteComment,
  deletePost,
  getComments,
  getFollowingPosts,
  getPostList,
  toggleFavorite,
  toggleFollow,
  toggleLike
} from '@/api'

const user = inject('user')
const isLoggedIn = inject('isLoggedIn')
const showToast = inject('showToast')

const posts = ref([])
const page = ref(1)
const size = 10
const hasMore = ref(false)
const loading = ref(false)
const mode = ref('all')

const currentUserId = computed(() => user.value?.id)

const normalizePosts = (items) => items.map((post) => ({
  ...post,
  showComments: false,
  comments: [],
  commentsLoading: false,
  commentSort: 'time_desc',
  newComment: ''
}))

const fetchPosts = async (reset = true) => {
  if (reset) {
    page.value = 1
    posts.value = []
  }

  loading.value = true
  try {
    const api = mode.value === 'following' ? getFollowingPosts : getPostList
    const res = await api(page.value, size)
    if (res.code !== 200 || !Array.isArray(res.data)) {
      posts.value = []
      hasMore.value = false
      return
    }
    const next = normalizePosts(res.data)
    posts.value = reset ? next : posts.value.concat(next)
    hasMore.value = next.length === size
  } catch {
    showToast('加载失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  page.value += 1
  await fetchPosts(false)
}

const changeMode = async (target) => {
  mode.value = target
  await fetchPosts(true)
}

const togglePostLike = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  try {
    const res = await toggleLike(post.id)
    if (res.code === 200) {
      post.liked = !!res.data.liked
      post.likeCount = Math.max(0, (post.likeCount || 0) + (post.liked ? 1 : -1))
    }
  } catch {
    showToast('点赞失败', 'error')
  }
}

const togglePostFavorite = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  try {
    const res = await toggleFavorite(post.id)
    if (res.code === 200) {
      post.favorited = !!res.data.favorited
    }
  } catch {
    showToast('收藏失败', 'error')
  }
}

const togglePostFollow = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  try {
    const res = await toggleFollow(post.userId)
    if (res.code === 200) {
      post.followed = !!res.data.followed
    }
  } catch {
    showToast('关注操作失败', 'error')
  }
}

const repost = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  const content = window.prompt('请输入转发语（可选）')
  if (content === null) return
  try {
    const res = await createPost({ content, repostId: post.id })
    if (res.code === 200) {
      post.repostCount = (post.repostCount || 0) + 1
      showToast('转发成功')
    } else {
      showToast(res.message || '转发失败', 'error')
    }
  } catch {
    showToast('转发失败', 'error')
  }
}

const removePost = async (postId) => {
  if (!window.confirm('确定删除这条动态吗？')) return
  try {
    const res = await deletePost(postId)
    if (res.code === 200) {
      posts.value = posts.value.filter((item) => item.id !== postId)
      showToast('删除成功')
    } else {
      showToast(res.message || '删除失败', 'error')
    }
  } catch {
    showToast('删除失败', 'error')
  }
}

const toggleComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && post.comments.length === 0) {
    await loadComments(post)
  }
}

const loadComments = async (post) => {
  post.commentsLoading = true
  try {
    const res = await getComments(post.id, post.commentSort)
    if (res.code === 200 && Array.isArray(res.data)) {
      post.comments = res.data
    }
  } catch {
    showToast('评论加载失败', 'error')
  } finally {
    post.commentsLoading = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    showToast('评论不能为空', 'error')
    return
  }
  try {
    const res = await createComment({ postId: post.id, content: post.newComment.trim() })
    if (res.code === 200) {
      post.comments.push(res.data)
      post.commentCount = (post.commentCount || 0) + 1
      post.newComment = ''
      showToast('评论成功')
    } else {
      showToast(res.message || '评论失败', 'error')
    }
  } catch {
    showToast('评论失败', 'error')
  }
}

const removeComment = async (post, commentId) => {
  try {
    const res = await apiDeleteComment(commentId)
    if (res.code === 200) {
      post.comments = post.comments.filter((item) => item.id !== commentId)
      post.commentCount = Math.max(0, (post.commentCount || 0) - 1)
      showToast('评论已删除')
    } else {
      showToast(res.message || '删除失败', 'error')
    }
  } catch {
    showToast('删除失败', 'error')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  fetchPosts(true)
})
</script>

<style scoped>
.feed-page {
  display: grid;
  gap: 14px;
}

.panel {
  border: 1px solid #e7dccf;
  border-radius: 16px;
  background: #fffdf8;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(66, 45, 17, 0.06);
}

.switcher {
  display: flex;
  gap: 10px;
}

.switch-btn {
  border: 1px solid #e7dccf;
  border-radius: 999px;
  padding: 8px 14px;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
}

.switch-btn.active {
  background: #f25a29;
  border-color: #f25a29;
  color: #fff;
}

.post-head {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 12px;
}

.post-head h3 {
  margin: 0;
  font-size: 1rem;
}

.post-head p {
  margin: 4px 0 0;
  color: #7a6d5a;
  font-size: 0.82rem;
}

.post-content {
  margin: 12px 0;
  white-space: pre-wrap;
}

.media {
  width: 100%;
  border-radius: 12px;
  margin-top: 6px;
  max-height: 460px;
  object-fit: cover;
}

.quote {
  margin-top: 10px;
  font-size: 0.9rem;
  color: #7a6d5a;
  border-left: 3px solid #f25a29;
  padding-left: 10px;
}

.post-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.action {
  border: 1px solid #e7dccf;
  background: #fff;
  border-radius: 999px;
  padding: 6px 12px;
  cursor: pointer;
  font-weight: 600;
}

.action.active {
  background: #f25a29;
  border-color: #f25a29;
  color: #fff;
}

.danger-link {
  border: 0;
  background: transparent;
  color: #cc2a2a;
  font-weight: 700;
  cursor: pointer;
}

.comments {
  margin-top: 12px;
  border-top: 1px dashed #e7dccf;
  padding-top: 12px;
}

.comment-toolbar {
  display: flex;
  justify-content: flex-end;
}

.comment-toolbar select {
  border: 1px solid #e7dccf;
  border-radius: 8px;
  padding: 6px;
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px dashed #f0e6db;
}

.comment-meta {
  display: flex;
  gap: 8px;
  align-items: center;
  font-size: 0.86rem;
  color: #7a6d5a;
}

.comment-meta strong {
  color: #2d2418;
}

.comment-editor {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
  margin-top: 10px;
}

.comment-editor input {
  border: 1px solid #e7dccf;
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

.load-more {
  text-align: center;
  cursor: pointer;
  font-weight: 700;
}

.hint,
.empty {
  text-align: center;
  color: #7a6d5a;
}
</style>
