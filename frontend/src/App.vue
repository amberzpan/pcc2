<template>
  <div
    class="shell"
    :class="{
      'auth-shell': layoutFlags.isAuthPage,
      'guest-home': layoutFlags.expandGuestHomeLayout
    }"
  >
    <header class="topbar">
      <button class="brand" @click="goHome" type="button" aria-label="回到首页">
        <Sparkles class="brand-icon" :size="16" />
        <span>SimpleSocial</span>
      </button>

      <form class="search-wrap" @submit.prevent="submitSearch" v-if="!layoutFlags.isAuthPage">
        <input v-model="keyword" placeholder="搜索内容或用户" />
        <button type="submit" aria-label="搜索">
          <Search :size="16" />
        </button>
      </form>

      <div class="auth-gap" v-else></div>

      <nav class="top-actions">
        <router-link to="/" class="icon-btn" title="首页" @click="scrollToTop">
          <House :size="16" />
        </router-link>
        <router-link to="/publish" class="icon-btn" v-if="isLoggedIn" title="发布" @click="scrollToTop">
          <SquarePen :size="16" />
        </router-link>
        <router-link to="/favorites" class="icon-btn" v-if="isLoggedIn" title="收藏" @click="scrollToTop">
          <Bookmark :size="16" />
        </router-link>
        <router-link to="/profile" class="icon-btn" v-if="isLoggedIn" title="主页" @click="scrollToTop">
          <CircleUserRound :size="16" />
        </router-link>
        <router-link to="/settings" class="icon-btn" v-if="isLoggedIn" title="设置" @click="scrollToTop">
          <Settings :size="16" />
        </router-link>
        <router-link to="/login" class="btn ghost" v-if="!isLoggedIn && !$route.path.startsWith('/login')">登录</router-link>
        <router-link to="/register" class="btn solid" v-if="!isLoggedIn && !$route.path.startsWith('/register')">注册</router-link>
        <button class="btn solid" v-if="isLoggedIn" @click="logout">退出</button>
      </nav>
    </header>

    <div class="layout" :class="layoutClassNames">
      <aside class="sidebar" v-if="layoutFlags.showSidebar" draggable="false">
        <div class="card profile-mini" v-if="user">
          <img class="avatar" :src="user.avatar || defaultAvatar" alt="avatar" draggable="false" />
          <div>
            <h4>{{ user.nickname || user.username }}</h4>
            <p>@{{ user.username }}</p>
          </div>
        </div>

        <nav class="menu card">
          <button class="menu-item mode-btn" :class="{ active: currentFeedMode === 'all' }" @click="setFeedMode('all')">
            <House :size="16" />
            <span>首页</span>
          </button>
          <button class="menu-item mode-btn" :class="{ active: currentFeedMode === 'hot' }" @click="setFeedMode('hot')">
            <Flame :size="16" />
            <span>热门</span>
          </button>
          <button class="menu-item mode-btn" :class="{ active: currentFeedMode === 'following' }" @click="setFeedMode('following')">
            <UsersRound :size="16" />
            <span>关注</span>
          </button>
          <router-link class="menu-item" to="/favorites" @click="scrollToTop">
            <Bookmark :size="16" />
            <span>收藏</span>
          </router-link>
          <router-link class="menu-item" to="/profile" @click="scrollToTop">
            <CircleUserRound :size="16" />
            <span>主页</span>
          </router-link>
          <router-link class="menu-item" to="/settings" @click="scrollToTop">
            <Settings :size="16" />
            <span>设置</span>
          </router-link>
        </nav>
        <router-link class="publish-cta card" to="/publish" @click="scrollToTop">
          <SquarePen :size="17" />
          <span>发布</span>
        </router-link>

        <div class="card tip-box">
          <h5>今日提示</h5>
          <p>一句话也可以，真实就好。</p>
        </div>
        <div class="side-footer">
          <span>SimpleSocial</span>
          <small>Premium social feed</small>
        </div>
      </aside>

      <main class="page" :class="layoutFlags.isAuthPage ? 'auth-page-main' : ''">
        <router-view />
      </main>

      <aside class="right-rail" :class="{ 'solo-trends': !showSuggestedRail }" v-if="showRightRail">
        <section class="card rail-card trend-card">
          <header class="rail-head">
            <h5>今日热搜</h5>
            <button
              class="rail-refresh"
              type="button"
              :disabled="rightRailLoading"
              :aria-busy="rightRailLoading"
              title="刷新热搜"
              @click.stop.prevent="refreshRightRail"
            >
              {{ rightRailLoading ? '刷新中' : '刷新' }}
            </button>
          </header>
          <div class="rail-loading" v-if="rightRailLoading && hotTopics.length === 0">加载中</div>
          <button v-for="topic in hotTopics" :key="topic.key" class="trend-item" type="button" @click="openTrend(topic)">
            <span class="trend-rank">{{ topic.rank }}</span>
            <span class="trend-body">
              <span class="trend-title">{{ topic.title }}</span>
            </span>
            <span class="trend-side">
              <small class="trend-heat">{{ topic.heatLabel }}</small>
              <span class="trend-chip" v-if="topic.rank <= 3">热</span>
            </span>
          </button>
          <p class="rail-empty" v-if="!rightRailLoading && hotTopics.length === 0">暂无热搜</p>
        </section>

        <section class="card rail-card suggest-card" v-if="showSuggestedRail">
          <header class="rail-head">
            <h5>推荐关注</h5>
          </header>
          <div class="rail-loading" v-if="rightRailLoading && suggestedUsers.length === 0">加载中</div>
          <article class="suggest-item" v-for="item in suggestedUsers" :key="item.id">
            <button class="suggest-profile" type="button" @click="openProfile(item.id)">
              <img :src="item.avatar || defaultAvatar" alt="avatar" />
              <span>
                <strong>{{ item.nickname || item.username }}</strong>
                <small>@{{ item.username }}</small>
              </span>
            </button>
            <button class="suggest-follow" type="button" :class="{ active: item.followed }" @click="toggleSuggestedFollow(item)">
              {{ item.followed ? '已关注' : '关注' }}
            </button>
          </article>
          <p class="rail-empty" v-if="!rightRailLoading && suggestedUsers.length === 0">暂无推荐用户</p>
        </section>

      </aside>
    </div>

    <transition name="toast-fade">
      <div v-if="toast.show" class="toast" :class="toast.type">{{ toast.message }}</div>
    </transition>
  </div>
</template>

<script setup>
import { Bookmark, CircleUserRound, Flame, House, Search, Settings, Sparkles, SquarePen, UsersRound } from 'lucide-vue-next'
import { computed, onMounted, onUnmounted, provide, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPostList, getTodayHotPosts, getUserInfo, toggleFollow } from './api'
import { normalizeFeedMode, resolveAccessibleFeedMode } from './utils/home-mode'
import { resolveLayoutFlags } from './utils/layout'
import { getStoredTheme, setTheme as persistTheme } from './utils/theme'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('token') || '')
const user = ref(null)
const keyword = ref('')
const toast = ref({ show: false, message: '', type: 'success' })
const themeRef = ref(token.value ? getStoredTheme() : persistTheme('light'))
const rightRailLoading = ref(false)
const hotTopics = ref([])
const suggestedUsers = ref([])
const RIGHT_RAIL_TREND_LIMIT = 12
const SUGGESTION_LIMIT = 5

const isLoggedIn = computed(() => !!token.value)
const layoutFlags = computed(() => resolveLayoutFlags(route.path, isLoggedIn.value))
const showRightRail = computed(() => !layoutFlags.value.isAuthPage && route.path !== '/settings')
const showSuggestedRail = computed(() => isLoggedIn.value && showRightRail.value)
const currentFeedMode = computed(() => {
  if (route.path !== '/') {
    return null
  }
  const normalized = normalizeFeedMode((route.query.mode || '').toString())
  return resolveAccessibleFeedMode(normalized, isLoggedIn.value)
})
const layoutClassNames = computed(() => ({
  'auth-layout': layoutFlags.value.isAuthPage,
  'guest-layout': layoutFlags.value.isGuestPage,
  'guest-home-layout': layoutFlags.value.expandGuestHomeLayout,
  'with-sidebar': layoutFlags.value.showSidebar,
  'with-right-rail': showRightRail.value,
  'settings-layout': route.path === '/settings'
}))

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23d8dde6" width="80" height="80"/><circle fill="%239aa3b3" cx="40" cy="30" r="12"/><rect fill="%239aa3b3" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 2200)
}

const scrollToTop = () => {
  const page = document.querySelector('.page')
  if (page) {
    page.scrollTo({ top: 0, left: 0, behavior: 'smooth' })
  }
  window.scrollTo({ top: 0, left: 0, behavior: 'smooth' })
}

const forwardRailWheel = (event) => {
  const target = event.target
  if (!(target instanceof Element) || !target.closest('.sidebar, .right-rail')) {
    return
  }
  const page = document.querySelector('.page')
  if (!page) {
    return
  }
  event.preventDefault()
  page.scrollBy({ top: event.deltaY, left: 0, behavior: 'auto' })
}

const loadUser = async () => {
  if (!token.value) {
    user.value = null
    return
  }
  try {
    const res = await getUserInfo()
    if (res.code === 200) {
      user.value = res.data
    } else {
      localStorage.removeItem('token')
      token.value = ''
      user.value = null
    }
  } catch {
    localStorage.removeItem('token')
    token.value = ''
    user.value = null
  }
}

const setToken = async (newToken) => {
  token.value = newToken
  localStorage.setItem('token', newToken)
  await loadUser()
}

const logout = () => {
  localStorage.removeItem('token')
  token.value = ''
  user.value = null
  themeRef.value = persistTheme('light')
  showToast('已退出')
  router.push('/login')
}

const clearAuthState = () => {
  token.value = ''
  user.value = null
  themeRef.value = persistTheme('light')
}

const submitSearch = () => {
  const value = keyword.value.trim()
  if (!value) {
    showToast('请输入搜索关键词', 'error')
    return
  }
  router.push({ path: '/search', query: { q: value } })
}

const goHome = () => {
  router.push('/').finally(scrollToTop)
}

const normalizeTopicTitle = (value) => value.replace(/\s+/g, ' ').trim()

const topicFromPost = (post) => {
  const content = typeof post?.content === 'string' ? normalizeTopicTitle(post.content) : ''
  if (!content) {
    return post?.username ? `@${post.username}` : '热门话题'
  }
  const hashMatch = content.match(/#([^#\s]{2,28})#?/)
  if (hashMatch?.[1]) {
    return `#${hashMatch[1]}`
  }
  const trimmed = content.slice(0, 18)
  return content.length > 18 ? `${trimmed}...` : trimmed
}

const topicHeat = (post) => {
  const base = Number(post?.hotScore || 0)
  if (base > 0) {
    return base
  }
  const like = Number(post?.likeCount || 0)
  const comment = Number(post?.commentCount || 0)
  const repost = Number(post?.repostCount || 0)
  return like + comment * 2 + repost * 2
}

const buildHotTopics = (posts) => {
  return posts
    .map((post) => ({
      key: `post-${post.id}`,
      postId: post.id,
      title: topicFromPost(post),
      heat: topicHeat(post)
    }))
    .filter((item) => item.postId && item.title)
    .sort((a, b) => b.heat - a.heat)
    .slice(0, RIGHT_RAIL_TREND_LIMIT)
    .map((item, index) => ({
      ...item,
      rank: index + 1,
      heatLabel: `${Math.max(1, Math.round(item.heat))} 热度`
    }))
}

const buildSuggestedUsers = (posts) => {
  const pool = new Map()
  for (const post of posts) {
    const userId = Number(post?.userId)
    if (!Number.isFinite(userId) || userId <= 0) {
      continue
    }
    if (user.value?.id && userId === user.value.id) {
      continue
    }
    const current = pool.get(userId) || {
      id: userId,
      username: post.username || '',
      nickname: post.nickname || post.username || '用户',
      avatar: post.avatar || '',
      followed: !!post.followed,
      score: 0
    }
    current.followed = current.followed || !!post.followed
    current.score += topicHeat(post) + 1
    pool.set(userId, current)
  }

  const allUsers = Array.from(pool.values()).sort((a, b) => b.score - a.score)
  const unfollowedUsers = allUsers.filter((item) => !item.followed)
  const followedUsers = allUsers.filter((item) => item.followed)
  return unfollowedUsers.concat(followedUsers).slice(0, SUGGESTION_LIMIT)
}

const loadRightRailData = async () => {
  if (!showRightRail.value) {
    hotTopics.value = []
    suggestedUsers.value = []
    return
  }

  rightRailLoading.value = true
  try {
    const [hotRes, latestRes] = await Promise.all([
      getTodayHotPosts(1, 40),
      showSuggestedRail.value ? getPostList(1, 24) : Promise.resolve(null)
    ])
    const hotPosts = hotRes?.code === 200 && Array.isArray(hotRes.data) ? hotRes.data : []
    const latestPosts = latestRes?.code === 200 && Array.isArray(latestRes.data) ? latestRes.data : []
    hotTopics.value = buildHotTopics(hotPosts)
    suggestedUsers.value = showSuggestedRail.value ? buildSuggestedUsers(hotPosts.concat(latestPosts)) : []
  } catch {
    hotTopics.value = []
    suggestedUsers.value = []
  } finally {
    rightRailLoading.value = false
  }
}

const refreshRightRail = async () => {
  if (rightRailLoading.value) {
    return
  }
  await loadRightRailData()
}

const openTrend = (topic) => {
  const postId = Number(topic?.postId)
  if (!Number.isFinite(postId) || postId <= 0) {
    return
  }
  router.push(`/post/${postId}`).finally(scrollToTop)
}

const openProfile = (id) => {
  if (!id) {
    return
  }
  router.push(`/profile/${id}`)
}

const toggleSuggestedFollow = async (item) => {
  if (!isLoggedIn.value) {
    showToast('请登录后关注用户', 'error')
    router.push('/login')
    return
  }
  try {
    const res = await toggleFollow(item.id)
    if (res.code !== 200) {
      return
    }
    const followed = !!res.data?.followed
    suggestedUsers.value = suggestedUsers.value.map((userItem) => (
      userItem.id === item.id ? { ...userItem, followed } : userItem
    ))
  } catch {
    showToast('关注失败，请稍后重试', 'error')
  }
}

const setFeedMode = (mode) => {
  const targetMode = resolveAccessibleFeedMode(mode, isLoggedIn.value)
  if (targetMode !== mode) {
    showToast('请登录后查看该信息流', 'error')
    return
  }
  if (route.path === '/' && currentFeedMode.value === targetMode) {
    scrollToTop()
    return
  }
  router.push({ path: '/', query: targetMode === 'all' ? {} : { mode: targetMode } }).finally(scrollToTop)
}

watch(themeRef, (theme) => {
  persistTheme(theme)
})

watch(
  () => [route.path, route.query.mode, isLoggedIn.value],
  () => {
    loadRightRailData()
  }
)

provide('token', token)
provide('user', user)
provide('isLoggedIn', isLoggedIn)
provide('showToast', showToast)
provide('setToken', setToken)
provide('reloadUser', loadUser)
provide('themeRef', themeRef)

onMounted(async () => {
  window.addEventListener('auth:expired', clearAuthState)
  window.addEventListener('wheel', forwardRailWheel, { passive: false })
  await loadUser()
  await loadRightRailData()
})

onUnmounted(() => {
  window.removeEventListener('auth:expired', clearAuthState)
  window.removeEventListener('wheel', forwardRailWheel)
})
</script>

<style>
:root {
  --bg: #eef2f6;
  --paper: #f8fafc;
  --surface: #ffffff;
  --ink: #111827;
  --muted: #6b7280;
  --line: #dbe3ee;
  --accent: #1f2937;
  --publish: #0f8bdc;
  --on-publish: #f8fafc;
  --toast-ink: #f8fafc;
  --success: #0f766e;
  --error: #b91c1c;
  --ring: color-mix(in srgb, #1d9bf0 60%, transparent);
  --elev-1: 0 8px 24px color-mix(in srgb, #0f172a 8%, transparent);
  --topbar-height: 60px;
  --layout-top-gap: 0px;
  --rail-sticky-top: 0px;
}

:root[data-theme='dark'] {
  --bg: #171a20;
  --paper: #20242c;
  --surface: #2a303a;
  --ink: #d9e0ea;
  --muted: #9ca8b8;
  --line: #3c4654;
  --accent: #c9d2df;
  --publish: #5ca7e8;
  --on-publish: #17202a;
  --toast-ink: #17202a;
  --success: #54c6b7;
  --error: #f08a98;
  --ring: color-mix(in srgb, #60a5fa 50%, transparent);
  --elev-1: 0 10px 28px color-mix(in srgb, #020617 52%, transparent);
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Segoe UI', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  color: var(--ink);
  background: var(--bg);
  text-rendering: optimizeLegibility;
}

html,
body,
#app {
  width: 100%;
  max-width: 100%;
  height: 100%;
  overflow-x: hidden;
  overflow-y: hidden;
  overscroll-behavior: none;
  scrollbar-gutter: auto;
}

html {
  min-height: 100%;
}

body:not(.allow-selection) {
  user-select: none;
  -webkit-user-select: none;
}

input,
textarea,
select,
button,
[contenteditable='true'],
.allow-selection,
.allow-selection * {
  user-select: text;
  -webkit-user-select: text;
}

button,
a {
  user-select: none;
  -webkit-user-select: none;
}

:focus-visible {
  outline: 2px solid var(--ring);
  outline-offset: 2px;
}

::selection {
  background: color-mix(in srgb, #1d9bf0 22%, transparent);
}

.shell {
  height: 100dvh;
  min-height: 100vh;
  width: 100%;
  max-width: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
  background: var(--bg);
}

.topbar {
  position: relative;
  top: 0;
  z-index: 40;
  display: grid;
  grid-template-columns: minmax(170px, 210px) minmax(220px, 560px) minmax(0, 1fr);
  gap: 10px;
  align-items: center;
  min-height: var(--topbar-height);
  padding: 6px 16px;
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
  background: color-mix(in srgb, var(--paper) 98%, var(--bg));
}

.shell.guest-home .topbar {
  border-bottom: 1px solid var(--line);
  background: color-mix(in srgb, var(--paper) 98%, var(--bg));
  backdrop-filter: blur(10px);
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: var(--ink);
  font-family: 'Segoe UI Variable', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  font-size: 1.04rem;
  font-weight: 800;
  cursor: pointer;
}

.brand-icon {
  color: color-mix(in srgb, var(--muted) 82%, var(--ink));
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  width: 100%;
  max-width: 560px;
  justify-self: start;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface) 90%, var(--paper));
  overflow: hidden;
  transition: border-color 0.18s ease, background-color 0.18s ease;
}

.search-wrap:focus-within {
  border-color: color-mix(in srgb, #1d9bf0 45%, var(--line));
  background: var(--surface);
}

.search-wrap input {
  border: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
  padding: 8px 13px;
  outline: none;
  min-width: 0;
}

.search-wrap input::placeholder {
  color: color-mix(in srgb, var(--muted) 88%, transparent);
}

.search-wrap button {
  width: 40px;
  border: 0;
  background: transparent;
  color: var(--muted);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: color 0.16s ease;
}

.search-wrap button:hover {
  color: var(--ink);
}

.top-actions {
  display: flex;
  gap: 7px;
  align-items: center;
  justify-self: end;
}

.icon-btn {
  width: 36px;
  height: 36px;
  border: 1px solid var(--line);
  border-radius: 10px;
  display: grid;
  place-items: center;
  background: var(--surface);
  color: var(--ink);
  text-decoration: none;
  transition: background-color 0.18s ease, border-color 0.18s ease, transform 0.18s ease;
}

.icon-btn:hover {
  background: color-mix(in srgb, var(--surface) 65%, var(--line));
  border-color: color-mix(in srgb, var(--line) 72%, var(--muted));
  transform: translateY(-1px);
}

.btn {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 0.86rem;
  font-weight: 700;
  text-decoration: none;
  background: var(--surface);
  color: var(--ink);
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, filter 0.16s ease;
}

.btn:hover {
  transform: translateY(-1px);
}

.btn.ghost:hover {
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
  background: color-mix(in srgb, var(--surface) 70%, var(--line));
}

.btn.solid:hover {
  filter: brightness(1.02);
}

.btn.solid {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

.layout {
  max-width: 1240px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  align-items: start;
  gap: 14px;
  width: 100%;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  padding: 0 12px;
}

.layout.with-sidebar {
  grid-template-columns: 248px minmax(0, 1fr);
}

.layout.with-right-rail {
  grid-template-columns: minmax(0, 1fr) 300px;
}

.layout.with-sidebar.with-right-rail {
  grid-template-columns: 248px minmax(0, 1fr) 300px;
}

.layout.auth-layout,
.layout.guest-layout {
  grid-template-columns: minmax(0, 1fr);
}

.layout.guest-layout.with-right-rail {
  grid-template-columns: minmax(0, 1fr) 300px;
}

.layout.guest-layout .page {
  max-width: 920px;
  width: 100%;
  margin: 0 auto;
}

.layout.guest-home-layout {
  max-width: 1320px;
}

.layout.guest-home-layout .page {
  max-width: 920px;
  margin: 0 auto;
  padding-inline: 0;
}

.sidebar {
  display: grid;
  grid-template-rows: auto auto auto minmax(0, 1fr) auto;
  align-content: stretch;
  gap: 10px;
  position: sticky;
  top: var(--rail-sticky-top);
  width: 100%;
  height: 100%;
  min-height: 0;
  max-height: none;
  overflow: hidden;
  overscroll-behavior: none;
  padding: 8px 0 12px;
}

.sidebar,
.sidebar * {
  user-select: none;
  -webkit-user-select: none;
  -webkit-user-drag: none;
}

.layout.with-sidebar .page {
  margin-left: 0;
  min-width: 0;
  align-self: start;
}

.layout.with-sidebar:not(.with-right-rail) .page {
  max-width: 760px;
  margin: 0 auto;
}

.layout.settings-layout.with-sidebar:not(.with-right-rail) .page {
  width: 100%;
  max-width: 980px;
  margin: 0;
}

.layout:not(.with-sidebar):not(.with-right-rail):not(.auth-layout) .page {
  max-width: 760px;
  margin: 0 auto;
}

.layout.with-right-rail .page {
  align-self: start;
}

.right-rail {
  display: grid;
  grid-template-rows: auto auto;
  gap: 12px;
  align-content: start;
  position: sticky;
  top: var(--rail-sticky-top);
  width: 100%;
  height: 100%;
  min-height: 0;
  max-height: none;
  overflow: hidden;
  overscroll-behavior: none;
  padding: 8px 0 12px;
}

.right-rail.solo-trends {
  grid-template-rows: auto;
  align-content: start;
}

.right-rail::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.sidebar::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.card {
  border: 1px solid var(--line);
  border-radius: 14px;
  background: var(--paper);
  padding: 12px;
  box-shadow: var(--elev-1);
}

.profile-mini {
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 14px;
  padding: 12px;
  transition: background-color 0.16s ease;
}

.profile-mini:hover {
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-mini h4 {
  margin: 0;
}

.profile-mini p {
  margin: 2px 0 0;
  color: var(--muted);
  font-size: 0.82rem;
}

.menu {
  display: grid;
  gap: 6px;
}

.publish-cta {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  text-decoration: none;
  color: var(--on-publish);
  font-weight: 800;
  border-radius: 999px;
  background: var(--publish);
  border-color: color-mix(in srgb, var(--publish) 82%, var(--line));
  box-shadow: 0 8px 18px color-mix(in srgb, var(--publish) 22%, transparent);
  transition: transform 0.18s ease, filter 0.18s ease, box-shadow 0.18s ease;
}

.publish-cta:hover {
  transform: translateY(-1px);
  filter: brightness(1.04);
  box-shadow: 0 10px 22px color-mix(in srgb, var(--publish) 28%, transparent);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 10px;
  border: 1px solid transparent;
  min-height: 40px;
  padding: 8px 10px;
  text-decoration: none;
  color: var(--ink);
  font-weight: 650;
  transition: background-color 0.16s ease, border-color 0.16s ease, transform 0.16s ease;
}

.menu-item:hover {
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
  background: color-mix(in srgb, var(--surface) 75%, var(--line));
  transform: translateY(-1px);
}

.menu-item.router-link-active {
  border-color: var(--line);
  background: var(--surface);
}

.menu-item.active {
  border-color: var(--line);
  background: var(--surface);
}

.mode-btn {
  width: 100%;
  font: inherit;
  font-weight: 650;
  cursor: pointer;
  border: 1px solid transparent;
  background: transparent;
  text-align: left;
}

.tip-box {
  align-self: start;
}

.tip-box h5 {
  margin: 0;
}

.tip-box p {
  margin: 5px 0 0;
  color: var(--muted);
  line-height: 1.5;
}

.side-footer {
  align-self: end;
  display: grid;
  gap: 2px;
  margin-top: 6px;
  padding: 0 4px;
  color: var(--muted);
  font-size: 0.74rem;
}

.side-footer span {
  color: var(--ink);
  font-weight: 800;
}

.rail-card {
  display: grid;
  gap: 0;
  min-height: 0;
  overflow: hidden;
  align-content: start;
}

.trend-card {
  padding: 0;
  overflow: hidden;
  padding-bottom: 8px;
}

.suggest-card {
  gap: 8px;
  overflow: hidden;
  max-height: none;
  padding: 10px;
  align-content: start;
}

.rail-card::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.rail-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.rail-head h5 {
  margin: 0;
  font-size: 0.95rem;
}

.rail-refresh {
  position: relative;
  z-index: 2;
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 0.74rem;
  background: var(--surface);
  color: var(--ink);
  cursor: pointer;
  pointer-events: auto;
  transition: background-color 0.16s ease, border-color 0.16s ease, color 0.16s ease;
}

.rail-refresh:hover:not(:disabled) {
  border-color: color-mix(in srgb, var(--line) 62%, var(--muted));
  background: color-mix(in srgb, var(--surface) 72%, var(--line));
}

.rail-refresh:disabled {
  color: var(--muted);
  cursor: progress;
  opacity: 0.82;
}

.rail-loading,
.rail-empty {
  margin: 0;
  color: var(--muted);
  font-size: 0.84rem;
}

.trend-card .rail-head {
  padding: 12px 12px 6px;
}

.trend-card .rail-loading,
.trend-card .rail-empty {
  padding: 10px 12px 14px;
}

.trend-item {
  width: 100%;
  border: 1px solid transparent;
  border-radius: 0;
  background: transparent;
  color: var(--ink);
  padding: 8px 12px;
  text-align: left;
  display: grid;
  grid-template-columns: 24px minmax(0, 1fr) minmax(68px, auto);
  gap: 10px;
  align-items: start;
  cursor: pointer;
  transition: background-color 0.16s ease;
}

.trend-item:last-of-type {
  margin-bottom: 10px;
}

.trend-item:hover {
  background: color-mix(in srgb, var(--surface) 76%, var(--line));
}

.trend-rank {
  color: var(--muted);
  font-size: 0.78rem;
  font-weight: 800;
  text-align: center;
}

.trend-body {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.trend-side {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  gap: 6px;
  min-width: 82px;
  white-space: nowrap;
}

.trend-title {
  font-size: 0.88rem;
  line-height: 1.24;
  font-weight: 700;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trend-item small {
  color: var(--muted);
  font-size: 0.72rem;
  line-height: 1.12;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.trend-heat {
  color: color-mix(in srgb, var(--ink) 72%, var(--muted));
  font-size: 0.72rem;
  font-weight: 700;
}

.trend-chip {
  border-radius: 999px;
  padding: 2px 6px;
  background: color-mix(in srgb, var(--publish) 10%, transparent);
  color: var(--publish);
  font-size: 0.68rem;
  font-weight: 800;
}

.suggest-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 6px;
  align-items: center;
  border: 1px solid color-mix(in srgb, var(--line) 75%, transparent);
  border-radius: 12px;
  padding: 6px;
  background: color-mix(in srgb, var(--surface) 88%, var(--paper));
  transition: border-color 0.16s ease, background-color 0.16s ease;
}

.suggest-item:hover {
  border-color: color-mix(in srgb, var(--line) 58%, var(--muted));
  background: color-mix(in srgb, var(--surface) 96%, var(--paper));
}

.suggest-profile {
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--ink);
  display: flex;
  gap: 7px;
  align-items: center;
  padding: 4px 5px;
  cursor: pointer;
  text-align: left;
}

.suggest-profile:hover {
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.suggest-profile img {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  object-fit: cover;
}

.suggest-profile span {
  display: grid;
  min-width: 0;
}

.suggest-profile strong,
.suggest-profile small {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.suggest-profile strong {
  font-size: 0.84rem;
  color: var(--ink);
}

:root[data-theme='dark'] .suggest-profile strong {
  color: #f8fafc;
}

.suggest-profile small {
  color: var(--muted);
  font-size: 0.74rem;
}

.suggest-follow {
  border: 1px solid var(--line);
  border-radius: 999px;
  background: transparent;
  color: var(--ink);
  font-size: 0.76rem;
  font-weight: 700;
  padding: 5px 10px;
  cursor: pointer;
}

@media (max-height: 760px) and (min-width: 1041px) {
  .right-rail {
    gap: 8px;
  }

  .trend-item {
    padding-block: 4px;
  }

  .trend-title {
    font-size: 0.82rem;
  }

  .trend-item small {
    display: none;
  }

  .suggest-profile img {
    width: 30px;
    height: 30px;
  }

  .suggest-follow {
    padding: 4px 9px;
  }
}

.suggest-follow.active {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

.page {
  width: 100%;
  height: 100%;
  min-height: 0;
  min-width: 0;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior-x: none;
  overscroll-behavior-y: contain;
  padding: 8px 0 24px;
  scrollbar-gutter: auto;
}

.page::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.auth-page-main {
  display: grid;
  place-items: center;
  min-height: 0;
}

.toast {
  position: fixed;
  right: 14px;
  bottom: 16px;
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--toast-ink);
  font-weight: 700;
  font-size: 0.84rem;
}

.toast.success {
  background: var(--success);
}

.toast.error {
  background: var(--error);
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.22s;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
}

@media (max-width: 1040px), (hover: none) and (pointer: coarse) {
  html,
  body,
  #app {
    height: auto;
    min-height: 100%;
    max-width: 100%;
    overflow-x: hidden !important;
    overflow-y: auto !important;
    overscroll-behavior-y: none;
    -webkit-overflow-scrolling: touch;
    touch-action: pan-y pinch-zoom;
  }

  .shell {
    display: block;
    height: auto;
    min-height: 100dvh;
    overflow-x: hidden;
    overflow-y: visible;
    touch-action: pan-y pinch-zoom;
  }

  .topbar {
    position: sticky;
    top: 0;
    grid-template-columns: minmax(0, 1fr) auto;
    gap: 8px;
    padding: 8px 12px;
  }

  .brand {
    min-width: 0;
  }

  .search-wrap {
    grid-column: 1 / -1;
    max-width: 100%;
    order: 3;
  }

  .top-actions {
    max-width: 100%;
    justify-self: end;
    gap: 6px;
    flex-wrap: nowrap;
    overflow-x: auto;
    overflow-y: hidden;
    padding-bottom: 2px;
    scrollbar-width: none;
  }

  .top-actions::-webkit-scrollbar {
    width: 0;
    height: 0;
  }

  .icon-btn {
    width: 40px;
    height: 40px;
    flex: 0 0 auto;
  }

  .layout {
    grid-template-columns: 1fr;
    height: auto;
    min-height: calc(100dvh - var(--topbar-height));
    max-width: 100%;
    overflow: visible;
    padding: 8px 12px 16px;
    touch-action: pan-y pinch-zoom;
  }

  .layout.with-sidebar {
    grid-template-columns: 1fr;
  }

  .layout.with-right-rail,
  .layout.with-sidebar.with-right-rail {
    grid-template-columns: 1fr;
  }

  .layout.with-sidebar .page {
    margin-left: 0;
  }

  .layout.with-sidebar:not(.with-right-rail) .page {
    max-width: 100%;
    margin: 0;
  }

  .layout:not(.with-sidebar):not(.with-right-rail):not(.auth-layout) .page {
    max-width: 100%;
    margin: 0;
  }

  .sidebar,
  .right-rail {
    display: none;
  }

  .page {
    order: 1;
    height: auto;
    min-height: calc(100dvh - 116px);
    overflow-x: hidden;
    overflow-y: visible;
    max-width: 100%;
    padding: 0 0 18px;
    -webkit-overflow-scrolling: touch;
    touch-action: pan-y pinch-zoom;
  }

  .layout.guest-home-layout {
    max-width: 100%;
  }

  .layout.guest-layout .page,
  .layout.guest-home-layout .page,
  .layout.with-sidebar .page,
  .layout.with-right-rail .page,
  .layout.with-sidebar.with-right-rail .page {
    max-width: 100%;
    margin: 0;
  }
}

@media (max-width: 860px) {
  .topbar {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .search-wrap {
    width: 100%;
    max-width: 100%;
  }

  .top-actions {
    flex-wrap: wrap;
    justify-self: start;
  }
}
</style>
