<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>商品管理</span>
          <el-button type="primary" @click="openDialog()">新增商品</el-button>
        </div>
      </template>

      <div class="toolbar">
        <el-input v-model="query.keyword" placeholder="搜索商品" clearable style="width: 220px" @keyup.enter="loadData" />
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
        <el-table-column label="分类" width="110" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ categoryNameOf(row) }}
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
              :model-value="row.status === 1 || row.onSale === true || row.shelfStatus === 1"
              @change="(v) => toggleShelf(row, v)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="table-ops">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑商品' : '新增商品'" width="860px" class="product-dialog" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <div class="product-edit-top">
          <el-form-item label="封面" prop="cover" class="cover-item">
            <div class="cover-field">
              <ImageUpload
                v-model="form.cover"
                type="cover"
                shape="square"
                :width="168"
                :height="168"
                upload-text="上传封面"
                tip="仅 1 张，列表/卡片主图"
              />
              <p class="cover-help">封面用于商城卡片、购物车、订单等处展示</p>
            </div>
          </el-form-item>

          <div class="product-edit-main">
            <el-form-item label="名称" prop="name">
              <el-input v-model="form.name" placeholder="商品名称" />
            </el-form-item>
            <el-form-item label="分类" prop="categoryId">
              <el-select v-model="form.categoryId" placeholder="选择分类" style="width: 100%">
                <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
            <div class="price-row">
              <el-form-item label="价格" prop="price">
                <el-input-number
                  v-model="form.price"
                  class="num-field"
                  :min="0"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                />
              </el-form-item>
              <el-form-item label="原价">
                <el-input-number
                  v-model="form.originalPrice"
                  class="num-field"
                  :min="0"
                  :precision="2"
                  :step="1"
                  controls-position="right"
                />
              </el-form-item>
              <el-form-item label="库存" prop="stock">
                <el-input-number
                  v-model="form.stock"
                  class="num-field"
                  :min="0"
                  :step="1"
                  controls-position="right"
                />
              </el-form-item>
            </div>
            <el-form-item label="上架">
              <el-switch v-model="form.onSale" />
            </el-form-item>
          </div>
        </div>

        <el-form-item label="缩略图">
          <MultiImageUpload
            :key="`gallery-${form.id || 'new'}`"
            v-model="form.imageList"
            :max="8"
            tip="详情页图册使用这些图；请上传与商品相关的图片，建议 3～6 张"
          />
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="列表展示用的一句话简介" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="商品详情" prop="detail">
          <RichTextEditor
            :key="`detail-${form.id || 'new'}-${detailEditorKey}`"
            v-model="form.detail"
            placeholder="编辑商品详情，支持标题、列表、图片等"
            min-height="280px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>

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
              {{ categoryNameOf(detailRow) }} · 库存 {{ detailRow.stock ?? 0 }} · 销量 {{ detailRow.sales ?? 0 }}
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
import ImageUpload from '@/components/ImageUpload.vue'
import MultiImageUpload from '@/components/MultiImageUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import { formatPrice, mediaUrl, plainTextOf, productCover, toContentHtml } from '@/utils/format'

const loading = ref(false)
const submitting = ref(false)
const visible = ref(false)
const detailVisible = ref(false)
const detailLoading = ref(false)
const detailRow = ref(null)
const detailImages = ref([])
const tableData = ref([])
const categories = ref([])
const total = ref(0)
const formRef = ref()
const detailEditorKey = ref(0)
const query = reactive({ keyword: '', page: 1, size: 10 })
const form = reactive({
  id: null,
  name: '',
  categoryId: null,
  price: 0,
  originalPrice: 0,
  stock: 0,
  cover: '',
  imageList: [],
  description: '',
  detail: '',
  onSale: true
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  cover: [{ required: true, message: '请上传封面', trigger: 'change' }]
}

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

function categoryNameOf(row) {
  if (row?.categoryName) return row.categoryName
  const hit = categories.value.find((c) => String(c.id) === String(row?.categoryId))
  return hit?.name || '—'
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

  // 去掉与封面完全相同的项；若去掉后为空，则保留原图以便回显
  const gallery = unique.filter((u) => normalizeUrl(u) !== coverKey)
  return gallery.length ? gallery : unique.slice()
}

function serializeImages(list) {
  const urls = (Array.isArray(list) ? list : []).filter(Boolean)
  return JSON.stringify(urls)
}

async function loadCategories() {
  try {
    const data = await request.get('/user/categories', { silent: true })
    categories.value = unwrapList(data)
  } catch {
    categories.value = []
  }
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/merchant/products', {
      params: { ...query, keyword: query.keyword || undefined }
    })
    tableData.value = unwrapList(data)
    total.value = Number(data?.total ?? tableData.value.length) || 0
  } finally {
    loading.value = false
  }
}

function fillForm(row) {
  const cover = row?.cover || row?.image || ''
  const categoryId = row?.categoryId || null
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    categoryId,
    price: Number(row?.price || 0),
    originalPrice: Number(row?.originalPrice || row?.price || 0),
    stock: Number(row?.stock || 0),
    cover,
    imageList: parseImages(row?.images, cover),
    description: row?.description || '',
    detail: row?.detail || row?.content || '',
    onSale: row ? row.status === 1 || row.onSale === true || row.shelfStatus === 1 : true
  })
  detailEditorKey.value += 1
}

async function openDialog(row) {
  if (!row?.id) {
    fillForm(null)
    visible.value = true
    return
  }
  fillForm(row)
  visible.value = true
  try {
    const data = await request.get(`/merchant/products/${row.id}`, { silent: true })
    if (data) fillForm(data)
  } catch {
    /* keep list row data */
  }
}

async function openDetail(row) {
  detailVisible.value = true
  detailRow.value = row
  detailImages.value = parseImages(row?.images, row?.cover).map((u) => mediaUrl(u) || u).filter(Boolean)
  if (!row?.id) return
  detailLoading.value = true
  try {
    const data = await request.get(`/merchant/products/${row.id}`, { silent: true })
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

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const { imageList, onSale, ...rest } = form
    const payload = {
      ...rest,
      detail: form.detail || '',
      description: form.description || '',
      images: serializeImages(imageList),
      status: onSale ? 1 : 0,
      shelfStatus: onSale ? 1 : 0,
      onSale
    }
    if (form.id) await request.put(`/merchant/products/${form.id}`, payload)
    else await request.post('/merchant/products', payload)
    ElMessage.success('已保存')
    visible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function toggleShelf(row, val) {
  if (val) await request.post(`/merchant/products/${row.id}/on-shelf`)
  else await request.post(`/merchant/products/${row.id}/off-shelf`)
  ElMessage.success(val ? '已上架' : '已下架')
  await loadData()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await request.delete(`/merchant/products/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(async () => {
  await loadCategories()
  await loadData()
})
</script>

<style scoped>
.header-row,
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.toolbar {
  justify-content: flex-start;
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

.cover-field {
  width: auto;
}

.cover-help {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
  max-width: 200px;
}

.product-edit-top {
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
  gap: 8px 20px;
  align-items: start;
  margin-bottom: 4px;
}

.cover-item {
  margin-bottom: 0;
}

.product-edit-main {
  min-width: 0;
}

.price-row {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 14px;
  align-items: start;
}

.price-row :deep(.el-form-item) {
  margin-bottom: 18px;
}

.price-row :deep(.el-form-item__content) {
  min-width: 0;
  line-height: normal;
}

.price-row :deep(.el-input-number) {
  width: 100%;
}

.price-row :deep(.num-field.el-input-number) {
  width: 100%;
}

.price-row :deep(.el-input-number .el-input__wrapper) {
  min-height: 36px;
  padding: 0 11px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
}

.price-row :deep(.el-input-number .el-input__inner) {
  height: 34px;
  line-height: 34px;
  text-align: left;
  font-size: 14px;
  font-variant-numeric: tabular-nums;
}

.price-row :deep(.el-input-number__increase),
.price-row :deep(.el-input-number__decrease) {
  width: 32px;
  background: #f5f7fa;
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

:deep(.product-dialog .el-dialog__body) {
  max-height: min(72vh, 780px);
  overflow-y: auto;
}

@media (max-width: 720px) {
  .product-edit-top,
  .price-row {
    grid-template-columns: 1fr;
  }
}
</style>
