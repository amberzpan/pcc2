<template>
  <section class="profile-page" v-if="viewUser">
    <article class="panel hero">
      <div class="cover clickable-media" @click="onCoverClick">
        <img v-if="viewUser.coverUrl" :src="viewUser.coverUrl" alt="cover" />
        <div v-else class="cover-placeholder">暂无背景图</div>
        <button v-if="isSelf && editing" class="camera-btn cover-camera-btn" type="button" title="更换背景图" @click.stop="triggerCoverUpload">
          <Camera :size="14" />
        </button>
      </div>

      <div class="profile-main">
        <div class="avatar-wrap clickable-media" @click="onAvatarClick">
          <img class="avatar" :src="viewUser.avatar || defaultAvatar" alt="avatar" />
          <button v-if="isSelf && editing" class="camera-btn avatar-camera-btn" type="button" title="更换头像" @click.stop="triggerAvatarUpload">
            <Camera :size="14" />
          </button>
        </div>

        <div class="identity">
          <h2>{{ viewUser.nickname || viewUser.username || '未设置昵称' }}</h2>
          <p class="username-line">@{{ viewUser.username }}</p>
          <p class="meta-line">注册于 {{ formatDate(viewUser.createdAt) }}</p>
          <p class="bio-inline">{{ viewUser.bio || '该用户暂未填写简介。' }}</p>
          <div class="stats-row">
            <div class="stat plain">
              <strong>{{ profilePostCount }}</strong>
              <span>帖子</span>
            </div>
            <button class="stat" @click="openRelation('following')">
              <strong>{{ viewUser.followingCount || 0 }}</strong>
              <span>关注</span>
            </button>
            <button class="stat" @click="openRelation('followers')">
              <strong>{{ viewUser.followersCount || 0 }}</strong>
              <span>粉丝</span>
            </button>
          </div>
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

      <input ref="avatarFileInput" class="hidden-input" type="file" accept="image/*" @change="onAvatarUpload" />
      <input ref="coverFileInput" class="hidden-input" type="file" accept="image/*" @change="onCoverUpload" />
    </article>

    <article class="panel edit-panel" v-if="isSelf && editing">
      <h3>编辑资料</h3>
      <div class="grid">
        <label>
          昵称
          <input v-model="form.nickname" placeholder="你的昵称" required />
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

      <div class="tabs" v-if="isSelf">
        <button class="tab-btn" :class="{ active: activeTab === 'posts' }" data-tab="posts" @click="switchTab('posts')">动态</button>
        <button class="tab-btn" :class="{ active: activeTab === 'liked' }" data-tab="liked" @click="switchTab('liked')">喜欢</button>
        <button class="tab-btn" :class="{ active: activeTab === 'favorites' }" data-tab="favorites" @click="switchTab('favorites')">收藏</button>
      </div>

      <div v-if="postsLoading && posts.length === 0" class="hint">帖子加载中</div>
      <div v-else-if="posts.length === 0" class="hint">暂无发布内容</div>

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

      <div ref="loadMoreRef" class="auto-load"></div>
    </article>

    <div class="overlay" v-if="relation.show" @click.self="closeRelation">
      <article class="relation-modal panel">
        <header>
          <h3>{{ relation.type === 'followers' ? '粉丝列表' : '关注列表' }}</h3>
          <button class="close-btn" @click="closeRelation">关闭</button>
        </header>
        <div v-if="relation.loading" class="hint">列表加载中</div>
        <div v-else-if="relation.items.length === 0" class="hint">暂无数据</div>
        <div v-else class="relation-list">
          <button class="relation-user" v-for="item in relation.items" :key="`${relation.type}-${item.id}`" @click="jumpToUser(item.id)">
            <img :src="item.avatar || defaultAvatar" alt="avatar" />
            <div>
              <strong>{{ item.nickname || item.username }}</strong>
              <p>@{{ item.username }}</p>
            </div>
          </button>
          <div v-if="relation.hasMore" ref="relationLoadMoreRef" class="auto-load relation-auto-load" aria-hidden="true"></div>
        </div>
      </article>
    </div>

    <div class="overlay image-preview-overlay" v-if="imagePreview.show" @click.self="closeImagePreview">
      <article class="image-preview-card">
        <header>
          <h3>{{ imagePreview.title }}</h3>
          <button class="close-btn" @click="closeImagePreview">关闭</button>
        </header>
        <img :src="imagePreview.url" :alt="imagePreview.title" />
      </article>
    </div>

    <div class="overlay image-editor-overlay" v-if="imageEditor.show" @click.self="closeImageEditor">
      <article class="image-editor-card">
        <header>
          <div>
            <h3>{{ imageEditor.title }}</h3>
            <p>调整显示位置后再保存。</p>
          </div>
          <button class="close-btn" type="button" @click="closeImageEditor">关闭</button>
        </header>

        <div class="edit-preview" :class="imageEditor.type">
          <img
            :src="imageEditor.sourceUrl"
            alt="图片编辑预览"
            :style="{
              transform: `translate(-50%, -50%) translate(${imageEditor.offsetX}%, ${imageEditor.offsetY}%) scale(${imageEditor.scale})`
            }"
          />
        </div>

        <div class="edit-controls">
          <label>
            缩放
            <input v-model.number="imageEditor.scale" type="range" min="1" max="2.4" step="0.02" />
          </label>
          <label>
            水平位置
            <input v-model.number="imageEditor.offsetX" type="range" min="-35" max="35" step="1" />
          </label>
          <label>
            垂直位置
            <input v-model.number="imageEditor.offsetY" type="range" min="-35" max="35" step="1" />
          </label>
        </div>

        <footer class="editor-actions">
          <button class="close-btn" type="button" @click="closeImageEditor">取消</button>
          <button class="save-btn" type="button" :disabled="imageEditor.saving" @click="saveEditedImage">
            {{ imageEditor.saving ? '正在保存' : '保存图片' }}
          </button>
        </footer>
      </article>
    </div>
  </section>

  <section v-else-if="hasResolvedProfile" class="panel hint">用户不存在或已被删除</section>
  <section v-else class="panel hint">主页加载中</section>
</template>

<script setup>
import { Camera } from 'lucide-vue-next'
import { computed, inject, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createComment,
  createPost,
  deleteComment as apiDeleteComment,
  deletePost,
  getComments,
  getFavorites,
  getFollowers,
  getFollowing,
  getLikedPosts,
  getUserPosts,
  getUserProfile,
  toggleCommentLike as apiToggleCommentLike,
  toggleFavorite,
  toggleFollow,
  toggleLike,
  updateUserInfo,
  uploadImage
} from '@/api'
import PostCard from '@/components/PostCard.vue'
import { shouldTriggerInfiniteLoad } from '@/utils/infinite-scroll'
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
  bio: ''
})
const imagePreview = ref({
  show: false,
  url: '',
  title: ''
})
const imageEditor = ref({
  show: false,
  type: 'cover',
  title: '',
  file: null,
  sourceUrl: '',
  scale: 1,
  offsetX: 0,
  offsetY: 0,
  saving: false
})

const posts = ref([])
const postsPage = ref(1)
const postsSize = 10
const postsLoading = ref(false)
const postsHasMore = ref(false)
const activeTab = ref('posts')
const profilePostCount = ref(0)
const loadMoreRef = ref(null)
const relationLoadMoreRef = ref(null)
const avatarFileInput = ref(null)
const coverFileInput = ref(null)
let observer = null
let relationObserver = null
let latestPostRequestId = 0

const relation = ref({
  show: false,
  type: 'followers',
  page: 1,
  size: 12,
  loading: false,
  hasMore: false,
  items: []
})

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="90" height="90"><rect fill="%23d8dde6" width="90" height="90"/><circle fill="%239aa3b3" cx="45" cy="33" r="14"/><rect fill="%239aa3b3" x="24" y="54" width="42" height="20" rx="10"/></svg>'

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
  form.value.bio = viewUser.value.bio || ''
}

const loadProfile = async () => {
  hasResolvedProfile.value = false
  if (!targetUserId.value) {
    viewUser.value = null
    posts.value = []
    profilePostCount.value = 0
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
      const initialPostCount = Number(res.data.postCount)
      profilePostCount.value = Number.isFinite(initialPostCount) && initialPostCount >= 0 ? initialPostCount : 0
      syncForm()
    } else {
      viewUser.value = null
      posts.value = []
      profilePostCount.value = 0
    }
  } catch {
    viewUser.value = null
    posts.value = []
    profilePostCount.value = 0
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
  const requestId = ++latestPostRequestId
  try {
    let res
    if (activeTab.value === 'posts') {
      res = await getUserPosts(viewUser.value.id, postsPage.value, postsSize)
    } else if (activeTab.value === 'favorites') {
      res = await getFavorites(postsPage.value, postsSize)
    } else {
      res = await getLikedPosts(postsPage.value, postsSize)
    }
    if (requestId !== latestPostRequestId) {
      return
    }
    const next = res.code === 200 ? normalizePostList(res.data) : []
    posts.value = reset ? next : posts.value.concat(next)
    postsHasMore.value = next.length === postsSize
    if (activeTab.value === 'posts') {
      profilePostCount.value = Math.max(profilePostCount.value, posts.value.length)
    }
  } catch {
    showToast('帖子加载失败，请稍后重试', 'error')
  } finally {
    postsLoading.value = false
  }
}

const loadMorePosts = async () => {
  if (!postsHasMore.value || postsLoading.value) {
    return
  }
  postsPage.value += 1
  await fetchPosts(false)
}

const switchTab = async (tab) => {
  if (activeTab.value === tab) {
    return
  }
  activeTab.value = tab
  await fetchPosts(true)
}

const saveProfile = async () => {
  if (!isSelf.value) return
  if (!form.value.nickname.trim()) {
    showToast('昵称不能为空', 'error')
    return
  }
  try {
    const payload = {
      nickname: form.value.nickname.trim(),
      bio: form.value.bio.trim()
    }
    const res = await updateUserInfo(payload)
    if (res.code === 200) {
      showToast('资料已更新')
      editing.value = false
      viewUser.value = {
        ...viewUser.value,
        ...res.data,
        followed: viewUser.value.followed,
        followersCount: viewUser.value.followersCount,
        followingCount: viewUser.value.followingCount
      }
      syncForm()
      await reloadUser()
    } else {
      showToast(res.message || '保存失败，请稍后重试', 'error')
    }
  } catch {
    showToast('保存失败，请稍后重试', 'error')
  }
}

const triggerAvatarUpload = () => {
  if (isSelf.value && avatarFileInput.value) {
    avatarFileInput.value.click()
  }
}

const triggerCoverUpload = () => {
  if (isSelf.value && coverFileInput.value) {
    coverFileInput.value.click()
  }
}

const openImagePreview = (url, title) => {
  if (!url) {
    return
  }
  imagePreview.value = {
    show: true,
    url,
    title
  }
}

const closeImagePreview = () => {
  imagePreview.value = {
    show: false,
    url: '',
    title: ''
  }
}

const revokeEditorSource = () => {
  if (imageEditor.value.sourceUrl) {
    URL.revokeObjectURL(imageEditor.value.sourceUrl)
  }
}

const openImageEditor = (file, type) => {
  revokeEditorSource()
  imageEditor.value = {
    show: true,
    type,
    title: type === 'avatar' ? '编辑头像' : '编辑背景图',
    file,
    sourceUrl: URL.createObjectURL(file),
    scale: 1,
    offsetX: 0,
    offsetY: 0,
    saving: false
  }
}

const closeImageEditor = () => {
  revokeEditorSource()
  imageEditor.value = {
    show: false,
    type: 'cover',
    title: '',
    file: null,
    sourceUrl: '',
    scale: 1,
    offsetX: 0,
    offsetY: 0,
    saving: false
  }
}

const loadImageElement = (url) => new Promise((resolve, reject) => {
  const image = new Image()
  image.onload = () => resolve(image)
  image.onerror = reject
  image.src = url
})

const renderEditedImage = async () => {
  const editor = imageEditor.value
  const source = await loadImageElement(editor.sourceUrl)
  const output = editor.type === 'avatar'
    ? { width: 640, height: 640 }
    : { width: 1600, height: 520 }
  const canvas = document.createElement('canvas')
  canvas.width = output.width
  canvas.height = output.height
  const context = canvas.getContext('2d')
  context.fillStyle = '#f8fafc'
  context.fillRect(0, 0, output.width, output.height)

  const fitScale = Math.max(output.width / source.naturalWidth, output.height / source.naturalHeight)
  const drawScale = fitScale * editor.scale
  const drawWidth = source.naturalWidth * drawScale
  const drawHeight = source.naturalHeight * drawScale
  const dx = (output.width - drawWidth) / 2 + (editor.offsetX / 100) * output.width * 0.5
  const dy = (output.height - drawHeight) / 2 + (editor.offsetY / 100) * output.height * 0.5
  context.drawImage(source, dx, dy, drawWidth, drawHeight)

  return new Promise((resolve) => {
    canvas.toBlob((blob) => resolve(blob), 'image/jpeg', 0.9)
  })
}

const saveEditedImage = async () => {
  if (!imageEditor.value.file || imageEditor.value.saving) {
    return
  }
  imageEditor.value.saving = true
  const type = imageEditor.value.type
  try {
    const blob = await renderEditedImage()
    if (!blob) {
      showToast('图片处理失败，请稍后重试', 'error')
      return
    }
    const filename = type === 'avatar' ? 'avatar.jpg' : 'cover.jpg'
    const editedFile = new File([blob], filename, { type: 'image/jpeg' })
    const res = await uploadImage(editedFile)
    if (res.code !== 200 || !res.data?.url) {
      showToast(type === 'avatar' ? '头像上传失败，请稍后重试' : '背景图上传失败，请稍后重试', 'error')
      return
    }
    const payload = {
      nickname: viewUser.value.nickname || viewUser.value.username,
      bio: viewUser.value.bio || ''
    }
    if (type === 'avatar') {
      payload.avatar = res.data.url
    } else {
      payload.coverUrl = res.data.url
    }
    const updateRes = await updateUserInfo(payload)
    if (updateRes.code === 200 && updateRes.data) {
      viewUser.value = {
        ...viewUser.value,
        ...updateRes.data,
        followed: viewUser.value.followed,
        followersCount: viewUser.value.followersCount,
        followingCount: viewUser.value.followingCount
      }
      syncForm()
      await reloadUser()
      showToast(type === 'avatar' ? '头像已更新' : '背景图已更新')
      closeImageEditor()
      return
    }
    showToast(type === 'avatar' ? '头像上传失败，请稍后重试' : '背景图上传失败，请稍后重试', 'error')
  } catch {
    showToast(type === 'avatar' ? '头像上传失败，请稍后重试' : '背景图上传失败，请稍后重试', 'error')
  } finally {
    if (imageEditor.value.show) {
      imageEditor.value.saving = false
    }
  }
}

const onAvatarClick = () => {
  openImagePreview(viewUser.value?.avatar || '', '头像预览')
}

const onCoverClick = () => {
  openImagePreview(viewUser.value?.coverUrl || '', '背景图预览')
}

const onAvatarUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  openImageEditor(file, 'avatar')
  event.target.value = ''
}

const onCoverUpload = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  openImageEditor(file, 'cover')
  event.target.value = ''
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
    showToast('关注失败，请稍后重试', 'error')
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

const togglePostFavorite = async (post) => {
  if (!isLoggedIn.value) {
    showToast('请登录后收藏', 'error')
    return
  }
  try {
    const res = await toggleFavorite(post.id)
    if (res.code === 200) {
      post.favorited = !!res.data.favorited
    }
  } catch {
    showToast('收藏失败，请稍后重试', 'error')
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
      if (viewUser.value && post.userId === viewUser.value.id && !isSelf.value) {
        viewUser.value.followed = post.followed
      }
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
      profilePostCount.value = Math.max(0, profilePostCount.value - 1)
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
  await nextTick()
  setupRelationInfiniteLoad()
}

const loadRelation = async (reset = false) => {
  if (!viewUser.value?.id) {
    return
  }
  if (relation.value.loading) {
    return
  }
  if (!reset && !relation.value.hasMore) {
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
    await nextTick()
    setupRelationInfiniteLoad()
  } catch {
    showToast('列表加载失败，请稍后重试', 'error')
  } finally {
    relation.value.loading = false
  }
}

const closeRelation = () => {
  relation.value.show = false
  teardownRelationInfiniteLoad()
}

const setupRelationInfiniteLoad = () => {
  if (!relation.value.show || !relationLoadMoreRef.value) {
    teardownRelationInfiniteLoad()
    return
  }
  teardownRelationInfiniteLoad()
  relationObserver = new IntersectionObserver((entries) => {
    for (const entry of entries) {
      if (shouldTriggerInfiniteLoad({
        isIntersecting: entry.isIntersecting,
        loading: relation.value.loading,
        hasMore: relation.value.hasMore
      })) {
        loadRelation(false)
      }
    }
  }, { rootMargin: '220px 0px 140px 0px' })
  relationObserver.observe(relationLoadMoreRef.value)
}

const teardownRelationInfiniteLoad = () => {
  if (relationObserver) {
    relationObserver.disconnect()
    relationObserver = null
  }
}

const jumpToUser = (id) => {
  closeRelation()
  router.push(`/profile/${id}`)
}

const formatDate = (value) => {
  if (!value) {
    return '未知'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '未知'
  }
  return date.toLocaleDateString('zh-CN')
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
  teardownRelationInfiniteLoad()
  activeTab.value = 'posts'
  await initialize()
})

watch(() => relation.value.show, async (visible) => {
  if (!visible) {
    teardownRelationInfiniteLoad()
    return
  }
  await nextTick()
  setupRelationInfiniteLoad()
})

watch(() => currentUserId.value, async () => {
  if (!route.params.id) {
    await initialize()
  }
})

onMounted(async () => {
  await initialize()
  if (loadMoreRef.value) {
    observer = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (shouldTriggerInfiniteLoad({
          isIntersecting: entry.isIntersecting,
          loading: postsLoading.value,
          hasMore: postsHasMore.value
        })) {
          loadMorePosts()
        }
      }
    }, { rootMargin: '300px 0px 200px 0px' })
    observer.observe(loadMoreRef.value)
  }
})

onBeforeUnmount(() => {
  if (observer) {
    observer.disconnect()
    observer = null
  }
  teardownRelationInfiniteLoad()
  revokeEditorSource()
})
</script>

<style scoped>
.profile-page {
  display: grid;
  gap: 14px;
  width: 100%;
  max-width: 920px;
  margin: 0 auto;
}

.panel {
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--paper);
  padding: 14px;
  box-shadow: 0 10px 26px color-mix(in srgb, #0f172a 8%, transparent);
}

.hero {
  padding: 0;
  overflow: hidden;
}

.cover {
  height: 150px;
  width: 100%;
  background: var(--surface);
  overflow: hidden;
  position: relative;
  cursor: default;
}

.clickable-media {
  cursor: default;
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  cursor: default;
}

.cover-placeholder {
  display: grid;
  place-items: center;
  height: 100%;
  color: var(--muted);
  font-weight: 700;
}

.profile-main {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: start;
  gap: 14px;
  margin-top: 0;
  padding: 0 14px 10px;
}

.avatar-wrap {
  position: relative;
  width: fit-content;
  margin-top: -38px;
  cursor: default;
}

.avatar {
  width: 92px;
  height: 92px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid var(--paper);
  cursor: default;
}

.identity {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: flex-start;
  gap: 0;
  min-width: 0;
}

.identity h2 {
  margin: 0;
  font-size: 1.34rem;
  line-height: 1.2;
  letter-spacing: 0.01em;
  color: var(--ink);
  font-weight: 700;
}

.username-line {
  margin: 2px 0 0;
  color: var(--muted);
  font-size: 0.92rem;
  line-height: 1.28;
  font-weight: 400;
}

.bio-inline {
  margin: 10px 0 0;
  color: var(--ink);
  line-height: 1.55;
  font-size: 0.98rem;
  font-weight: 400;
  max-width: 65ch;
}

.meta-line {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 0.92rem;
  font-weight: 400;
  line-height: 1.35;
}

.hero-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  align-self: start;
  justify-self: end;
  margin-left: 14px;
}

.action-btn {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--ink);
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  font-weight: 700;
  transition: transform 0.16s ease, border-color 0.16s ease, filter 0.16s ease;
}

.action-btn:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.action-btn.primary {
  background: var(--accent);
  border-color: var(--accent);
  color: var(--paper);
}

.action-btn.primary:hover {
  background: var(--accent);
  filter: brightness(1.03);
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-top: 10px;
  align-items: baseline;
}

.stat {
  border: 0;
  background: transparent;
  min-width: auto;
  padding: 0;
  cursor: pointer;
  display: inline-flex;
  align-items: baseline;
  gap: 6px;
  line-height: 1.28;
  transition: transform 0.16s ease, color 0.16s ease;
}

.stat:hover {
  transform: translateY(-1px);
  color: var(--ink);
}

.stat strong {
  font-size: 0.92rem;
  font-weight: 700;
  color: var(--ink);
}

.stat span {
  color: color-mix(in srgb, var(--ink) 62%, var(--muted));
  font-size: 0.92rem;
  font-weight: 400;
}

.stat.plain {
  cursor: default;
}

.stat.plain:hover {
  transform: none;
  color: inherit;
}

.timeline {
  display: grid;
  gap: 10px;
}

.timeline-head h3 {
  margin: 0;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tab-btn {
  border: 1px solid var(--line);
  border-radius: 999px;
  background: transparent;
  color: var(--muted);
  padding: 6px 12px;
  font-weight: 700;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, background-color 0.16s ease;
}

.tab-btn:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 66%, var(--muted));
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.tab-btn.active {
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}

.grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
}

label {
  display: grid;
  gap: 6px;
  color: var(--muted);
  font-weight: 700;
}

input,
textarea {
  border: 1px solid var(--line);
  border-radius: 10px;
  padding: 8px 10px;
  font: inherit;
  background: color-mix(in srgb, var(--surface) 92%, var(--paper));
  color: var(--ink);
  transition: border-color 0.16s ease, background-color 0.16s ease;
}

input:focus,
textarea:focus {
  outline: none;
  border-color: color-mix(in srgb, #1d9bf0 45%, var(--line));
  background: var(--surface);
}

.bio-label {
  margin-top: 10px;
}

.save-btn {
  margin-top: 10px;
  border: 1px solid var(--accent);
  border-radius: 10px;
  background: var(--accent);
  color: var(--paper);
  font-weight: 700;
  padding: 9px 14px;
  cursor: pointer;
  transition: transform 0.16s ease, filter 0.16s ease;
}

.save-btn:hover {
  transform: translateY(-1px);
  filter: brightness(1.03);
}

.auto-load {
  width: 100%;
  height: 1px;
}

.relation-auto-load {
  margin-top: 4px;
}

.camera-btn {
  position: absolute;
  border: 1px solid var(--line);
  border-radius: 999px;
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  background: color-mix(in srgb, var(--paper) 92%, transparent);
  color: var(--ink);
  cursor: pointer;
}

.cover-camera-btn {
  right: 10px;
  bottom: 10px;
  z-index: 2;
}

.avatar-camera-btn {
  right: 2px;
  bottom: 2px;
  z-index: 2;
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(17, 24, 39, 0.4);
  display: grid;
  place-items: center;
  z-index: 40;
}

.relation-modal {
  width: min(560px, calc(100vw - 24px));
  max-height: 75vh;
  overflow: auto;
}

.image-preview-overlay {
  z-index: 50;
}

.image-preview-card {
  width: min(900px, calc(100vw - 24px));
  max-height: calc(100vh - 24px);
  border: 1px solid var(--line);
  border-radius: 16px;
  background: var(--paper);
  padding: 10px;
  display: grid;
  gap: 10px;
}

.image-preview-card header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.image-preview-card h3 {
  margin: 0;
}

.image-preview-card img {
  width: 100%;
  max-height: calc(100vh - 110px);
  object-fit: contain;
  border-radius: 12px;
  background: var(--surface);
}

.image-editor-overlay {
  z-index: 60;
}

.image-editor-card {
  width: min(720px, calc(100vw - 24px));
  border: 1px solid var(--line);
  border-radius: 18px;
  background: var(--paper);
  padding: 12px;
  display: grid;
  gap: 12px;
  box-shadow: 0 24px 60px color-mix(in srgb, #0f172a 24%, transparent);
}

.image-editor-card header,
.editor-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.image-editor-card h3,
.image-editor-card p {
  margin: 0;
}

.image-editor-card p {
  color: var(--muted);
  font-size: 0.86rem;
}

.edit-preview {
  position: relative;
  overflow: hidden;
  background: var(--surface);
  border: 1px solid var(--line);
  display: grid;
  place-items: center;
}

.edit-preview.cover {
  height: 230px;
  border-radius: 14px;
}

.edit-preview.avatar {
  width: 260px;
  height: 260px;
  border-radius: 50%;
  justify-self: center;
}

.edit-preview img {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transform-origin: center;
  will-change: transform;
  user-select: none;
  pointer-events: none;
}

.edit-controls {
  display: grid;
  gap: 10px;
}

.edit-controls input[type='range'] {
  padding: 0;
  cursor: pointer;
  accent-color: var(--accent);
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
  border: 1px solid var(--line);
  border-radius: 999px;
  background: transparent;
  color: var(--ink);
  padding: 6px 10px;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, background-color 0.16s ease;
}

.close-btn:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.relation-list {
  display: grid;
  gap: 8px;
}

.relation-user {
  border: 1px solid var(--line);
  border-radius: 12px;
  background: transparent;
  width: 100%;
  padding: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  text-align: left;
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, background-color 0.16s ease;
}

.relation-user:hover {
  transform: translateY(-1px);
  border-color: color-mix(in srgb, var(--line) 66%, var(--muted));
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.relation-user img {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.relation-user p {
  margin: 3px 0 0;
  color: var(--muted);
  font-size: 0.84rem;
}

.relation-user strong {
  color: var(--ink);
}

:global(:root[data-theme='dark']) .relation-user strong {
  color: #f8fafc;
}

.hint {
  text-align: center;
  color: var(--muted);
}

.hidden-input {
  display: none;
}

@media (max-width: 840px) {
  .profile-page {
    gap: 12px;
  }

  .panel {
    padding: 12px;
    border-radius: 16px;
  }

  .hero {
    padding: 0;
  }

  .cover {
    height: 124px;
  }

  .profile-main {
    grid-template-columns: 76px minmax(0, 1fr);
    align-items: end;
    gap: 12px;
    padding: 0 12px 12px;
    margin-top: -22px;
    text-align: left;
  }

  .avatar-wrap {
    margin: 0;
    width: 76px;
    height: 76px;
  }

  .avatar {
    width: 76px;
    height: 76px;
    margin: 0;
  }

  .hero-actions {
    grid-column: 2;
    justify-content: flex-start;
    margin-left: 0;
    margin-top: 2px;
  }

  .identity {
    align-items: flex-start;
    text-align: left;
  }

  .stats-row {
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 12px;
  }

  .tabs {
    flex-wrap: wrap;
  }

  .relation-modal,
  .image-preview-card,
  .image-editor-card {
    width: min(100vw - 20px, 720px);
  }
}
</style>
