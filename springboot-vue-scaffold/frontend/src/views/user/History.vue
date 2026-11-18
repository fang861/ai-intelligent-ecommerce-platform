<template>
  <div class="page">
    <div class="toolbar" v-if="list.length">
      <span class="count">共 {{ total }} 条足迹</span>
      <button type="button" class="clear-btn" :disabled="clearing" @click="clearAll">
        {{ clearing ? '清空中…' : '清空足迹' }}
      </button>
    </div>

    <div class="content" v-loading="loading">
      <template v-if="grouped.length">
        <section v-for="group in grouped" :key="group.day" class="day-block">
          <header class="day-head">
            <strong>{{ group.label }}</strong>
            <span>{{ group.items.length }} 件商品</span>
          </header>
          <div class="product-grid">
            <article v-for="item in group.items" :key="item.id" class="product-card">
              <router-link :to="productLink(item)" class="cover">
                <img :src="coverOf(item)" alt="" />
              </router-link>
              <div class="info">
                <router-link :to="productLink(item)" class="title">
                  {{ productOf(item)?.name || '商品已下架或不存在' }}
                </router-link>
                <div class="price-row">
                  <strong>{{ formatPrice(productOf(item)?.price) }}</strong>
                  <span class="sales">已售 {{ productOf(item)?.sales ?? 0 }}</span>
                  <span v-if="hasOriginal(item)" class="origin">{{ formatPrice(productOf(item).originalPrice) }}</span>
                </div>
                <div class="meta">
                  <time>{{ formatClock(item.viewedAt || item.updatedAt || item.createdAt) }}</time>
                  <button type="button" class="del" @click="removeOne(item)">删除</button>
                </div>
              </div>
            </article>
          </div>
        </section>
      </template>

      <EmptyState
        v-else-if="!loading"
        type="product"
        title="暂无浏览足迹"
        description="去逛逛商城，看过的商品会出现在这里"
      >
        <router-link to="/user/category" class="empty-cta">去逛逛</router-link>
      </EmptyState>

      <div v-if="total > query.size" class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="load"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatPrice, formatTime, mediaUrl, productCover } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const clearing = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ page: 1, size: 24 })

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function productOf(item) {
  return item?.product || null
}

function productLink(item) {
  const id = item?.productId || productOf(item)?.id
  return id ? `/user/product/${id}` : '/user/category'
}

function coverOf(item) {
  const p = productOf(item)
  return (
    productCover(p) ||
    mediaUrl(p?.cover || item?.productCover) ||
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=240&q=80'
  )
}

function hasOriginal(item) {
  const p = productOf(item)
  const price = Number(p?.price)
  const origin = Number(p?.originalPrice)
  return Number.isFinite(origin) && origin > price
}

function dayKey(raw) {
  if (!raw) return 'unknown'
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return 'unknown'
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function dayLabel(key) {
  if (key === 'unknown') return '更早'
  const now = new Date()
  const today = dayKey(now)
  const y = new Date(now)
  y.setDate(y.getDate() - 1)
  const yesterday = dayKey(y)
  if (key === today) return '今天'
  if (key === yesterday) return '昨天'
  return key.replace(/-/g, '.')
}

function formatClock(raw) {
  if (!raw) return ''
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return formatTime(raw)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const grouped = computed(() => {
  const map = new Map()
  for (const item of list.value) {
    const key = dayKey(item.viewedAt || item.updatedAt || item.createdAt)
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(item)
  }
  return [...map.entries()].map(([day, items]) => ({
    day,
    label: dayLabel(day),
    items
  }))
})

async function load() {
  loading.value = true
  try {
    const data = await request.get('/user/history', {
      params: { page: query.page, size: query.size },
      silent: true
    })
    list.value = unwrapList(data)
    total.value = Number(data?.total ?? list.value.length) || 0
  } catch {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function removeOne(item) {
  const productId = item.productId || productOf(item)?.id
  if (!productId) return
  await request.delete(`/user/history/${productId}`)
  ElMessage.success('已删除该足迹')
  if (list.value.length <= 1 && query.page > 1) query.page -= 1
  await load()
}

async function clearAll() {
  await ElMessageBox.confirm('确定清空全部浏览足迹？此操作不可恢复。', '清空足迹', { type: 'warning' })
  clearing.value = true
  try {
    await request.delete('/user/history')
    ElMessage.success('已清空足迹')
    query.page = 1
    await load()
  } finally {
    clearing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  --accent: #e1251b;
  --ink: #1a1a1a;
  --muted: #8b929c;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding: 12px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
}
.count { font-size: 13px; color: #999; }
.clear-btn {
  height: 34px;
  padding: 0 14px;
  border: 1px solid #e5e5e5;
  border-radius: 10px;
  background: #fff;
  color: #666;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  margin-left: auto;
}
.clear-btn:hover:not(:disabled) {
  border-color: var(--accent);
  color: var(--accent);
}
.clear-btn:disabled { opacity: 0.6; cursor: wait; }

.content {
  padding: 4px 4px 8px;
  min-height: 280px;
}

.day-block { margin-bottom: 18px; }
.day-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 0 4px;
}
.day-head strong { font-size: 15px; }
.day-head span { font-size: 12px; color: #999; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.product-card {
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 8px 22px rgba(26, 26, 26, 0.03);
  transition: 0.2s ease;
}
.product-card:hover {
  border-color: rgba(225, 37, 27, 0.16);
  box-shadow: 0 14px 30px rgba(225, 37, 27, 0.08);
  transform: translateY(-2px);
}

.cover {
  display: block;
  aspect-ratio: 1;
  background: #f7f7f7;
  overflow: hidden;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.25s ease;
}
.product-card:hover .cover img { transform: scale(1.04); }

.info { padding: 12px; }
.title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
  color: var(--ink);
  text-decoration: none;
  font-size: 13px;
  line-height: 1.4;
  font-weight: 650;
}
.title:hover { color: var(--accent); }

.price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 8px;
}
.price-row strong {
  color: var(--accent);
  font-size: 16px;
  font-weight: 780;
}
.price-row .origin {
  color: #bbb;
  font-size: 12px;
  text-decoration: line-through;
}

.price-row .sales {
  font-size: 12px;
  color: #6b7280;
  font-weight: 600;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
}
.meta time { font-size: 12px; color: #aaa; }
.del {
  border: none;
  background: transparent;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
}
.del:hover { color: var(--accent); }

.pager {
  display: flex;
  justify-content: center;
  padding: 12px 0 4px;
}

.empty-cta {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 16px;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  text-decoration: none;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
