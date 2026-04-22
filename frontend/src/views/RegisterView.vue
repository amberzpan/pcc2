<template>
  <section class="auth-page">
    <article class="card">
      <form class="auth-form" @submit.prevent="submit">
      <h2>创建账号</h2>
      <p class="tip">加入社区，分享你的动态</p>

      <label>用户名</label>
      <input
        v-model="form.username"
        placeholder="3-20 位字符"
        autocomplete="username"
        autocapitalize="none"
        spellcheck="false"
      />

      <label>昵称</label>
      <input v-model="form.nickname" placeholder="可选，不填则用用户名" />

      <label>密码</label>
      <input
        v-model="form.password"
        type="password"
        placeholder="8-24 位，需包含字母和数字"
        autocomplete="new-password"
      />

      <label>确认密码</label>
      <input
        v-model="form.confirmPassword"
        type="password"
        placeholder="再次输入密码"
        autocomplete="new-password"
      />

      <button class="btn" type="submit" :disabled="loading">{{ loading ? '注册中...' : '注册并登录' }}</button>

      <p class="tip">已有账号？<router-link to="/login">去登录</router-link></p>
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
      showToast(res.message || '注册失败', 'error')
    }
  } catch {
    showToast('注册失败', 'error')
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
