<template>
  <section class="search-page">
    <article class="panel header">
      <h2>搜索结果</h2>
      <p v-if="query">关键词：{{ query }}</p>
      <p v-else>输入关键词，找人或帖子</p>
    </article>

    <article class="panel" v-if="loading && !hasSearched">搜索中</article>

    <template v-else>
      <article class="panel users-panel">
        <h3>相关用户</h3>
        <div v-if="users.length === 0" class="hint">没有匹配的用户</div>
        <div class="user-row" v-for="item in users" :key="`u-${item.id}`" @click="openUserProfile(item.id)">
          <img class="avatar" :src="item.avatar || defaultAvatar" alt="avatar" />
          <div class="meta">
            <strong>{{ item.nickname || item.username }}</strong>
            <span>@{{ item.username }}</span>
          </div>
        </div>
      </article>

      <PostCard
        v-for="post in posts"
        :key="post.id"
        :post="post"
        :current-user-id="currentUserId"
        :is-logged-in="isLoggedIn"
        @delete-post="removePost"
        @toggle-like="togglePostLike"
        @toggle-comments="toggleComments"
        @toggle-favorite="togglePostFavorite"
        @toggle-follow="togglePostFollow"
        @repost="repost"
        @load-comments="loadComments"
        @submit-comment="submitComment"
        @delete-comment="removeComment"
        @toggle-comment-like="toggleCommentLike"
      />

      <article class="panel empty" v-if="posts.length === 0">没有匹配的帖子</article>
    </template>

    <article class="panel hint" v-if="loading && hasSearched">结果更新中</article>
  </section>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createComment,
  createPost,
  deleteComment as apiDeleteComment,
  deletePost,
  getComments,
  searchPosts,
  searchUsers,
  toggleCommentLike as apiToggleCommentLike,
  toggleFavorite,
  toggleFollow,
  toggleLike
} from '@/api'
import PostCard from '@/components/PostCard.vue'
import { normalizeComments, normalizePostList } from '@/utils/post-utils'

const route = useRoute()
const router = useRouter()
const user = inject('user')
const isLoggedIn = inject('isLoggedIn')
const showToast = inject('showToast')

const loading = ref(false)
const hasSearched = ref(false)
const posts = ref([])
const users = ref([])
let latestSearchRequestId = 0
const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23f4e7d6" width="80" height="80"/><circle fill="%23d3b89d" cx="40" cy="30" r="12"/><rect fill="%23d3b89d" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const query = computed(() => (route.query.q || '').toString().trim())
const currentUserId = computed(() => user.value?.id)

const runSearch = async () => {
  if (!query.value) {
    posts.value = []
    users.value = []
    hasSearched.value = false
    return
  }
  const requestId = ++latestSearchRequestId
  loading.value = true
  try {
    const [postRes, userRes] = await Promise.all([
      searchPosts(query.value, 1, 20),
      searchUsers(query.value, 1, 20)
    ])
    if (requestId !== latestSearchRequestId) {
      return
    }

    posts.value = postRes.code === 200 ? normalizePostList(postRes.data) : []
    users.value = userRes.code === 200 && Array.isArray(userRes.data) ? userRes.data : []
    hasSearched.value = true
  } finally {
    if (requestId === latestSearchRequestId) {
      loading.value = false
    }
  }
}

const openUserProfile = (id) => {
  if (!id) return
  router.push(`/profile/${id}`)
}

const togglePostLike = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后点赞', 'error')
    return
  }
  try {
    const res = await toggleLike(post.id)
    if (res.code === 200) {
      post.liked = !!res.data.liked
      post.likeCount = Math.max(0, (post.likeCount || 0) + (post.liked ? 1 : -1))
    }
  } catch {
    showToast('点赞失败，请稍后重试', 'error')
  }
}

const togglePostFavorite = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后收藏', 'error')
    return
  }
  try {
    const res = await toggleFavorite(post.id)
    if (res.code === 200) {
      post.favorited = !!res.data.favorited
    }
  } catch {
    showToast('收藏失败，请稍后重试', 'error')
  }
}

const togglePostFollow = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后关注用户', 'error')
    return
  }
  try {
    const res = await toggleFollow(post.userId)
    if (res.code === 200) {
      post.followed = !!res.data.followed
    }
  } catch {
    showToast('关注失败，请稍后重试', 'error')
  }
}

const repost = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后转发', 'error')
    return
  }
  const content = window.prompt('请输入转发内容（可选）')
  if (content === null) return
  try {
    const res = await createPost({ content, repostId: post.id })
    if (res.code === 200) {
      post.repostCount = (post.repostCount || 0) + 1
      post.reposted = true
      showToast('转发成功')
    } else {
      showToast(res.message || '转发失败，请稍后重试', 'error')
    }
  } catch {
    showToast('转发失败，请稍后重试', 'error')
  }
}

const removePost = async (postId) => {
  if (!window.confirm('确认删除该内容？')) return
  try {
    const res = await deletePost(postId)
    if (res.code === 200) {
      posts.value = posts.value.filter((item) => item.id !== postId)
      showToast('删除成功')
    } else {
      showToast(res.message || '删除失败，请稍后重试', 'error')
    }
  } catch {
    showToast('删除失败，请稍后重试', 'error')
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
      post.comments = normalizeComments(res.data)
    }
  } catch {
    showToast('评论加载失败，请稍后重试', 'error')
  } finally {
    post.commentsLoading = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    showToast('请输入评论内容', 'error')
    return
  }
  try {
    const res = await createComment({ postId: post.id, content: post.newComment.trim() })
    if (res.code === 200) {
      post.comments.push({
        ...res.data,
        liked: false,
        likeCount: res.data?.likeCount || 0
      })
      post.commentCount = (post.commentCount || 0) + 1
      post.newComment = ''
      showToast('评论发布成功')
    } else {
      showToast(res.message || '评论发布失败，请稍后重试', 'error')
    }
  } catch {
    showToast('评论发布失败，请稍后重试', 'error')
  }
}

const removeComment = async ({ post, commentId }) => {
  try {
    const res = await apiDeleteComment(commentId)
    if (res.code === 200) {
      post.comments = post.comments.filter((item) => item.id !== commentId)
      post.commentCount = Math.max(0, (post.commentCount || 0) - 1)
      showToast('评论已删除')
    } else {
      showToast(res.message || '删除失败，请稍后重试', 'error')
    }
  } catch {
    showToast('删除失败，请稍后重试', 'error')
  }
}

const toggleCommentLike = async ({ post, comment }) => {
  if (!isLoggedIn.value) {
    showToast('请登录后点赞', 'error')
    return
  }
  try {
    const res = await apiToggleCommentLike(comment.id)
    if (res.code === 200) {
      comment.liked = !!res.data.liked
      comment.likeCount = Math.max(0, (comment.likeCount || 0) + (comment.liked ? 1 : -1))
      if (post.commentSort === 'hot') {
        post.comments = [...post.comments].sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
      }
    }
  } catch {
    showToast('点赞失败，请稍后重试', 'error')
  }
}

watch(() => route.query.q, () => {
  runSearch()
})

onMounted(() => {
  runSearch()
})
</script>

<style scoped>
.search-page {
  display: grid;
  gap: 14px;
  width: 100%;
  max-width: 920px;
  margin: 0 auto;
}

.panel {
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--paper);
  padding: 14px;
  box-shadow: 0 10px 24px color-mix(in srgb, #0f172a 7%, transparent);
}

.header h2 {
  margin: 0;
}

.header p {
  margin: 4px 0 0;
  color: var(--muted);
}

.users-panel h3 {
  margin: 0 0 8px;
}

.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-top: 1px solid var(--line);
  cursor: pointer;
  border-radius: 10px;
  transition: background-color 0.16s ease, border-color 0.16s ease, transform 0.16s ease;
}

.user-row:hover {
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
  transform: translateY(-1px);
}

.user-row:first-of-type {
  border-top: 0;
}

.avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
}

.meta {
  display: grid;
  gap: 2px;
}

.meta span {
  color: var(--muted);
  font-size: 0.85rem;
}

.empty,
.hint {
  text-align: center;
  color: var(--muted);
}
</style>
