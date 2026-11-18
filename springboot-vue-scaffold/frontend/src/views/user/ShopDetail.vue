<template>
  <div class="shop-page" v-loading="loading">
    <template v-if="shop">
      <div class="page-head">
        <div class="head-shell">
          <div class="head-glow" aria-hidden="true" />
          <div class="head-inner">
            <div class="head-copy">
              <nav class="crumb">
                <router-link to="/user/home">首页</router-link>
                <span>/</span>
                <router-link to="/user/shops">店铺街</router-link>
                <span>/</span>
                <em>{{ shop.shopName }}</em>
              </nav>
              <h1>{{ shop.shopName }}</h1>
              <p class="head-desc">
                {{ shop.shopDesc || '品质好物，值得信赖' }}
                · 共 <b>{{ total }}</b> 件在售
              </p>
            </div>
            <div class="head-search">
              <div class="search-panel">
                <el-icon class="search-ico"><Search /></el-icon>
                <el-input
                  v-model="query.keyword"
                  placeholder="搜索店内商品"
                  clearable
                  class="search-input"
                  @keyup.enter="search"
                  @clear="search"
                />
              <button type="button" class="search-btn" @click="search">搜索</button>
            </div>
          </div>
          </div>
        </div>
      </div>

      <section class="shop-hero">
        <div class="hero-row">
          <div class="avatar" :class="{ 'has-img': !!shopAvatar }">
            <img v-if="shopAvatar" :src="shopAvatar" alt="" />
            <span v-else>{{ avatarText }}</span>
          </div>

          <div class="hero-info">
            <div class="title-line">
              <span class="tag">官方认证</span>
              <span class="tag muted">正品保障</span>
              <span class="tag muted">售后无忧</span>
            </div>
            <div class="meta-line">
              <span>在售 <b>{{ shop.productCount ?? 0 }}</b></span>
              <i>|</i>
              <span>销量 <b>{{ shop.salesCount ?? 0 }}</b></span>
              <i>|</i>
              <span>商家发货</span>
            </div>
          </div>

          <div class="hero-right">
            <div class="metric">
              <strong>{{ shop.productCount ?? 0 }}</strong>
              <em>在售</em>
            </div>
            <div class="metric">
              <strong>{{ shop.salesCount ?? 0 }}</strong>
              <em>销量</em>
            </div>
            <button
              type="button"
              class="follow-btn"
              :class="{ followed }"
              :disabled="followActing"
              @click="toggleFollow"
            >
              {{ followed ? '已关注' : '+ 关注' }}
            </button>
          </div>
        </div>
      </section>

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
              {{ query.sort === 'price_asc' ? '↑' : '↓' }}
            </span>
          </button>
          <span class="result-count">共 <b>{{ total }}</b> 件</span>
        </div>
      </div>

      <div class="product-grid" v-loading="productLoading">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
        <EmptyState
          v-if="!productLoading && !products.length"
          type="product"
          title="暂无在售商品"
          description="该店铺暂时还没有可购买的商品"
        />
      </div>

      <div v-if="total > query.size" class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="loadProducts"
        />
      </div>
    </template>
    <EmptyState
      v-else-if="!loading"
      type="shop"
      title="店铺不存在或未开放"
      description="换一家店铺看看，或返回店铺列表"
    >
      <router-link to="/user/shops" class="empty-cta">浏览店铺</router-link>
    </EmptyState>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { mediaUrl } from '@/utils/format'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const productLoading = ref(false)
const followActing = ref(false)
const followed = ref(false)
const shop = ref(null)
const products = ref([])
const total = ref(0)

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' }
]

const query = reactive({
  keyword: '',
  sort: 'default',
  page: 1,
  size: 12
})

const avatarText = computed(() => String(shop.value?.shopName || '店').slice(0, 1))
const shopAvatar = computed(() => mediaUrl(shop.value?.avatar) || '')

function isSortActive(value) {
  if (value === 'price') return String(query.sort).startsWith('price')
  return query.sort === value
}

function changeSort(value) {
  if (value === 'price') {
    query.sort = query.sort === 'price_asc' ? 'price_desc' : 'price_asc'
  } else {
    query.sort = value
  }
  query.page = 1
  loadProducts()
}

function search() {
  query.page = 1
  loadProducts()
}

async function loadShop() {
  loading.value = true
  try {
    shop.value = await request.get(`/user/shops/${route.params.id}`, { silent: true })
    if (userStore.token && shop.value?.id) {
      try {
        followed.value = !!(await request.get(`/user/follows/check/${shop.value.id}`, { silent: true }))
      } catch {
        followed.value = false
      }
    } else {
      followed.value = false
    }
  } catch {
    shop.value = null
  } finally {
    loading.value = false
  }
}

async function toggleFollow() {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  if (!shop.value?.id) return
  followActing.value = true
  try {
    if (followed.value) {
      await request.delete(`/user/follows/${shop.value.id}`)
      followed.value = false
      ElMessage.success('已取消关注')
    } else {
      await request.post('/user/follows', { merchantId: shop.value.id })
      followed.value = true
      ElMessage.success('关注成功')
    }
  } finally {
    followActing.value = false
  }
}

async function loadProducts() {
  if (!route.params.id) return
  productLoading.value = true
  try {
    const data = await request.get('/user/products', {
      params: {
        merchantId: route.params.id,
        keyword: query.keyword.trim() || undefined,
        sort: query.sort,
        page: query.page,
        size: query.size
      },
      silent: true
    })
    products.value = data?.list || data?.records || []
    total.value = Number(data?.total ?? products.value.length)
  } catch {
    products.value = []
    total.value = 0
  } finally {
    productLoading.value = false
  }
}

async function bootstrap() {
  query.keyword = ''
  query.sort = 'default'
  query.page = 1
  await loadShop()
  if (shop.value) await loadProducts()
}

watch(() => route.params.id, bootstrap)
onMounted(bootstrap)
</script>

<style scoped>
.shop-page {
  --accent: #e1251b;
  --ink: #1a1a1a;
  --muted: #8b929c;
  max-width: 1180px;
  margin: 0 auto;
  padding: 22px 24px 64px;
  min-height: 50vh;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.page-head {
  margin-bottom: 14px;
}

.head-shell {
  position: relative;
  overflow: hidden;
  border-radius: 22px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 12px 32px rgba(26, 26, 26, 0.06),
    0 28px 56px rgba(225, 37, 27, 0.05);
}

.head-glow {
  pointer-events: none;
  position: absolute;
  width: 280px;
  height: 180px;
  right: -40px;
  top: -60px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(225, 37, 27, 0.14), transparent 70%);
}

.head-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 28px 26px;
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
  max-width: 520px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.head-desc b {
  color: var(--accent);
  font-weight: 700;
}

.head-search {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.search-panel {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 5px 5px 5px 14px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.06);
  box-shadow:
    0 2px 8px rgba(26, 26, 26, 0.04),
    0 10px 24px rgba(26, 26, 26, 0.06);
  min-width: 320px;
}

.search-panel:focus-within {
  border-color: rgba(225, 37, 27, 0.35);
  box-shadow: 0 0 0 3px rgba(225, 37, 27, 0.08);
}

.search-ico {
  color: #b0b6c0;
  margin-right: 6px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  min-width: 0;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  padding: 0 8px 0 0;
}

.search-btn {
  height: 36px;
  padding: 0 18px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ef4a46, #e1251b);
  color: #fff;
  font-size: 13px;
  font-weight: 720;
  cursor: pointer;
  flex-shrink: 0;
}

.search-btn:hover {
  filter: brightness(1.03);
}

.shop-hero {
  margin-bottom: 14px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #eee;
}

.hero-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 18px;
}

.avatar {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: #fff1f0;
  border: 1px solid #f0e0de;
  color: var(--accent);
  font-size: 22px;
  font-weight: 780;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar.has-img {
  background: #f5f5f5;
  border-color: #eee;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-info {
  flex: 1;
  min-width: 0;
}

.title-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.tag {
  height: 22px;
  padding: 0 8px;
  border-radius: 3px;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.tag.muted {
  background: #f5f5f5;
  color: #888;
}

.meta-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}

.meta-line b {
  color: var(--accent);
  font-weight: 740;
}

.meta-line i {
  font-style: normal;
  color: #ddd;
}

.hero-right {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  padding-left: 12px;
  border-left: 1px solid #f0f0f0;
}

.metric {
  min-width: 56px;
  padding: 0 8px;
  text-align: center;
}

.metric strong {
  display: block;
  font-size: 18px;
  font-weight: 780;
  line-height: 1.15;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.metric em {
  font-style: normal;
  font-size: 11px;
  color: #999;
  font-weight: 600;
}

.follow-btn {
  margin-left: 10px;
  height: 34px;
  min-width: 88px;
  padding: 0 16px;
  border-radius: 4px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.15s ease;
}

.follow-btn:hover:not(:disabled) {
  background: #c81e16;
}

.follow-btn.followed {
  background: #fff;
  color: var(--accent);
  border: 1px solid var(--accent);
}

.follow-btn:disabled {
  opacity: 0.7;
  cursor: wait;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #eee;
}

.sort-group {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  min-width: 0;
}

.sort-btn {
  height: 32px;
  padding: 0 14px;
  border-radius: 4px;
  border: none;
  background: transparent;
  color: #666;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.sort-btn:hover {
  color: var(--accent);
}

.sort-btn.active {
  background: #fff1f0;
  color: var(--accent);
}

.price-dir {
  margin-left: 2px;
  font-size: 12px;
  font-weight: 780;
}

.result-count {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
  font-weight: 600;
}

.result-count b {
  color: var(--accent);
  font-weight: 740;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  min-height: 200px;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

.empty-cta {
  display: inline-flex;
  margin-top: 12px;
  height: 36px;
  padding: 0 16px;
  align-items: center;
  border-radius: 4px;
  background: var(--accent);
  color: #fff;
  text-decoration: none;
  font-size: 13px;
  font-weight: 700;
}

@media (max-width: 900px) {
  .head-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .head-search {
    align-items: stretch;
  }

  .search-panel {
    min-width: 0;
    width: 100%;
  }

  .hero-row {
    flex-wrap: wrap;
  }

  .hero-right {
    width: 100%;
    padding-left: 0;
    padding-top: 12px;
    border-left: none;
    border-top: 1px solid #f0f0f0;
    justify-content: flex-start;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .head-desc {
    white-space: normal;
  }

  .metric {
    min-width: 48px;
    padding: 0 6px;
  }
}
</style>


