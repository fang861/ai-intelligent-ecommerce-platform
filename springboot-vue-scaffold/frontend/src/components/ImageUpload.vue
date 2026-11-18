<template>
  <div
    class="image-upload"
    :class="[
      `is-${type}`,
      { 'has-value': !!modelValue, 'is-placeholder': showingPlaceholder, 'is-square': isSquare }
    ]"
  >
    <el-upload
      :show-file-list="false"
      :http-request="handleUpload"
      accept="image/jpeg,image/png,image/gif,image/webp,image/svg+xml,.svg,image/*"
      :disabled="uploading"
    >
      <!-- 头像 / Logo：圆形；SVG 用 img 预览，避免 el-avatar 不渲染 -->
      <div v-if="type === 'avatar'" class="avatar-box" :style="avatarBoxStyle">
        <img v-if="previewUrl" :src="previewUrl" class="avatar-img-el" alt="logo" />
        <el-avatar v-else :size="avatarSize" class="avatar-img">
          {{ fallbackText }}
        </el-avatar>
        <span class="mask">
          <span>{{ uploading ? '上传中…' : uploadText || '上传头像' }}</span>
        </span>
      </div>

      <!-- 封面 / 缩略图：横图预览区 -->
      <div v-else class="cover-box" :class="{ 'is-empty': !previewUrl || imgBroken }" :style="coverBoxStyle">
        <img
          v-if="previewUrl && !imgBroken"
          :src="previewUrl"
          alt="thumbnail"
          class="cover-img"
          @error="onImgError"
          @load="onImgLoad"
        />
        <div v-else-if="!imgBroken" class="cover-empty">
          <span class="cover-plus">
            <el-icon :size="22"><Plus /></el-icon>
          </span>
          <span class="cover-title">{{ uploading ? '上传中…' : emptyText }}</span>
          <span class="cover-hint">{{ tip || '支持 jpg / png / webp，建议 800×800' }}</span>
        </div>
        <span v-if="previewUrl && !imgBroken" class="mask">
          {{ uploading ? '上传中…' : changeText }}
        </span>
        <div v-if="imgBroken" class="cover-empty broken">
          <span class="cover-title">图片加载失败</span>
          <span class="cover-hint">请重新上传</span>
        </div>
      </div>
    </el-upload>

    <div v-if="type !== 'avatar' && !hideActions" class="cover-actions">
      <el-upload
        :show-file-list="false"
        :http-request="handleUpload"
        accept="image/jpeg,image/png,image/gif,image/webp,image/svg+xml,.svg,image/*"
        :disabled="uploading"
      >
        <button type="button" class="cover-upload-btn" :disabled="uploading">
          {{ uploading ? '上传中…' : previewUrl ? changeText : emptyText }}
        </button>
      </el-upload>
      <button
        v-if="clearable && modelValue"
        type="button"
        class="cover-clear"
        @click.stop="clear"
      >
        移除
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { mediaUrl } from '@/utils/format'

const props = defineProps({
  modelValue: { type: String, default: '' },
  /** cover | avatar */
  type: { type: String, default: 'cover' },
  uploadText: { type: String, default: '' },
  clearable: { type: Boolean, default: true },
  tip: { type: String, default: '' },
  width: { type: [Number, String], default: '100%' },
  height: { type: [Number, String], default: 'auto' },
  /** square：商品缩略图方形预览 */
  shape: { type: String, default: '' },
  avatarSize: { type: Number, default: 96 },
  fallback: { type: String, default: '图' },
  /** 无自定义值时展示的占位图（如默认 Logo） */
  placeholderSrc: { type: String, default: '' },
  showSuccess: { type: Boolean, default: true },
  /** 隐藏底部上传/移除按钮条 */
  hideActions: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'success'])

const uploading = ref(false)
const imgBroken = ref(false)

const isSquare = computed(() => props.shape === 'square' || props.type === 'thumb')
const emptyText = computed(() => props.uploadText || '上传图片')
const changeText = computed(() => (props.uploadText ? `更换${props.uploadText.replace(/^上传/, '')}` : '更换图片'))

const previewUrl = computed(() => {
  const custom = mediaUrl(props.modelValue)
  if (custom) return custom
  return props.placeholderSrc || ''
})

watch(
  () => props.modelValue,
  () => {
    imgBroken.value = false
  }
)

function onImgError() {
  imgBroken.value = true
}

function onImgLoad() {
  imgBroken.value = false
}

const fallbackText = computed(() => (props.fallback || '图').slice(0, 1).toUpperCase())
const showingPlaceholder = computed(() => !props.modelValue && !!props.placeholderSrc)

function toCssSize(value) {
  if (value == null || value === '') return ''
  return typeof value === 'number' ? `${value}px` : String(value)
}

const coverBoxStyle = computed(() => {
  const style = {}
  if (isSquare.value) {
    const size = toCssSize(props.width !== '100%' ? props.width : 168) || '168px'
    style.width = size
    style.height = toCssSize(props.height !== 'auto' ? props.height : size) || size
    return style
  }
  if (props.width && props.width !== '100%') {
    style.width = toCssSize(props.width)
  }
  if (props.height && props.height !== 'auto') {
    style.height = toCssSize(props.height)
  }
  return style
})

const avatarBoxStyle = computed(() => ({
  width: `${props.avatarSize}px`,
  height: `${props.avatarSize}px`
}))

async function handleUpload({ file }) {
  if (!file) return
  const name = file.name || ''
  const type = String(file.type || '').toLowerCase()
  const isImage =
    type.startsWith('image/') ||
    type === 'image/svg+xml' ||
    /\.(jpe?g|png|gif|webp|svg)$/i.test(name)
  if (!isImage) {
    ElMessage.warning('请上传图片文件（jpg / png / gif / webp / svg）')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await axios.post(`${baseURL}/files/upload`, formData, {
      headers: {
        Authorization: token ? `Bearer ${token}` : undefined
      },
      timeout: 60000
    })
    const result = response.data
    if (result.code !== 200) {
      ElMessage.error(result.message || '上传失败')
      throw new Error(result.message || '上传失败')
    }
    const data = result.data
    const url = data?.url || data?.path || data || ''
    emit('update:modelValue', url)
    emit('success', url)
    if (props.showSuccess) {
      ElMessage.success('上传成功')
    }
  } finally {
    uploading.value = false
  }
}

function clear() {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.image-upload {
  display: inline-flex;
  align-items: flex-end;
  flex-wrap: wrap;
  gap: 8px;
}

.image-upload.is-cover {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  width: 100%;
  gap: 10px;
}

.image-upload.is-cover.is-square {
  width: auto;
  align-items: flex-start;
}

.image-upload :deep(.el-upload) {
  display: inline-block;
}

.image-upload.is-cover :deep(.el-upload) {
  display: block;
  width: 100%;
}

.image-upload.is-cover.is-square :deep(.el-upload) {
  width: auto;
}

.cover-box {
  position: relative;
  width: 100%;
  height: 168px;
  box-sizing: border-box;
  border-radius: 14px;
  overflow: hidden;
  border: 1px dashed #d4d4d8;
  cursor: pointer;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.image-upload.is-square .cover-box {
  flex-shrink: 0;
  border-radius: 16px;
}

.cover-box.is-empty:hover {
  border-color: var(--app-primary, #3b82f6);
  background: color-mix(in srgb, var(--app-primary, #3b82f6) 6%, #ffffff);
}

.cover-box:not(.is-empty) {
  border-style: solid;
  border-color: #e5e7eb;
  background: #0f172a;
}

.cover-box img,
.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.cover-empty.broken {
  position: absolute;
  inset: 0;
  background: #f8fafc;
}

.cover-empty {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #64748b;
}

.cover-plus {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: var(--app-primary, #3b82f6);
  background: #fff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 10px rgba(15, 23, 42, 0.06);
}

.cover-title {
  font-size: 14px;
  font-weight: 650;
  color: #334155;
}

.cover-hint {
  font-size: 12px;
  color: #94a3b8;
}

.cover-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.cover-upload-btn {
  height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(15, 23, 42, 0.12);
  border-radius: 999px;
  background: #fff;
  color: #0f172a;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  transition: border-color 0.2s, color 0.2s, background 0.2s;
}

.cover-upload-btn:hover:not(:disabled) {
  color: var(--app-primary, #3b82f6);
  border-color: color-mix(in srgb, var(--app-primary, #3b82f6) 35%, #e2e8f0);
  background: color-mix(in srgb, var(--app-primary, #3b82f6) 6%, #ffffff);
}

.cover-upload-btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.cover-clear {
  height: 34px;
  padding: 0 12px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  color: #ef4444;
  font-size: 13px;
  cursor: pointer;
}

.cover-clear:hover {
  background: #fef2f2;
}

.avatar-box {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
}

.avatar-img {
  display: block;
}

.avatar-img-el {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
  background: #f5f7fa;
}

.mask {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.45);
  color: #fff;
  font-size: 13px;
  opacity: 0;
  transition: opacity 0.2s;
  text-align: center;
  padding: 4px;
  line-height: 1.3;
}

.cover-box:hover .mask,
.avatar-box:hover .mask {
  opacity: 1;
}
</style>
