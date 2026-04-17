<template>
  <section class="profile-page" v-if="user">
    <article class="panel profile-card">
      <div class="avatar-wrap">
        <img :src="user.avatar || defaultAvatar" alt="avatar" class="avatar" />
      </div>

      <div class="main-info">
        <h2>{{ user.nickname || user.username }}</h2>
        <p>@{{ user.username }}</p>
      </div>

      <button class="btn" @click="editing = !editing">{{ editing ? '取消编辑' : '编辑资料' }}</button>
    </article>

    <article class="panel" v-if="editing">
      <h3>编辑资料</h3>
      <div class="grid">
        <input v-model="form.nickname" placeholder="昵称" />
        <input v-model="form.avatar" placeholder="头像 URL（可选）" />
      </div>
      <textarea v-model="form.bio" rows="3" placeholder="个人简介"></textarea>
      <button class="btn primary" @click="saveProfile">保存</button>
    </article>

    <article class="panel">
      <h3>我的动态</h3>
      <div v-if="loading" class="hint">加载中...</div>
      <div v-else-if="posts.length === 0" class="hint">暂无内容</div>
      <div v-else class="post-list">
        <div class="post-item" v-for="post in posts" :key="post.id">
          <p class="content">{{ post.content || '（无文字内容）' }}</p>
          <img v-if="post.mediaUrl && post.mediaType === 'image'" class="media" :src="post.mediaUrl" alt="media" />
          <video v-if="post.mediaUrl && post.mediaType === 'video'" class="media" controls :src="post.mediaUrl"></video>
          <div class="meta">
            <span>{{ formatTime(post.createdAt) }}</span>
            <span>赞 {{ post.likeCount || 0 }}</span>
            <span>评 {{ post.commentCount || 0 }}</span>
          </div>
        </div>
      </div>
    </article>
  </section>

  <section v-else class="panel hint">请先登录</section>
</template>

<script setup>
import { inject, onMounted, ref, watch } from 'vue'
import { getUserPosts, updateUserInfo } from '@/api'

const user = inject('user')
const showToast = inject('showToast')
const reloadUser = inject('reloadUser')

const editing = ref(false)
const loading = ref(false)
const posts = ref([])
const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23eee3d3" width="80" height="80"/><text fill="%23766" x="20" y="45" font-size="16">USER</text></svg>'

const form = ref({
  nickname: '',
  avatar: '',
  bio: ''
})

const syncForm = () => {
  if (!user.value) return
  form.value.nickname = user.value.nickname || ''
  form.value.avatar = user.value.avatar || ''
  form.value.bio = user.value.bio || ''
}

const loadPosts = async () => {
  if (!user.value?.id) {
    posts.value = []
    return
  }
  loading.value = true
  try {
    const res = await getUserPosts(user.value.id, 1, 20)
    if (res.code === 200 && Array.isArray(res.data)) {
      posts.value = res.data
    } else {
      posts.value = []
    }
  } catch {
    showToast('加载个人动态失败', 'error')
  } finally {
    loading.value = false
  }
}

const saveProfile = async () => {
  try {
    const payload = {
      nickname: form.value.nickname.trim(),
      avatar: form.value.avatar.trim(),
      bio: form.value.bio.trim()
    }
    const res = await updateUserInfo(payload)
    if (res.code === 200) {
      showToast('资料已更新')
      editing.value = false
      await reloadUser()
      syncForm()
    } else {
      showToast(res.message || '更新失败', 'error')
    }
  } catch {
    showToast('更新失败', 'error')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const d = new Date(time)
  if (Number.isNaN(d.getTime())) return ''
  return d.toLocaleString('zh-CN', { hour12: false })
}

watch(user, () => {
  syncForm()
  loadPosts()
}, { immediate: true })

onMounted(() => {
  syncForm()
  loadPosts()
})
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 14px;
}

.panel {
  border: 1px solid #e7dccf;
  border-radius: 16px;
  background: #fffdf8;
  padding: 14px;
  box-shadow: 0 8px 24px rgba(66, 45, 17, 0.06);
}

.profile-card {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 14px;
  align-items: center;
}

.avatar {
  width: 78px;
  height: 78px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #f0e3d2;
}

.main-info h2 {
  margin: 0;
}

.main-info p {
  margin: 4px 0 0;
  color: #7a6d5a;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-bottom: 8px;
}

input,
textarea {
  width: 100%;
  border: 1px solid #e7dccf;
  border-radius: 10px;
  padding: 8px 10px;
  font: inherit;
}

.btn {
  border: 1px solid #e7dccf;
  border-radius: 999px;
  padding: 8px 14px;
  font-weight: 700;
  background: #fff;
  cursor: pointer;
}

.btn.primary {
  margin-top: 8px;
  border-color: #f25a29;
  background: #f25a29;
  color: #fff;
}

.post-list {
  display: grid;
  gap: 12px;
}

.post-item {
  border: 1px dashed #eedfcd;
  border-radius: 12px;
  padding: 10px;
}

.content {
  margin: 0 0 8px;
}

.media {
  width: 100%;
  border-radius: 10px;
  margin-bottom: 8px;
}

.meta {
  display: flex;
  gap: 10px;
  color: #7a6d5a;
  font-size: 0.86rem;
}

.hint {
  text-align: center;
  color: #7a6d5a;
}

@media (max-width: 720px) {
  .profile-card {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
