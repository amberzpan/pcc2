<template>
  <section class="favorites-page">
    <article class="panel heading">
      <h2>我的收藏</h2>
      <p>查看你标记的帖子</p>
    </article>

    <article class="panel" v-if="loading">加载中...</article>
    <article class="panel" v-else-if="posts.length === 0">暂无收藏内容</article>

    <article v-else class="panel post-item" v-for="post in posts" :key="post.id">
      <h3>{{ post.nickname || post.username }}</h3>
      <p class="content">{{ post.content || '（无文字内容）' }}</p>
      <img v-if="post.mediaUrl && post.mediaType === 'image'" class="media" :src="post.mediaUrl" alt="media" />
      <video v-if="post.mediaUrl && post.mediaType === 'video'" class="media" controls :src="post.mediaUrl"></video>
      <div class="meta">
        <span>{{ formatTime(post.createdAt) }}</span>
        <button class="btn small" @click="toggle(post)">{{ post.favorited ? '取消收藏' : '收藏' }}</button>
      </div>
    </article>

    <article class="panel load-more" v-if="hasMore" @click="loadMore">加载更多</article>
  </section>
</template>

<script setup>
import { inject, onMounted, ref } from 'vue'
import { getFavorites, toggleFavorite } from '@/api'

const showToast = inject('showToast')

const posts = ref([])
const page = ref(1)
const size = 10
const hasMore = ref(false)
const loading = ref(false)

const load = async (reset = true) => {
  if (reset) {
    page.value = 1
    posts.value = []
  }
  loading.value = true
  try {
    const res = await getFavorites(page.value, size)
    if (res.code === 200 && Array.isArray(res.data)) {
      const data = res.data.map((item) => ({ ...item, favorited: true }))
      posts.value = reset ? data : posts.value.concat(data)
      hasMore.value = data.length === size
    } else {
      posts.value = []
      hasMore.value = false
    }
  } catch {
    showToast('加载收藏失败', 'error')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  page.value += 1
  await load(false)
}

const toggle = async (post) => {
  try {
    const res = await toggleFavorite(post.id)
    if (res.code === 200) {
      post.favorited = !!res.data.favorited
      if (!post.favorited) {
        posts.value = posts.value.filter((item) => item.id !== post.id)
      }
      showToast(post.favorited ? '已收藏' : '已取消收藏')
    }
  } catch {
    showToast('收藏操作失败', 'error')
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', { hour12: false })
}

onMounted(() => {
  load(true)
})
</script>

<style scoped>
.favorites-page {
  display: grid;
  gap: 12px;
}

.panel {
  border: 1px solid #e7dccf;
  border-radius: 16px;
  background: #fffdf8;
  padding: 14px;
}

.heading h2 {
  margin: 0;
}

.heading p {
  margin: 4px 0 0;
  color: #7a6d5a;
}

.post-item h3 {
  margin: 0 0 8px;
}

.content {
  margin: 0 0 8px;
  white-space: pre-wrap;
}

.media {
  width: 100%;
  border-radius: 12px;
  margin-bottom: 10px;
}

.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #7a6d5a;
}

.btn {
  border: 1px solid #e7dccf;
  border-radius: 999px;
  padding: 8px 14px;
  background: #fff;
  cursor: pointer;
  font-weight: 700;
}

.btn.small {
  padding: 5px 10px;
}

.load-more {
  text-align: center;
  font-weight: 700;
  cursor: pointer;
}
</style>
