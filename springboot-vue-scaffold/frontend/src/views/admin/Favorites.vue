<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>收藏管理</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column label="用户" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.nickname || row.username || '—' }}
          </template>
        </el-table-column>
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
              <strong>{{ row.productName || row.product?.name || '未知商品' }}</strong>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110" align="center">
          <template #default="{ row }">
            {{ formatPrice(row.price ?? row.productPrice ?? row.product?.price) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="收藏时间" width="170" align="center" />
        <el-table-column label="操作" width="90" align="center">
          <template #default="{ row }">
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="page"
          :page-size="10"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatPrice, mediaUrl } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function coverOf(row) {
  return mediaUrl(row?.productCover || row?.product?.cover) || ''
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/favorites', { params: { page: page.value, size: 10 } })
    tableData.value = unwrapList(data)
    total.value = Number(data?.total ?? tableData.value.length) || 0
  } finally {
    loading.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该收藏记录？', '提示', { type: 'warning' })
  await request.delete(`/admin/favorites/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

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
</style>
