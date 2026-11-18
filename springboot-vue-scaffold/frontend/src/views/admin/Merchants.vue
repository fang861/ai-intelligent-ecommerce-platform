<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>商家审核</span>
        </div>
      </template>
      <div class="filter-bar">
        <button
          v-for="tab in statusTabs"
          :key="tab.value === '' ? 'all' : tab.value"
          type="button"
          class="filter-pill"
          :class="{ active: query.auditStatus === tab.value }"
          @click="selectStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="username" label="账号" width="120" />
        <el-table-column prop="shopName" label="店铺名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="shopDesc" label="简介" min-width="160" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" width="120" />
        <el-table-column label="证件" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">查看</el-button>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusType(row)" effect="light" round>{{ statusLabel(row) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center">
          <template #default="{ row }">
            <template v-if="isPending(row)">
              <el-button link type="success" @click="audit(row, true)">通过</el-button>
              <el-button link type="danger" @click="audit(row, false)">拒绝</el-button>
            </template>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-drawer v-model="detailVisible" title="入驻资料" size="420px">
      <div v-if="current" class="detail">
        <div class="detail-row">
          <em>账号</em>
          <strong>{{ current.username }}</strong>
        </div>
        <div class="detail-row">
          <em>店铺名称</em>
          <strong>{{ current.shopName || '—' }}</strong>
        </div>
        <div class="detail-row">
          <em>联系电话</em>
          <strong>{{ current.phone || '—' }}</strong>
        </div>
        <div class="detail-row block">
          <em>店铺简介</em>
          <p>{{ current.shopDesc || '暂无简介' }}</p>
        </div>

        <div class="docs-section">
          <h4>入驻证件</h4>
          <div class="docs">
            <div class="doc-card">
              <span>营业执照</span>
              <el-image
                v-if="docUrl(current.licenseImage)"
                :src="docUrl(current.licenseImage)"
                :preview-src-list="[docUrl(current.licenseImage)]"
                fit="cover"
                class="doc-img"
              />
              <div v-else class="doc-empty">未上传</div>
            </div>
            <div class="doc-card">
              <span>身份证正面</span>
              <el-image
                v-if="docUrl(current.idCardFront)"
                :src="docUrl(current.idCardFront)"
                :preview-src-list="previewList"
                fit="cover"
                class="doc-img"
              />
              <div v-else class="doc-empty">未上传</div>
            </div>
            <div class="doc-card">
              <span>身份证反面</span>
              <el-image
                v-if="docUrl(current.idCardBack)"
                :src="docUrl(current.idCardBack)"
                :preview-src-list="previewList"
                fit="cover"
                class="doc-img"
              />
              <div v-else class="doc-empty">未上传</div>
            </div>
          </div>
        </div>

        <div v-if="isPending(current)" class="detail-actions">
          <el-button type="success" @click="audit(current, true)">通过</el-button>
          <el-button type="danger" plain @click="audit(current, false)">拒绝</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { mediaUrl } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const query = reactive({ auditStatus: 0 })
const detailVisible = ref(false)
const current = ref(null)

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 0 },
  { label: '已通过', value: 1 },
  { label: '已拒绝', value: 2 }
]

const previewList = computed(() => {
  if (!current.value) return []
  return [current.value.idCardFront, current.value.idCardBack]
    .map((u) => docUrl(u))
    .filter(Boolean)
})

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function docUrl(url) {
  return mediaUrl(url) || ''
}

function selectStatus(value) {
  query.auditStatus = value
  loadData()
}

function isPending(row) {
  return Number(row.auditStatus) === 0
}

function statusLabel(row) {
  const s = Number(row.auditStatus)
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  return '待审核'
}

function statusType(row) {
  const label = statusLabel(row)
  if (label === '已通过') return 'success'
  if (label === '已拒绝') return 'danger'
  return 'warning'
}

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/merchants', {
      params: {
        auditStatus: query.auditStatus === '' || query.auditStatus == null ? undefined : query.auditStatus,
        page: 1,
        size: 50
      }
    })
    tableData.value = unwrapList(data)
  } finally {
    loading.value = false
  }
}

async function audit(row, approved) {
  if (!approved) {
    await ElMessageBox.confirm('确定拒绝该商家入驻申请？', '提示', { type: 'warning' })
  }
  await request.post(`/admin/merchants/${row.id}/audit`, {
    auditStatus: approved ? 1 : 2
  })
  ElMessage.success(approved ? '已通过' : '已拒绝')
  detailVisible.value = false
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.filter-pill {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #64748b;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
  transition: 0.2s ease;
}

.filter-pill:hover {
  background: #fff;
  color: #1f2937;
}

.filter-pill.active {
  color: #fff;
  background: #334155;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.16);
}

.panel { border-radius: 14px; }
.muted { color: #94a3b8; }

.detail {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.detail-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f1f5f9;
}

.detail-row.block {
  flex-direction: column;
  align-items: flex-start;
}

.detail-row em {
  font-style: normal;
  font-size: 12px;
  color: #94a3b8;
}

.detail-row strong {
  font-size: 14px;
  color: #1f2937;
}

.detail-row p {
  margin: 6px 0 0;
  font-size: 13px;
  color: #475569;
  line-height: 1.5;
}

.docs-section h4 {
  margin: 4px 0 12px;
  font-size: 14px;
  color: #1f2937;
}

.docs {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.doc-card {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.doc-card span {
  font-size: 12px;
  color: #64748b;
}

.doc-img {
  width: 100%;
  height: 160px;
  border-radius: 12px;
  background: #f8fafc;
}

.doc-empty {
  height: 100px;
  border-radius: 12px;
  display: grid;
  place-items: center;
  background: #f8fafc;
  color: #94a3b8;
  font-size: 13px;
  border: 1px dashed #e2e8f0;
}

.detail-actions {
  display: flex;
  gap: 10px;
  padding-top: 8px;
}
</style>
