<template>
  <section class="auth-page">
    <article class="card">
      <form class="auth-form" @submit.prevent="submit">
        <header class="auth-head">
          <p class="eyebrow">SimpleSocial</p>
          <h2>创建账号</h2>
          <p class="tip">注册后可发布动态并参与互动</p>
        </header>

        <label class="field">
          <span>用户名</span>
          <input
            v-model="form.username"
            placeholder="3-20 位字符"
            autocomplete="username"
            autocapitalize="none"
            spellcheck="false"
          />
        </label>

        <label class="field">
          <span>昵称</span>
          <input v-model="form.nickname" placeholder="可选，不填则使用用户名" />
        </label>

        <label class="field">
          <span>密码</span>
          <input
            v-model="form.password"
            type="password"
            placeholder="8-24 位，需包含字母和数字"
            autocomplete="new-password"
          />
        </label>

        <label class="field">
          <span>确认密码</span>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再输一次"
            autocomplete="new-password"
          />
        </label>

        <button class="btn" type="submit" :disabled="loading">{{ loading ? '正在创建' : '注册并登录' }}</button>

        <p class="tip foot-tip">已有账号？<router-link to="/login">去登录</router-link></p>
      </form>
    </article>
  </section>
</template>

<script setup>
import { inject, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api'
import { validateRegisterPassword } from '@/utils/password-policy'

const router = useRouter()
const setToken = inject('setToken')
const showToast = inject('showToast')

const loading = ref(false)
const form = ref({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const submit = async () => {
  const username = form.value.username.trim()
  const nickname = form.value.nickname.trim()

  if (username.length < 3 || username.length > 20) {
    showToast('用户名长度需在 3 到 20 之间', 'error')
    return
  }
  const passwordResult = validateRegisterPassword(form.value.password)
  if (!passwordResult.valid) {
    showToast(passwordResult.message, 'error')
    return
  }
  if (form.value.password !== form.value.confirmPassword) {
    showToast('两次密码不一致', 'error')
    return
  }

  loading.value = true
  try {
    const res = await register({
      username,
      password: form.value.password,
      nickname: nickname || username
    })
    if (res.code === 200) {
      await setToken(res.data.token)
      showToast('注册成功')
      router.push('/')
    } else {
      showToast(res.message || '注册失败，请稍后重试', 'error')
    }
  } catch {
    showToast('注册失败，请稍后重试', 'error')
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
  width: min(920px, 100%);
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--paper);
  padding: 32px;
  box-shadow: 0 20px 42px color-mix(in srgb, #0f172a 10%, transparent);
}

.auth-form {
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

.foot-tip {
  margin-top: 2px;
}

.foot-tip a {
  color: var(--ink);
  font-weight: 700;
  text-decoration: none;
}
</style>
