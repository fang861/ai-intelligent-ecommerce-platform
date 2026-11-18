<template>
  <div class="rag-page" v-loading="loading">
    <header class="page-head">
      <div class="head-copy">
        <p class="eyebrow">Spring AI · RAG</p>
        <h2>RAG 知识库</h2>
        <p class="desc">内置文档在 classpath:rag/*.md；也可在此上传 .md，保存到 uploads/rag 并立即重建向量</p>
      </div>
      <div class="head-actions">
        <el-upload
          :show-file-list="false"
          accept=".md,text/markdown"
          :http-request="uploadDoc"
          :disabled="uploading"
        >
          <el-button type="primary" :loading="uploading">上传 Markdown</el-button>
        </el-upload>
        <el-button :loading="reloading" @click="reload">重建向量</el-button>
        <el-button type="primary" plain @click="showSearch = !showSearch">
          {{ showSearch ? '收起检索' : '检索测试' }}
        </el-button>
      </div>
    </header>

    <div class="stat-row">
      <article class="stat">
        <em>文档数</em>
        <strong>{{ overview.docCount || 0 }}</strong>
      </article>
      <article class="stat">
        <em>向量片段</em>
        <strong>{{ overview.chunkCount || 0 }}</strong>
      </article>
      <article class="stat">
        <em>总字符</em>
        <strong>{{ overview.totalChars || 0 }}</strong>
      </article>
      <article class="stat wide">
        <em>存储 / 向量</em>
        <strong class="sm">{{ overview.storeType || '—' }}</strong>
        <span>{{ overview.embeddingType || '' }}</span>
      </article>
    </div>

    <section v-if="showSearch" class="search-panel">
      <div class="search-bar">
        <el-input
          v-model="query"
          clearable
          placeholder="例如：怎么退换货、发货要多久"
          @keyup.enter="runSearch"
        />
        <el-button type="primary" :loading="searching" @click="runSearch">检索</el-button>
      </div>
      <div v-if="hits.length" class="hit-list">
        <article v-for="(h, i) in hits" :key="i" class="hit-card">
          <header>
            <b>{{ h.filename || '未知来源' }}</b>
            <span v-if="h.score != null">相关度 {{ Number(h.score).toFixed(3) }}</span>
          </header>
          <p>{{ h.text }}</p>
        </article>
      </div>
      <p v-else-if="searched && !searching" class="empty-tip">暂无命中片段，换个说法试试</p>
    </section>

    <div class="workspace">
      <aside class="doc-list">
        <div class="aside-head">
          <h3>知识文档</h3>
          <span>{{ docs.length }} 篇</span>
        </div>
        <button
          v-for="d in docs"
          :key="d.filename"
          type="button"
          class="doc-item"
          :class="{ active: activeName === d.filename }"
          @click="selectDoc(d.filename)"
        >
          <strong>
            {{ d.title || d.filename }}
            <el-tag :type="d.uploaded ? 'success' : 'info'" size="small" effect="plain">
              {{ d.uploaded ? '上传' : '内置' }}
            </el-tag>
          </strong>
          <em>{{ d.filename }}</em>
          <p>{{ d.summary }}</p>
          <div class="doc-meta">
            <span>{{ d.chunkCount }} 片段</span>
            <span>{{ d.charCount }} 字</span>
          </div>
        </button>
        <p v-if="!docs.length && !loading" class="empty-tip">暂无知识库文档</p>
      </aside>

      <main class="doc-detail" v-loading="detailLoading">
        <template v-if="current">
          <div class="detail-head">
            <div>
              <h3>{{ current.title }}</h3>
              <p>
                <code>{{ current.filename }}</code>
                <span>· {{ current.chunkCount }} 个检索片段</span>
                <span>· {{ current.charCount }} 字符</span>
              </p>
            </div>
            <div class="detail-actions">
              <el-radio-group v-model="viewMode" size="small">
                <el-radio-button value="content">原文</el-radio-button>
                <el-radio-button value="chunks">片段</el-radio-button>
              </el-radio-group>
              <el-button
                v-if="current.uploaded"
                type="danger"
                plain
                size="small"
                :loading="removing"
                @click="removeDoc"
              >
                删除
              </el-button>
            </div>
          </div>

          <pre v-if="viewMode === 'content'" class="md-body">{{ current.content }}</pre>

          <div v-else class="chunk-list">
            <article v-for="c in current.chunks || []" :key="c.index" class="chunk-card">
              <header>
                <b>片段 #{{ c.index + 1 }}</b>
                <span>{{ c.charCount }} 字</span>
              </header>
              <p>{{ c.text }}</p>
            </article>
            <p v-if="!(current.chunks || []).length" class="empty-tip">该文档暂无切分片段</p>
          </div>
        </template>
        <div v-else class="detail-empty">
          <h3>选择左侧文档</h3>
          <p>查看原文或检索片段。可上传 .md 扩展知识库（如优惠券、积分规则）</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const loading = ref(false)
const detailLoading = ref(false)
const reloading = ref(false)
const uploading = ref(false)
const removing = ref(false)
const searching = ref(false)
const searched = ref(false)
const showSearch = ref(false)
const viewMode = ref('content')
const query = ref('')
const activeName = ref('')
const docs = ref([])
const hits = ref([])
const current = ref(null)
const overview = reactive({
  docCount: 0,
  chunkCount: 0,
  totalChars: 0,
  storeType: '',
  embeddingType: ''
})

async function loadOverview() {
  loading.value = true
  try {
    const data = await request.get('/admin/rag/overview')
    applyOverview(data)
    if (!activeName.value && docs.value.length) {
      await selectDoc(docs.value[0].filename)
    } else if (activeName.value) {
      const still = docs.value.some((d) => d.filename === activeName.value)
      if (still) await selectDoc(activeName.value)
      else if (docs.value.length) await selectDoc(docs.value[0].filename)
      else {
        activeName.value = ''
        current.value = null
      }
    }
  } finally {
    loading.value = false
  }
}

async function selectDoc(filename) {
  if (!filename) return
  activeName.value = filename
  detailLoading.value = true
  try {
    current.value = await request.get(`/admin/rag/docs/${encodeURIComponent(filename)}`)
    viewMode.value = 'content'
  } finally {
    detailLoading.value = false
  }
}

async function runSearch() {
  const q = query.value.trim()
  if (!q) {
    ElMessage.warning('请输入检索内容')
    return
  }
  searching.value = true
  searched.value = true
  try {
    hits.value = (await request.post('/admin/rag/search', { query: q, topK: 5 })) || []
  } finally {
    searching.value = false
  }
}

async function uploadDoc(options) {
  const file = options?.file
  if (!file) return
  const name = String(file.name || '')
  if (!name.toLowerCase().endsWith('.md')) {
    ElMessage.warning('请上传 .md 文件')
    return
  }
  uploading.value = true
  try {
    const form = new FormData()
    form.append('file', file)
    const data = await request.post('/admin/rag/docs', form, { timeout: 60000 })
    applyOverview(data)
    ElMessage.success('已上传并重建知识库，Agent 可立即检索')
    if (docs.value.length) {
      const uploadedName = name.split(/[/\\]/).pop()
      const hit = docs.value.find((d) => d.filename === uploadedName) || docs.value[0]
      await selectDoc(hit.filename)
    }
  } finally {
    uploading.value = false
  }
}

async function removeDoc() {
  const filename = current.value?.filename
  if (!filename) return
  try {
    await ElMessageBox.confirm(`确定删除上传文档「${filename}」并重建向量库？`, '删除知识库文档', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  removing.value = true
  try {
    const data = await request.delete(`/admin/rag/docs/${encodeURIComponent(filename)}`)
    applyOverview(data)
    ElMessage.success('已删除并重建知识库')
    if (docs.value.length) await selectDoc(docs.value[0].filename)
    else {
      activeName.value = ''
      current.value = null
    }
  } finally {
    removing.value = false
  }
}

function applyOverview(data) {
  Object.assign(overview, {
    docCount: data?.docCount || 0,
    chunkCount: data?.chunkCount || 0,
    totalChars: data?.totalChars || 0,
    storeType: data?.storeType || '',
    embeddingType: data?.embeddingType || ''
  })
  docs.value = Array.isArray(data?.docs) ? data.docs : []
}

async function reload() {
  reloading.value = true
  try {
    const data = await request.post('/admin/rag/reload')
    applyOverview(data)
    ElMessage.success('已重建向量库，Agent 将使用最新文档')
    if (activeName.value && docs.value.some((d) => d.filename === activeName.value)) {
      await selectDoc(activeName.value)
    } else if (docs.value.length) {
      await selectDoc(docs.value[0].filename)
    }
  } finally {
    reloading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.rag-page {
  --ink: #1a1a1a;
  --muted: #8b929c;
  --line: rgba(26, 26, 26, 0.08);
  --accent: #e63935;
  color: var(--ink);
}

.page-head {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--accent);
}

.page-head h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 780;
}

.desc {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.head-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
  flex-wrap: wrap;
  align-items: center;
}

.doc-item strong {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.detail-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(220px, 1.4fr);
  gap: 12px;
  margin-bottom: 14px;
}

.stat {
  padding: 14px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.stat em {
  display: block;
  font-style: normal;
  font-size: 12px;
  color: var(--muted);
  font-weight: 650;
  margin-bottom: 8px;
}

.stat strong {
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1;
}

.stat strong.sm {
  font-size: 14px;
  letter-spacing: 0;
  line-height: 1.35;
}

.stat span {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--muted);
}

.search-panel {
  margin-bottom: 14px;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--line);
}

.search-bar {
  display: flex;
  gap: 10px;
}

.hit-list {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.hit-card {
  padding: 12px 14px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid var(--line);
}

.hit-card header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 13px;
}

.hit-card header span {
  color: var(--muted);
  flex-shrink: 0;
}

.hit-card p {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: #374151;
  white-space: pre-wrap;
}

.workspace {
  display: grid;
  grid-template-columns: 300px minmax(0, 1fr);
  gap: 14px;
  min-height: 560px;
}

.doc-list,
.doc-detail {
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.04);
}

.doc-list {
  padding: 12px;
  overflow: auto;
  max-height: calc(100vh - 260px);
}

.aside-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 4px 6px 12px;
}

.aside-head h3 {
  margin: 0;
  font-size: 15px;
}

.aside-head span {
  font-size: 12px;
  color: var(--muted);
}

.doc-item {
  width: 100%;
  text-align: left;
  border: 1px solid transparent;
  background: transparent;
  border-radius: 12px;
  padding: 12px;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background 0.15s ease, border-color 0.15s ease;
}

.doc-item:hover {
  background: #fafbfc;
}

.doc-item.active {
  background: #fff5f5;
  border-color: rgba(230, 57, 53, 0.22);
}

.doc-item strong {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 14px;
  margin-bottom: 4px;
}

.doc-item em {
  display: block;
  font-style: normal;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 8px;
}

.doc-item p {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.doc-meta {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  font-size: 11px;
  color: var(--muted);
  font-weight: 650;
}

.doc-detail {
  padding: 18px 20px;
  overflow: auto;
  max-height: calc(100vh - 260px);
}

.detail-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--line);
}

.detail-head h3 {
  margin: 0 0 8px;
  font-size: 18px;
}

.detail-head p {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  color: var(--muted);
  font-size: 12px;
}

.detail-head code {
  padding: 2px 8px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 11px;
}

.md-body {
  margin: 0;
  padding: 16px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid var(--line);
  white-space: pre-wrap;
  word-break: break-word;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.7;
  color: #1f2937;
}

.chunk-list {
  display: grid;
  gap: 10px;
}

.chunk-card {
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: #fff;
}

.chunk-card header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--muted);
}

.chunk-card p {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  white-space: pre-wrap;
}

.detail-empty {
  height: 100%;
  min-height: 320px;
  display: grid;
  place-content: center;
  text-align: center;
  color: var(--muted);
}

.detail-empty h3 {
  margin: 0 0 8px;
  color: var(--ink);
}

.detail-empty p {
  margin: 0;
}

.empty-tip {
  margin: 12px 6px;
  color: var(--muted);
  font-size: 13px;
}

@media (max-width: 1100px) {
  .stat-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .workspace {
    grid-template-columns: 1fr;
  }

  .doc-list,
  .doc-detail {
    max-height: none;
  }
}
</style>
