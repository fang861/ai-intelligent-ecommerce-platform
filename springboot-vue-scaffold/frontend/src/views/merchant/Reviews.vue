<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>评价管理</span>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column label="商品" min-width="180">
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
        <el-table-column label="用户" width="160">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="32" :src="avatarOf(row)">
                {{ (row.nickname || row.username || '用').slice(0, 1) }}
              </el-avatar>
              <span>{{ row.nickname || row.username || '—' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="评分" width="160" align="center">
          <template #default="{ row }">
            <el-rate :model-value="Number(row.score || row.rating || 5)" disabled />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="时间" width="170" align="center" />
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
import request from '@/api/request'
import { mediaUrl } from '@/utils/format'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const page = ref(1)

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function coverOf(row) {
  return mediaUrl(row?.productCover || row?.cover) || ''
}

function avatarOf(row) {
  return mediaUrl(row?.avatar || row?.userAvatar) || ''
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/merchant/reviews', { params: { page: page.value, size: 10 }, silent: true })
    tableData.value = unwrapList(data)
    total.value = Number(data?.total ?? tableData.value.length) || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.panel { border-radius: 14px; }

.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.user-cell span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
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
