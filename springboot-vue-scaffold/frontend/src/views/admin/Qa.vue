<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>问答管理</span>
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
              <strong>{{ row.productName || '未知商品' }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="question" label="问题" min-width="180" show-overflow-tooltip />
        <el-table-column label="回答" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ muted: !row.answer }">{{ row.answer || '未回复' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="提问人" width="110" align="center" />
        <el-table-column prop="createdAt" label="时间" width="170" align="center" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { mediaUrl } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])

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
    const data = await request.get('/admin/qa')
    tableData.value = unwrapList(data)
  } finally {
    loading.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该问答？', '提示', { type: 'warning' })
  await request.delete(`/admin/qa/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.panel {
  border-radius: 14px;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.thumb,
.thumb-fallback {
  width: 40px;
  height: 40px;
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

.product-cell strong {
  font-size: 13px;
  font-weight: 650;
  color: #1f2937;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.muted {
  color: #94a3b8;
}
</style>
