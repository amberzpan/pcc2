<template>
  <section class="favorites-page">
    <article class="panel heading">
      <h2>我的收藏</h2>
      <p>查看已收藏的帖子</p>
    </article>

    <article class="panel" v-if="loading && !hasLoadedOnce && posts.length === 0">收藏加载中</article>
    <article class="panel" v-else-if="!loading && hasLoadedOnce && posts.length === 0">暂无收藏内容</article>

    <PostCard
      v-else
      v-for="post in posts"
      :key="post.id"
      :post="post"
      :current-user-id="currentUserId"
      :is-logged-in="isLoggedIn"
      @delete-post="removePost"
      @toggle-like="togglePostLike"
      @toggle-comments="toggleComments"
      @toggle-favorite="togglePostFavorite"
      @toggle-follow="togglePostFollow"
      @repost="repost"
      @load-comments="loadComments"
      @submit-comment="submitComment"
      @delete-comment="removeComment"
      @toggle-comment-like="toggleCommentLike"
    />

    <div ref="loadMoreRef" class="auto-load"></div>
  </section>
</template>

<script setup>
import { computed, inject, onBeforeUnmount, onMounted, ref } from 'vue'
import {
  createComment,
  createPost,
  deleteComment as apiDeleteComment,
  deletePost,
  getComments,
  getFavorites,
  toggleCommentLike as apiToggleCommentLike,
  toggleFavorite,
  toggleFollow,
  toggleLike
} from '@/api'
import PostCard from '@/components/PostCard.vue'
import { shouldTriggerInfiniteLoad } from '@/utils/infinite-scroll'
import { normalizeComments, normalizePostList } from '@/utils/post-utils'

const user = inject('user')
const isLoggedIn = inject('isLoggedIn')
const showToast = inject('showToast')

const posts = ref([])
const page = ref(1)
const size = 10
const hasMore = ref(false)
const loading = ref(false)
const hasLoadedOnce = ref(false)
const loadMoreRef = ref(null)
const currentUserId = computed(() => user.value?.id)
let observer = null
let latestRequestId = 0

const load = async (reset = true) => {
  if (reset) {
    page.value = 1
    if (!hasLoadedOnce.value) {
      posts.value = []
    }
  }
  loading.value = true
  const requestId = ++latestRequestId
  try {
    const res = await getFavorites(page.value, size)
    if (requestId !== latestRequestId) {
      return
    }
    if (res.code === 200 && Array.isArray(res.data)) {
      const data = normalizePostList(res.data).map((item) => ({ ...item, favorited: true }))
      posts.value = reset ? data : posts.value.concat(data)
      hasMore.value = data.length === size
      hasLoadedOnce.value = true
    } else {
      posts.value = []
      hasMore.value = false
      hasLoadedOnce.value = true
    }
  } catch {
    showToast('收藏加载失败，请稍后重试', 'error')
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (!hasMore.value || loading.value) {
    return
  }
  page.value += 1
  await load(false)
}

const togglePostFavorite = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后收藏', 'error')
    return
  }
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
    showToast('收藏失败，请稍后重试', 'error')
  }
}

const togglePostLike = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后点赞', 'error')
    return
  }
  try {
    const res = await toggleLike(post.id)
    if (res.code === 200) {
      post.liked = !!res.data.liked
      post.likeCount = Math.max(0, (post.likeCount || 0) + (post.liked ? 1 : -1))
    }
  } catch {
    showToast('点赞失败，请稍后重试', 'error')
  }
}

const togglePostFollow = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后关注用户', 'error')
    return
  }
  try {
    const res = await toggleFollow(post.userId)
    if (res.code === 200) {
      post.followed = !!res.data.followed
    }
  } catch {
    showToast('关注失败，请稍后重试', 'error')
  }
}

const repost = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后转发', 'error')
    return
  }
  const content = window.prompt('请输入转发内容（可选）')
  if (content === null) return
  try {
    const res = await createPost({ content, repostId: post.id })
    if (res.code === 200) {
      post.repostCount = (post.repostCount || 0) + 1
      post.reposted = true
      showToast('转发成功')
    } else {
      showToast(res.message || '转发失败，请稍后重试', 'error')
    }
  } catch {
    showToast('转发失败，请稍后重试', 'error')
  }
}

const removePost = async (postId) => {
  if (!window.confirm('确认删除该内容？')) return
  try {
    const res = await deletePost(postId)
    if (res.code === 200) {
      posts.value = posts.value.filter((item) => item.id !== postId)
      showToast('删除成功')
    } else {
      showToast(res.message || '删除失败，请稍后重试', 'error')
    }
  } catch {
    showToast('删除失败，请稍后重试', 'error')
  }
}

const toggleComments = async (post) => {
  post.showComments = !post.showComments
  if (post.showComments && post.comments.length === 0) {
    await loadComments(post)
  }
}

const loadComments = async (post) => {
  post.commentsLoading = true
  try {
    const res = await getComments(post.id, post.commentSort)
    if (res.code === 200 && Array.isArray(res.data)) {
      post.comments = normalizeComments(res.data)
    }
  } catch {
    showToast('评论加载失败，请稍后重试', 'error')
  } finally {
    post.commentsLoading = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    showToast('请输入评论内容', 'error')
    return
  }
  try {
    const res = await createComment({ postId: post.id, content: post.newComment.trim() })
    if (res.code === 200) {
      post.comments.push({
        ...res.data,
        liked: false,
        likeCount: res.data?.likeCount || 0
      })
      post.commentCount = (post.commentCount || 0) + 1
      post.newComment = ''
      showToast('评论发布成功')
    } else {
      showToast(res.message || '评论发布失败，请稍后重试', 'error')
    }
  } catch {
    showToast('评论发布失败，请稍后重试', 'error')
  }
}

const removeComment = async ({ post, commentId }) => {
  try {
    const res = await apiDeleteComment(commentId)
    if (res.code === 200) {
      post.comments = post.comments.filter((item) => item.id !== commentId)
      post.commentCount = Math.max(0, (post.commentCount || 0) - 1)
      showToast('评论已删除')
    } else {
      showToast(res.message || '删除失败，请稍后重试', 'error')
    }
  } catch {
    showToast('删除失败，请稍后重试', 'error')
  }
}

const toggleCommentLike = async ({ post, comment }) => {
  if (!isLoggedIn.value) {
    showToast('请登录后点赞', 'error')
    return
  }
  try {
    const res = await apiToggleCommentLike(comment.id)
    if (res.code === 200) {
      comment.liked = !!res.data.liked
      comment.likeCount = Math.max(0, (comment.likeCount || 0) + (comment.liked ? 1 : -1))
      if (post.commentSort === 'hot') {
        post.comments = [...post.comments].sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
      }
    }
  } catch {
    showToast('点赞失败，请稍后重试', 'error')
  }
}

onMounted(() => {
  load(true)
  if (loadMoreRef.value) {
    observer = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (shouldTriggerInfiniteLoad({
          isIntersecting: entry.isIntersecting,
          loading: loading.value,
          hasMore: hasMore.value
        })) {
          loadMore()
        }
      }
    }, { rootMargin: '280px 0px 200px 0px' })
    observer.observe(loadMoreRef.value)
  }
})

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
})
</script>

<style scoped>
.favorites-page {
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
  padding: 14px;
}

.heading h2 {
  margin: 0;
}

.heading p {
  margin: 4px 0 0;
  color: var(--muted);
}

.auto-load {
  width: 100%;
  height: 1px;
}
</style>
