<template>
  <section class="publish-page">
    <article class="panel">
      <h2>发布动态</h2>
      <textarea ref="editorRef" v-model="content" rows="6" placeholder="请输入动态内容" />

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
          <input ref="mediaInputRef" type="file" :accept="mediaAccept" @change="onMedia" />
        </label>
      </div>

      <div class="sticker-panel" v-if="showStickers" aria-label="微博微信常用表情">
        <div class="sticker-head">
          <h3>常用表情</h3>
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

      <img v-if="previewImage" class="preview" :src="previewImage" alt="preview image" />
      <video v-if="previewVideo" class="preview" controls :src="previewVideo"></video>

      <div class="actions">
        <button class="btn" @click="clearMedia" v-if="mediaType">移除媒体</button>
        <button class="btn primary" :disabled="publishing" @click="publish">{{ publishing ? '发送中' : '发布' }}</button>
      </div>
    </article>
  </section>
</template>

<script setup>
import { ImagePlus, Smile } from 'lucide-vue-next'
import { inject, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, uploadImage, uploadVideo } from '@/api'

const router = useRouter()
const showToast = inject('showToast')

const content = ref('')
const mediaUrl = ref('')
const mediaType = ref('')
const previewImage = ref('')
const previewVideo = ref('')
const publishing = ref(false)
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
  { key: 'thumb', label: '赞', value: '👍' },
  { key: 'clap', label: '鼓掌', value: '👏' },
  { key: 'bye', label: '拜拜', value: '👋' },
  { key: 'kiss', label: '亲亲', value: '😘' },
  { key: 'thinking', label: '思考', value: '🤔' },
  { key: 'surprise', label: '吃惊', value: '😮' },
  { key: 'sleepy', label: '困', value: '😴' },
  { key: 'cool', label: '酷', value: '😎' },
  { key: 'fire', label: '火', value: '🔥' },
  { key: 'party', label: '庆祝', value: '🎉' },
  { key: 'hug', label: '抱抱', value: '🤗' }
]

const revokePreviewUrl = (url) => {
  if (url) {
    URL.revokeObjectURL(url)
  }
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
    content.value = `${content.value}${content.value ? ' ' : ''}${value}`
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

const onMedia = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  const safeImageTypes = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif'])
  const safeVideoTypes = new Set(['video/mp4', 'video/webm', 'video/quicktime'])
  const isVideo = safeVideoTypes.has(file.type)
  const isImage = safeImageTypes.has(file.type)
  if (!isVideo && !isImage) {
    event.target.value = ''
    showToast('仅支持 JPG、PNG、WebP、GIF、MP4、WebM 或 MOV', 'error')
    return
  }

  if (isImage && file.size > 5 * 1024 * 1024) {
    event.target.value = ''
    showToast('图片不能超过 5MB', 'error')
    return
  }
  if (isVideo && file.size > 50 * 1024 * 1024) {
    event.target.value = ''
    showToast('视频不能超过 50MB', 'error')
    return
  }

  try {
    const uploader = isVideo ? uploadVideo : uploadImage
    const res = await uploader(file)
    if (res.code === 200) {
      revokePreviewUrl(previewImage.value)
      revokePreviewUrl(previewVideo.value)
      mediaUrl.value = res.data.url
      mediaType.value = isVideo ? 'video' : 'image'
      if (isVideo) {
        previewVideo.value = URL.createObjectURL(file)
        previewImage.value = ''
      } else {
        previewImage.value = URL.createObjectURL(file)
        previewVideo.value = ''
      }
    } else {
      showToast(res.message || '媒体上传失败，请稍后重试', 'error')
    }
  } catch {
    showToast('媒体上传失败，请稍后重试', 'error')
  } finally {
    event.target.value = ''
  }
}

const clearMedia = () => {
  revokePreviewUrl(previewImage.value)
  revokePreviewUrl(previewVideo.value)
  mediaUrl.value = ''
  mediaType.value = ''
  previewImage.value = ''
  previewVideo.value = ''
}

const publish = async () => {
  if (!content.value.trim() && !mediaUrl.value) {
    showToast('请输入内容或添加媒体后发布', 'error')
    return
  }
  publishing.value = true
  try {
    const res = await createPost({
      content: content.value,
      mediaUrl: mediaUrl.value,
      mediaType: mediaType.value
    })
    if (res.code === 200) {
      showToast('发布成功')
      router.push('/')
    } else {
      showToast(res.message || '发布失败，请稍后重试', 'error')
    }
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
  margin-top: 0;
}

textarea {
  width: 100%;
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 10px;
  font: inherit;
  resize: vertical;
  background: transparent;
  color: var(--ink);
}

.toolbar-row {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  position: relative;
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

@media (max-width: 560px) {
  .sticker-grid {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}

.preview {
  width: auto;
  max-width: min(100%, 520px);
  margin-top: 12px;
  border-radius: 12px;
  max-height: 420px;
  object-fit: contain;
}

.actions {
  margin-top: 12px;
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

.btn.primary {
  border-color: var(--publish);
  background: var(--publish);
  color: var(--on-publish);
}
</style>
