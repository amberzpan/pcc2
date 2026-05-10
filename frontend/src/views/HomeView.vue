<template>
  <section class="feed-page">
    <div class="panel guest-hero" v-if="!isLoggedIn">
      <div class="hero-copy">
        <p class="eyebrow">SimpleSocial 社区</p>
        <h1>浏览社区动态，参与关注话题。</h1>
        <p class="hero-desc">
          未登录状态下可浏览公开内容；登录后可关注用户、参与评论并管理收藏。
        </p>
        <div class="hero-actions">
          <router-link class="hero-btn solid" to="/register">注册账号</router-link>
          <router-link class="hero-btn ghost" to="/login">登录账号</router-link>
        </div>
      </div>
      <div class="hero-highlights">
        <article class="highlight-item">
          <strong>热门内容</strong>
          <span>查看社区当前讨论度较高的内容。</span>
        </article>
        <article class="highlight-item">
          <strong>关注用户</strong>
          <span>关注感兴趣的用户，持续查看其最新发布。</span>
        </article>
        <article class="highlight-item">
          <strong>参与互动</strong>
          <span>支持点赞、评论、转发与收藏等常用操作。</span>
        </article>
      </div>
    </div>

    <div class="panel switcher" v-if="isLoggedIn">
      <button :class="['switch-btn', mode === 'all' ? 'active' : '']" @click="changeMode('all')">首页</button>
      <button :class="['switch-btn', mode === 'hot' ? 'active' : '']" :disabled="!isLoggedIn" @click="changeMode('hot')">热门</button>
      <button
        :class="['switch-btn', mode === 'following' ? 'active' : '']"
        :disabled="!isLoggedIn"
        :title="isLoggedIn ? '' : '登录后可查看关注内容'"
        @click="changeMode('following')"
      >
        关注
      </button>
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

    <div class="panel empty" v-if="!loading && hasLoadedOnce && posts.length === 0">暂无内容</div>

    <div ref="loadMoreRef" class="auto-load" aria-hidden="true"></div>
    <div class="panel hint" v-if="loading && !hasLoadedOnce && posts.length === 0">动态加载中</div>
  </section>
</template>

<script setup>
import { computed, inject, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createComment,
  createPost,
  deleteComment as apiDeleteComment,
  deletePost,
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
import { shouldTriggerInfiniteLoad } from '@/utils/infinite-scroll'
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
const hasLoadedOnce = ref(false)
const loadMoreRef = ref(null)
let observer = null
let latestRequestId = 0

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
    if (!hasLoadedOnce.value) {
      posts.value = []
    }
  }

  loading.value = true
  const requestId = ++latestRequestId
  try {
    let api = getPostList
    if (mode.value === 'following') {
      api = getFollowingPosts
    } else if (mode.value === 'hot') {
      api = getHotPosts
    }
    const res = await api(page.value, size)
    if (requestId !== latestRequestId) {
      return
    }
    if (res.code !== 200 || !Array.isArray(res.data)) {
      posts.value = []
      hasMore.value = false
      hasLoadedOnce.value = true
      return
    }
    const next = normalizePostList(res.data)
    posts.value = reset ? next : posts.value.concat(next)
    hasMore.value = next.length === size
    hasLoadedOnce.value = true
  } catch {
    showToast('动态加载失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) {
    return
  }
  page.value += 1
  await fetchPosts(false)
}

const changeMode = async (target) => {
  setMode(target)
  document.querySelector('.page')?.scrollTo({ top: 0, left: 0, behavior: 'smooth' })
  window.scrollTo({ top: 0, left: 0, behavior: 'smooth' })
  await fetchPosts(true)
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
  if (!loggedIn && mode.value !== 'all') {
    setMode('all')
    fetchPosts(true)
  }
})

const setupInfiniteLoad = () => {
  if (!loadMoreRef.value || observer) {
    return
  }
  observer = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (shouldTriggerInfiniteLoad({
        isIntersecting: entry.isIntersecting,
        loading: loading.value,
        hasMore: hasMore.value
      })) {
        loadMore()
      }
    }
  }, { rootMargin: '300px 0px 200px 0px' })
  observer.observe(loadMoreRef.value)
}

onMounted(() => {
  setupInfiniteLoad()
})

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style scoped>
.feed-page {
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
  box-shadow: 0 10px 24px color-mix(in srgb, #0f172a 8%, transparent);
}

.switcher {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  position: sticky;
  top: calc(var(--rail-sticky-top) - 8px);
  z-index: 12;
  background: color-mix(in srgb, var(--paper) 94%, transparent);
  backdrop-filter: blur(6px);
  box-shadow: 0 6px 18px color-mix(in srgb, #0f172a 7%, transparent);
}

.guest-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(260px, 0.95fr);
  gap: 14px;
  border-color: var(--line);
  background: var(--surface);
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
  color: var(--muted);
}

.hero-desc {
  margin: 10px 0 0;
  color: var(--muted);
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
  border: 1px solid var(--line);
  transition: transform 0.16s ease, border-color 0.16s ease, filter 0.16s ease;
}

.hero-btn:hover {
  transform: translateY(-1px);
}

.hero-btn.solid {
  color: var(--paper);
  border-color: var(--accent);
  background: var(--accent);
}

.hero-btn.ghost {
  color: var(--ink);
  background: transparent;
}

.hero-highlights {
  display: grid;
  gap: 8px;
  align-content: start;
}

.highlight-item {
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--paper);
  padding: 10px;
  display: grid;
  gap: 4px;
  transition: border-color 0.16s ease, background-color 0.16s ease;
}

.highlight-item:hover {
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.highlight-item strong {
  font-size: 0.92rem;
}

.highlight-item span {
  color: var(--muted);
  line-height: 1.45;
  font-size: 0.86rem;
}

.switch-btn {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 8px 14px;
  background: transparent;
  color: var(--ink);
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.16s ease, border-color 0.16s ease, background-color 0.16s ease;
}

.switch-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 66%, var(--muted));
  background: color-mix(in srgb, var(--surface) 70%, var(--line));
}

.switch-btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.switch-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--paper);
}

.mode-intro {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  background: var(--surface);
}

.mode-intro h2 {
  margin: 0;
  font-size: 1.02rem;
}

.mode-intro p {
  margin: 4px 0 0;
  color: var(--muted);
  line-height: 1.5;
}

.mode-badge {
  flex: 0 0 auto;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 6px 10px;
  background: var(--paper);
  font-size: 0.78rem;
  font-weight: 800;
  letter-spacing: 0.06em;
  color: var(--muted);
}

@media (max-width: 860px) {
  .feed-page {
    gap: 10px;
  }

  .panel {
    padding: 12px;
    border-radius: 14px;
  }

  .switcher {
    top: calc(var(--topbar-height) + 8px);
    gap: 8px;
    padding: 10px 12px;
  }

  .guest-hero {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .hero-actions {
    width: 100%;
  }

  .hero-btn {
    flex: 1 1 0;
    text-align: center;
  }

  .mode-intro {
    display: grid;
    justify-content: stretch;
  }

  .mode-badge {
    justify-self: start;
  }
}

.auto-load {
  width: 100%;
  height: 1px;
}

.hint,
.empty {
  text-align: center;
  color: var(--muted);
}
</style>
