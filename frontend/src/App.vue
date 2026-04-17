<template>
  <div class="shell">
    <header class="topbar">
      <div class="brand" @click="goHome">SimpleSocial</div>
      <form class="search-wrap" @submit.prevent="submitSearch">
        <input v-model="keyword" placeholder="搜索内容/用户" />
        <button type="submit">搜索</button>
      </form>
      <nav class="top-actions">
        <router-link to="/" class="btn ghost">首页</router-link>
        <router-link to="/publish" class="btn ghost" v-if="isLoggedIn">发布</router-link>
        <router-link to="/favorites" class="btn ghost" v-if="isLoggedIn">收藏</router-link>
        <router-link to="/profile" class="btn ghost" v-if="isLoggedIn">我的</router-link>
        <router-link to="/login" class="btn ghost" v-if="!isLoggedIn">登录</router-link>
        <router-link to="/register" class="btn solid" v-if="!isLoggedIn">注册</router-link>
        <button class="btn solid" v-if="isLoggedIn" @click="logout">退出</button>
      </nav>
    </header>

    <main class="page">
      <router-view :key="$route.fullPath" />
    </main>

    <transition name="toast-fade">
      <div v-if="toast.show" class="toast" :class="toast.type">{{ toast.message }}</div>
    </transition>
  </div>
</template>

<script setup>
import { computed, onMounted, provide, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo } from './api'

const router = useRouter()
const token = ref(localStorage.getItem('token') || '')
const user = ref(null)
const keyword = ref('')
const toast = ref({ show: false, message: '', type: 'success' })

const isLoggedIn = computed(() => !!token.value)

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
  --bg: #f3efe7;
  --paper: #fffdf8;
  --ink: #2d2418;
  --muted: #7a6d5a;
  --accent: #f25a29;
  --accent-strong: #d8481a;
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
    radial-gradient(circle at 10% 10%, #ffe4c5 0, transparent 36%),
    radial-gradient(circle at 90% 20%, #ffd1bd 0, transparent 38%),
    var(--bg);
}

.shell {
  min-height: 100vh;
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 180px 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(8px);
  background: rgba(255, 251, 244, 0.85);
}

.brand {
  font-family: 'Outfit', sans-serif;
  font-weight: 800;
  font-size: 1.3rem;
  cursor: pointer;
  letter-spacing: 0.03em;
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
  padding: 0 18px;
  cursor: pointer;
}

.top-actions {
  display: flex;
  gap: 8px;
  align-items: center;
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

.btn:hover {
  transform: translateY(-1px);
}

.page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 18px;
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

@media (max-width: 860px) {
  .topbar {
    grid-template-columns: 1fr;
  }

  .top-actions {
    flex-wrap: wrap;
  }
}
</style>
