<template>
  <section class="search-page">
    <article class="panel header">
      <h2>搜索结果</h2>
      <p v-if="query">关键词：{{ query }}</p>
      <p v-else>请输入关键词后进行搜索</p>
    </article>

    <article class="panel" v-if="loading">搜索中...</article>

    <article class="panel" v-else>
      <h3>帖子</h3>
      <div v-if="posts.length === 0" class="hint">未找到相关帖子</div>
      <div class="result-item" v-for="post in posts" :key="`p-${post.id}`">
        <strong>{{ post.nickname || post.username }}</strong>
        <p>{{ post.content || '（无文字内容）' }}</p>
      </div>
    </article>

    <article class="panel" v-if="!loading">
      <h3>用户</h3>
      <div v-if="users.length === 0" class="hint">未找到相关用户</div>
      <div class="result-item" v-for="item in users" :key="`u-${item.id}`">
        <strong>{{ item.nickname || item.username }}</strong>
        <p>@{{ item.username }}</p>
      </div>
    </article>
  </section>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { searchPosts, searchUsers } from '@/api'

const route = useRoute()
const loading = ref(false)
const posts = ref([])
const users = ref([])

const query = computed(() => (route.query.q || '').toString().trim())

const runSearch = async () => {
  if (!query.value) {
    posts.value = []
    users.value = []
    return
  }
  loading.value = true
  try {
    const [postRes, userRes] = await Promise.all([
      searchPosts(query.value, 1, 20),
      searchUsers(query.value, 1, 20)
    ])

    posts.value = postRes.code === 200 && Array.isArray(postRes.data) ? postRes.data : []
    users.value = userRes.code === 200 && Array.isArray(userRes.data) ? userRes.data : []
  } finally {
    loading.value = false
  }
}

watch(() => route.query.q, () => {
  runSearch()
})

onMounted(() => {
  runSearch()
})
</script>

<style scoped>
.search-page {
  display: grid;
  gap: 12px;
}

.panel {
  border: 1px solid #e7dccf;
  border-radius: 16px;
  background: #fffdf8;
  padding: 14px;
}

.header h2 {
  margin: 0;
}

.header p {
  margin: 4px 0 0;
  color: #7a6d5a;
}

.result-item {
  border-top: 1px dashed #eadfce;
  padding: 10px 0;
}

.result-item:first-of-type {
  border-top: 0;
}

.result-item p {
  margin: 4px 0 0;
  color: #5c4d39;
}

.hint {
  color: #7a6d5a;
}
</style>
