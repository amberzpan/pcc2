<template>
  <section class="settings-page">
    <article class="panel head">
      <h2>设置</h2>
      <p>管理主题、帮助入口与登录密码。</p>
    </article>

    <article class="panel">
      <h3>主题模式</h3>
      <div class="theme-row">
        <button :class="['theme-btn', theme === 'light' ? 'active' : '']" @click="switchTheme('light')">浅色</button>
        <button :class="['theme-btn', theme === 'dark' ? 'active' : '']" @click="switchTheme('dark')">深色</button>
      </div>
    </article>

    <article class="panel help">
      <h3>帮助</h3>
      <p>查看使用说明与项目规范。</p>
      <div class="help-actions">
        <router-link class="help-link" to="/help">打开帮助</router-link>
      </div>
    </article>

    <article class="panel">
      <h3>修改密码</h3>
      <form class="password-form" @submit.prevent="changePassword">
        <label>
          当前密码
          <span class="password-field">
            <input v-model="form.oldPassword" :type="visiblePasswords.old ? 'text' : 'password'" autocomplete="current-password" />
            <button
              class="password-eye"
              type="button"
              :aria-label="visiblePasswords.old ? '隐藏当前密码' : '显示当前密码'"
              @click="togglePasswordVisibility('old')"
            >
              <EyeOff v-if="visiblePasswords.old" :size="16" />
              <Eye v-else :size="16" />
            </button>
          </span>
        </label>
        <label>
          新密码
          <span class="password-field">
            <input v-model="form.newPassword" :type="visiblePasswords.new ? 'text' : 'password'" autocomplete="new-password" />
            <button
              class="password-eye"
              type="button"
              :aria-label="visiblePasswords.new ? '隐藏新密码' : '显示新密码'"
              @click="togglePasswordVisibility('new')"
            >
              <EyeOff v-if="visiblePasswords.new" :size="16" />
              <Eye v-else :size="16" />
            </button>
          </span>
        </label>
        <label>
          确认新密码
          <span class="password-field">
            <input v-model="form.confirmPassword" :type="visiblePasswords.confirm ? 'text' : 'password'" autocomplete="new-password" />
            <button
              class="password-eye"
              type="button"
              :aria-label="visiblePasswords.confirm ? '隐藏确认密码' : '显示确认密码'"
              @click="togglePasswordVisibility('confirm')"
            >
              <EyeOff v-if="visiblePasswords.confirm" :size="16" />
              <Eye v-else :size="16" />
            </button>
          </span>
        </label>
        <button class="save-btn" :disabled="loading">{{ loading ? '正在保存' : '保存密码' }}</button>
      </form>
    </article>
  </section>
</template>

<script setup>
import { Eye, EyeOff } from 'lucide-vue-next'
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
const visiblePasswords = ref({
  old: false,
  new: false,
  confirm: false
})

const switchTheme = (next) => {
  const applied = saveTheme(next)
  theme.value = applied
  if (themeRef) {
    themeRef.value = applied
  }
  showToast('主题已切换')
}

const togglePasswordVisibility = (field) => {
  visiblePasswords.value[field] = !visiblePasswords.value[field]
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
      showToast('密码已更新')
      form.value.oldPassword = ''
      form.value.newPassword = ''
      form.value.confirmPassword = ''
    } else {
      showToast(res.message || '修改失败，请稍后重试', 'error')
    }
  } catch {
    showToast('修改失败，请稍后重试', 'error')
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
  max-width: 980px;
  margin: 0 auto;
}

.panel {
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--paper);
  padding: 16px;
  box-shadow: 0 10px 24px color-mix(in srgb, #0f172a 7%, transparent);
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
  background: var(--surface);
  color: var(--ink);
  border-radius: 999px;
  padding: 8px 13px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, background-color 0.16s ease;
}

:global(:root[data-theme='dark']) .theme-btn {
  background: color-mix(in srgb, var(--surface) 92%, var(--paper));
  color: var(--ink);
}

.theme-btn:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 66%, var(--muted));
  background: color-mix(in srgb, var(--surface) 72%, var(--line));
}

.theme-btn.active {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--paper);
}

:global(:root[data-theme='dark']) .theme-btn.active {
  background: #f8fafc;
  border-color: #f8fafc;
  color: #171a20;
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
  transition: transform 0.16s ease, border-color 0.16s ease, background-color 0.16s ease;
}

.help-link:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 66%, var(--muted));
  background: color-mix(in srgb, var(--surface) 72%, var(--line));
}

.password-form {
  display: grid;
  gap: 10px;
  max-width: 620px;
}

.password-form label {
  display: grid;
  gap: 6px;
  font-weight: 700;
  color: var(--muted);
}

.password-form input {
  border: 1px solid var(--line);
  background: var(--surface);
  border-radius: 10px;
  padding: 9px 42px 9px 10px;
  color: var(--ink);
  font: inherit;
  width: 100%;
}

.password-form input::-ms-reveal,
.password-form input::-ms-clear {
  display: none;
}

.password-field {
  position: relative;
  display: block;
  width: 100%;
}

.password-eye {
  position: absolute;
  top: 50%;
  right: 8px;
  transform: translateY(-50%);
  width: 30px;
  height: 30px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--ink);
  display: grid;
  place-items: center;
  cursor: pointer;
  transition: background-color 0.16s ease, color 0.16s ease;
}

.password-eye:hover {
  background: color-mix(in srgb, var(--surface) 68%, var(--line));
}

:global(:root[data-theme='dark']) .password-eye {
  color: #f8fafc;
}

.save-btn {
  justify-self: start;
  border: 1px solid var(--accent);
  border-radius: 999px;
  background: var(--accent);
  color: var(--paper);
  font-weight: 700;
  padding: 8px 14px;
  cursor: pointer;
  transition: transform 0.16s ease, filter 0.16s ease;
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  filter: brightness(1.02);
}

.save-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
