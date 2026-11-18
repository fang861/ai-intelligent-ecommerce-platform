<template>
  <div class="multi-upload">
    <div class="multi-head">
      <span class="multi-count">已选 {{ list.length }} / {{ max }}</span>
      <button
        v-if="list.length"
        type="button"
        class="multi-clear"
        @click="clearAll"
      >
        清空
      </button>
    </div>

    <div class="thumb-grid">
      <div
        v-for="(url, idx) in list"
        :key="`${url}-${idx}`"
        class="thumb-item"
      >
        <el-image
          :src="previewOf(url)"
          fit="cover"
          class="thumb-img"
          :preview-src-list="previewList"
          :initial-index="idx"
          preview-teleported
        >
          <template #error>
            <div class="thumb-broken">加载失败</div>
          </template>
        </el-image>
        <button type="button" class="thumb-remove" title="移除" @click.stop="removeAt(idx)">
          ×
        </button>
        <span class="thumb-idx">{{ idx + 1 }}</span>
      </div>

      <el-upload
        v-if="list.length < max"
        class="thumb-add"
        :show-file-list="false"
        :http-request="handleUpload"
        :disabled="uploading"
        accept="image/jpeg,image/png,image/gif,image/webp,image/svg+xml,.svg,image/*"
        multiple
      >
        <div class="add-box" :class="{ busy: uploading }">
          <span class="add-plus">+</span>
          <span>{{ uploading ? '上传中…' : '添加图片' }}</span>
          <em>支持多选</em>
        </div>
      </el-upload>
    </div>

    <p v-if="tip" class="multi-tip">{{ tip }}</p>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mediaUrl } from '@/utils/format'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 8 },
  tip: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

const uploading = ref(false)

const list = computed(() => (Array.isArray(props.modelValue) ? props.modelValue.filter(Boolean) : []))
const previewList = computed(() => list.value.map((u) => mediaUrl(u)))

function previewOf(url) {
  return mediaUrl(url)
}

function sync(next) {
  emit('update:modelValue', next)
}

function removeAt(idx) {
  const next = list.value.slice()
  next.splice(idx, 1)
  sync(next)
}

async function clearAll() {
  await ElMessageBox.confirm('确定清空全部缩略图？', '提示', { type: 'warning' })
  sync([])
}

async function uploadOne(file) {
  const name = file.name || ''
  const type = String(file.type || '').toLowerCase()
  const isImage =
    type.startsWith('image/') ||
    type === 'image/svg+xml' ||
    /\.(jpe?g|png|gif|webp|svg)$/i.test(name)
  if (!isImage) {
    ElMessage.warning('请上传图片文件')
    return null
  }
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
    throw new Error(result.message || '上传失败')
  }
  const data = result.data
  return data?.url || data?.path || data || ''
}

async function handleUpload({ file }) {
  if (!file) return
  if (list.value.length >= props.max) {
    ElMessage.warning(`最多上传 ${props.max} 张缩略图`)
    return
  }
  uploading.value = true
  try {
    const url = await uploadOne(file)
    if (!url) return
    const next = [...list.value, url].slice(0, props.max)
    sync(next)
    ElMessage.success('上传成功')
  } catch (e) {
    ElMessage.error(e?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.multi-upload {
  width: 100%;
}

.multi-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.multi-count {
  font-size: 12px;
  font-weight: 650;
  color: #64748b;
}

.multi-clear {
  border: 0;
  background: transparent;
  color: #ef4444;
  font-size: 12px;
  cursor: pointer;
  padding: 0 4px;
}

.multi-clear:hover {
  text-decoration: underline;
}

.thumb-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.thumb-item,
.add-box {
  width: 104px;
  height: 104px;
  border-radius: 14px;
  box-sizing: border-box;
}

.thumb-item {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #f1f5f9;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.06);
}

.thumb-img {
  width: 100%;
  height: 100%;
  display: block;
}

.thumb-img :deep(img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-broken {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  font-size: 12px;
  color: #94a3b8;
  background: #f8fafc;
}

.thumb-remove {
  position: absolute;
  top: 6px;
  right: 6px;
  z-index: 2;
  width: 24px;
  height: 24px;
  border: 0;
  border-radius: 50%;
  background: rgba(15, 23, 42, 0.72);
  color: #fff;
  font-size: 15px;
  line-height: 1;
  cursor: pointer;
}

.thumb-remove:hover {
  background: #ef4444;
}

.thumb-idx {
  position: absolute;
  left: 6px;
  bottom: 6px;
  z-index: 2;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.58);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.thumb-add :deep(.el-upload) {
  display: block;
}

.add-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 1px dashed #cbd5e1;
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  color: #64748b;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, color 0.2s, transform 0.2s;
}

.add-plus {
  width: 28px;
  height: 28px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 20px;
  line-height: 1;
  color: #0f172a;
  background: #fff;
  border: 1px solid #e2e8f0;
}

.add-box span:not(.add-plus) {
  font-size: 12px;
  font-weight: 700;
}

.add-box em {
  font-style: normal;
  font-size: 11px;
  color: #94a3b8;
}

.add-box:hover {
  border-color: #e63935;
  color: #e63935;
  background: #fff8f7;
  transform: translateY(-1px);
}

.add-box:hover .add-plus {
  color: #e63935;
  border-color: rgba(230, 57, 53, 0.25);
}

.add-box.busy {
  opacity: 0.7;
  pointer-events: none;
}

.multi-tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}
</style>
