<template>
  <div class="rich-editor" :class="{ disabled }">
    <div class="toolbar" v-if="!disabled">
      <button type="button" title="加粗" @mousedown.prevent="cmd('bold')"><b>B</b></button>
      <button type="button" title="斜体" @mousedown.prevent="cmd('italic')"><i>I</i></button>
      <button type="button" title="下划线" @mousedown.prevent="cmd('underline')"><u>U</u></button>
      <span class="sep" />
      <button type="button" title="标题" @mousedown.prevent="cmd('formatBlock', 'h3')">H</button>
      <button type="button" title="段落" @mousedown.prevent="cmd('formatBlock', 'p')">P</button>
      <button type="button" title="无序列表" @mousedown.prevent="cmd('insertUnorderedList')">• 列表</button>
      <button type="button" title="有序列表" @mousedown.prevent="cmd('insertOrderedList')">1. 列表</button>
      <span class="sep" />
      <button type="button" title="插入链接" @mousedown.prevent="insertLink">链接</button>
      <button type="button" title="插入图片" :disabled="uploading" @mousedown.prevent="pickImage">
        {{ uploading ? '上传中…' : '图片' }}
      </button>
      <button type="button" title="清除格式" @mousedown.prevent="cmd('removeFormat')">清除</button>
      <input ref="fileRef" type="file" accept="image/*" class="file-input" @change="onFile" />
    </div>
    <div
      ref="editorRef"
      class="editor-body"
      :contenteditable="!disabled"
      :data-placeholder="placeholder"
      @input="onInput"
      @blur="onInput"
      @paste="onPaste"
    />
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { mediaUrl } from '@/utils/format'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '请输入商品详情…' },
  disabled: { type: Boolean, default: false },
  minHeight: { type: String, default: '220px' }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
const fileRef = ref(null)
const uploading = ref(false)
let syncing = false

function setHtml(html) {
  const el = editorRef.value
  if (!el) return
  const next = html || ''
  if (el.innerHTML === next) return
  syncing = true
  el.innerHTML = next
  nextTick(() => {
    syncing = false
  })
}

function onInput() {
  if (syncing || !editorRef.value) return
  emit('update:modelValue', editorRef.value.innerHTML)
}

function cmd(command, value = null) {
  document.execCommand(command, false, value)
  editorRef.value?.focus()
  onInput()
}

function insertLink() {
  const url = window.prompt('请输入链接地址')
  if (!url) return
  cmd('createLink', url)
}

function pickImage() {
  fileRef.value?.click()
}

async function onFile(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const token = localStorage.getItem('token')
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await axios.post(`${baseURL}/files/upload`, formData, {
      headers: { Authorization: token ? `Bearer ${token}` : undefined },
      timeout: 60000
    })
    const result = response.data
    if (result.code !== 200) {
      throw new Error(result.message || '上传失败')
    }
    const data = result.data
    const url = mediaUrl(data?.url || data?.path || data)
    if (!url) throw new Error('上传失败')
    cmd('insertImage', url)
    ElMessage.success('图片已插入')
  } catch (err) {
    ElMessage.error(err.message || '图片上传失败')
  } finally {
    uploading.value = false
  }
}

function onPaste(e) {
  e.preventDefault()
  const text = e.clipboardData?.getData('text/plain') || ''
  document.execCommand('insertText', false, text)
  onInput()
}

watch(
  () => props.modelValue,
  (val) => {
    nextTick(() => {
      if (!editorRef.value) return
      const next = val || ''
      if (editorRef.value.innerHTML !== next) {
        setHtml(next)
      }
    })
  },
  { immediate: true }
)

onMounted(() => {
  if (editorRef.value) {
    editorRef.value.style.minHeight = props.minHeight
    setHtml(props.modelValue)
  }
})
</script>

<style scoped>
.rich-editor {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  background: #fff;
}

.rich-editor.disabled {
  opacity: 0.75;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 10px;
  border-bottom: 1px solid #eef0f3;
  background: #f8fafc;
}

.toolbar button {
  height: 30px;
  padding: 0 10px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  background: #fff;
  color: #374151;
  font-size: 12px;
  cursor: pointer;
}

.toolbar button:hover:not(:disabled) {
  border-color: #e63935;
  color: #e63935;
}

.toolbar button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.sep {
  width: 1px;
  align-self: stretch;
  background: #e5e7eb;
  margin: 0 2px;
}

.file-input {
  display: none;
}

.editor-body {
  padding: 14px 16px;
  min-height: 220px;
  outline: none;
  line-height: 1.75;
  color: #1f2937;
  font-size: 14px;
}

.editor-body:empty::before {
  content: attr(data-placeholder);
  color: #9ca3af;
}

.editor-body :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

.editor-body :deep(h3) {
  margin: 12px 0 8px;
  font-size: 18px;
}

.editor-body :deep(p) {
  margin: 0 0 10px;
}

.editor-body :deep(ul),
.editor-body :deep(ol) {
  padding-left: 1.4em;
  margin: 0 0 10px;
}
</style>
