<template>
  <section class="publish-page">
    <article class="panel">
      <h2>发布动态</h2>
      <textarea
        ref="editorRef"
        v-model="content"
        rows="6"
        placeholder="说点什么，或配上图片一起发出"
      />

      <div class="toolbar-row" aria-label="发布工具">
        <button
          class="toolbar-btn sticker-trigger"
          type="button"
          :class="showStickers ? 'active' : ''"
          title="插入表情"
          aria-label="插入表情"
          :aria-expanded="showStickers"
          @click="showStickers = !showStickers"
        >
          <Smile :size="16" />
        </button>

        <label
          class="toolbar-btn media-picker"
          title="添加图片或视频"
          aria-label="添加图片或视频"
          role="button"
          tabindex="0"
          @keydown.enter.prevent="openMediaPicker"
          @keydown.space.prevent="openMediaPicker"
        >
          <ImagePlus :size="16" />
          <input
            ref="mediaInputRef"
            type="file"
            :accept="mediaAccept"
            :multiple="allowMultipleImageSelection"
            @change="onMedia"
          />
        </label>

        <span v-if="imageUploads.length" class="media-hint">已添加 {{ imageUploads.length }}/9 张图片</span>
        <span v-else-if="previewVideo" class="media-hint">已添加 1 个视频</span>
      </div>

      <div v-if="showStickers" class="sticker-panel" aria-label="微博微信常用表情">
        <div class="sticker-head">
          <h3>微博微信常用表情</h3>
        </div>
        <div class="sticker-grid">
          <button
            v-for="sticker in stickers"
            :key="sticker.key"
            class="sticker-item"
            type="button"
            :title="sticker.label"
            @click="insertSticker(sticker.value)"
          >
            <span class="sticker-face">{{ sticker.value }}</span>
          </button>
        </div>
      </div>

      <div v-if="imageUploads.length" class="preview-grid" :class="previewGridClass">
        <figure v-for="(item, index) in imageUploads" :key="`${item.url}-${index}`" class="preview-tile">
          <img class="preview-image" :src="item.previewUrl" :alt="item.name || `image-${index + 1}`" />
          <button class="remove-preview" type="button" aria-label="移除图片" @click="removeImage(index)">
            <X :size="14" />
          </button>
        </figure>
      </div>

      <div v-if="previewVideo" class="video-preview-shell">
        <video class="preview-video" controls :src="previewVideo"></video>
        <button class="remove-preview video-remove" type="button" aria-label="移除视频" @click="clearMedia">
          <X :size="14" />
        </button>
      </div>

      <div class="actions">
        <button v-if="hasMedia" class="btn" type="button" @click="clearMedia">移除媒体</button>
        <button class="btn primary" type="button" :disabled="publishing || mediaUploading" @click="publish">
          {{ mediaUploading ? '上传中' : publishing ? '发布中' : '发布' }}
        </button>
      </div>
    </article>
  </section>
</template>

<script setup>
import { ImagePlus, Smile, X } from 'lucide-vue-next'
import { computed, inject, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, uploadImage, uploadVideo } from '@/api'

const MAX_IMAGE_COUNT = 9
const MAX_IMAGE_SIZE = 5 * 1024 * 1024
const MAX_VIDEO_SIZE = 50 * 1024 * 1024
const safeImageTypes = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif'])
const safeVideoTypes = new Set(['video/mp4', 'video/webm', 'video/quicktime'])

const router = useRouter()
const showToast = inject('showToast')

const content = ref('')
const mediaUrl = ref('')
const mediaType = ref('')
const imageUploads = ref([])
const previewVideo = ref('')
const publishing = ref(false)
const mediaUploading = ref(false)
const showStickers = ref(false)
const editorRef = ref(null)
const mediaInputRef = ref(null)
const mediaAccept = 'image/jpeg,image/png,image/webp,image/gif,video/mp4,video/webm,video/quicktime'
const stickers = [
  { key: 'smile', label: '微笑', value: '🙂' },
  { key: 'laugh', label: '大笑', value: '😄' },
  { key: 'wink', label: '眨眼', value: '😉' },
  { key: 'cry', label: '流泪', value: '😢' },
  { key: 'angry', label: '生气', value: '😠' },
  { key: 'ok', label: 'OK', value: '👌' },
  { key: 'heart', label: '爱心', value: '❤️' },
  { key: 'thumb', label: '点赞', value: '👍' },
  { key: 'clap', label: '鼓掌', value: '👏' },
  { key: 'bye', label: '挥手', value: '👋' },
  { key: 'kiss', label: '亲亲', value: '😙' },
  { key: 'thinking', label: '思考', value: '🤔' },
  { key: 'surprise', label: '惊讶', value: '😮' },
  { key: 'sleepy', label: '困了', value: '😴' },
  { key: 'cool', label: '酷', value: '😎' },
  { key: 'fire', label: '火', value: '🔥' },
  { key: 'party', label: '庆祝', value: '🎉' },
  { key: 'hug', label: '抱抱', value: '🫶' }
]

const hasMedia = computed(() => (mediaType.value === 'image' ? imageUploads.value.length > 0 : !!mediaUrl.value))
const allowMultipleImageSelection = computed(() => mediaType.value !== 'video')
const previewGridClass = computed(() => {
  const count = imageUploads.value.length
  if (count <= 1) return 'count-1'
  if (count === 2) return 'count-2'
  if (count === 3) return 'count-3'
  if (count === 4) return 'count-4'
  return 'count-many'
})

const revokeObjectUrl = (url) => {
  if (url) {
    URL.revokeObjectURL(url)
  }
}

const revokeImagePreviews = (items = imageUploads.value) => {
  items.forEach((item) => revokeObjectUrl(item.previewUrl))
}

const openMediaPicker = () => {
  mediaInputRef.value?.click()
}

const insertSticker = (value) => {
  if (!value) {
    return
  }

  const editor = editorRef.value
  if (!editor) {
    content.value = `${content.value}${value}`
    return
  }

  const start = editor.selectionStart ?? content.value.length
  const end = editor.selectionEnd ?? start
  const before = content.value.slice(0, start)
  const after = content.value.slice(end)
  content.value = `${before}${value}${after}`

  const nextPos = start + value.length
  requestAnimationFrame(() => {
    editor.focus()
    editor.setSelectionRange(nextPos, nextPos)
  })
}

const closeStickersOnOutsidePointer = (event) => {
  if (!showStickers.value) {
    return
  }

  const target = event.target
  if (!(target instanceof Element)) {
    showStickers.value = false
    return
  }

  if (target.closest('.sticker-panel') || target.closest('.sticker-trigger')) {
    return
  }

  showStickers.value = false
}

const clearMedia = () => {
  revokeImagePreviews()
  revokeObjectUrl(previewVideo.value)
  imageUploads.value = []
  mediaUrl.value = ''
  mediaType.value = ''
  previewVideo.value = ''
}

const removeImage = (index) => {
  const removed = imageUploads.value.splice(index, 1)
  revokeImagePreviews(removed)
  if (imageUploads.value.length === 0) {
    mediaType.value = ''
  }
}

const uploadImages = async (files) => {
  if (!files.length) {
    return
  }

  if (mediaType.value === 'video') {
    clearMedia()
  }

  if (imageUploads.value.length + files.length > MAX_IMAGE_COUNT) {
    showToast(`单条动态最多支持 ${MAX_IMAGE_COUNT} 张图片`, 'error')
    return
  }

  for (const file of files) {
    if (file.size > MAX_IMAGE_SIZE) {
      showToast('图片不能超过 5MB', 'error')
      return
    }
  }

  mediaUploading.value = true
  let hasFailure = false

  try {
    for (const file of files) {
      try {
        const res = await uploadImage(file)
        if (res.code !== 200 || !res.data?.url) {
          hasFailure = true
          continue
        }

        imageUploads.value.push({
          url: res.data.url,
          previewUrl: URL.createObjectURL(file),
          name: file.name
        })
      } catch {
        hasFailure = true
      }
    }

    if (imageUploads.value.length) {
      mediaType.value = 'image'
      mediaUrl.value = ''
    }

    if (hasFailure) {
      showToast('部分图片上传失败，请重试', 'error')
    }
  } finally {
    mediaUploading.value = false
  }
}

const uploadSingleVideo = async (file) => {
  if (!file) {
    return
  }

  if (file.size > MAX_VIDEO_SIZE) {
    showToast('视频不能超过 50MB', 'error')
    return
  }

  clearMedia()
  mediaUploading.value = true

  try {
    const res = await uploadVideo(file)
    if (res.code === 200 && res.data?.url) {
      mediaUrl.value = res.data.url
      mediaType.value = 'video'
      previewVideo.value = URL.createObjectURL(file)
      return
    }
    showToast(res.message || '视频上传失败，请稍后重试', 'error')
  } catch {
    showToast('视频上传失败，请稍后重试', 'error')
  } finally {
    mediaUploading.value = false
  }
}

const onMedia = async (event) => {
  const files = Array.from(event.target.files || [])
  if (!files.length) {
    return
  }

  try {
    const imageFiles = files.filter((file) => safeImageTypes.has(file.type))
    const videoFiles = files.filter((file) => safeVideoTypes.has(file.type))
    const invalidCount = files.length - imageFiles.length - videoFiles.length

    if (invalidCount > 0) {
      showToast('仅支持 JPG、PNG、WebP、GIF、MP4、WebM 或 MOV', 'error')
      return
    }

    if (imageFiles.length && videoFiles.length) {
      showToast('图片和视频不能混合添加', 'error')
      return
    }

    if (videoFiles.length > 1) {
      showToast('一次只能添加一个视频', 'error')
      return
    }

    if (videoFiles.length === 1) {
      await uploadSingleVideo(videoFiles[0])
      return
    }

    await uploadImages(imageFiles)
  } finally {
    event.target.value = ''
  }
}

const publish = async () => {
  const trimmedContent = content.value.trim()
  const imageUrls = imageUploads.value.map((item) => item.url)

  if (!trimmedContent && !hasMedia.value) {
    showToast('请输入内容或添加媒体后再发布', 'error')
    return
  }

  publishing.value = true

  try {
    const payload = {
      content: content.value,
      mediaType: mediaType.value
    }

    if (mediaType.value === 'image' && imageUrls.length) {
      payload.mediaUrls = imageUrls
      payload.mediaUrl = imageUrls.length === 1 ? imageUrls[0] : JSON.stringify(imageUrls)
    } else if (mediaType.value === 'video' && mediaUrl.value) {
      payload.mediaUrl = mediaUrl.value
    }

    const res = await createPost(payload)
    if (res.code === 200) {
      showToast('发布成功')
      clearMedia()
      content.value = ''
      router.push('/')
      return
    }

    showToast(res.message || '发布失败，请稍后重试', 'error')
  } catch {
    showToast('发布失败，请稍后重试', 'error')
  } finally {
    publishing.value = false
  }
}

onMounted(() => {
  document.addEventListener('pointerdown', closeStickersOnOutsidePointer, true)
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', closeStickersOnOutsidePointer, true)
  clearMedia()
})
</script>

<style scoped>
.publish-page {
  display: grid;
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

h2 {
  margin: 0 0 12px;
}

textarea {
  width: 100%;
  min-height: 148px;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 12px;
  font: inherit;
  resize: vertical;
  background: transparent;
  color: var(--ink);
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  position: relative;
  flex-wrap: wrap;
}

.toolbar-btn {
  border: 1px solid var(--line);
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--surface);
  color: var(--ink);
  display: grid;
  place-items: center;
  cursor: pointer;
}

.toolbar-btn.active {
  border-color: var(--publish);
  color: var(--publish);
  background: color-mix(in srgb, var(--publish) 10%, var(--surface));
}

.media-picker input {
  display: none;
}

.media-hint {
  color: var(--muted);
  font-size: 0.82rem;
}

.sticker-panel {
  margin-top: 8px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: color-mix(in srgb, var(--surface) 90%, var(--paper) 10%);
  padding: 8px;
  width: min(340px, calc(100vw - 56px));
  max-height: 248px;
  overflow: auto;
}

.sticker-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}

.sticker-head h3 {
  margin: 0;
  font-size: 0.96rem;
}

.sticker-grid {
  margin-top: 8px;
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 6px;
}

.sticker-item {
  border: 1px solid var(--line);
  border-radius: 8px;
  background: var(--paper);
  color: var(--ink);
  width: 100%;
  min-height: 38px;
  padding: 4px;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.sticker-face {
  font-size: 1.2rem;
  line-height: 1;
}

.preview-grid {
  margin-top: 14px;
  display: grid;
  gap: 4px;
  width: min(100%, 560px);
}

.preview-grid.count-1 {
  grid-template-columns: minmax(0, 1fr);
}

.preview-grid.count-2,
.preview-grid.count-4 {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.preview-grid.count-3 {
  grid-template-columns: 1.15fr 0.85fr;
  grid-template-rows: repeat(2, minmax(0, 1fr));
}

.preview-grid.count-3 .preview-tile:first-child {
  grid-row: 1 / span 2;
}

.preview-grid.count-many {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.preview-tile {
  position: relative;
  margin: 0;
  overflow: hidden;
  border-radius: 14px;
  background: color-mix(in srgb, var(--surface) 88%, var(--paper));
  aspect-ratio: 1 / 1;
}

.count-1 .preview-tile {
  aspect-ratio: auto;
}

.preview-image {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.count-1 .preview-image {
  max-height: 460px;
  object-fit: contain;
}

.video-preview-shell {
  position: relative;
  width: min(100%, 560px);
  margin-top: 14px;
  border-radius: 16px;
  overflow: hidden;
  background: color-mix(in srgb, var(--surface) 90%, var(--paper));
}

.preview-video {
  display: block;
  width: 100%;
  max-height: 480px;
}

.remove-preview {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 28px;
  height: 28px;
  border: 0;
  border-radius: 999px;
  background: color-mix(in srgb, #0f172a 72%, transparent);
  color: #f8fafc;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.video-remove {
  z-index: 1;
}

.actions {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn {
  border: 1px solid var(--line);
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  font-weight: 700;
  background: transparent;
  color: var(--ink);
}

.btn:disabled {
  cursor: wait;
  opacity: 0.7;
}

.btn.primary {
  border-color: var(--publish);
  background: var(--publish);
  color: var(--on-publish);
}

@media (max-width: 560px) {
  .sticker-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }

  .preview-grid,
  .video-preview-shell {
    width: 100%;
  }
}
</style>
