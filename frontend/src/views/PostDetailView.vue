<template>
  <section class="detail-page">
    <article class="panel" v-if="loading && !hasLoadedOnce">帖子加载中</article>
    <article class="panel" v-else-if="!post">这条帖子不存在，或已经被删除</article>

    <PostCard
      v-else
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
  </section>
</template>

<script setup>
import { computed, inject, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createComment,
  createPost,
  deleteComment as apiDeleteComment,
  deletePost,
  getComments,
  getPost,
  toggleCommentLike as apiToggleCommentLike,
  toggleFavorite,
  toggleFollow,
  toggleLike
} from '@/api'
import PostCard from '@/components/PostCard.vue'
import { normalizeComments, normalizePost } from '@/utils/post-utils'

const route = useRoute()
const router = useRouter()
const user = inject('user')
const isLoggedIn = inject('isLoggedIn')
const showToast = inject('showToast')

const loading = ref(true)
const hasLoadedOnce = ref(false)
const post = ref(null)
const currentUserId = computed(() => user.value?.id || null)

const loadPost = async () => {
  loading.value = true
  try {
    const res = await getPost(route.params.id)
    post.value = res.code === 200 && res.data ? normalizePost(res.data) : null
  } catch {
    post.value = null
  } finally {
    loading.value = false
    hasLoadedOnce.value = true
  }
}

const togglePostLike = async (item) => {
  if (!isLoggedIn.value) {
    showToast('请登录后点赞', 'error')
    return
  }
  try {
    const res = await toggleLike(item.id)
    if (res.code === 200) {
      item.liked = !!res.data.liked
      item.likeCount = Math.max(0, (item.likeCount || 0) + (item.liked ? 1 : -1))
    }
  } catch {
    showToast('点赞失败，请稍后重试', 'error')
  }
}

const togglePostFavorite = async (item) => {
  if (!isLoggedIn.value) {
    showToast('请登录后收藏', 'error')
    return
  }
  try {
    const res = await toggleFavorite(item.id)
    if (res.code === 200) {
      item.favorited = !!res.data.favorited
    }
  } catch {
    showToast('收藏失败，请稍后重试', 'error')
  }
}

const togglePostFollow = async (item) => {
  if (!isLoggedIn.value) {
    showToast('请登录后关注用户', 'error')
    return
  }
  try {
    const res = await toggleFollow(item.userId)
    if (res.code === 200) {
      item.followed = !!res.data.followed
    }
  } catch {
    showToast('关注失败，请稍后重试', 'error')
  }
}

const repost = async (item) => {
  if (!isLoggedIn.value) {
    showToast('请登录后转发', 'error')
    return
  }
  const content = window.prompt('请输入转发内容（可选）')
  if (content === null) return
  try {
    const res = await createPost({ content, repostId: item.id })
    if (res.code === 200) {
      item.repostCount = (item.repostCount || 0) + 1
      item.reposted = true
      showToast('转发成功')
    }
  } catch {
    showToast('转发失败，请稍后重试', 'error')
  }
}

const removePost = async (id) => {
  if (!window.confirm('确认删除该内容？')) return
  try {
    const res = await deletePost(id)
    if (res.code === 200) {
      showToast('删除成功')
      router.push('/')
    }
  } catch {
    showToast('删除失败，请稍后重试', 'error')
  }
}

const toggleComments = async (item) => {
  item.showComments = !item.showComments
  if (item.showComments && item.comments.length === 0) {
    await loadComments(item)
  }
}

const loadComments = async (item) => {
  item.commentsLoading = true
  try {
    const res = await getComments(item.id, item.commentSort)
    if (res.code === 200 && Array.isArray(res.data)) {
      item.comments = normalizeComments(res.data)
    }
  } finally {
    item.commentsLoading = false
  }
}

const submitComment = async (item) => {
  if (!item.newComment.trim()) {
    showToast('请输入评论内容', 'error')
    return
  }
  try {
    const res = await createComment({ postId: item.id, content: item.newComment.trim() })
    if (res.code === 200) {
      item.comments.push({ ...res.data, liked: false, likeCount: res.data?.likeCount || 0 })
      item.commentCount = (item.commentCount || 0) + 1
      item.newComment = ''
    }
  } catch {
    showToast('评论发布失败，请稍后重试', 'error')
  }
}

const removeComment = async ({ post: item, commentId }) => {
  try {
    const res = await apiDeleteComment(commentId)
    if (res.code === 200) {
      item.comments = item.comments.filter((comment) => comment.id !== commentId)
      item.commentCount = Math.max(0, (item.commentCount || 0) - 1)
    }
  } catch {
    showToast('评论删除失败，请稍后重试', 'error')
  }
}

const toggleCommentLike = async ({ post: item, comment }) => {
  if (!isLoggedIn.value) {
    showToast('请登录后点赞', 'error')
    return
  }
  try {
    const res = await apiToggleCommentLike(comment.id)
    if (res.code === 200) {
      comment.liked = !!res.data.liked
      comment.likeCount = Math.max(0, (comment.likeCount || 0) + (comment.liked ? 1 : -1))
      if (item.commentSort === 'hot') {
        item.comments = [...item.comments].sort((a, b) => (b.likeCount || 0) - (a.likeCount || 0))
      }
    }
  } catch {
    showToast('点赞失败，请稍后重试', 'error')
  }
}

onMounted(loadPost)

watch(() => route.params.id, () => {
  loadPost()
})
</script>

<style scoped>
.detail-page {
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
</style>
