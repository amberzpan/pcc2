<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>注册</h2>
      <form @submit.prevent="handleRegister">
        <div class="form-group">
          <input v-model="form.username" type="text" placeholder="用户名" required minlength="3" maxlength="20" />
        </div>
        <div class="form-group">
          <input v-model="form.password" type="password" placeholder="密码" required minlength="6" />
        </div>
        <div class="form-group">
          <input v-model="form.confirmPassword" type="password" placeholder="确认密码" required />
        </div>
        <div class="form-group">
          <input v-model="form.nickname" type="text" placeholder="昵称（可选）" />
        </div>
        <button type="submit" class="btn-primary" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </form>
      <p class="auth-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api'

const router = useRouter()
const setToken = inject('setToken')
const showToast = inject('showToast')

const form = ref({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: ''
})
const loading = ref(false)

const handleRegister = async () => {
  if (!form.value.username || !form.value.password) {
    showToast('请填写完整', 'error')
    return
  }
  
  if (form.value.password !== form.value.confirmPassword) {
    showToast('两次密码不一致', 'error')
    return
  }
  
  if (form.value.password.length < 6) {
    showToast('密码至少6位', 'error')
    return
  }
  
  loading.value = true
  try {
    const res = await register({
      username: form.value.username,
      password: form.value.password,
      nickname: form.value.nickname || form.value.username
    })
    if (res.code === 200) {
      setToken(res.data.token)
      showToast('注册成功')
      router.push('/')
    } else {
      showToast(res.message || '注册失败', 'error')
    }
  } catch (e) {
    showToast(e.response?.data?.message || '注册失败', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 360px;
  background: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.auth-card h2 {
  text-align: center;
  margin-bottom: 24px;
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input:focus {
  outline: none;
  border-color: #e6162d;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background: #e6162d;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #d51225;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.auth-link {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.auth-link a {
  color: #e6162d;
  text-decoration: none;
}
</style>
