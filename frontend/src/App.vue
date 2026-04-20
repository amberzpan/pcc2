<template>
  <div
    class="shell"
    :class="{
      'auth-shell': layoutFlags.isAuthPage,
      'guest-home': layoutFlags.expandGuestHomeLayout
    }"
  >
    <header class="topbar">
      <div class="brand" @click="goHome">
        <span class="logo">🌊</span>
        <span>SimpleSocial</span>
      </div>

      <form class="search-wrap" @submit.prevent="submitSearch" v-if="!layoutFlags.isAuthPage">
        <input v-model="keyword" placeholder="搜索内容/用户" />
        <button type="submit">🔎</button>
      </form>

      <div class="auth-gap" v-else></div>

      <nav class="top-actions">
        <router-link to="/" class="icon-btn" title="首页">🏠</router-link>
        <router-link to="/publish" class="icon-btn" v-if="isLoggedIn" title="发布">✍️</router-link>
        <router-link to="/favorites" class="icon-btn" v-if="isLoggedIn" title="收藏">⭐</router-link>
        <router-link to="/profile" class="icon-btn" v-if="isLoggedIn" title="我的">👤</router-link>
        <router-link to="/login" class="btn ghost" v-if="!isLoggedIn && !$route.path.startsWith('/login')">登录</router-link>
        <router-link to="/register" class="btn solid" v-if="!isLoggedIn && !$route.path.startsWith('/register')">注册</router-link>
        <button class="btn solid" v-if="isLoggedIn" @click="logout">退出</button>
      </nav>
    </header>

    <div class="layout" :class="layoutClassNames">
      <aside class="sidebar" v-if="layoutFlags.showSidebar">
        <div class="card profile-mini" v-if="user">
          <img class="avatar" :src="user.avatar || defaultAvatar" alt="avatar" />
          <div>
            <h4>{{ user.nickname || user.username }}</h4>
            <p>@{{ user.username }}</p>
          </div>
        </div>

        <nav class="menu card">
          <router-link class="menu-item" to="/">
            <span>🏠</span>
            <span>首页</span>
          </router-link>
          <button class="menu-item mode-btn" @click="setFeedMode('hot')">
            <span>🔥</span>
            <span>热门帖</span>
          </button>
          <button class="menu-item mode-btn" @click="setFeedMode('following')">
            <span>👥</span>
            <span>关注帖</span>
          </button>
          <button class="menu-item mode-btn" @click="setFeedMode('discover')">
            <span>🧭</span>
            <span>发现帖</span>
          </button>
          <router-link class="menu-item" to="/publish">
            <span>✍️</span>
            <span>发布动态</span>
          </router-link>
          <router-link class="menu-item" to="/favorites">
            <span>⭐</span>
            <span>我的收藏</span>
          </router-link>
          <router-link class="menu-item" to="/profile">
            <span>👤</span>
            <span>个人主页</span>
          </router-link>
        </nav>

        <div class="card tip-box">
          <h5>今日灵感</h5>
          <p>把你的想法写下来，和朋友们一起互动。</p>
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
import { computed, onMounted, provide, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserInfo } from './api'
import { resolveLayoutFlags } from './utils/layout'

const router = useRouter()
const route = useRoute()
const token = ref(localStorage.getItem('token') || '')
const user = ref(null)
const keyword = ref('')
const toast = ref({ show: false, message: '', type: 'success' })

const isLoggedIn = computed(() => !!token.value)
const layoutFlags = computed(() => resolveLayoutFlags(route.path, isLoggedIn.value))
const layoutClassNames = computed(() => ({
  'auth-layout': layoutFlags.value.isAuthPage,
  'guest-layout': layoutFlags.value.isGuestPage,
  'guest-home-layout': layoutFlags.value.expandGuestHomeLayout
}))

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23eadac6" width="80" height="80"/><circle fill="%23c9af8f" cx="40" cy="30" r="12"/><rect fill="%23c9af8f" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 2400)
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
  router.push({ path: '/', query: { mode } })
}

provide('token', token)
provide('user', user)
provide('isLoggedIn', isLoggedIn)
provide('showToast', showToast)
provide('setToken', setToken)
provide('reloadUser', loadUser)

onMounted(loadUser)
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;700;800&family=Space+Grotesk:wght@400;500;700&display=swap');

:root {
  --bg: #f6f0e7;
  --paper: #fffdf8;
  --ink: #2f2619;
  --muted: #7a6d5a;
  --accent: #f25a29;
  --accent-strong: #d84a1d;
  --line: #e7dccf;
}

* {
  box-sizing: border-box;
}

body {
  margin: 0;
  font-family: 'Space Grotesk', 'Segoe UI', sans-serif;
  color: var(--ink);
  background:
    radial-gradient(circle at 7% 10%, #ffdcb9 0, transparent 38%),
    radial-gradient(circle at 90% 22%, #ffe8c8 0, transparent 34%),
    linear-gradient(180deg, #f8f2e9, #f3ecdf);
}

.shell {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: grid;
  grid-template-columns: 220px minmax(280px, 1fr) auto;
  gap: 16px;
  align-items: center;
  padding: 12px 18px;
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
  background: rgba(255, 251, 244, 0.86);
}

.shell.guest-home .topbar {
  position: static;
  border-bottom: 0;
  background: transparent;
  backdrop-filter: none;
  padding: 14px 20px 6px;
}

.auth-gap {
  min-height: 40px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-family: 'Outfit', sans-serif;
  font-weight: 800;
  font-size: 1.24rem;
  cursor: pointer;
}

.logo {
  font-size: 1.3rem;
}

.search-wrap {
  display: grid;
  grid-template-columns: 1fr auto;
  background: var(--paper);
  border: 1px solid var(--line);
  border-radius: 999px;
  overflow: hidden;
}

.search-wrap input {
  border: 0;
  padding: 10px 14px;
  background: transparent;
  font-size: 0.95rem;
  outline: none;
}

.search-wrap button {
  border: 0;
  background: var(--accent);
  color: #fff;
  font-weight: 700;
  width: 48px;
  cursor: pointer;
}

.top-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.icon-btn {
  width: 38px;
  height: 38px;
  border: 1px solid var(--line);
  border-radius: 10px;
  display: grid;
  place-items: center;
  text-decoration: none;
  color: var(--ink);
  background: var(--paper);
}

.btn {
  border: 1px solid var(--line);
  padding: 8px 12px;
  border-radius: 999px;
  text-decoration: none;
  color: var(--ink);
  background: var(--paper);
  cursor: pointer;
  font-weight: 600;
  font-size: 0.9rem;
}

.btn.solid {
  border-color: var(--accent);
  background: var(--accent);
  color: #fff;
}

.layout {
  max-width: 1280px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 16px;
  padding: 14px;
}

.layout.auth-layout {
  grid-template-columns: minmax(0, 1fr);
  max-width: 1280px;
  padding-top: 6px;
}

.layout.guest-layout {
  grid-template-columns: minmax(0, 1fr);
  max-width: 1280px;
  margin: 0 auto;
}

.layout.guest-layout .page {
  max-width: 980px;
  width: 100%;
  margin: 0 auto;
}

.layout.guest-home-layout .page {
  max-width: none;
  width: 100%;
  margin: 0;
}

.layout.guest-home-layout {
  max-width: 1400px;
  padding-top: 6px;
}

.auth-page-main {
  width: 100%;
  display: grid;
  place-items: center;
  min-height: calc(100vh - 120px);
}

.sidebar {
  display: grid;
  gap: 12px;
  align-content: start;
  position: sticky;
  top: 86px;
  max-height: calc(100vh - 100px);
}

.card {
  border: 1px solid #e7dccf;
  border-radius: 16px;
  background: #fffdf8;
  padding: 12px;
  box-shadow: 0 8px 22px rgba(66, 45, 17, 0.05);
}

.profile-mini {
  display: flex;
  align-items: center;
  gap: 10px;
}

.avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  object-fit: cover;
}

.profile-mini h4 {
  margin: 0;
}

.profile-mini p {
  margin: 2px 0 0;
  font-size: 0.84rem;
  color: var(--muted);
}

.menu {
  display: grid;
  gap: 8px;
}

.menu-item {
  display: flex;
  gap: 10px;
  align-items: center;
  text-decoration: none;
  color: var(--ink);
  padding: 9px 10px;
  border-radius: 10px;
  border: 1px solid transparent;
}

.mode-btn {
  width: 100%;
  text-align: left;
  background: #fff;
  cursor: pointer;
  font: inherit;
}

.menu-item.router-link-active {
  background: #fff3ea;
  border-color: #f0cfb6;
  color: #ad3f1c;
}

.tip-box h5 {
  margin: 0;
}

.tip-box p {
  margin: 6px 0 0;
  color: var(--muted);
  line-height: 1.5;
}

.page {
  min-width: 0;
}

.layout.guest-home-layout .page {
  padding-inline: clamp(4px, 2.6vw, 26px);
}

.toast {
  position: fixed;
  right: 16px;
  bottom: 18px;
  padding: 10px 14px;
  border-radius: 10px;
  color: #fff;
  font-weight: 600;
}

.toast.success {
  background: #2f855a;
}

.toast.error {
  background: #c53030;
}

.toast-fade-enter-active,
.toast-fade-leave-active {
  transition: opacity 0.25s;
}

.toast-fade-enter-from,
.toast-fade-leave-to {
  opacity: 0;
}

@media (max-width: 1040px) {
  .layout {
    grid-template-columns: 1fr;
  }

  .layout.guest-layout .page,
  .layout.guest-home-layout .page {
    max-width: 100%;
    margin: 0;
  }

  .sidebar {
    position: static;
    max-height: none;
  }
}

@media (max-width: 860px) {
  .topbar {
    grid-template-columns: 1fr;
  }

  .top-actions {
    flex-wrap: wrap;
  }
}
</style>
