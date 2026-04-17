<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-container">
        <router-link to="/" class="nav-logo">
          <span class="logo-icon">微博</span>
        </router-link>
        <div class="nav-search">
          <input type="text" placeholder="搜索" />
        </div>
        <div class="nav-links">
          <router-link to="/" class="nav-item">
            <span class="icon">🏠</span>
            <span>首页</span>
          </router-link>
          <template v-if="isLoggedIn">
            <router-link to="/publish" class="nav-item">
              <span class="icon">✏️</span>
              <span>发布</span>
            </router-link>
            <router-link to="/profile" class="nav-item">
              <span class="icon">👤</span>
              <span>我的</span>
            </router-link>
            <a @click="logout" class="nav-item logout-btn">退出</a>
          </template>
          <template v-else>
            <router-link to="/login" class="nav-item">登录</router-link>
            <router-link to="/register" class="nav-item register-btn">注册</router-link>
          </template>
        </div>
      </div>
    </nav>
    <div class="layout-container">
      <aside class="sidebar">
        <div class="sidebar-menu">
          <router-link to="/" class="menu-item">
            <span class="menu-icon">🏠</span>
            <span>首页</span>
          </router-link>
          <a href="#" class="menu-item">
            <span class="menu-icon">🌍</span>
            <span>发现</span>
          </a>
          <a href="#" class="menu-item">
            <span class="menu-icon">🔥</span>
            <span>热门</span>
          </a>
          <template v-if="isLoggedIn">
            <router-link to="/favorites" class="menu-item">
              <span class="menu-icon">⭐</span>
              <span>收藏</span>
            </router-link>
          </template>
        </div>
        <div class="sidebar-user" v-if="user">
          <img :src="user.avatar || defaultAvatar" alt="avatar" class="user-avatar" />
          <div class="user-info">
            <span class="user-name">{{ user.nickname || user.username }}</span>
            <span class="user-desc">{{ user.bio || '这个人很懒，什么都没写' }}</span>
          </div>
        </div>
      </aside>
      <main class="main-content">
        <router-view :key="$route.fullPath" />
      </main>
    </div>
    <div v-if="toast.show" class="toast" :class="toast.type">{{ toast.message }}</div>
  </div>
</template>

<script setup>
import { ref, provide, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getUserInfo } from './api'

const router = useRouter()
const token = ref(localStorage.getItem('token') || '')
const user = ref(null)

const defaultAvatar = 'data:image/svg+xml,<svg xmlns=%22http://www.w3.org/2000/svg%22 width=%2240%22 height=%2240%22><rect fill=%22%23ffaabb%22 width=%2240%22 height=%2240%22/><text fill=%22white%22 x=%2210%22 y=%2225%22 font-size=%2216%22>头像</text></svg>'

const toast = ref({ show: false, message: '', type: '' })

const showToast = (message, type = 'success') => {
  toast.value = { show: true, message, type }
  setTimeout(() => {
    toast.value.show = false
  }, 3000)
}

const isLoggedIn = computed(() => !!token.value)

const loadUser = async () => {
  if (token.value) {
    try {
      const res = await getUserInfo()
      if (res.code === 200) {
        user.value = res.data
      }
    } catch (e) {
      console.error(e)
    }
  }
}

const logout = () => {
  localStorage.removeItem('token')
  token.value = ''
  user.value = null
  router.push('/login')
}

const setToken = (newToken) => {
  localStorage.setItem('token', newToken)
  token.value = newToken
}

provide('token', token)
provide('user', user)
provide('isLoggedIn', isLoggedIn)
provide('showToast', showToast)
provide('setToken', setToken)

onMounted(() => {
  loadUser()
})
</script>

<style>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+SC:wght@400;500;700&display=swap');

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

:root {
  --primary-color: #ff8200;
  --primary-hover: #f75a00;
  --text-primary: #333;
  --text-secondary: #888;
  --bg-main: #f5f5f5;
  --bg-card: #fff;
  --border-color: #eee;
  --shadow: 0 2px 8px rgba(0,0,0,0.08);
}

body {
  font-family: 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  background-color: var(--bg-main);
  color: var(--text-primary);
  line-height: 1.5;
}

#app {
  min-height: 100vh;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 54px;
  background: linear-gradient(90deg, #ff8200 0%, #ff6a00 100%);
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  z-index: 1000;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.nav-logo {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.logo-icon {
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.nav-search {
  flex: 0 0 300px;
  margin: 0 20px;
}

.nav-search input {
  width: 100%;
  height: 32px;
  padding: 0 12px;
  border: none;
  border-radius: 16px;
  background: rgba(255,255,255,0.2);
  color: #fff;
  font-size: 14px;
  outline: none;
}

.nav-search input::placeholder {
  color: rgba(255,255,255,0.7);
}

.nav-search input:focus {
  background: rgba(255,255,255,0.3);
}

.nav-links {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  color: rgba(255,255,255,0.9);
  text-decoration: none;
  font-size: 14px;
  border-radius: 4px;
  transition: background 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.2);
}

.nav-item .icon {
  font-size: 16px;
}

.register-btn {
  background: #fff;
  color: var(--primary-color) !important;
  font-weight: 500;
}

.register-btn:hover {
  background: #fff !important;
  color: var(--primary-hover) !important;
}

.logout-btn {
  color: rgba(255,255,255,0.8) !important;
}

.layout-container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding-top: 64px;
  min-height: 100vh;
}

.sidebar {
  width: 180px;
  flex-shrink: 0;
  padding: 20px 0;
  position: sticky;
  top: 64px;
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.sidebar-menu {
  background: var(--bg-card);
  border-radius: 8px;
  padding: 8px;
  box-shadow: var(--shadow);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  color: var(--text-primary);
  text-decoration: none;
  font-size: 14px;
  border-radius: 6px;
  transition: all 0.2s;
}

.menu-item:hover {
  background: #fff5e6;
  color: var(--primary-color);
}

.menu-item.router-link-active {
  background: #fff5e6;
  color: var(--primary-color);
  font-weight: 500;
}

.menu-icon {
  font-size: 18px;
}

.sidebar-user {
  margin-top: 16px;
  background: var(--bg-card);
  border-radius: 8px;
  padding: 16px;
  box-shadow: var(--shadow);
}

.user-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
}

.user-info {
  margin-top: 12px;
}

.user-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-desc {
  display: block;
  font-size: 12px;
  color: var(--text-secondary);
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.main-content {
  flex: 1;
  padding: 20px;
  min-width: 0;
}

.toast {
  position: fixed;
  top: 70px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 24px;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  z-index: 2000;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  animation: slideDown 0.3s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.toast.success {
  background: linear-gradient(135deg, #52c41a, #389e0d);
}

.toast.error {
  background: linear-gradient(135deg, #ff4d4f, #cf1322);
}

@media (max-width: 768px) {
  .nav-search {
    display: none;
  }
  
  .sidebar {
    display: none;
  }
  
  .layout-container {
    padding-top: 54px;
  }
  
  .main-content {
    padding: 12px;
  }
}
</style>
