<template>
  <div class="category-page">
    <div class="page-head">
      <div class="head-shell">
        <div class="head-glow" aria-hidden="true" />
        <div class="head-inner">
          <div class="head-copy">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <router-link to="/user/category">商城</router-link>
              <template v-if="query.categoryId">
                <span>/</span>
                <em>{{ activeCategoryName }}</em>
              </template>
            </nav>
            <h1>{{ activeCategoryName }}</h1>
            <p class="head-desc">发现心仪好物 · 共 <b>{{ total }}</b> 件在售</p>
          </div>
        </div>
      </div>
    </div>

    <div class="page-body">
      <div class="cat-strip">
        <button
          type="button"
          class="cat-pill"
          :class="{ active: !query.categoryId }"
          @click="selectCategory(null)"
        >
          全部
        </button>
        <button
          v-for="c in categories"
          :key="c.id"
          type="button"
          class="cat-pill"
          :class="{ active: String(query.categoryId) === String(c.id) }"
          @click="selectCategory(c.id)"
        >
          {{ c.name }}
        </button>
      </div>

      <div class="toolbar">
        <div class="sort-group">
          <button
            v-for="s in sortOptions"
            :key="s.value"
            type="button"
            class="sort-btn"
            :class="{ active: isSortActive(s.value) }"
            @click="changeSort(s.value)"
          >
            {{ s.label }}
            <span v-if="s.value === 'price' && String(query.sort).startsWith('price')" class="price-dir">
              {{ query.sort === 'price_asc' ? '低→高' : '高→低' }}
            </span>
          </button>
        </div>
        <span class="count">{{ total }} 件商品</span>
      </div>

      <div class="product-grid" v-loading="loading">
        <ProductCard
          v-for="(p, idx) in products"
          :key="p.id"
          :product="p"
          class="reveal"
          :style="{ '--delay': `${Math.min(idx, 8) * 35}ms` }"
        />
        <EmptyState
          v-if="!loading && !products.length"
          type="search"
          title="暂无相关商品"
          description="试试其他分类或换个关键词再搜一次"
        />
      </div>

      <div v-if="total > query.size" class="pager">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="loadProducts"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '@/api/request'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const categories = ref([])
const products = ref([])
const total = ref(0)

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' }
]

const query = reactive({
  keyword: '',
  categoryId: '',
  sort: 'default',
  page: 1,
  size: 12
})

const activeCategoryName = computed(() => {
  if (!query.categoryId) return '全部商品'
  const hit = categories.value.find((c) => String(c.id) === String(query.categoryId))
  return hit?.name || '分类商品'
})

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || data?.content || []
}

function syncRoute() {
  router.replace({
    path: '/user/category',
    query: {
      ...(query.keyword ? { keyword: query.keyword } : {}),
      ...(query.categoryId ? { categoryId: query.categoryId } : {}),
      ...(query.sort && query.sort !== 'default' ? { sort: query.sort } : {})
    }
  })
}

function selectCategory(id) {
  query.categoryId = id == null ? '' : id
  query.page = 1
  syncRoute()
}

function changeSort(value) {
  if (value === 'price') {
    query.sort = query.sort === 'price_asc' ? 'price_desc' : 'price_asc'
  } else {
    query.sort = value
  }
  query.page = 1
  syncRoute()
}

function isSortActive(value) {
  if (value === 'price') return String(query.sort).startsWith('price')
  return query.sort === value
}

function search() {
  query.page = 1
  syncRoute()
}

async function loadCategories() {
  try {
    const data = await request.get('/user/categories', { silent: true })
    categories.value = unwrapList(data)
  } catch {
    categories.value = []
  }
}

async function loadProducts() {
  loading.value = true
  try {
    const data = await request.get('/user/products', {
      params: {
        keyword: query.keyword || undefined,
        categoryId: query.categoryId || undefined,
        sort: query.sort || 'default',
        page: query.page,
        size: query.size
      },
      silent: true
    })
    products.value = unwrapList(data)
    total.value = Number(data?.total ?? products.value.length) || 0
  } catch {
    products.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

watch(
  () => ({
    keyword: route.query.keyword,
    categoryId: route.query.categoryId,
    sort: route.query.sort,
    _: route.query._
  }),
  (q) => {
    query.keyword = typeof q.keyword === 'string' ? q.keyword : ''
    query.categoryId = q.categoryId != null && q.categoryId !== '' ? String(q.categoryId) : ''
    query.sort = typeof q.sort === 'string' ? q.sort : 'default'
    loadProducts()
  },
  { immediate: true }
)

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.category-page {
  --ink: #1a1a1a;
  --muted: #6b7280;
  --accent: #e63935;
  --line: rgba(26, 26, 26, 0.08);
  --surface: #ffffff;
  min-height: 70vh;
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
  color: var(--ink);
  background:
    radial-gradient(ellipse 55% 30% at 100% 0%, rgba(230, 57, 53, 0.05), transparent 50%),
    #f7f8fa;
  animation: pageIn 0.45s ease;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-head {
  padding: 20px 24px 0;
}

.head-shell {
  position: relative;
  max-width: 1180px;
  margin: 0 auto;
  overflow: hidden;
  border-radius: 22px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 12px 32px rgba(26, 26, 26, 0.06),
    0 28px 56px rgba(230, 57, 53, 0.05);
}

.head-glow {
  pointer-events: none;
  position: absolute;
  width: 280px;
  height: 180px;
  right: -40px;
  top: -60px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.14), transparent 70%);
}

.head-inner {
  position: relative;
  z-index: 1;
  padding: 28px 28px 26px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.crumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  font-size: 12px;
  color: #9ca3af;
}

.crumb a {
  color: #9ca3af;
  text-decoration: none;
  transition: color 0.2s ease;
}

.crumb a:hover {
  color: var(--accent);
}

.crumb em {
  font-style: normal;
  color: var(--ink);
  font-weight: 600;
}

.head-copy h1 {
  margin: 0 0 10px;
  font-size: clamp(26px, 3vw, 34px);
  font-weight: 760;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.head-desc {
  margin: 0;
  color: var(--muted);
  font-size: 13.5px;
}

.head-desc b {
  color: var(--accent);
  font-weight: 700;
}

.page-body {
  max-width: 1180px;
  margin: 0 auto;
  padding: 22px 24px 64px;
}

.cat-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 18px;
}

.cat-pill {
  height: 36px;
  padding: 0 16px;
  border-radius: 999px;
  border: 1px solid var(--line);
  background: #fff;
  color: #4b5563;
  font-size: 13px;
  font-weight: 550;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cat-pill:hover {
  border-color: rgba(230, 57, 53, 0.3);
  color: var(--accent);
}

.cat-pill.active {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
  font-weight: 650;
  box-shadow: 0 8px 18px rgba(230, 57, 53, 0.22);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  padding: 8px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow: 0 8px 24px rgba(26, 26, 26, 0.03);
}

.sort-group {
  display: flex;
  gap: 4px;
  padding: 2px;
  border-radius: 10px;
  background: #f3f4f6;
}

.sort-btn {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: #6b7280;
  font-size: 13px;
  font-weight: 550;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.sort-btn:hover {
  color: var(--ink);
}

.sort-btn.active {
  background: #fff;
  color: var(--accent);
  font-weight: 700;
  box-shadow: 0 2px 8px rgba(26, 26, 26, 0.06);
}

.price-dir {
  margin-left: 4px;
  font-size: 11px;
  font-weight: 600;
  opacity: 0.85;
}

.count {
  padding-right: 10px;
  color: var(--muted);
  font-size: 13px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  min-height: 240px;
}

.reveal {
  animation: riseIn 0.4s ease both;
  animation-delay: var(--delay, 0ms);
}

@keyframes riseIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-wrap {
  grid-column: 1 / -1;
  padding: 56px 0;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.8);
  border: 1px dashed rgba(26, 26, 26, 0.1);
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.pager :deep(.el-pagination.is-background .el-pager li.is-active) {
  background: var(--accent);
}

@media (max-width: 960px) {
  .page-head {
    padding: 16px 16px 0;
  }

  .head-inner {
    flex-direction: column;
    align-items: stretch;
    padding: 22px 18px 18px;
  }

  .page-body {
    padding: 16px 16px 56px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .sort-group {
    width: 100%;
  }

  .sort-btn {
    flex: 1;
  }

  .count {
    text-align: right;
    padding: 0 4px;
  }

  .cat-strip {
    flex-wrap: nowrap;
    overflow-x: auto;
    scrollbar-width: none;
    padding-bottom: 4px;
  }

  .cat-strip::-webkit-scrollbar {
    display: none;
  }

  .cat-pill {
    flex-shrink: 0;
  }
}
</style>
