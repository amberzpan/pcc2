<template>
  <div class="publish-container">
    <div class="publish-card">
      <h2>发布动态</h2>
      <form @submit.prevent="handlePublish">
        <div class="form-group">
          <textarea v-model="content" placeholder="分享你的想法..." rows="6" required></textarea>
        </div>
        <div class="form-group">
          <div class="media-upload">
            <input type="file" id="imageInput" accept="image/*" @change="handleImageSelect" />
            <label for="imageInput" class="upload-label">
              <span v-if="!previewUrl">+ 添加图片</span>
              <span v-else>更换图片</span>
            </label>
            <input type="file" id="videoInput" accept="video/*" @change="handleVideoSelect" />
            <label for="videoInput" class="upload-label video-label">
              <span v-if="!videoPreviewUrl">+ 添加视频</span>
              <span v-else>更换视频</span>
            </label>
            <img v-if="previewUrl" :src="previewUrl" alt="preview" class="media-preview" />
            <video v-if="videoPreviewUrl" :src="videoPreviewUrl" controls class="media-preview"></video>
            <span v-if="previewUrl || videoPreviewUrl" class="remove-media" @click="mediaType === 'image' ? removeImage() : removeVideo()">×</span>
          </div>
        </div>
        <button type="submit" class="btn-primary" :disabled="publishing">
          {{ publishing ? '发布中...' : '发布' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, inject } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, uploadImage, uploadVideo } from '@/api'

const router = useRouter()
const showToast = inject('showToast')

const content = ref('')
const imageUrl = ref('')
const videoUrl = ref('')
const previewUrl = ref('')
const videoPreviewUrl = ref('')
const mediaType = ref('')
const publishing = ref(false)

const handleImageSelect = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  if (file.size > 5 * 1024 * 1024) {
    showToast('图片不能超过5MB', 'error')
    return
  }
  
  previewUrl.value = URL.createObjectURL(file)
  videoPreviewUrl.value = ''
  mediaType.value = 'image'
  
  try {
    const res = await uploadImage(file)
    if (res.code === 200) {
      imageUrl.value = res.data.url
    } else {
      showToast(res.message || '上传失败', 'error')
    }
  } catch (e) {
    showToast('上传失败', 'error')
  }
}

const handleVideoSelect = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  
  if (file.size > 50 * 1024 * 1024) {
    showToast('视频不能超过50MB', 'error')
    return
  }
  
  videoPreviewUrl.value = URL.createObjectURL(file)
  previewUrl.value = ''
  mediaType.value = 'video'
  
  try {
    const res = await uploadVideo(file)
    if (res.code === 200) {
      videoUrl.value = res.data.url
    } else {
      showToast(res.message || '上传失败', 'error')
    }
  } catch (e) {
    showToast('上传失败', 'error')
  }
}

const removeImage = () => {
  imageUrl.value = ''
  previewUrl.value = ''
  mediaType.value = ''
  document.getElementById('imageInput').value = ''
}

const removeVideo = () => {
  videoUrl.value = ''
  videoPreviewUrl.value = ''
  mediaType.value = ''
  document.getElementById('videoInput').value = ''
}

const handlePublish = async () => {
  if (!content.value.trim()) {
    showToast('请输入内容', 'error')
    return
  }
  
  publishing.value = true
  try {
    const mediaUrl = mediaType.value === 'image' ? imageUrl.value : (mediaType.value === 'video' ? videoUrl.value : '')
    const res = await createPost({
      content: content.value,
      imageUrl: mediaUrl,
      mediaType: mediaType.value
    })
    if (res.code === 200) {
      showToast('发布成功')
      router.push('/')
    } else {
      showToast(res.message || '发布失败', 'error')
    }
  } catch (e) {
    showToast('发布失败', 'error')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.publish-container {
  display: flex;
  justify-content: center;
  padding: 20px 12px;
}

.publish-card {
  width: 100%;
  max-width: 600px;
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
}

.publish-card h2 {
  margin-bottom: 20px;
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

.form-group textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 15px;
  resize: none;
  font-family: inherit;
}

.form-group textarea:focus {
  outline: none;
  border-color: #e6162d;
}

.media-upload {
  position: relative;
  display: flex;
  gap: 12px;
}

.media-upload input {
  display: none;
}

.upload-label {
  display: inline-block;
  padding: 8px 16px;
  background: #f5f5f5;
  border: 1px dashed #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.upload-label:hover {
  border-color: #e6162d;
  color: #e6162d;
}

.video-label {
  margin-left: 12px;
}

.media-preview {
  display: block;
  max-width: 200px;
  max-height: 150px;
  margin-top: 12px;
  border-radius: 8px;
}

.remove-media {
  position: absolute;
  top: -8px;
  left: 70px;
  width: 24px;
  height: 24px;
  background: #ff4d4f;
  color: #fff;
  border-radius: 50%;
  text-align: center;
  line-height: 24px;
  cursor: pointer;
  font-size: 16px;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background: #e6162d;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #d51225;
}

.btn-primary:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>
