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
        <router-link to="/" class="icon-btn" title="首页">
          <House :size="16" />
        </router-link>
        <router-link to="/publish" class="icon-btn" v-if="isLoggedIn" title="发布">
          <SquarePen :size="16" />
        </router-link>
        <router-link to="/favorites" class="icon-btn" v-if="isLoggedIn" title="收藏">
          <Bookmark :size="16" />
        </router-link>
        <router-link to="/profile" class="icon-btn" v-if="isLoggedIn" title="主页">
          <CircleUserRound :size="16" />
        </router-link>
        <router-link to="/settings" class="icon-btn" v-if="isLoggedIn" title="设置">
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
          <router-link class="menu-item" to="/">
            <House :size="16" />
            <span>首页</span>
          </router-link>
          <button class="menu-item mode-btn" @click="setFeedMode('hot')">
            <Flame :size="16" />
            <span>热门</span>
          </button>
          <button class="menu-item mode-btn" @click="setFeedMode('following')">
            <UsersRound :size="16" />
            <span>关注</span>
          </button>
          <router-link class="menu-item" to="/publish">
            <SquarePen :size="16" />
            <span>发布</span>
          </router-link>
          <router-link class="menu-item" to="/favorites">
            <Bookmark :size="16" />
            <span>收藏</span>
          </router-link>
          <router-link class="menu-item" to="/profile">
            <CircleUserRound :size="16" />
            <span>主页</span>
          </router-link>
          <router-link class="menu-item" to="/settings">
            <Settings :size="16" />
            <span>设置</span>
          </router-link>
        </nav>

        <div class="card tip-box">
          <h5>今日提示</h5>
          <p>保持简洁表达，持续发布有价值的动态。</p>
        </div>
      </aside>

      <main class="page" :class="layoutFlags.isAuthPage ? 'auth-page-main' : ''">
        <router-view :key="$route.fullPath" />
      </main>
    </div>

    <transition name="toast-fade">
      <div v-if="toast.show" class="toast" :class="toast.type">{{ toast.message }}</div>
    </transition>
  </div>
</template>

<script setup>
import { Bookmark, CircleUserRound, Flame, House, Search, Settings, Sparkles, SquarePen, UsersRound } from 'lucide-vue-next'
import { computed, onMounted, provide, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo } from './api'
import { resolveLayoutFlags } from './utils/layout'
import { getStoredTheme, setTheme as persistTheme } from './utils/theme'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('token') || '')
const user = ref(null)
const keyword = ref('')
const toast = ref({ show: false, message: '', type: 'success' })
const themeRef = ref(getStoredTheme())

const isLoggedIn = computed(() => !!token.value)
const layoutFlags = computed(() => resolveLayoutFlags(route.path, isLoggedIn.value))
const layoutClassNames = computed(() => ({
  'auth-layout': layoutFlags.value.isAuthPage,
  'guest-layout': layoutFlags.value.isGuestPage,
  'guest-home-layout': layoutFlags.value.expandGuestHomeLayout,
  'with-sidebar': layoutFlags.value.showSidebar
}))

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23d8dde6" width="80" height="80"/><circle fill="%239aa3b3" cx="40" cy="30" r="12"/><rect fill="%239aa3b3" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 2200)
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
  showToast('已退出登录')
  router.push('/login')
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
  router.push('/')
}

const setFeedMode = (mode) => {
  if (!isLoggedIn.value && mode !== 'all') {
    showToast('请先登录后查看该信息流', 'error')
    return
  }
  router.push({ path: '/', query: mode === 'all' ? {} : { mode } })
}

watch(themeRef, (theme) => {
  persistTheme(theme)
})

provide('token', token)
provide('user', user)
provide('isLoggedIn', isLoggedIn)
provide('showToast', showToast)
provide('setToken', setToken)
provide('reloadUser', loadUser)
provide('themeRef', themeRef)

onMounted(loadUser)
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@400;500;600;700;800&family=Fraunces:opsz,wght@9..144,600&display=swap');

:root {
  --bg: #eef2f6;
  --paper: #f8fafc;
  --surface: #ffffff;
  --ink: #111827;
  --muted: #6b7280;
  --line: #dbe3ee;
  --accent: #1f2937;
  --success: #0f766e;
  --error: #b91c1c;
}

:root[data-theme='dark'] {
  --bg: #0f172a;
  --paper: #111827;
  --surface: #1f2937;
  --ink: #f8fafc;
  --muted: #cbd5e1;
  --line: #475569;
  --accent: #f3f4f6;
  --success: #14b8a6;
  --error: #fb7185;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Manrope', 'Segoe UI', sans-serif;
  color: var(--ink);
  background: var(--bg);
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

.shell {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: grid;
  grid-template-columns: 200px minmax(220px, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 12px 18px;
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
  background: color-mix(in srgb, var(--paper) 92%, transparent);
}

.shell.guest-home .topbar {
  position: static;
  border-bottom: 0;
  background: transparent;
  backdrop-filter: none;
  padding-bottom: 4px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 0;
  background: transparent;
  color: var(--ink);
  font-family: 'Fraunces', serif;
  font-size: 1.16rem;
  cursor: pointer;
}

.brand-icon {
  color: var(--muted);
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  border: 1px solid var(--line);
  border-radius: 999px;
  background: var(--surface);
  overflow: hidden;
}

.search-wrap input {
  border: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
  padding: 9px 13px;
  outline: none;
}

.search-wrap button {
  width: 42px;
  border: 0;
  background: transparent;
  color: var(--muted);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.top-actions {
  display: flex;
  gap: 6px;
  align-items: center;
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
  gap: 14px;
  padding: 12px;
}

.layout.with-sidebar {
  grid-template-columns: minmax(0, 1fr);
}

.layout.auth-layout,
.layout.guest-layout {
  grid-template-columns: minmax(0, 1fr);
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
  max-width: none;
  margin: 0;
  padding-inline: clamp(4px, 2vw, 20px);
}

.layout.with-sidebar.guest-home-layout .page {
  margin-left: calc(248px + 20px);
}

.sidebar {
  display: grid;
  gap: 10px;
  align-content: start;
  position: fixed;
  top: 84px;
  width: 248px;
  max-height: calc(100vh - 100px);
  overflow: auto;
  overscroll-behavior: contain;
}

.sidebar,
.sidebar * {
  user-select: none;
  -webkit-user-select: none;
  -webkit-user-drag: none;
}

.layout.with-sidebar .page {
  margin-left: calc(248px + 20px);
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
}

.profile-mini {
  display: flex;
  align-items: center;
  gap: 10px;
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

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 10px;
  border: 1px solid transparent;
  padding: 8px 10px;
  text-decoration: none;
  color: var(--ink);
}

.menu-item.router-link-active {
  border-color: var(--line);
  background: var(--surface);
}

.mode-btn {
  width: 100%;
  font: inherit;
  cursor: pointer;
  border: 1px solid transparent;
  background: transparent;
  text-align: left;
}

.tip-box h5 {
  margin: 0;
}

.tip-box p {
  margin: 5px 0 0;
  color: var(--muted);
  line-height: 1.5;
}

.page {
  width: 100%;
  min-height: calc(100vh - 116px);
}

.auth-page-main {
  display: grid;
  place-items: center;
  min-height: calc(100vh - 140px);
}

.toast {
  position: fixed;
  right: 14px;
  bottom: 16px;
  padding: 10px 12px;
  border-radius: 10px;
  color: #fff;
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

@media (max-width: 1040px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .layout.with-sidebar {
    grid-template-columns: 1fr;
    padding-left: 12px;
  }

  .layout.with-sidebar .page {
    margin-left: 0;
  }

  .sidebar {
    position: static;
    width: auto;
    max-height: none;
    overflow: visible;
  }

  .layout.guest-layout .page,
  .layout.guest-home-layout .page {
    max-width: 100%;
    margin: 0;
  }

  .layout.with-sidebar.guest-home-layout .page {
    margin-left: 0;
  }
}

@media (max-width: 860px) {
  .topbar {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .top-actions {
    flex-wrap: wrap;
  }
}
</style>
