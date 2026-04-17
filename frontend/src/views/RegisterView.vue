<template>
  <section class="auth-page">
    <article class="card">
      <h2>创建账号</h2>
      <p class="tip">加入社区，分享你的动态</p>

      <label>用户名</label>
      <input v-model="form.username" placeholder="3-20 位字符" />

      <label>昵称</label>
      <input v-model="form.nickname" placeholder="可选，不填则用用户名" />

      <label>密码</label>
      <input v-model="form.password" type="password" placeholder="至少 6 位" />

      <label>确认密码</label>
      <input v-model="form.confirmPassword" type="password" placeholder="再次输入密码" />

      <button class="btn" :disabled="loading" @click="submit">{{ loading ? '注册中...' : '注册并登录' }}</button>

      <p class="tip">已有账号？<router-link to="/login">去登录</router-link></p>
    </article>
  </section>
</template>

<script setup>
import { inject, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api'

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
  if (form.value.password.length < 6) {
    showToast('密码至少 6 位', 'error')
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
  min-height: calc(100vh - 120px);
  display: grid;
  place-items: center;
}

.card {
  width: min(440px, 100%);
  border: 1px solid #e7dccf;
  border-radius: 18px;
  background: #fffdf8;
  padding: 18px;
  box-shadow: 0 8px 24px rgba(66, 45, 17, 0.06);
}

h2 {
  margin: 0;
}

.tip {
  color: #7a6d5a;
}

label {
  display: block;
  margin-top: 10px;
  margin-bottom: 4px;
  font-weight: 700;
}

input {
  width: 100%;
  border: 1px solid #e7dccf;
  border-radius: 10px;
  padding: 8px 10px;
  font: inherit;
}

.btn {
  margin-top: 12px;
  width: 100%;
  border: 0;
  border-radius: 999px;
  padding: 10px;
  background: #f25a29;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
