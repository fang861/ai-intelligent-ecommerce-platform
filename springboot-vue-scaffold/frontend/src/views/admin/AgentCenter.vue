<template>
  <div class="agent-page" v-loading="loading">
    <header class="page-head">
      <div class="head-copy">
        <p class="eyebrow">Spring AI · Agent</p>
        <h2>AI Agent</h2>
        <p class="desc">接入状态、工具能力与 RAG 知识库一览</p>
      </div>
      <div class="head-actions">
        <el-button @click="load">刷新</el-button>
        <el-button type="primary" plain @click="$router.push('/admin/rag')">打开知识库</el-button>
      </div>
    </header>

    <div class="stat-row">
      <article class="stat">
        <em>接入状态</em>
        <strong :class="data.ready ? 'ok' : 'off'">{{ data.ready ? '已就绪' : '未就绪' }}</strong>
        <span>{{ data.enabled ? '大模型已启用' : '大模型未启用' }}</span>
      </article>
      <article class="stat">
        <em>Agent 工具</em>
        <strong>{{ data.toolCount || 0 }}</strong>
        <span>导购 / 购物车 / 下单 / 支付</span>
      </article>
      <article class="stat">
        <em>RAG 文档</em>
        <strong>{{ data.ragDocCount || 0 }}</strong>
        <span>{{ data.ragChunkCount || 0 }} 个向量片段</span>
      </article>
      <article class="stat wide">
        <em>对话入口</em>
        <strong class="sm">{{ data.chatEndpoint || '/ai/agent' }}</strong>
        <span>识图 {{ data.visionEndpoint || '/ai/guide/vision' }}</span>
      </article>
    </div>

    <section class="panel">
      <div class="panel-head">
        <h3>AI 接入状态</h3>
        <el-tag :type="data.ready ? 'success' : 'info'" size="small">
          {{ data.ready ? '可调用 Agent' : '将回退普通导购' }}
        </el-tag>
      </div>
      <div class="kv-grid">
        <div class="kv">
          <em>服务商</em>
          <strong>{{ data.provider || '—' }}</strong>
        </div>
        <div class="kv">
          <em>文本模型</em>
          <strong>{{ data.model || '—' }}</strong>
        </div>
        <div class="kv">
          <em>识图模型</em>
          <strong>{{ data.visionModel || '—' }}</strong>
        </div>
        <div class="kv">
          <em>API Key</em>
          <strong class="mono">{{ data.apiKeyMasked || '未配置' }}</strong>
        </div>
        <div class="kv span2">
          <em>接口地址</em>
          <strong class="mono wrap">{{ data.apiUrl || '—' }}</strong>
        </div>
        <div class="kv">
          <em>向量库</em>
          <strong>{{ data.storeType || '—' }}</strong>
        </div>
        <div class="kv">
          <em>Embedding</em>
          <strong>{{ data.embeddingType || '—' }}</strong>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h3>Agent Tools</h3>
        <span>{{ groupedTools.length }} 组 · {{ data.tools?.length || 0 }} 个工具</span>
      </div>
      <div v-for="g in groupedTools" :key="g.name" class="tool-group">
        <h4>{{ g.name }}</h4>
        <div class="tool-grid">
          <article v-for="t in g.items" :key="t.name" class="tool-card">
            <header>
              <code>{{ t.name }}</code>
              <div class="tags">
                <el-tag v-if="t.needLogin" size="small" type="warning" effect="plain">需登录</el-tag>
                <el-tag v-if="t.confirmRequired" size="small" type="danger" effect="plain">需确认</el-tag>
              </div>
            </header>
            <p>{{ t.description }}</p>
            <ul v-if="t.params?.length">
              <li v-for="p in t.params" :key="p.name">
                <b>{{ p.name }}</b>
                <em>{{ p.type }}</em>
                <span>{{ p.description || '—' }}</span>
              </li>
            </ul>
          </article>
        </div>
      </div>
    </section>

    <section class="panel">
      <div class="panel-head">
        <h3>RAG 知识文档</h3>
        <el-button link type="primary" @click="$router.push('/admin/rag')">查看详情</el-button>
      </div>
      <div class="rag-grid">
        <button
          v-for="d in data.ragDocs || []"
          :key="d.filename"
          type="button"
          class="rag-card"
          @click="$router.push('/admin/rag')"
        >
          <strong>{{ d.title || d.filename }}</strong>
          <em>{{ d.filename }}</em>
          <p>{{ d.summary }}</p>
          <div class="rag-meta">
            <span>{{ d.chunkCount }} 片段</span>
            <span>{{ d.charCount }} 字</span>
          </div>
        </button>
        <p v-if="!(data.ragDocs || []).length" class="empty-tip">暂无知识库文档</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import request from '@/api/request'

const loading = ref(false)
const data = reactive({
  ready: false,
  enabled: false,
  provider: '',
  apiUrl: '',
  model: '',
  visionModel: '',
  apiKeyMasked: '',
  chatEndpoint: '',
  visionEndpoint: '',
  storeType: '',
  embeddingType: '',
  toolCount: 0,
  ragDocCount: 0,
  ragChunkCount: 0,
  tools: [],
  ragDocs: []
})

const groupedTools = computed(() => {
  const map = new Map()
  for (const t of data.tools || []) {
    const name = t.groupName || '其它'
    if (!map.has(name)) map.set(name, [])
    map.get(name).push(t)
  }
  return [...map.entries()].map(([name, items]) => ({ name, items }))
})

async function load() {
  loading.value = true
  try {
    const res = await request.get('/admin/agent/overview')
    Object.assign(data, {
      ready: !!res?.ready,
      enabled: !!res?.enabled,
      provider: res?.provider || '',
      apiUrl: res?.apiUrl || '',
      model: res?.model || '',
      visionModel: res?.visionModel || '',
      apiKeyMasked: res?.apiKeyMasked || '',
      chatEndpoint: res?.chatEndpoint || '',
      visionEndpoint: res?.visionEndpoint || '',
      storeType: res?.storeType || '',
      embeddingType: res?.embeddingType || '',
      toolCount: res?.toolCount || 0,
      ragDocCount: res?.ragDocCount || 0,
      ragChunkCount: res?.ragChunkCount || 0,
      tools: Array.isArray(res?.tools) ? res.tools : [],
      ragDocs: Array.isArray(res?.ragDocs) ? res.ragDocs : []
    })
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.agent-page {
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
}

.stat-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(220px, 1.4fr);
  gap: 12px;
  margin-bottom: 14px;
}

.stat,
.panel {
  border-radius: 16px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.stat {
  padding: 14px 16px;
}

.stat em,
.kv em {
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
  font-size: 15px;
  letter-spacing: 0;
}

.stat strong.ok {
  color: #16a34a;
  font-size: 22px;
}

.stat strong.off {
  color: #9ca3af;
  font-size: 22px;
}

.stat span {
  display: block;
  margin-top: 8px;
  font-size: 12px;
  color: var(--muted);
}

.panel {
  padding: 16px 18px 18px;
  margin-bottom: 14px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-head h3 {
  margin: 0;
  font-size: 16px;
}

.panel-head span {
  font-size: 12px;
  color: var(--muted);
}

.kv-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.kv {
  padding: 12px 14px;
  border-radius: 12px;
  background: #fafbfc;
  border: 1px solid var(--line);
}

.kv.span2 {
  grid-column: span 2;
}

.kv strong {
  font-size: 13.5px;
  font-weight: 700;
  line-height: 1.45;
}

.kv strong.mono,
.tool-card header code {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
}

.kv strong.wrap {
  word-break: break-all;
}

.tool-group {
  margin-bottom: 16px;
}

.tool-group:last-child {
  margin-bottom: 0;
}

.tool-group h4 {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--muted);
  font-weight: 720;
}

.tool-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.tool-card {
  padding: 14px;
  border-radius: 14px;
  border: 1px solid var(--line);
  background: linear-gradient(180deg, #fff, #fafbfc);
}

.tool-card header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
}

.tool-card header code {
  font-size: 13px;
  font-weight: 740;
  color: var(--ink);
}

.tags {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.tool-card p {
  margin: 0 0 10px;
  font-size: 13px;
  line-height: 1.6;
  color: #4b5563;
}

.tool-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
  display: grid;
  gap: 6px;
}

.tool-card li {
  display: grid;
  grid-template-columns: 110px 72px minmax(0, 1fr);
  gap: 8px;
  font-size: 12px;
  color: #6b7280;
}

.tool-card li b {
  color: var(--ink);
  font-weight: 700;
}

.tool-card li em {
  font-style: normal;
  color: var(--muted);
}

.rag-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.rag-card {
  text-align: left;
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 14px;
  padding: 14px;
  cursor: pointer;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.rag-card:hover {
  border-color: rgba(230, 57, 53, 0.22);
  box-shadow: 0 8px 20px rgba(230, 57, 53, 0.06);
}

.rag-card strong {
  display: block;
  font-size: 14px;
  margin-bottom: 4px;
}

.rag-card em {
  display: block;
  font-style: normal;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 8px;
}

.rag-card p {
  margin: 0;
  font-size: 12px;
  color: #6b7280;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.rag-meta {
  display: flex;
  gap: 10px;
  margin-top: 10px;
  font-size: 11px;
  color: var(--muted);
  font-weight: 650;
}

.empty-tip {
  margin: 8px 0;
  color: var(--muted);
  font-size: 13px;
}

@media (max-width: 1100px) {
  .stat-row,
  .kv-grid,
  .tool-grid,
  .rag-grid {
    grid-template-columns: 1fr;
  }

  .kv.span2 {
    grid-column: auto;
  }

  .tool-card li {
    grid-template-columns: 1fr;
    gap: 2px;
  }
}
</style>
