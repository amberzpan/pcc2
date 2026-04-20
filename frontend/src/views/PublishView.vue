<template>
  <section class="publish-page">
    <article class="panel">
      <h2>发布动态</h2>
      <textarea v-model="content" rows="6" placeholder="分享你的想法..." />

      <div class="upload-row">
        <label class="upload">
          添加媒体（图片/视频）
          <input type="file" accept="image/*,video/*" @change="onMedia" />
        </label>
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
}

.panel {
  border: 1px solid #e7dccf;
  border-radius: 16px;
  background: #fffdf8;
  padding: 14px;
}

h2 {
  margin-top: 0;
}

textarea {
  width: 100%;
  border: 1px solid #e7dccf;
  border-radius: 12px;
  padding: 10px;
  font: inherit;
  resize: vertical;
}

.upload-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.upload {
  border: 1px dashed #d7c7b3;
  border-radius: 999px;
  padding: 8px 12px;
  cursor: pointer;
  font-weight: 700;
}

.upload input {
  display: none;
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
  border: 1px solid #e7dccf;
  border-radius: 999px;
  padding: 8px 14px;
  cursor: pointer;
  font-weight: 700;
  background: #fff;
}

.btn.primary {
  border-color: #f25a29;
  background: #f25a29;
  color: #fff;
}
</style>
