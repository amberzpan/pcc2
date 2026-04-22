<template>
  <section class="settings-page">
    <article class="panel head">
      <h2>设置中心</h2>
      <p>调整界面风格、查看帮助并修改登录密码</p>
    </article>

    <article class="panel">
      <h3>主题模式</h3>
      <div class="theme-row">
        <button :class="['theme-btn', theme === 'light' ? 'active' : '']" @click="switchTheme('light')">浅色</button>
        <button :class="['theme-btn', theme === 'dark' ? 'active' : '']" @click="switchTheme('dark')">深色</button>
      </div>
    </article>

    <article class="panel help">
      <h3>帮助文档</h3>
      <p>遇到问题可查看项目说明文档，快速完成排查与恢复。</p>
      <div class="help-actions">
        <router-link class="help-link" to="/help">打开帮助中心</router-link>
        <a class="help-link" href="/SPEC.md" target="_blank" rel="noopener">查看 SPEC</a>
      </div>
    </article>

    <article class="panel">
      <h3>修改密码</h3>
      <form class="password-form" @submit.prevent="changePassword">
        <label>
          当前密码
          <input v-model="form.oldPassword" type="password" autocomplete="current-password" />
        </label>
        <label>
          新密码
          <input v-model="form.newPassword" type="password" autocomplete="new-password" />
        </label>
        <label>
          确认新密码
          <input v-model="form.confirmPassword" type="password" autocomplete="new-password" />
        </label>
        <button class="save-btn" :disabled="loading">{{ loading ? '保存中...' : '保存密码' }}</button>
      </form>
    </article>
  </section>
</template>

<script setup>
import { inject, ref } from 'vue'
import { changePassword as apiChangePassword } from '@/api'
import { validateChangePasswordForm } from '@/utils/password-policy'
import { getStoredTheme, setTheme as saveTheme } from '@/utils/theme'

const showToast = inject('showToast')
const themeRef = inject('themeRef')

const theme = ref(getStoredTheme())
const loading = ref(false)
const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const switchTheme = (next) => {
  const applied = saveTheme(next)
  theme.value = applied
  if (themeRef) {
    themeRef.value = applied
  }
  showToast('主题已更新')
}

const changePassword = async () => {
  const validation = validateChangePasswordForm(form.value)
  if (!validation.valid) {
    showToast(validation.message, 'error')
    return
  }
  loading.value = true
  try {
    const res = await apiChangePassword({
      oldPassword: form.value.oldPassword,
      newPassword: form.value.newPassword,
      confirmPassword: form.value.confirmPassword
    })
    if (res.code === 200) {
      showToast('密码修改成功')
      form.value.oldPassword = ''
      form.value.newPassword = ''
      form.value.confirmPassword = ''
    } else {
      showToast(res.message || '修改失败', 'error')
    }
  } catch {
    showToast('修改失败', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.settings-page {
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
  padding: 16px;
}

.head h2 {
  margin: 0;
}

.head p {
  margin: 6px 0 0;
  color: var(--muted);
}

.theme-row {
  display: flex;
  gap: 8px;
}

.theme-btn {
  border: 1px solid var(--line);
  background: transparent;
  border-radius: 999px;
  padding: 8px 13px;
  font-weight: 700;
  cursor: pointer;
}

.theme-btn.active {
  background: var(--ink);
  border-color: var(--ink);
  color: #fff;
}

.help a {
  color: inherit;
}

.help-actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.help-link {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 7px 12px;
  font-weight: 700;
  text-decoration: none;
  color: var(--ink);
  background: var(--surface);
}

.password-form {
  display: grid;
  gap: 10px;
}

.password-form label {
  display: grid;
  gap: 6px;
  font-weight: 700;
  color: var(--muted);
}

.password-form input {
  border: 1px solid var(--line);
  background: transparent;
  border-radius: 10px;
  padding: 9px 10px;
  color: var(--ink);
  font: inherit;
}

.save-btn {
  justify-self: start;
  border: 1px solid var(--ink);
  border-radius: 999px;
  background: var(--ink);
  color: #fff;
  font-weight: 700;
  padding: 8px 14px;
  cursor: pointer;
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
