<template>
  <section class="profile-page" v-if="viewUser">
    <article class="panel hero">
      <div class="cover"></div>
      <div class="profile-main">
        <img class="avatar" :src="viewUser.avatar || defaultAvatar" alt="avatar" />
        <div class="identity">
          <h2>{{ viewUser.nickname || viewUser.username }}</h2>
          <p>@{{ viewUser.username }}</p>
          <small>{{ viewUser.bio || '这个人很神秘，还没有留下简介。' }}</small>
        </div>
        <div class="hero-actions">
          <button v-if="isSelf" class="action-btn" @click="editing = !editing">
            {{ editing ? '收起编辑' : '编辑资料' }}
          </button>
          <button v-else-if="isLoggedIn" class="action-btn primary" @click="toggleProfileFollow">
            {{ viewUser.followed ? '已关注' : '关注 Ta' }}
          </button>
        </div>
      </div>
      <div class="stats-row">
        <button class="stat" @click="openRelation('following')">
          <strong>{{ viewUser.followingCount || 0 }}</strong>
          <span>关注</span>
        </button>
        <button class="stat" @click="openRelation('followers')">
          <strong>{{ viewUser.followersCount || 0 }}</strong>
          <span>粉丝</span>
        </button>
      </div>
    </article>

    <article class="panel edit-panel" v-if="isSelf && editing">
      <h3>编辑资料</h3>
      <div class="grid">
        <label>
          昵称
          <input v-model="form.nickname" placeholder="请输入昵称" />
        </label>
        <label>
          头像 URL
          <input v-model="form.avatar" placeholder="可填写图片链接" />
        </label>
      </div>
      <label class="bio-label">
        简介
        <textarea v-model="form.bio" rows="3" placeholder="介绍一下自己"></textarea>
      </label>
      <button class="save-btn" @click="saveProfile">保存</button>
    </article>

    <article class="panel timeline">
      <div class="timeline-head">
        <h3>{{ isSelf ? '我的动态' : 'Ta 的动态' }}</h3>
      </div>

      <div v-if="postsLoading" class="hint">加载中...</div>
      <div v-else-if="posts.length === 0" class="hint">还没有发布内容</div>

      <PostCard
        v-for="post in posts"
        v-else
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

      <button class="load-more" v-if="postsHasMore" @click="loadMorePosts">加载更多</button>
    </article>

    <div class="overlay" v-if="relation.show" @click.self="closeRelation">
      <article class="relation-modal panel">
        <header>
          <h3>{{ relation.type === 'followers' ? '粉丝列表' : '关注列表' }}</h3>
          <button class="close-btn" @click="closeRelation">关闭</button>
        </header>
        <div v-if="relation.loading" class="hint">加载中...</div>
        <div v-else-if="relation.items.length === 0" class="hint">暂无数据</div>
        <div v-else class="relation-list">
          <button class="relation-user" v-for="item in relation.items" :key="`${relation.type}-${item.id}`" @click="jumpToUser(item.id)">
            <img :src="item.avatar || defaultAvatar" alt="avatar" />
            <div>
              <strong>{{ item.nickname || item.username }}</strong>
              <p>@{{ item.username }}</p>
            </div>
          </button>
          <button class="load-more" v-if="relation.hasMore" @click="loadRelation(false)">加载更多</button>
        </div>
      </article>
    </div>
  </section>

  <section v-else-if="hasResolvedProfile" class="panel hint">用户不存在或已被删除</section>
  <section v-else class="panel hint">加载中...</section>
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
  getFollowers,
  getFollowing,
  getUserPosts,
  getUserProfile,
  toggleCommentLike as apiToggleCommentLike,
  toggleFavorite,
  toggleFollow,
  toggleLike,
  updateUserInfo
} from '@/api'
import PostCard from '@/components/PostCard.vue'
import { normalizeComments, normalizePostList } from '@/utils/post-utils'

const route = useRoute()
const router = useRouter()

const user = inject('user')
const isLoggedIn = inject('isLoggedIn')
const showToast = inject('showToast')
const reloadUser = inject('reloadUser')

const viewUser = ref(null)
const editing = ref(false)
const form = ref({
  nickname: '',
  avatar: '',
  bio: ''
})

const posts = ref([])
const postsPage = ref(1)
const postsSize = 10
const postsLoading = ref(false)
const postsHasMore = ref(false)

const relation = ref({
  show: false,
  type: 'followers',
  page: 1,
  size: 12,
  loading: false,
  hasMore: false,
  items: []
})

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="90" height="90"><rect fill="%23ead8c3" width="90" height="90"/><circle fill="%23c9ab8c" cx="45" cy="33" r="14"/><rect fill="%23c9ab8c" x="24" y="54" width="42" height="20" rx="10"/></svg>'

const currentUserId = computed(() => user.value?.id || null)
const hasResolvedProfile = ref(false)
const targetUserId = computed(() => {
  const value = route.params.id
  if (value === undefined) {
    return currentUserId.value
  }
  const parsed = Number(value)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null
})
const isSelf = computed(() => currentUserId.value && viewUser.value && currentUserId.value === viewUser.value.id)

const syncForm = () => {
  if (!viewUser.value) return
  form.value.nickname = viewUser.value.nickname || ''
  form.value.avatar = viewUser.value.avatar || ''
  form.value.bio = viewUser.value.bio || ''
}

const loadProfile = async () => {
  hasResolvedProfile.value = false
  if (!targetUserId.value) {
    viewUser.value = null
    posts.value = []
    hasResolvedProfile.value = true
    return
  }
  try {
    const res = await getUserProfile(targetUserId.value)
    if (res.code === 200 && res.data) {
      viewUser.value = {
        ...res.data,
        followersCount: res.data.followersCount || 0,
        followingCount: res.data.followingCount || 0,
        followed: !!res.data.followed
      }
      syncForm()
    } else {
      viewUser.value = null
      posts.value = []
    }
  } catch {
    viewUser.value = null
    posts.value = []
  } finally {
    hasResolvedProfile.value = true
  }
}

const fetchPosts = async (reset = true) => {
  if (!viewUser.value?.id) {
    posts.value = []
    return
  }

  if (reset) {
    postsPage.value = 1
    posts.value = []
  }

  postsLoading.value = true
  try {
    const res = await getUserPosts(viewUser.value.id, postsPage.value, postsSize)
    const next = res.code === 200 ? normalizePostList(res.data) : []
    posts.value = reset ? next : posts.value.concat(next)
    postsHasMore.value = next.length === postsSize
  } catch {
    showToast('加载动态失败', 'error')
  } finally {
    postsLoading.value = false
  }
}

const loadMorePosts = async () => {
  postsPage.value += 1
  await fetchPosts(false)
}

const saveProfile = async () => {
  if (!isSelf.value) return
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
      await loadProfile()
    } else {
      showToast(res.message || '更新失败', 'error')
    }
  } catch {
    showToast('更新失败', 'error')
  }
}

const toggleProfileFollow = async () => {
  if (!isLoggedIn.value || !viewUser.value || isSelf.value) {
    return
  }
  try {
    const res = await toggleFollow(viewUser.value.id)
    if (res.code === 200) {
      const followed = !!res.data.followed
      viewUser.value.followed = followed
      viewUser.value.followersCount = Math.max(0, (viewUser.value.followersCount || 0) + (followed ? 1 : -1))
      await reloadUser()
    }
  } catch {
    showToast('关注操作失败', 'error')
  }
}

const togglePostLike = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  try {
    const res = await toggleLike(post.id)
    if (res.code === 200) {
      post.liked = !!res.data.liked
      post.likeCount = Math.max(0, (post.likeCount || 0) + (post.liked ? 1 : -1))
    }
  } catch {
    showToast('点赞失败', 'error')
  }
}

const togglePostFavorite = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  try {
    const res = await toggleFavorite(post.id)
    if (res.code === 200) {
      post.favorited = !!res.data.favorited
    }
  } catch {
    showToast('收藏失败', 'error')
  }
}

const togglePostFollow = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  try {
    const res = await toggleFollow(post.userId)
    if (res.code === 200) {
      post.followed = !!res.data.followed
      if (viewUser.value && post.userId === viewUser.value.id && !isSelf.value) {
        viewUser.value.followed = post.followed
      }
    }
  } catch {
    showToast('关注操作失败', 'error')
  }
}

const repost = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
    return
  }
  const content = window.prompt('请输入转发语（可选）')
  if (content === null) return
  try {
    const res = await createPost({ content, repostId: post.id })
    if (res.code === 200) {
      post.repostCount = (post.repostCount || 0) + 1
      showToast('转发成功')
    } else {
      showToast(res.message || '转发失败', 'error')
    }
  } catch {
    showToast('转发失败', 'error')
  }
}

const removePost = async (postId) => {
  if (!window.confirm('确定删除这条动态吗？')) return
  try {
    const res = await deletePost(postId)
    if (res.code === 200) {
      posts.value = posts.value.filter((item) => item.id !== postId)
      showToast('删除成功')
    } else {
      showToast(res.message || '删除失败', 'error')
    }
  } catch {
    showToast('删除失败', 'error')
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
    showToast('评论加载失败', 'error')
  } finally {
    post.commentsLoading = false
  }
}

const submitComment = async (post) => {
  if (!post.newComment.trim()) {
    showToast('评论不能为空', 'error')
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
      showToast('评论成功')
    } else {
      showToast(res.message || '评论失败', 'error')
    }
  } catch {
    showToast('评论失败', 'error')
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
      showToast(res.message || '删除失败', 'error')
    }
  } catch {
    showToast('删除失败', 'error')
  }
}

const toggleCommentLike = async ({ post, comment }) => {
  if (!isLoggedIn.value) {
    showToast('请先登录', 'error')
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
    showToast('评论点赞失败', 'error')
  }
}

const mapRelationItem = (item, type) => {
  if (type === 'followers') {
    return {
      id: item.followerId,
      username: item.followerUsername,
      nickname: item.followerNickname,
      avatar: item.followerAvatar
    }
  }
  return {
    id: item.followingId,
    username: item.followingUsername,
    nickname: item.followingNickname,
    avatar: item.followingAvatar
  }
}

const openRelation = async (type) => {
  relation.value.show = true
  relation.value.type = type
  relation.value.page = 1
  relation.value.items = []
  relation.value.hasMore = false
  await loadRelation(true)
}

const loadRelation = async (reset = false) => {
  if (!viewUser.value?.id) {
    return
  }
  if (reset) {
    relation.value.page = 1
    relation.value.items = []
  }
  relation.value.loading = true
  try {
    const requestPage = relation.value.page
    const api = relation.value.type === 'followers' ? getFollowers : getFollowing
    const res = await api(viewUser.value.id, requestPage, relation.value.size)
    const list = res.code === 200 && Array.isArray(res.data) ? res.data.map((item) => mapRelationItem(item, relation.value.type)) : []
    relation.value.items = reset ? list : relation.value.items.concat(list)
    relation.value.hasMore = list.length === relation.value.size
    relation.value.page = requestPage + 1
  } catch {
    showToast('加载列表失败', 'error')
  } finally {
    relation.value.loading = false
  }
}

const closeRelation = () => {
  relation.value.show = false
}

const jumpToUser = (id) => {
  closeRelation()
  router.push(`/profile/${id}`)
}

const initialize = async () => {
  await loadProfile()
  if (viewUser.value) {
    await fetchPosts(true)
  }
}

watch(() => route.params.id, async () => {
  editing.value = false
  relation.value.show = false
  await initialize()
})

watch(() => currentUserId.value, async () => {
  if (!route.params.id) {
    await initialize()
  }
})

onMounted(async () => {
  await initialize()
})
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 14px;
}

.panel {
  border: 1px solid #e7dccf;
  border-radius: 18px;
  background: #fffdf8;
  padding: 14px;
  box-shadow: 0 12px 30px rgba(66, 45, 17, 0.06);
}

.hero {
  padding: 0;
  overflow: hidden;
}

.cover {
  height: 96px;
  background:
    radial-gradient(circle at 20% 25%, rgba(242, 90, 41, 0.45), transparent 35%),
    radial-gradient(circle at 80% 60%, rgba(240, 179, 87, 0.4), transparent 40%),
    linear-gradient(135deg, #f8e8cf, #f4d8b8);
}

.profile-main {
  display: grid;
  grid-template-columns: auto 1fr auto;
  align-items: center;
  gap: 14px;
  margin-top: -26px;
  padding: 0 14px 10px;
}

.avatar {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #fffdf8;
  box-shadow: 0 10px 24px rgba(37, 25, 13, 0.15);
}

.identity h2 {
  margin: 0;
  font-size: 1.4rem;
}

.identity p {
  margin: 4px 0;
  color: #7a6d5a;
}

.identity small {
  color: #5f4d38;
  display: block;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-btn {
  border: 1px solid #e7dccf;
  background: #fff;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  font-weight: 700;
}

.action-btn.primary {
  background: #f25a29;
  border-color: #f25a29;
  color: #fff;
}

.stats-row {
  display: flex;
  gap: 8px;
  padding: 0 14px 14px;
}

.stat {
  border: 1px solid #ebdecf;
  border-radius: 12px;
  background: #fff;
  min-width: 120px;
  padding: 10px 14px;
  cursor: pointer;
  display: grid;
  text-align: left;
}

.stat strong {
  font-size: 1.05rem;
}

.stat span {
  color: #7a6d5a;
  font-size: 0.85rem;
}

.timeline {
  display: grid;
  gap: 10px;
}

.timeline-head h3 {
  margin: 0;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

label {
  display: grid;
  gap: 6px;
  color: #5f4d38;
  font-weight: 700;
}

input,
textarea {
  border: 1px solid #eadfcf;
  border-radius: 10px;
  padding: 8px 10px;
  font: inherit;
}

.bio-label {
  margin-top: 10px;
}

.save-btn {
  margin-top: 10px;
  border: 0;
  border-radius: 10px;
  background: #f25a29;
  color: #fff;
  font-weight: 700;
  padding: 9px 14px;
}

.load-more {
  border: 1px solid #ebdecf;
  border-radius: 999px;
  background: #fff;
  color: #4f3f2c;
  font-weight: 700;
  padding: 8px 12px;
  cursor: pointer;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(34, 25, 18, 0.4);
  display: grid;
  place-items: center;
  z-index: 40;
}

.relation-modal {
  width: min(560px, calc(100vw - 24px));
  max-height: 75vh;
  overflow: auto;
}

.relation-modal header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.relation-modal h3 {
  margin: 0;
}

.close-btn {
  border: 1px solid #eadfcf;
  border-radius: 999px;
  background: #fff;
  padding: 6px 10px;
  cursor: pointer;
}

.relation-list {
  display: grid;
  gap: 8px;
}

.relation-user {
  border: 1px solid #eadfcf;
  border-radius: 12px;
  background: #fff;
  width: 100%;
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  cursor: pointer;
}

.relation-user img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.relation-user p {
  margin: 3px 0 0;
  color: #7a6d5a;
  font-size: 0.84rem;
}

.hint {
  text-align: center;
  color: #7a6d5a;
}

@media (max-width: 840px) {
  .profile-main {
    grid-template-columns: 1fr;
    text-align: center;
    margin-top: -18px;
  }

  .avatar {
    margin: 0 auto;
  }

  .hero-actions {
    justify-content: center;
  }

  .stats-row {
    justify-content: center;
  }

  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
