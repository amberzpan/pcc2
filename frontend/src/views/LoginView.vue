<template>
  <section class="auth-page">
    <article class="card">
      <form class="auth-form" @submit.prevent="submit">
      <h2>欢迎回来</h2>
      <p class="tip">登录后即可发布、评论、收藏</p>

      <label>用户名</label>
      <input
        v-model="form.username"
        placeholder="请输入用户名"
        autocomplete="username"
        autocapitalize="none"
        spellcheck="false"
      />

      <label>密码</label>
      <input
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        autocomplete="current-password"
      />

      <button class="btn" type="submit" :disabled="loading">{{ loading ? '登录中...' : '登录' }}</button>

      <p class="tip">还没有账号？<router-link to="/register">去注册</router-link></p>
      </form>
    </article>
  </section>
</template>

<script setup>
import { inject, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api'

const router = useRouter()
const setToken = inject('setToken')
const showToast = inject('showToast')

const loading = ref(false)
const form = ref({
  username: '',
  password: ''
})

const submit = async () => {
  if (!form.value.username.trim() || !form.value.password) {
    showToast('请输入用户名和密码', 'error')
    return
  }

  loading.value = true
  try {
    const res = await login({
      username: form.value.username.trim(),
      password: form.value.password
    })
    if (res.code === 200) {
      await setToken(res.data.token)
      showToast('登录成功')
      router.push('/')
    } else {
      showToast(res.message || '登录失败', 'error')
    }
  } catch {
    showToast('登录失败', 'error')
  } finally {
    loading.value = false
  }
}

</script>

<style scoped>
.auth-page {
  min-height: calc(100vh - 80px);
  display: grid;
  place-items: center;
  padding: 24px 12px;
}

.card {
  width: min(760px, 100%);
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--paper);
  padding: 24px;
  box-shadow: 0 18px 40px color-mix(in srgb, var(--line) 35%, transparent);
}

.auth-form {
  display: grid;
  gap: 2px;
}

h2 {
  margin: 0;
}

.tip {
  color: var(--muted);
}

label {
  display: block;
  margin-top: 10px;
  margin-bottom: 4px;
  font-weight: 700;
}

input {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 8px 10px;
  font: inherit;
  background: transparent;
  color: var(--ink);
}

.btn {
  margin-top: 12px;
  width: 100%;
  border: 0;
  border-radius: 999px;
  padding: 10px;
  background: var(--accent);
  color: var(--paper);
  font-weight: 700;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
