<template>
  <article class="post-card" ref="postCardRef">
    <div class="post-grid">
      <div class="avatar-rail">
        <img class="avatar clickable" :src="post.avatar || defaultAvatar" alt="avatar" @click="openProfile" />
      </div>

      <div class="post-main">
        <header class="meta-row">
          <div class="identity clickable" @click="openProfile">
            <h3>{{ post.nickname || post.username || '匿名用户' }}</h3>
            <p class="meta-inline">
              <span>{{ post.username ? `@${post.username}` : '@匿名用户' }}</span>
              <span class="dot">·</span>
              <span>{{ formatTime(post.createdAt) }}</span>
            </p>
          </div>
          <div class="head-actions">
            <button v-if="showFollow" class="head-follow" @click.stop="$emit('toggle-follow', post)">
              {{ post.followed ? '已关注' : '关注' }}
            </button>
            <div v-if="canDelete" class="post-menu">
              <button class="more-btn" type="button" title="更多" aria-label="更多操作" @click.stop="showPostMenu = !showPostMenu">
                <MoreHorizontal :size="17" />
              </button>
              <div class="post-menu-popover" v-if="showPostMenu">
                <button class="delete-menu-item" type="button" @click.stop="requestDeletePost">
                  <Trash2 :size="15" />
                  <span>删除</span>
                </button>
              </div>
            </div>
          </div>
        </header>

        <p v-if="post.content" class="post-content allow-selection">{{ post.content }}</p>

        <div
          v-if="post.mediaType === 'image' && visibleImageUrls.length"
          class="media-wrap media-gallery-wrap"
          :class="mediaGalleryClass"
        >
          <button
            v-for="(imageUrl, imageIndex) in visibleImageUrls"
            :key="`${post.id || 'post'}-${imageIndex}-${imageUrl}`"
            class="media-open-surface"
            type="button"
            aria-label="预览图片"
            @click="openMediaPreview('image', imageUrl)"
          >
            <img class="media media-image" :src="imageUrl" alt="post media" />
          </button>
        </div>
        <div v-if="post.mediaUrl && post.mediaType === 'video'" class="media-wrap">
          <div class="video-shell">
            <video
            class="media media-video"
            muted
            playsinline
            preload="metadata"
            :src="post.mediaUrl"
            @click="toggleFeedVideo"
            @loadedmetadata="syncVideoState"
            @timeupdate="syncVideoState"
            @play="syncVideoState"
            @pause="syncVideoState"
            @volumechange="syncVideoState"
          ></video>
          <div class="video-controls" @click.stop>
            <button class="video-control-btn" type="button" :aria-label="videoState.paused ? '播放' : '暂停'" @click="toggleFeedVideo">
              <Play v-if="videoState.paused" :size="15" />
              <Pause v-else :size="15" />
            </button>
            <span class="video-time">{{ videoTimeLabel }}</span>
            <input
              class="video-progress"
              type="range"
              min="0"
              max="100"
              step="0.1"
              :value="videoState.progress"
              aria-label="视频进度"
              @input="seekFeedVideo"
            />
            <button class="video-control-btn" type="button" :aria-label="videoState.muted ? '打开声音' : '静音'" @click="toggleFeedMute">
              <VolumeX v-if="videoState.muted" :size="15" />
              <Volume2 v-else :size="15" />
            </button>
            <input
              class="video-volume"
              type="range"
              min="0"
              max="1"
              step="0.01"
              :value="videoState.muted ? 0 : videoState.volume"
              aria-label="Video volume"
              @input="setFeedVolume"
            />
            <button class="video-control-btn" type="button" aria-label="放大观看" @click="openMediaPreview('video', post.mediaUrl)">
              <Maximize2 :size="15" />
            </button>
            </div>
          </div>
        </div>

        <button v-if="post.repostId && post.originalContent" class="quote" @click="openOriginalPost">
          <ArrowRightLeft :size="14" />
          <span>转发自：{{ post.originalContent }}</span>
        </button>

        <footer class="post-actions">
          <div class="action-group">
            <button class="action like-action" :class="{ active: post.liked }" @click="$emit('toggle-like', post)">
              <ThumbsUp :size="14" />
              <span>{{ post.likeCount || 0 }}</span>
            </button>
            <button class="action comment-action" :class="{ active: post.showComments }" @click="$emit('toggle-comments', post)">
              <MessageCircle :size="14" />
              <span>{{ post.commentCount || 0 }}</span>
            </button>
            <button class="action repost-action" :class="{ active: post.reposted }" @click="requestRepost">
              <Repeat2 :size="14" />
              <span>{{ post.repostCount || 0 }}</span>
            </button>
          </div>
          <button
            class="action favorite-action"
            :class="post.favorited ? 'active' : ''"
            :title="post.favorited ? '取消收藏' : '收藏'"
            :aria-label="post.favorited ? '取消收藏' : '收藏'"
            @click="$emit('toggle-favorite', post)"
          >
            <Bookmark :size="14" />
          </button>
        </footer>

        <div class="comments" v-if="post.showComments">
          <div class="comment-toolbar">
            <select v-model="post.commentSort" @change="$emit('load-comments', post)">
              <option value="time_desc">最新评论</option>
              <option value="time_asc">最早评论</option>
              <option value="hot">热门评论</option>
            </select>
          </div>

          <div v-if="post.commentsLoading" class="hint">评论加载中</div>
          <template v-else>
            <div v-if="post.comments.length === 0" class="hint">暂无评论</div>
            <div class="comment-item" v-for="comment in post.comments" :key="comment.id">
              <div class="comment-meta">
                <strong>{{ comment.nickname || comment.username }}</strong>
                <span>{{ formatTime(comment.createdAt) }}</span>
                <button class="tiny-action" :class="comment.liked ? 'liked' : ''" @click="$emit('toggle-comment-like', { post, comment })">
                  <ThumbsUp :size="12" />
                  {{ comment.likeCount || 0 }}
                </button>
                <button v-if="comment.userId === currentUserId" class="tiny-action danger" @click="$emit('delete-comment', { post, commentId: comment.id })">
                  删除
                </button>
              </div>
              <p>{{ comment.content }}</p>
            </div>
          </template>

          <div class="comment-editor" v-if="isLoggedIn">
            <button
              class="comment-sticker-trigger"
              type="button"
              title="插入表情"
              aria-label="插入表情"
              @click="showCommentStickers = !showCommentStickers"
            >
              <Smile :size="15" />
            </button>
            <input v-model="post.newComment" placeholder="请输入评论内容" @keyup.enter="submitCurrentComment" />
            <button class="comment-send" @click="submitCurrentComment">发送</button>
          </div>
          <div class="comment-sticker-panel" v-if="isLoggedIn && showCommentStickers" aria-label="评论常用表情">
            <button
              v-for="sticker in commentStickers"
              :key="sticker.key"
              class="comment-sticker"
              type="button"
              :title="sticker.label"
              @click="insertCommentSticker(sticker.value)"
            >
              {{ sticker.value }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <teleport to="body">
      <div class="media-overlay" v-if="mediaPreview.show" @click.self="closeMediaPreview">
        <article class="media-dialog">
          <header>
            <button class="close-preview-btn" type="button" aria-label="关闭预览" @click="closeMediaPreview">
              <X :size="18" />
            </button>
          </header>
          <img v-if="mediaPreview.type === 'image'" :src="mediaPreview.url" alt="preview" />
          <video v-else controls autoplay playsinline :src="mediaPreview.url"></video>
        </article>
      </div>
    </teleport>
  </article>
</template>

<script setup>
import {
  ArrowRightLeft,
  Bookmark,
  Maximize2,
  MessageCircle,
  MoreHorizontal,
  Pause,
  Play,
  Repeat2,
  Smile,
  ThumbsUp,
  Trash2,
  Volume2,
  VolumeX,
  X
} from 'lucide-vue-next'
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { normalizeMediaUrls } from '@/utils/post-utils'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  currentUserId: {
    type: Number,
    default: null
  },
  isLoggedIn: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits([
  'delete-post',
  'toggle-like',
  'toggle-comments',
  'toggle-favorite',
  'toggle-follow',
  'repost',
  'load-comments',
  'submit-comment',
  'delete-comment',
  'toggle-comment-like'
])

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="%23d8dde6" width="80" height="80"/><circle fill="%239aa3b3" cx="40" cy="30" r="12"/><rect fill="%239aa3b3" x="20" y="48" width="40" height="18" rx="9"/></svg>'

const canDelete = computed(() => props.currentUserId && props.post.userId === props.currentUserId)
const showFollow = computed(() => props.currentUserId && props.post.userId !== props.currentUserId)
const router = useRouter()
const mediaImageUrls = computed(() => normalizeMediaUrls(props.post.mediaUrls?.length ? props.post.mediaUrls : props.post.mediaUrl))
const visibleImageUrls = computed(() => mediaImageUrls.value.slice(0, 9))
const mediaGalleryClass = computed(() => {
  const count = visibleImageUrls.value.length
  if (count <= 1) {
    return 'media-count-1'
  }
  if (count === 2) {
    return 'media-count-2'
  }
  if (count === 3) {
    return 'media-count-3'
  }
  if (count === 4) {
    return 'media-count-4'
  }
  return 'media-count-many'
})
const mediaPreview = ref({
  show: false,
  type: 'image',
  url: ''
})
const showPostMenu = ref(false)
const showCommentStickers = ref(false)
const postCardRef = ref(null)
const videoState = ref({
  paused: true,
  muted: true,
  progress: 0,
  duration: 0,
  currentTime: 0,
  volume: 0
})
const commentStickers = [
  { key: 'smile', label: '微笑', value: '🙂' },
  { key: 'laugh', label: '大笑', value: '😃' },
  { key: 'wink', label: '眨眼', value: '😉' },
  { key: 'cry', label: '流泪', value: '😢' },
  { key: 'angry', label: '生气', value: '😠' },
  { key: 'ok', label: 'OK', value: '👌' },
  { key: 'heart', label: '爱心', value: '❤️' },
  { key: 'spark', label: '赞同', value: '✨' },
  { key: 'clap', label: '鼓掌', value: '👏' },
  { key: 'bye', label: '拜拜', value: '👋' },
  { key: 'thinking', label: '思考', value: '🤔' },
  { key: 'surprise', label: '惊讶', value: '😮' }
]

const formatVideoTime = (value) => {
  const totalSeconds = Number.isFinite(value) && value >= 0 ? Math.floor(value) : 0
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${minutes}:${String(seconds).padStart(2, '0')}`
}

const videoTimeLabel = computed(() => `${formatVideoTime(videoState.value.currentTime)} / ${formatVideoTime(videoState.value.duration)}`)

const openProfile = () => {
  if (!props.post.userId) return
  router.push(`/profile/${props.post.userId}`)
}

const openOriginalPost = () => {
  if (!props.post.repostId) {
    return
  }
  router.push(`/post/${props.post.repostId}`)
}

const requestDeletePost = () => {
  showPostMenu.value = false
  emit('delete-post', props.post.id)
}

const requestRepost = () => {
  emit('repost', props.post)
}

const getFeedVideo = (event) => event.currentTarget?.closest?.('.video-shell')?.querySelector('video')

const syncVideoElementState = (video) => {
  if (!video) {
    return
  }
  const duration = Number.isFinite(video.duration) && video.duration > 0 ? video.duration : 0
  const currentTime = Number.isFinite(video.currentTime) && video.currentTime > 0 ? video.currentTime : 0
  const volume = Number.isFinite(video.volume) ? video.volume : 0
  videoState.value = {
    paused: video.paused,
    muted: video.muted || volume <= 0,
    progress: duration ? Math.min(100, (currentTime / duration) * 100) : 0,
    duration,
    currentTime,
    volume
  }
}

const syncVideoState = (event) => {
  syncVideoElementState(event.currentTarget)
}

const toggleFeedVideo = async (event) => {
  const video = event.currentTarget?.tagName === 'VIDEO' ? event.currentTarget : getFeedVideo(event)
  if (!video) {
    return
  }
  if (video.paused) {
    await video.play().catch(() => {})
  } else {
    video.pause()
  }
  syncVideoElementState(video)
}

const seekFeedVideo = (event) => {
  const video = getFeedVideo(event)
  if (!video || !Number.isFinite(video.duration) || video.duration <= 0) {
    return
  }
  video.currentTime = (Number(event.target.value) / 100) * video.duration
  syncVideoElementState(video)
}

const toggleFeedMute = (event) => {
  const video = getFeedVideo(event)
  if (!video) {
    return
  }
  if (video.muted || video.volume <= 0) {
    video.muted = false
    if (video.volume <= 0) {
      video.volume = 0.65
    }
  } else {
    video.muted = true
  }
  syncVideoElementState(video)
}

const setFeedVolume = (event) => {
  const video = getFeedVideo(event)
  if (!video) {
    return
  }
  const nextVolume = Math.min(1, Math.max(0, Number(event.target.value)))
  video.volume = nextVolume
  video.muted = nextVolume <= 0
  syncVideoElementState(video)
}

const insertCommentSticker = (value) => {
  props.post.newComment = `${props.post.newComment || ''}${value}`
}

const closeCommentStickersOnOutsidePointer = (event) => {
  const root = postCardRef.value
  const target = event.target
  if (!(target instanceof Element) || !root) {
    showCommentStickers.value = false
    return
  }

  const panel = root.querySelector('.comment-sticker-panel')
  const trigger = root.querySelector('.comment-sticker-trigger')
  if (panel?.contains(target) || trigger?.contains(target)) {
    return
  }
  showCommentStickers.value = false
}

const closePostMenuOnOutsidePointer = (event) => {
  const root = postCardRef.value
  const target = event.target
  if (!(target instanceof Element) || !root) {
    showPostMenu.value = false
    return
  }

  const menu = root.querySelector('.post-menu')
  if (menu?.contains(target)) {
    return
  }
  showPostMenu.value = false
}

const submitCurrentComment = () => {
  showCommentStickers.value = false
  emit('submit-comment', props.post)
}

const openMediaPreview = (type, url) => {
  if (!url) {
    return
  }
  mediaPreview.value = {
    show: true,
    type,
    url
  }
}

const closeMediaPreview = () => {
  mediaPreview.value = {
    show: false,
    type: 'image',
    url: ''
  }
}

watch(showCommentStickers, (open) => {
  const action = open ? 'addEventListener' : 'removeEventListener'
  document[action]('pointerdown', closeCommentStickersOnOutsidePointer, true)
})

watch(showPostMenu, (open) => {
  const action = open ? 'addEventListener' : 'removeEventListener'
  document[action]('pointerdown', closePostMenuOnOutsidePointer, true)
})

watch(
  () => props.post.showComments,
  (open) => {
    if (!open) {
      showCommentStickers.value = false
    }
  }
)

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', closeCommentStickersOnOutsidePointer, true)
  document.removeEventListener('pointerdown', closePostMenuOnOutsidePointer, true)
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', { hour12: false })
}
</script>

<style scoped>
.post-card {
  border: 0;
  border-bottom: 1px solid var(--line);
  border-radius: 0;
  background: var(--paper);
  padding: 12px 16px;
  box-shadow: none;
  transition: background-color 0.16s ease;
  content-visibility: auto;
  contain-intrinsic-size: 300px;
  cursor: default;
}

.post-card:hover {
  background: color-mix(in srgb, var(--surface) 42%, transparent);
}

.post-grid {
  display: grid;
  grid-template-columns: 40px minmax(0, 1fr);
  column-gap: 12px;
  align-items: start;
  cursor: default;
}

.avatar-rail {
  grid-column: 1;
  align-self: start;
  min-height: 100%;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.post-main {
  grid-column: 2;
  display: grid;
  gap: 6px;
  min-width: 0;
  cursor: default;
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  cursor: default;
}

.identity {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  min-width: 0;
}

.identity h3 {
  margin: 0;
  max-width: min(260px, 48vw);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.96rem;
  font-weight: 700;
  line-height: 1.25;
}

.meta-inline {
  margin: 0;
  color: var(--muted);
  font-size: 0.92rem;
  font-weight: 400;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}

.dot {
  opacity: 0.8;
}

.head-actions {
  display: flex;
  gap: 8px;
  flex: 0 0 auto;
}

.head-follow {
  border: 1px solid var(--ink);
  background: var(--ink);
  border-radius: 999px;
  padding: 5px 13px;
  color: var(--paper);
  font-size: 0.8rem;
  font-weight: 700;
  cursor: pointer;
  transition: filter 0.16s ease;
}

.head-follow:hover {
  filter: brightness(0.92);
}

.clickable {
  cursor: pointer;
}

.post-menu {
  position: relative;
  padding: 2px;
  margin: -2px;
}

.more-btn {
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: var(--muted);
  cursor: pointer;
  display: grid;
  place-items: center;
  transition: background-color 0.16s ease, color 0.16s ease;
}

.more-btn:hover,
.more-btn:focus-visible {
  background: color-mix(in srgb, #1d9bf0 12%, transparent);
  color: #1d9bf0;
}

.post-menu-popover {
  position: absolute;
  top: 40px;
  right: 0;
  z-index: 6;
  min-width: 128px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--paper);
  box-shadow: 0 14px 34px color-mix(in srgb, #0f172a 16%, transparent);
  padding: 6px;
}

.post-menu-popover::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 0;
  right: 0;
  height: 4px;
}

.delete-menu-item {
  width: 100%;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--error);
  min-height: 42px;
  padding: 10px 12px;
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font: inherit;
  font-size: 0.88rem;
  font-weight: 700;
  text-align: left;
}

.delete-menu-item:hover {
  background: color-mix(in srgb, var(--error) 10%, transparent);
}

.post-content {
  margin: 1px 0 0;
  color: var(--ink);
  font-size: 0.96rem;
  line-height: 1.45;
  white-space: pre-wrap;
  cursor: text;
}

.media {
  display: block;
  max-width: 100%;
  border-radius: 16px;
}

.media-image {
  width: auto;
  height: auto;
  max-width: min(100%, 520px);
  max-height: min(68vh, 560px);
  object-fit: contain;
  background: transparent;
}

.media-video {
  width: min(100%, 520px);
  height: auto;
  max-height: min(68vh, 560px);
  object-fit: contain;
  background: transparent;
  cursor: default;
}

.media-wrap {
  position: relative;
  display: inline-grid;
  justify-items: start;
  align-items: start;
  justify-self: start;
  width: fit-content;
  max-width: 100%;
  margin-top: 6px;
  overflow: visible;
  border: 0;
  border-radius: 16px;
  background: transparent;
  max-height: min(68vh, 560px);
  overflow: hidden;
}

.media-gallery-wrap {
  display: grid;
  width: min(100%, 520px);
  max-height: none;
  gap: 2px;
  background: transparent;
}

.media-gallery-wrap.media-count-1 {
  display: inline-grid;
  width: fit-content;
  max-width: 100%;
}

.media-gallery-wrap.media-count-2,
.media-gallery-wrap.media-count-4 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.media-gallery-wrap.media-count-3 {
  grid-template-columns: 1.1fr 0.9fr;
  grid-template-rows: repeat(2, minmax(0, 1fr));
}

.media-gallery-wrap.media-count-3 .media-open-surface:first-child {
  grid-row: 1 / span 2;
}

.media-gallery-wrap.media-count-many {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.media-gallery-wrap .media-open-surface {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 148px;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--line) 78%, transparent);
  border-radius: 0;
  background: color-mix(in srgb, var(--surface) 88%, var(--paper));
}

.media-gallery-wrap.media-count-1 .media-open-surface {
  min-height: auto;
  width: fit-content;
  border: 0;
  border-radius: 16px;
  background: transparent;
}

.media-gallery-wrap .media-image {
  width: 100%;
  height: 100%;
  max-width: none;
  max-height: none;
  object-fit: cover;
}

.media-gallery-wrap.media-count-1 .media-image {
  width: auto;
  height: auto;
  max-width: min(100%, 520px);
  max-height: min(68vh, 560px);
  object-fit: contain;
}

.video-shell {
  position: relative;
  display: block;
  max-width: 100%;
  border-radius: 16px;
  overflow: hidden;
  line-height: 0;
}

.media-open-surface {
  display: block;
  max-width: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: default;
}

.video-controls {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 12px 10px 8px;
  border-radius: 0 0 16px 16px;
  background: linear-gradient(
    to top,
    color-mix(in srgb, #111827 82%, transparent),
    color-mix(in srgb, #111827 48%, transparent) 58%,
    transparent
  );
  color: #f8fafc;
  opacity: 0;
  pointer-events: none;
  transform: translateY(6px);
  transition: opacity 0.16s ease, transform 0.16s ease;
}

.video-shell:hover .video-controls {
  opacity: 1;
  pointer-events: auto;
  transform: translateY(0);
}

.video-control-btn {
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 999px;
  background: color-mix(in srgb, #ffffff 12%, transparent);
  color: #f8fafc;
  display: grid;
  place-items: center;
  cursor: pointer;
  flex: 0 0 auto;
}

.video-control-btn:hover {
  background: color-mix(in srgb, #ffffff 22%, transparent);
}

.video-progress {
  order: -1;
  flex: 1 0 100%;
  width: 100%;
  height: 4px;
  accent-color: #f8fafc;
  cursor: pointer;
  margin: 0;
}

.video-time {
  min-width: 84px;
  color: #f8fafc;
  font-size: 0.74rem;
  font-variant-numeric: tabular-nums;
  white-space: nowrap;
}

.video-volume {
  width: 72px;
  max-width: 20vw;
  height: 4px;
  accent-color: #f8fafc;
  cursor: pointer;
  margin: 0;
}

.video-progress::-webkit-slider-runnable-track {
  height: 4px;
  background: color-mix(in srgb, #f8fafc 28%, transparent);
}

.video-volume::-webkit-slider-runnable-track {
  height: 4px;
  background: color-mix(in srgb, #f8fafc 36%, transparent);
}

.video-progress::-webkit-slider-thumb {
  margin-top: -5px;
}

.video-volume::-webkit-slider-thumb {
  margin-top: -5px;
}

.quote {
  width: min(100%, 520px);
  display: inline-flex;
  gap: 6px;
  align-items: center;
  border: 1px solid var(--line);
  border-radius: 14px;
  background: transparent;
  color: var(--muted);
  padding: 8px 10px;
  text-align: left;
  cursor: pointer;
}

.post-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  width: 100%;
  margin-top: 4px;
  color: var(--muted);
  cursor: default;
}

.action-group {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: clamp(14px, 6vw, 54px);
  min-width: 0;
}

.action {
  border: 0;
  background: transparent;
  border-radius: 999px;
  min-width: 34px;
  height: 32px;
  padding: 0 8px;
  color: var(--muted);
  display: inline-flex;
  gap: 6px;
  align-items: center;
  font-size: 0.82rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.16s ease, color 0.16s ease, transform 0.16s ease;
}

.action:hover {
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  color: var(--accent);
}

.action:active {
  transform: scale(0.96);
}

.action.active {
  background: transparent;
  color: var(--accent);
}

.like-action:hover,
.like-action.active {
  background: color-mix(in srgb, #f91880 12%, transparent);
  color: #f91880;
}

.comment-action:hover,
.comment-action.active {
  background: color-mix(in srgb, #1d9bf0 12%, transparent);
  color: #1d9bf0;
}

.repost-action:hover,
.repost-action.active {
  background: color-mix(in srgb, #00ba7c 12%, transparent);
  color: #00a86f;
}

.favorite-action:hover,
.favorite-action.active {
  background: color-mix(in srgb, #7856ff 12%, transparent);
  color: #7856ff;
}

.favorite-action {
  width: 32px;
  min-width: 32px;
  padding: 0;
  justify-content: center;
  flex: 0 0 auto;
}

.comments {
  border-top: 1px solid var(--line);
  padding-top: 12px;
}

.comment-toolbar {
  display: flex;
  justify-content: flex-end;
}

.comment-toolbar select {
  border: 1px solid var(--line);
  border-radius: 8px;
  padding: 6px;
  background: transparent;
  color: var(--ink);
}

.comment-item {
  padding: 10px 0;
  border-bottom: 1px solid color-mix(in srgb, var(--line) 70%, transparent);
}

.comment-meta {
  display: flex;
  align-items: center;
  gap: 7px;
  color: var(--muted);
  font-size: 0.82rem;
}

.comment-meta strong {
  color: var(--ink);
}

.tiny-action {
  border: 1px solid var(--line);
  background: transparent;
  color: var(--muted);
  border-radius: 999px;
  display: inline-flex;
  gap: 4px;
  align-items: center;
  font-size: 0.76rem;
  padding: 2px 7px;
  cursor: pointer;
}

.tiny-action.liked {
  color: var(--ink);
}

.tiny-action.danger {
  color: var(--error);
}

.comment-editor {
  display: grid;
  grid-template-columns: auto 1fr auto;
  gap: 8px;
  margin-top: 10px;
}

.comment-sticker-trigger {
  width: 38px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: transparent;
  color: var(--muted);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.comment-sticker-trigger:hover {
  color: var(--ink);
  background: color-mix(in srgb, var(--surface) 74%, var(--line));
}

.comment-editor input {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: transparent;
  color: var(--ink);
  padding: 8px 10px;
  font: inherit;
}

.comment-send {
  border: 1px solid var(--accent);
  border-radius: 10px;
  background: var(--accent);
  color: var(--paper);
  font-weight: 700;
  padding: 0 12px;
}

.comment-sticker-panel {
  width: min(320px, 100%);
  margin-top: 8px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: color-mix(in srgb, var(--surface) 92%, var(--paper));
  padding: 8px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 6px;
}

.comment-sticker {
  min-height: 34px;
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--paper);
  color: var(--ink);
  font-size: 1.05rem;
  cursor: pointer;
}

.comment-sticker:hover {
  background: var(--surface);
  border-color: color-mix(in srgb, var(--line) 65%, var(--muted));
}

.hint {
  text-align: center;
  color: var(--muted);
}

.media-overlay {
  position: fixed;
  inset: 0;
  z-index: 1200;
  background: rgba(17, 20, 26, 0.86);
  display: grid;
  place-items: center;
  padding: clamp(10px, 2.2vw, 24px);
}

.media-dialog {
  width: fit-content;
  max-width: min(96vw, 1400px);
  max-height: min(92vh, 920px);
  border: 0;
  border-radius: 0;
  background: transparent;
  display: grid;
  grid-template-rows: auto auto;
  gap: 10px;
  padding: 0;
  justify-items: center;
}

.media-dialog header {
  display: flex;
  justify-content: flex-end;
}

.close-preview-btn {
  border: 1px solid color-mix(in srgb, #d9e0ea 38%, transparent);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  color: #d9e0ea;
  width: 36px;
  height: 36px;
  padding: 0;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.media-dialog img,
.media-dialog video {
  width: auto;
  max-width: 100%;
  height: auto;
  max-height: min(calc(92vh - 64px), 820px);
  margin: 0 auto;
  object-fit: contain;
  border-radius: 12px;
}

.media-dialog img {
  background: transparent;
}

.media-dialog video {
  background: #171a20;
}

@media (max-width: 560px) {
  .post-grid {
    column-gap: 10px;
  }

  .post-actions {
    gap: 8px;
  }

  .action-group {
    gap: 6px;
  }

  .action {
    min-width: 30px;
    padding: 0 6px;
  }

  .media-wrap {
    justify-self: stretch;
  }

  .media-image,
  .media-video {
    width: 100%;
    max-width: 100%;
  }

  .media-gallery-wrap {
    width: 100%;
  }

  .video-time {
    min-width: 72px;
    font-size: 0.7rem;
  }

  .video-volume {
    width: 56px;
  }
}
</style>
