<template>
  <section class="publish-page">
    <article class="panel">
      <h2>发布动态</h2>
      <textarea ref="editorRef" v-model="content" rows="6" placeholder="分享你的想法..." />

      <div class="toolbar-row">
        <button class="toolbar-btn" type="button" :class="showStickers ? 'active' : ''" title="插入表情" @click="showStickers = !showStickers">
          <Smile :size="16" />
        </button>
        <label class="toolbar-btn media-picker" title="添加图片或视频">
          <ImagePlus :size="16" />
          <input type="file" accept="image/*,video/*" @change="onMedia" />
        </label>
      </div>

      <div class="sticker-panel" v-if="showStickers">
        <div class="sticker-head">
          <h3>微博微信常用表情</h3>
        </div>
        <div class="sticker-grid">
          <button
            v-for="sticker in stickers"
            :key="sticker.key"
            class="sticker-item"
            type="button"
            @click="insertSticker(sticker.value)"
          >
            <span class="sticker-face">{{ sticker.value }}</span>
            <span class="sticker-label">{{ sticker.label }}</span>
          </button>
        </div>
      </div>

      <img v-if="previewImage" class="preview" :src="previewImage" alt="preview image" />
      <video v-if="previewVideo" class="preview" controls :src="previewVideo"></video>

      <div class="actions">
        <button class="btn" @click="clearMedia" v-if="mediaType">清除媒体</button>
        <button class="btn primary" :disabled="publishing" @click="publish">{{ publishing ? '发布中...' : '立即发布' }}</button>
      </div>
    </article>
  </section>
</template>

<script setup>
import { ImagePlus, Smile } from 'lucide-vue-next'
import { inject, ref } from 'vue'
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
const stickers = [
  { key: 'smile', label: '微笑', value: '[微笑]' },
  { key: 'laugh', label: '偷笑', value: '[偷笑]' },
  { key: 'wink', label: '眨眼', value: '[眨眼]' },
  { key: 'cry', label: '流泪', value: '[流泪]' },
  { key: 'angry', label: '生气', value: '[生气]' },
  { key: 'ok', label: 'OK', value: '[OK]' },
  { key: 'heart', label: '爱心', value: '[爱心]' },
  { key: 'thumb', label: '赞', value: '[赞]' },
  { key: 'clap', label: '鼓掌', value: '[鼓掌]' },
  { key: 'bye', label: '拜拜', value: '[拜拜]' }
]

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

const onMedia = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  const isVideo = file.type.startsWith('video/')
  const isImage = file.type.startsWith('image/')
  if (!isVideo && !isImage) {
    showToast('仅支持图片或视频', 'error')
    return
  }

  if (isImage && file.size > 5 * 1024 * 1024) {
    showToast('图片不能超过 5MB', 'error')
    return
  }
  if (isVideo && file.size > 50 * 1024 * 1024) {
    showToast('视频不能超过 50MB', 'error')
    return
  }

  try {
    const uploader = isVideo ? uploadVideo : uploadImage
    const res = await uploader(file)
    if (res.code === 200) {
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
      showToast(res.message || '媒体上传失败', 'error')
    }
  } catch {
    showToast('媒体上传失败', 'error')
  }
}

const clearMedia = () => {
  mediaUrl.value = ''
  mediaType.value = ''
  previewImage.value = ''
  previewVideo.value = ''
}

const publish = async () => {
  if (!content.value.trim() && !mediaUrl.value) {
    showToast('内容和媒体不能同时为空', 'error')
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
      showToast(res.message || '发布失败', 'error')
    }
  } catch {
    showToast('发布失败', 'error')
  } finally {
    publishing.value = false
  }
}
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
  border-color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, var(--surface));
}

.media-picker input {
  display: none;
}

.sticker-panel {
  margin-top: 12px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: color-mix(in srgb, var(--surface) 90%, var(--paper) 10%);
  padding: 10px;
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
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(92px, 1fr));
  gap: 8px;
}

.sticker-item {
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--paper);
  color: var(--ink);
  padding: 7px 6px;
  display: grid;
  gap: 2px;
  place-items: center;
  cursor: pointer;
}

.sticker-face {
  font-size: 1.02rem;
}

.sticker-label {
  color: var(--muted);
  font-size: 0.76rem;
}

.preview {
  width: 100%;
  margin-top: 12px;
  border-radius: 12px;
  max-height: 420px;
  object-fit: cover;
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
  border-color: var(--accent);
  background: var(--accent);
  color: var(--paper);
}
</style>
