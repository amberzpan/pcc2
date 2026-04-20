<template>
  <section class="feed-page">
    <div class="panel guest-hero" v-if="!isLoggedIn">
      <div class="hero-copy">
        <p class="eyebrow">SimpleSocial 社区</p>
        <h1>看见正在发生的讨论，加入你关心的话题。</h1>
        <p class="hero-desc">
          不登录也能浏览公开内容；注册后可关注创作者、参与评论，并构建属于你的高质量信息流。
        </p>
        <div class="hero-actions">
          <router-link class="hero-btn solid" to="/register">立即注册</router-link>
          <router-link class="hero-btn ghost" to="/login">已有账号，去登录</router-link>
        </div>
      </div>
      <div class="hero-highlights">
        <article class="highlight-item">
          <strong>热门追踪</strong>
          <span>实时查看社区正在讨论的高热内容。</span>
        </article>
        <article class="highlight-item">
          <strong>发现作者</strong>
          <span>从公开动态里找到有价值的长期创作者。</span>
        </article>
        <article class="highlight-item">
          <strong>轻量参与</strong>
          <span>登录后即可点赞、收藏、评论并管理个人主页。</span>
        </article>
      </div>
    </div>

    <div class="panel switcher">
      <button :class="['switch-btn', mode === 'all' ? 'active' : '']" @click="changeMode('all')">全部动态</button>
      <button :class="['switch-btn', mode === 'hot' ? 'active' : '']" @click="changeMode('hot')">热门帖</button>
      <button
        :class="['switch-btn', mode === 'following' ? 'active' : '']"
        :disabled="!isLoggedIn"
        :title="isLoggedIn ? '' : '登录后可查看关注动态'"
        @click="changeMode('following')"
      >
        关注动态
      </button>
      <button :class="['switch-btn', mode === 'discover' ? 'active' : '']" @click="changeMode('discover')">发现动态</button>
    </div>

    <div class="panel mode-intro">
      <div>
        <h2>{{ modeMeta.label }}</h2>
        <p>{{ modeMeta.description }}</p>
      </div>
      <div class="mode-badge">{{ modeMeta.mode.toUpperCase() }}</div>
    </div>

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

    <div class="panel empty" v-if="!loading && posts.length === 0">当前没有内容</div>

    <div class="panel load-more" v-if="hasMore" @click="loadMore">加载更多</div>
    <div class="panel hint" v-if="loading">加载中...</div>
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
  getDiscoverPosts,
  getComments,
  getFollowingPosts,
  getHotPosts,
  getPostList,
  toggleCommentLike as apiToggleCommentLike,
  toggleFavorite,
  toggleFollow,
  toggleLike
} from '@/api'
import PostCard from '@/components/PostCard.vue'
import { normalizeComments, normalizePostList } from '@/utils/post-utils'
import { getFeedModeMeta, normalizeFeedMode, resolveAccessibleFeedMode } from '@/utils/home-mode'

const user = inject('user')
const isLoggedIn = inject('isLoggedIn')
const showToast = inject('showToast')
const route = useRoute()
const router = useRouter()

const posts = ref([])
const page = ref(1)
const size = 10
const hasMore = ref(false)
const loading = ref(false)
const mode = ref('all')

const currentUserId = computed(() => user.value?.id)
const modeMeta = computed(() => getFeedModeMeta(mode.value, isLoggedIn.value))

const setMode = (nextMode, syncRoute = true) => {
  const accessible = resolveAccessibleFeedMode(nextMode, isLoggedIn.value)
  mode.value = accessible
  if (!syncRoute) {
    return
  }
  const query = { ...route.query }
  if (accessible === 'all') {
    delete query.mode
  } else {
    query.mode = accessible
  }
  router.replace({ path: '/', query })
}

const fetchPosts = async (reset = true) => {
  if (reset) {
    page.value = 1
    posts.value = []
  }

  loading.value = true
  try {
    let api = getPostList
    if (mode.value === 'following') {
      api = getFollowingPosts
    } else if (mode.value === 'hot') {
      api = getHotPosts
    } else if (mode.value === 'discover') {
      api = getDiscoverPosts
    }
    const res = await api(page.value, size)
    if (res.code !== 200 || !Array.isArray(res.data)) {
      posts.value = []
      hasMore.value = false
      return
    }
    const next = normalizePostList(res.data)
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
  setMode(target)
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
      post.comments = normalizeComments(res.data)
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
      post.comments.push({
        ...res.data,
        liked: false,
        likeCount: res.data?.likeCount || 0
      })
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

const removeComment = async ({ post, commentId }) => {
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

const toggleCommentLike = async ({ post, comment }) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
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
    showToast('评论点赞失败', 'error')
  }
}

onMounted(() => {
  const queryMode = normalizeFeedMode((route.query.mode || '').toString())
  setMode(queryMode)
  fetchPosts(true)
})

watch(() => route.query.mode, (value) => {
  const next = normalizeFeedMode((value || '').toString())
  const accessible = resolveAccessibleFeedMode(next, isLoggedIn.value)
  if (accessible !== mode.value) {
    mode.value = accessible
    fetchPosts(true)
  }
})

watch(() => isLoggedIn.value, (loggedIn) => {
  if (!loggedIn && mode.value === 'following') {
    setMode('all')
    fetchPosts(true)
  }
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
  flex-wrap: wrap;
}

.guest-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(260px, 0.95fr);
  gap: 14px;
  border-color: #eecfb8;
  background:
    radial-gradient(circle at 14% 18%, #fff4e8 0, transparent 43%),
    radial-gradient(circle at 90% 85%, #ffe4ce 0, transparent 45%),
    #fffaf4;
}

.hero-copy h1 {
  margin: 0;
  font-size: clamp(1.2rem, 1.3rem + 0.4vw, 1.62rem);
  line-height: 1.35;
}

.eyebrow {
  margin: 0 0 4px;
  font-size: 0.82rem;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #a7421d;
}

.hero-desc {
  margin: 10px 0 0;
  color: #705f4c;
  line-height: 1.6;
}

.hero-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  flex-wrap: wrap;
}

.hero-btn {
  text-decoration: none;
  border-radius: 999px;
  font-weight: 700;
  padding: 8px 13px;
  border: 1px solid #e8c9b1;
}

.hero-btn.solid {
  color: #fff;
  border-color: #f25a29;
  background: #f25a29;
}

.hero-btn.ghost {
  color: #7a3b20;
  background: #fff7ef;
}

.hero-highlights {
  display: grid;
  gap: 8px;
  align-content: start;
}

.highlight-item {
  border: 1px solid #ecd7c5;
  border-radius: 12px;
  background: #fffdf9;
  padding: 10px;
  display: grid;
  gap: 4px;
}

.highlight-item strong {
  font-size: 0.92rem;
}

.highlight-item span {
  color: #786955;
  line-height: 1.45;
  font-size: 0.86rem;
}

.switch-btn {
  border: 1px solid #e7dccf;
  border-radius: 999px;
  padding: 8px 14px;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
}

.switch-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.switch-btn.active {
  background: #f25a29;
  border-color: #f25a29;
  color: #fff;
}

.mode-intro {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  background: linear-gradient(125deg, #fff4ea 0%, #fffdf8 58%, #ffeedf 100%);
}

.mode-intro h2 {
  margin: 0;
  font-size: 1.02rem;
}

.mode-intro p {
  margin: 4px 0 0;
  color: #7a6d5a;
  line-height: 1.5;
}

.mode-badge {
  flex: 0 0 auto;
  border: 1px solid #efcfb4;
  border-radius: 999px;
  padding: 6px 10px;
  background: #fff;
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: #a8431e;
}

@media (max-width: 860px) {
  .guest-hero {
    grid-template-columns: 1fr;
  }
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
