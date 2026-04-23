<template>
  <section class="auth-page">
    <article class="card">
      <form class="auth-form" @submit.prevent="submit">
        <header class="auth-head">
          <p class="eyebrow">SimpleSocial</p>
          <h2>{{ showReset ? '重置密码' : '欢迎回来' }}</h2>
          <p class="tip">{{ showReset ? '输入用户名并设置新密码。' : '登录后可发布、评论与管理收藏。' }}</p>
        </header>

        <template v-if="!showReset">
          <label class="field">
            <span>用户名</span>
            <input
              v-model="form.username"
              placeholder="你的用户名"
              autocomplete="username"
              autocapitalize="none"
              spellcheck="false"
            />
          </label>

          <label class="field">
            <span>密码</span>
            <input
              v-model="form.password"
              type="password"
              placeholder="你的密码"
              autocomplete="current-password"
            />
          </label>

          <button class="btn" type="submit" :disabled="loading">{{ loading ? '正在登录' : '登录' }}</button>
        </template>

        <section class="reset-panel" v-else>
          <label class="field">
            <span>用户名</span>
            <input
              v-model="resetForm.username"
              placeholder="需要重置的用户名"
              autocomplete="username"
              autocapitalize="none"
              spellcheck="false"
            />
          </label>

          <label class="field">
            <span>新密码</span>
            <input
              v-model="resetForm.newPassword"
              type="password"
              placeholder="8-24 位，包含字母和数字"
              autocomplete="new-password"
            />
          </label>

          <label class="field">
            <span>确认新密码</span>
            <input
              v-model="resetForm.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              autocomplete="new-password"
            />
          </label>

          <button class="btn" type="button" :disabled="resetLoading" @click="resetPassword">
            {{ resetLoading ? '正在重置' : '重置密码' }}
          </button>
        </section>

        <button class="text-action" type="button" @click="toggleReset">
          {{ showReset ? '返回登录' : '忘记密码？' }}
        </button>

        <p class="tip foot-tip">没有账号？<router-link to="/register">注册账号</router-link></p>
      </form>
    </article>
  </section>
</template>

<script setup>
import { inject, ref } from 'vue'
import { useRouter } from 'vue-router'
import { forgotPassword, login } from '@/api'
import { validateRegisterPassword } from '@/utils/password-policy'

const router = useRouter()
const setToken = inject('setToken')
const showToast = inject('showToast')

const loading = ref(false)
const resetLoading = ref(false)
const showReset = ref(false)
const form = ref({
  username: '',
  password: ''
})
const resetForm = ref({
  username: '',
  newPassword: '',
  confirmPassword: ''
})

const toggleReset = () => {
  showReset.value = !showReset.value
}

const submit = async () => {
  if (!form.value.username.trim() || !form.value.password) {
    showToast('用户名和密码都要填写', 'error')
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
      showToast(res.message || '账号或密码不正确', 'error')
    }
  } catch {
    showToast('账号或密码不正确', 'error')
  } finally {
    loading.value = false
  }
}

const resetPassword = async () => {
  const username = resetForm.value.username.trim()
  if (!username || !resetForm.value.newPassword || !resetForm.value.confirmPassword) {
    showToast('请完整填写重置信息', 'error')
    return
  }
  if (resetForm.value.newPassword !== resetForm.value.confirmPassword) {
    showToast('两次输入的新密码不一致', 'error')
    return
  }
  const validation = validateRegisterPassword(resetForm.value.newPassword)
  if (!validation.valid) {
    showToast(validation.message, 'error')
    return
  }

  resetLoading.value = true
  try {
    const res = await forgotPassword({
      username,
      newPassword: resetForm.value.newPassword,
      confirmPassword: resetForm.value.confirmPassword
    })
    if (res.code === 200) {
      showToast('密码已重置，请重新登录')
      form.value.username = username
      form.value.password = ''
      resetForm.value.newPassword = ''
      resetForm.value.confirmPassword = ''
      showReset.value = false
    } else {
      showToast(res.message || '重置失败，请稍后重试', 'error')
    }
  } catch {
    showToast('重置失败，请稍后重试', 'error')
  } finally {
    resetLoading.value = false
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
  width: min(920px, 100%);
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--paper);
  padding: 32px;
  box-shadow: 0 20px 42px color-mix(in srgb, #0f172a 10%, transparent);
}

.auth-form,
.reset-panel {
  display: grid;
  gap: 10px;
}

.auth-head {
  display: grid;
  gap: 4px;
}

.eyebrow {
  margin: 0;
  font-size: 0.78rem;
  font-weight: 800;
  color: var(--muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

h2 {
  margin: 0;
}

.tip {
  margin: 0;
  color: var(--muted);
}

.field {
  display: grid;
  gap: 6px;
}

.field span {
  font-weight: 700;
  color: var(--muted);
  font-size: 0.9rem;
}

input {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 10px 12px;
  font: inherit;
  background: color-mix(in srgb, var(--surface) 92%, var(--paper));
  color: var(--ink);
  transition: border-color 0.16s ease, background-color 0.16s ease;
}

input:focus {
  outline: none;
  border-color: color-mix(in srgb, #1d9bf0 45%, var(--line));
  background: var(--surface);
}

.btn {
  margin-top: 8px;
  width: 100%;
  border: 0;
  border-radius: 999px;
  padding: 10px;
  background: var(--accent);
  color: var(--paper);
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.16s ease, filter 0.16s ease;
}

.btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.text-action {
  justify-self: center;
  border: 0;
  background: transparent;
  color: var(--ink);
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.text-action:hover {
  text-decoration: underline;
}

.foot-tip {
  margin-top: 2px;
  text-align: center;
}

.foot-tip a {
  color: var(--ink);
  font-weight: 700;
  text-decoration: none;
}
</style>
