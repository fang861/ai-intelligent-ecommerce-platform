<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>问答管理</span>
          <el-radio-group v-model="unanswered" size="small" @change="loadData">
            <el-radio-button :value="false">全部</el-radio-button>
            <el-radio-button :value="true">待回复</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="product-cell">
              <el-image
                v-if="coverOf(row)"
                :src="coverOf(row)"
                fit="cover"
                class="thumb"
              >
                <template #error>
                  <div class="thumb-fallback">无图</div>
                </template>
              </el-image>
              <div v-else class="thumb-fallback">无图</div>
              <div class="product-meta">
                <strong>{{ row.productName || '未知商品' }}</strong>
                <em v-if="row.nickname">提问人 · {{ row.nickname }}</em>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="question" label="问题" min-width="180" show-overflow-tooltip />
        <el-table-column label="回答" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ muted: !row.answer }">{{ row.answer || '未回复' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="时间" width="170" align="center" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openAnswer(row)">
              {{ row.answer ? '修改回复' : '回复' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" title="回复问题" width="520px" destroy-on-close>
      <div class="dialog-product" v-if="current">
        <strong>{{ current.productName || '未知商品' }}</strong>
        <p class="q">{{ current.question }}</p>
      </div>
      <el-input v-model="answer" type="textarea" :rows="4" placeholder="输入回复内容" />
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { mediaUrl } from '@/utils/format'

const loading = ref(false)
const submitting = ref(false)
const visible = ref(false)
const unanswered = ref(false)
const tableData = ref([])
const current = ref(null)
const answer = ref('')

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function coverOf(row) {
  return mediaUrl(row?.productCover || row?.cover) || ''
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/merchant/qa', {
      params: {
        unanswered: unanswered.value || undefined,
        page: 1,
        size: 50
      },
      silent: true
    })
    tableData.value = unwrapList(data)
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

function openAnswer(row) {
  current.value = row
  answer.value = row.answer || ''
  visible.value = true
}

async function submit() {
  if (!answer.value.trim()) {
    ElMessage.warning('请输入回复')
    return
  }
  submitting.value = true
  try {
    await request.put(`/merchant/qa/${current.value.id}`, { answer: answer.value.trim() })
      .catch(() => request.post(`/merchant/qa/${current.value.id}/answer`, { answer: answer.value.trim() }))
    ElMessage.success('已回复')
    visible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.panel {
  border-radius: 14px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.thumb,
.thumb-fallback {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  flex-shrink: 0;
  background: #f1f5f9;
}

.thumb-fallback {
  color: #94a3b8;
  font-size: 11px;
  display: grid;
  place-items: center;
}

.product-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.product-meta strong {
  font-size: 13px;
  font-weight: 650;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta em {
  font-style: normal;
  font-size: 12px;
  color: #9ca3af;
}

.muted {
  color: #94a3b8;
}

.dialog-product {
  margin-bottom: 14px;
}

.dialog-product strong {
  display: block;
  margin-bottom: 8px;
  color: #1f2937;
}

.q {
  margin: 0;
  color: #334155;
  line-height: 1.6;
  padding: 10px 12px;
  border-radius: 10px;
  background: #f8fafc;
}
</style>
