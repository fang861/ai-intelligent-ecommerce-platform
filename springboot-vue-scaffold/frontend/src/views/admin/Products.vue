<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>商品管理</span>
        </div>
      </template>

      <div class="toolbar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索商品"
          clearable
          style="width: 220px"
          @keyup.enter="loadData"
        />
        <el-button type="primary" plain @click="loadData">查询</el-button>
      </div>

      <el-table v-loading="loading" :data="tableData" class="admin-table">
        <el-table-column label="封面" width="88" align="center">
          <template #default="{ row }">
            <div class="thumb-cell">
              <el-image
                v-if="coverOf(row)"
                :src="coverOf(row)"
                fit="cover"
                class="thumb-img"
              >
                <template #error>
                  <div class="thumb-fallback">无图</div>
                </template>
              </el-image>
              <div v-else class="thumb-fallback">无图</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="shopName" label="所属店铺" width="130" show-overflow-tooltip />
        <el-table-column label="分类" width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.categoryName || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="售价" width="110" align="center">
          <template #default="{ row }">
            <span class="price-cell">{{ formatPrice(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="原价" width="100" align="center">
          <template #default="{ row }">
            <span class="origin-cell">{{ formatPrice(row.originalPrice || row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="80" align="center" />
        <el-table-column label="销量" width="80" align="center">
          <template #default="{ row }">{{ row.sales ?? 0 }}</template>
        </el-table-column>
        <el-table-column label="详情摘要" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'cell-empty': !plainTextOf(row.detail || '').trim() }">
              {{ detailSummary(row) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="上架" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1 || row.onSale === true"
              @change="(v) => toggleShelf(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="商品详情预览" size="520px" destroy-on-close>
      <div v-if="detailRow" class="detail-preview" v-loading="detailLoading">
        <div class="detail-head">
          <el-image v-if="coverOf(detailRow)" :src="coverOf(detailRow)" fit="cover" class="detail-cover" />
          <div class="detail-meta">
            <h3>{{ detailRow.name || '商品' }}</h3>
            <p>
              <span class="price-cell">{{ formatPrice(detailRow.price) }}</span>
              <em v-if="detailRow.originalPrice">原价 {{ formatPrice(detailRow.originalPrice) }}</em>
            </p>
            <p class="quiet">
              {{ detailRow.shopName || '店铺' }} · {{ detailRow.categoryName || '未分类' }} ·
              库存 {{ detailRow.stock ?? 0 }} · 销量 {{ detailRow.sales ?? 0 }}
            </p>
          </div>
        </div>
        <div class="detail-block">
          <h4>简介</h4>
          <p>{{ detailRow.description || '暂无简介' }}</p>
        </div>
        <div class="detail-block">
          <h4>图册</h4>
          <div v-if="detailImages.length" class="detail-gallery">
            <el-image
              v-for="(img, i) in detailImages"
              :key="`${img}-${i}`"
              :src="img"
              fit="cover"
              class="gallery-item"
              :preview-src-list="detailImages"
              :initial-index="i"
            />
          </div>
          <p v-else class="quiet">暂无图册</p>
        </div>
        <div class="detail-block">
          <h4>详情内容</h4>
          <div
            v-if="plainTextOf(detailRow.detail || '').trim()"
            class="rich"
            v-html="toContentHtml(detailRow.detail)"
          />
          <p v-else class="quiet">暂无富文本详情</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatPrice, mediaUrl, plainTextOf, productCover, toContentHtml } from '@/utils/format'

const loading = ref(false)
const detailLoading = ref(false)
const detailVisible = ref(false)
const detailRow = ref(null)
const detailImages = ref([])
const tableData = ref([])
const total = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function coverOf(row) {
  return productCover(row) || ''
}

function detailSummary(row) {
  const text = plainTextOf(row?.detail || '').trim()
  if (!text) return '—'
  return text.length > 36 ? `${text.slice(0, 36)}…` : text
}

function normalizeUrl(url) {
  return String(url || '')
    .trim()
    .replace(/\\/g, '/')
    .replace(/\?.*$/, '')
}

function parseImages(raw, cover) {
  let list = []
  if (Array.isArray(raw)) list = raw
  else if (typeof raw === 'string' && raw.trim()) {
    try {
      const parsed = JSON.parse(raw)
      list = Array.isArray(parsed) ? parsed : raw.split(',').map((s) => s.trim())
    } catch {
      list = raw.split(',').map((s) => s.trim())
    }
  }
  list = list.filter(Boolean)
  const coverKey = normalizeUrl(cover)
  const seen = new Set()
  const unique = []
  for (const u of list) {
    const key = normalizeUrl(u)
    if (!key || seen.has(key)) continue
    seen.add(key)
    unique.push(u)
  }
  const gallery = unique.filter((u) => normalizeUrl(u) !== coverKey)
  return gallery.length ? gallery : unique.slice()
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/products', {
      params: { keyword: query.keyword || undefined, page: query.page, size: query.size }
    })
    tableData.value = unwrapList(data)
    total.value = Number(data?.total ?? tableData.value.length) || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailRow.value = row
  detailImages.value = parseImages(row?.images, row?.cover).map((u) => mediaUrl(u) || u).filter(Boolean)
  if (!row?.id) return
  detailLoading.value = true
  try {
    const data = await request.get(`/admin/products/${row.id}`, { silent: true })
    if (data) {
      detailRow.value = data
      const cover = data.cover || ''
      detailImages.value = parseImages(data.images, cover).map((u) => mediaUrl(u) || u).filter(Boolean)
      if (!detailImages.value.length && coverOf(data)) {
        detailImages.value = [coverOf(data)]
      }
    }
  } catch {
    /* keep list row */
  } finally {
    detailLoading.value = false
  }
}

async function toggleShelf(row, val) {
  if (val) await request.post(`/admin/products/${row.id}/on-shelf`)
  else await request.post(`/admin/products/${row.id}/off-shelf`)
  ElMessage.success(val ? '已上架' : '已下架')
  await loadData()
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await request.delete(`/admin/products/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.table-ops {
  display: inline-flex;
  gap: 4px;
}

.panel {
  border-radius: 14px;
}

.price-cell {
  color: #e1251b;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
}

.origin-cell {
  color: #9ca3af;
  text-decoration: line-through;
  font-size: 13px;
  font-variant-numeric: tabular-nums;
}

.thumb-cell {
  display: flex;
  justify-content: center;
}

.thumb-img {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  overflow: hidden;
  background: #f1f5f9;
}

.thumb-fallback {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 12px;
  display: grid;
  place-items: center;
}

.cell-empty {
  color: #94a3b8;
}

.detail-preview {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-head {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.detail-cover {
  width: 88px;
  height: 88px;
  border-radius: 12px;
  flex-shrink: 0;
  background: #f1f5f9;
}

.detail-meta {
  min-width: 0;
}

.detail-meta h3 {
  margin: 0 0 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1.4;
}

.detail-meta p {
  margin: 0 0 6px;
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.detail-meta em {
  font-style: normal;
  font-size: 12px;
  color: #9ca3af;
  text-decoration: line-through;
}

.quiet {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
  line-height: 1.6;
}

.detail-block h4 {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 700;
  color: #64748b;
}

.detail-block > p {
  margin: 0;
  font-size: 14px;
  color: #334155;
  line-height: 1.7;
}

.detail-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.gallery-item {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  background: #f1f5f9;
}

.rich {
  font-size: 14px;
  line-height: 1.75;
  color: #1f2937;
  word-break: break-word;
}

.rich :deep(img) {
  max-width: 100%;
  border-radius: 8px;
}

.rich :deep(h3) {
  margin: 12px 0 8px;
  font-size: 16px;
}

.rich :deep(p) {
  margin: 0 0 10px;
}

.rich :deep(ul),
.rich :deep(ol) {
  padding-left: 1.4em;
  margin: 0 0 10px;
}
</style>
