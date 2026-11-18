<template>
  <div class="detail-page" v-loading="loading">
    <template v-if="product">
      <nav class="crumb">
        <router-link to="/user/home">首页</router-link>
        <span>/</span>
        <router-link to="/user/category">商城</router-link>
        <span>/</span>
        <em>{{ product.categoryName || '商品详情' }}</em>
      </nav>

      <div class="detail-shell">
        <div class="shell-glow" aria-hidden="true" />
        <div class="detail-top">
          <div class="gallery">
            <div class="thumbs-col">
              <div ref="thumbsRef" class="thumbs" @scroll="onThumbsScroll">
                <button
                  v-for="(img, i) in images"
                  :key="`${img}-${i}`"
                  type="button"
                  class="thumb"
                  :class="{ active: i === activeIdx }"
                  @click="activeIdx = i"
                >
                  <img :src="img" alt="" />
                </button>
              </div>
              <button
                v-if="canScrollThumbs"
                type="button"
                class="thumbs-more"
                aria-label="查看更多图片"
                @click="scrollThumbs"
              >
                <span>⌄</span>
              </button>
            </div>

            <div class="main-img">
              <img :src="activeImage" :alt="product.name" />
              <button
                v-if="images.length > 1"
                type="button"
                class="nav-btn prev"
                aria-label="上一张"
                @click="prevImage"
              >
                ‹
              </button>
              <button
                v-if="images.length > 1"
                type="button"
                class="nav-btn next"
                aria-label="下一张"
                @click="nextImage"
              >
                ›
              </button>
            </div>
          </div>

          <div class="info">
            <div class="info-top">
            <div class="shop-row">
              <router-link
                v-if="product.merchantId"
                :to="`/user/shop/${product.merchantId}`"
                class="shop-pill"
              >{{ product.shopName || '官方店铺' }}</router-link>
              <span v-else class="shop-pill">{{ product.shopName || '官方店铺' }}</span>
              <span v-if="product.categoryName" class="cat-pill">{{ product.categoryName }}</span>
              <router-link
                v-if="product.merchantId"
                :to="`/user/shop/${product.merchantId}`"
                class="enter-shop"
              >进店逛逛</router-link>
            </div>
              <h1>{{ product.name }}</h1>
              <p class="desc">{{ shortDesc }}</p>
            </div>

            <div class="price-panel">
              <div class="price-main">
                <span class="price-label">售价</span>
                <div class="price-line">
                  <span class="price">{{ formatPrice(product.price) }}</span>
                  <span v-if="hasOriginal" class="origin">{{ formatPrice(product.originalPrice) }}</span>
                </div>
              </div>
              <div class="price-stats">
                <div class="stat">
                  <span>销量</span>
                  <strong>{{ product.sales ?? 0 }}</strong>
                </div>
                <div class="stat">
                  <span>库存</span>
                  <strong :class="{ warn: isLowStock }">{{ stockLabel }}</strong>
                </div>
                <div class="stat">
                  <span>评价</span>
                  <strong>{{ reviews.length }}</strong>
                </div>
              </div>
            </div>

            <div class="buy-panel">
              <div class="qty-row">
                <span class="qty-label">数量</span>
                <el-input-number
                  v-model="quantity"
                  class="qty-input"
                  :min="1"
                  :max="Math.max(1, product.stock || 99)"
                />
                <span class="qty-tip">支持加入购物车后统一结算</span>
              </div>

              <div class="actions">
                <button type="button" class="btn-buy" :disabled="buying" @click="buyNow">
                  {{ buying ? '处理中…' : '立即购买' }}
                </button>
                <button type="button" class="btn-cart" :disabled="carting" @click="addCart">
                  {{ carting ? '加入中…' : '加入购物车' }}
                </button>
                <button
                  type="button"
                  class="btn-fav"
                  :class="{ on: favorited }"
                  @click="toggleFavorite"
                >
                  {{ favorited ? '已收藏' : '收藏' }}
                </button>
              </div>
            </div>

            <ul class="trust-row">
              <li>正品保障</li>
              <li>售后无忧</li>
              <li>AI 智能导购</li>
            </ul>
          </div>
        </div>
      </div>

      <div class="detail-tabs">
        <div class="tab-bar" role="tablist">
          <button
            v-for="item in tabItems"
            :key="item.name"
            type="button"
            role="tab"
            class="tab-btn"
            :class="{ active: tab === item.name }"
            :aria-selected="tab === item.name"
            @click="tab = item.name"
          >
            <span class="tab-label">{{ item.label }}</span>
            <span v-if="item.count != null" class="tab-count">{{ item.count }}</span>
          </button>
        </div>

        <div class="tab-panels">
          <div v-show="tab === 'detail'" class="tab-panel" role="tabpanel">
            <div v-if="hasDetail" class="rich" v-html="detailHtml" />
            <div v-else class="panel-empty">
              <EmptyState compact type="product" title="暂无详情" description="商家暂未填写商品详情" />
            </div>
          </div>

          <div v-show="tab === 'reviews'" class="tab-panel" role="tabpanel">
            <div v-if="reviews.length" class="review-list">
              <div v-for="r in reviews" :key="r.id" class="review-item">
                <div class="review-head">
                  <div class="avatar" :class="{ 'has-img': !!reviewAvatar(r) }">
                    <img v-if="reviewAvatar(r)" :src="reviewAvatar(r)" alt="" />
                    <template v-else>{{ (r.nickname || r.username || '用').slice(0, 1) }}</template>
                  </div>
                  <div class="review-meta">
                    <strong>{{ r.nickname || r.username || '用户' }}</strong>
                    <el-rate :model-value="Number(r.score || r.rating || 5)" disabled />
                  </div>
                  <span class="time">{{ formatTime(r.createdAt) }}</span>
                </div>
                <p>{{ r.content || r.comment }}</p>
              </div>
            </div>
            <div v-else class="panel-empty">
              <EmptyState compact type="default" title="暂无评价" description="购买后可以来分享你的使用体验" />
            </div>
          </div>

          <div v-show="tab === 'qa'" class="tab-panel" role="tabpanel">
            <div class="qa-box">
              <div class="ask-card">
                <p class="ask-title">向商家提问</p>
                <div class="ask-row">
                  <el-input
                    v-model="question"
                    placeholder="想问商家什么？例如尺码、发货时间、适用场景…"
                    maxlength="200"
                    size="large"
                    show-word-limit
                  />
                  <button type="button" class="btn-ask" :disabled="asking" @click="submitQuestion">
                    {{ asking ? '提交中…' : '提问' }}
                  </button>
                </div>
              </div>

              <div v-if="qaList.length" class="qa-list">
                <div v-for="q in qaList" :key="q.id" class="qa-item">
                  <p class="q"><span class="qa-mark q-mark">问</span>{{ q.question || q.content }}</p>
                  <p class="a"><span class="qa-mark a-mark">答</span>{{ q.answer || '商家暂未回复' }}</p>
                </div>
              </div>
              <div v-else class="panel-empty">
                <EmptyState compact type="default" title="暂无问答" description="有疑问可以先向商家提问" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <section v-if="similar.length" class="similar">
        <div class="similar-shell">
          <div class="similar-head">
            <div class="similar-title">
              <span class="title-mark" aria-hidden="true" />
              <div class="title-copy">
                <h2>看了又看</h2>
                <p>根据当前商品为你精选相近好物</p>
              </div>
              <span class="title-tag">相似推荐</span>
            </div>
            <router-link to="/user/category" class="more-link">
              去逛逛
              <span aria-hidden="true">›</span>
            </router-link>
          </div>
          <div class="product-grid">
            <ProductCard v-for="p in similar" :key="p.id" :product="p" badge="相似" />
          </div>
        </div>
      </section>
    </template>
    <EmptyState
      v-else-if="!loading"
      type="product"
      title="商品不存在或已下架"
      description="看看其他好物，或返回分类继续逛"
    >
      <router-link to="/user/category" class="empty-cta">去分类逛逛</router-link>
    </EmptyState>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { formatPrice, formatTime, mediaUrl, plainTextOf, productCover, toContentHtml } from '@/utils/format'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const carting = ref(false)
const buying = ref(false)
const asking = ref(false)
const product = ref(null)
const reviews = ref([])
const qaList = ref([])
const similar = ref([])
const favorited = ref(false)
const quantity = ref(1)
const activeIdx = ref(0)
const tab = ref('detail')
const question = ref('')
const thumbsRef = ref(null)
const canScrollThumbs = ref(false)

const tabItems = computed(() => [
  { name: 'detail', label: '商品详情' },
  { name: 'reviews', label: '评价', count: reviews.value.length },
  { name: 'qa', label: '问答', count: qaList.value.length }
])

const images = computed(() => {
  const p = product.value
  if (!p) return []
  let gallery = []
  if (Array.isArray(p.images)) gallery = p.images
  else if (typeof p.images === 'string' && p.images) {
    try {
      const parsed = JSON.parse(p.images)
      gallery = Array.isArray(parsed) ? parsed : p.images.split(',').map((s) => s.trim())
    } catch {
      gallery = p.images.split(',').map((s) => s.trim())
    }
  }
  gallery = gallery.filter(Boolean)

  const list = []
  const pushUnique = (raw) => {
    const url = mediaUrl(raw)
    if (url && !list.includes(url)) list.push(url)
  }
  if (p.cover) pushUnique(p.cover)
  gallery.forEach(pushUnique)

  if (!list.length) {
    pushUnique(
      productCover(p) ||
        'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80'
    )
  }
  return list
})

const activeImage = computed(() => images.value[activeIdx.value] || images.value[0])

function reviewAvatar(r) {
  return mediaUrl(r?.avatar || r?.userAvatar) || ''
}

function prevImage() {
  const len = images.value.length
  if (!len) return
  activeIdx.value = (activeIdx.value - 1 + len) % len
}

function nextImage() {
  const len = images.value.length
  if (!len) return
  activeIdx.value = (activeIdx.value + 1) % len
}

function onThumbsScroll() {
  const el = thumbsRef.value
  if (!el) return
  canScrollThumbs.value = el.scrollHeight - el.scrollTop - el.clientHeight > 8
}

function scrollThumbs() {
  const el = thumbsRef.value
  if (!el) return
  el.scrollBy({ top: 88, behavior: 'smooth' })
}

async function refreshThumbsScroll() {
  await nextTick()
  const el = thumbsRef.value
  if (!el) {
    canScrollThumbs.value = false
    return
  }
  canScrollThumbs.value = el.scrollHeight > el.clientHeight + 4
}

watch(images, () => {
  activeIdx.value = 0
  refreshThumbsScroll()
})

watch(activeIdx, async () => {
  await nextTick()
  const el = thumbsRef.value
  if (!el) return
  const active = el.querySelector('.thumb.active')
  active?.scrollIntoView({ block: 'nearest', behavior: 'smooth' })
})

const detailHtml = computed(() =>
  toContentHtml(product.value?.detail || product.value?.content || '')
)

const hasDetail = computed(() => {
  const raw = String(product.value?.detail || product.value?.content || '').trim()
  return !!plainTextOf(raw)
})

const shortDesc = computed(() => {
  const text = plainTextOf(product.value?.description || product.value?.summary || '')
  if (text) return text
  const fromDetail = plainTextOf(product.value?.detail || '')
  return fromDetail || '精选好物，品质保证'
})

const hasOriginal = computed(() => {
  const price = Number(product.value?.price)
  const origin = Number(product.value?.originalPrice)
  return Number.isFinite(origin) && Number.isFinite(price) && origin > price
})

const isLowStock = computed(() => {
  const stock = Number(product.value?.stock)
  return Number.isFinite(stock) && stock > 0 && stock <= 30
})

const stockLabel = computed(() => {
  const stock = Number(product.value?.stock)
  if (!Number.isFinite(stock)) return '—'
  if (stock <= 0) return '缺货'
  if (stock <= 30) return `仅剩 ${stock}`
  return String(stock)
})

function requireLogin() {
  if (!userStore.token) {
    ElMessage.warning('请先登录')
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function loadDetail() {
  loading.value = true
  activeIdx.value = 0
  quantity.value = 1
  tab.value = 'detail'
  try {
    const id = route.params.id
    const data = await request.get(`/user/products/${id}`, { silent: true })
    product.value = data
    reviews.value = data?.reviews || data?.reviewList || []
    qaList.value = data?.qaList || data?.questions || []
    favorited.value = !!(data?.favorited || data?.isFavorite)
    if (!reviews.value.length) {
      try {
        const r = await request.get(`/user/products/${id}/reviews`, { silent: true })
        reviews.value = Array.isArray(r) ? r : r?.list || []
      } catch { /* ignore */ }
    }
    if (!qaList.value.length) {
      try {
        const q = await request.get(`/user/products/${id}/qa`, { silent: true })
        qaList.value = Array.isArray(q) ? q : q?.list || []
      } catch { /* ignore */ }
    }
    try {
      const sim = await request.get('/user/products/recommend', {
        params: { productId: id, mode: 'itemcf', limit: 8 },
        silent: true
      })
      similar.value = (Array.isArray(sim) ? sim : sim?.list || [])
        .filter((x) => String(x.id) !== String(id))
        .slice(0, 4)
    } catch {
      similar.value = []
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
    refreshThumbsScroll()
  }
}

async function addCart() {
  if (!requireLogin()) return
  carting.value = true
  try {
    await request.post('/user/cart', { productId: product.value.id, quantity: quantity.value })
    ElMessage.success('已加入购物车')
  } finally {
    carting.value = false
  }
}

async function buyNow() {
  if (!requireLogin()) return
  buying.value = true
  try {
    await request.post('/user/cart', { productId: product.value.id, quantity: quantity.value })
    router.push('/user/cart')
  } finally {
    buying.value = false
  }
}

async function toggleFavorite() {
  if (!requireLogin()) return
  try {
    if (favorited.value) {
      await request.delete(`/user/favorites/${product.value.id}`)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await request.post('/user/favorites', { productId: product.value.id })
      favorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch { /* handled */ }
}

async function submitQuestion() {
  if (!requireLogin()) return
  if (!question.value.trim()) {
    ElMessage.warning('请输入问题')
    return
  }
  asking.value = true
  try {
    await request.post('/user/qa', { productId: product.value.id, question: question.value.trim() })
    ElMessage.success('已提交问题')
    question.value = ''
    await loadDetail()
  } finally {
    asking.value = false
  }
}

watch(() => route.params.id, loadDetail)
onMounted(loadDetail)
</script>

<style scoped>
.detail-page {
  --accent: #e63935;
  --ink: #1a1a1a;
  --muted: #8b929c;
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 28px 80px;
  min-height: 50vh;
}

.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--muted);
}

.crumb a {
  color: var(--muted);
  text-decoration: none;
  transition: color 0.2s;
}

.crumb a:hover {
  color: var(--accent);
}

.crumb em {
  font-style: normal;
  color: #4b5563;
}

.detail-shell {
  position: relative;
  margin-bottom: 28px;
  border-radius: 24px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 12px 36px rgba(15, 23, 42, 0.07);
  overflow: hidden;
}

.shell-glow {
  position: absolute;
  right: -80px;
  top: -100px;
  width: 280px;
  height: 280px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.12), transparent 68%);
  pointer-events: none;
}

.detail-top {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(300px, 0.92fr);
  gap: 28px;
  padding: 24px 28px;
  align-items: start;
}

.gallery {
  display: grid;
  grid-template-columns: 72px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
  width: 100%;
  max-width: 640px;
}

.main-img {
  position: relative;
  width: 100%;
  height: 480px;
  border-radius: 14px;
  overflow: hidden;
  background: #f3f4f6;
  box-shadow: inset 0 0 0 1px rgba(15, 23, 42, 0.04);
}

.main-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.nav-btn {
  position: absolute;
  top: 50%;
  z-index: 2;
  width: 34px;
  height: 34px;
  margin-top: -17px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.94);
  color: #374151;
  font-size: 22px;
  line-height: 1;
  cursor: pointer;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.14);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s, transform 0.2s;
}

.main-img:hover .nav-btn {
  opacity: 1;
}

.nav-btn.prev {
  left: 10px;
}

.nav-btn.next {
  right: 10px;
}

.nav-btn:hover {
  transform: scale(1.06);
  color: var(--accent);
}

.thumbs-col {
  display: flex;
  flex-direction: column;
  width: 72px;
  height: 480px;
  min-height: 0;
  align-self: start;
}

.thumbs {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-height: 0;
  overflow-y: auto;
  overflow-x: hidden;
  scrollbar-width: none;
}

.thumbs::-webkit-scrollbar {
  display: none;
}

.thumb {
  width: 72px;
  height: 72px;
  flex: 0 0 72px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  background: #f3f4f6;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.thumb:hover {
  border-color: rgba(230, 57, 53, 0.35);
}

.thumb.active {
  border-color: var(--accent);
  box-shadow: 0 0 0 1px rgba(230, 57, 53, 0.12);
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumbs-more {
  flex-shrink: 0;
  margin-top: 10px;
  width: 72px;
  height: 28px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 8px;
  background: #fff;
  color: #6b7280;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.thumbs-more:hover {
  color: var(--accent);
  border-color: rgba(230, 57, 53, 0.28);
}

.info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 0;
  padding-top: 4px;
}

.info-top {
  margin-bottom: 4px;
}

.shop-row {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.shop-pill,
.cat-pill {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  text-decoration: none;
}

.shop-pill {
  background: #111827;
  color: #fff;
}

.shop-pill:hover {
  background: #1f2937;
}

.cat-pill {
  background: #fff1f0;
  color: var(--accent);
}

.enter-shop {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  color: var(--accent);
  background: #fff;
  border: 1px solid rgba(230, 57, 53, 0.28);
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  transition: background 0.2s;
}

.enter-shop:hover {
  background: #fff5f4;
}

.info h1 {
  margin: 0 0 12px;
  font-size: 28px;
  font-weight: 760;
  line-height: 1.35;
  letter-spacing: -0.02em;
  color: var(--ink);
}

.desc {
  margin: 0;
  color: #6b7280;
  line-height: 1.75;
  font-size: 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.price-panel {
  margin-top: 28px;
  padding: 22px 24px;
  border-radius: 18px;
  background:
    linear-gradient(135deg, rgba(230, 57, 53, 0.08), rgba(255, 244, 242, 0.95) 42%, #fff);
  border: 1px solid rgba(230, 57, 53, 0.1);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.price-label {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: #b4534a;
  font-weight: 600;
}

.price-line {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 12px;
}

.price {
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--accent);
  line-height: 1;
}

.origin {
  color: #a1a8b3;
  text-decoration: line-through;
  font-size: 14px;
}

.price-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 18px;
  padding-top: 16px;
  border-top: 1px dashed rgba(230, 57, 53, 0.18);
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.stat span {
  font-size: 12px;
  color: var(--muted);
}

.stat strong {
  font-size: 16px;
  color: var(--ink);
  font-weight: 700;
}

.stat strong.warn {
  color: #d97706;
}

.buy-panel {
  margin-top: 24px;
  padding: 18px 18px 16px;
  border-radius: 14px;
  background: #fafbfc;
  border: 1px solid rgba(26, 26, 26, 0.05);
}

.qty-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.qty-label {
  color: #6b7280;
  font-size: 13px;
  font-weight: 650;
  min-width: 36px;
}

.qty-input {
  width: 132px;
}

.qty-input :deep(.el-input-number) {
  width: 132px;
}

.qty-input :deep(.el-input-number__decrease),
.qty-input :deep(.el-input-number__increase) {
  width: 34px;
  background: #fff;
  color: #4b5563;
  border-color: #e5e7eb;
}

.qty-input :deep(.el-input-number__decrease:hover),
.qty-input :deep(.el-input-number__increase:hover) {
  color: #e1251b;
  background: #fff8f7;
}

.qty-input :deep(.el-input__wrapper) {
  padding-left: 36px;
  padding-right: 36px;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
  border-radius: 8px;
  background: #fff;
}

.qty-input :deep(.el-input__inner) {
  text-align: center;
  font-weight: 700;
  color: #1a1a1a;
}

.qty-tip {
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.4;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 16px;
}

.btn-buy,
.btn-cart,
.btn-fav {
  height: 46px;
  padding: 0 22px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 720;
  font-family: inherit;
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease, color 0.18s ease, border-color 0.18s ease;
}

.btn-buy {
  min-width: 140px;
  color: #fff;
  background: #e1251b;
  box-shadow: 0 8px 18px rgba(225, 37, 27, 0.28);
}

.btn-buy:hover:not(:disabled) {
  background: #c9181a;
  transform: translateY(-1px);
  box-shadow: 0 10px 22px rgba(225, 37, 27, 0.34);
}

.btn-cart {
  min-width: 128px;
  color: #e1251b;
  background: #fff;
  border: 1.5px solid #e1251b;
}

.btn-cart:hover:not(:disabled) {
  background: #fff5f4;
  transform: translateY(-1px);
}

.btn-fav {
  min-width: 88px;
  color: #fff;
  background: #64748b;
  border: 1.5px solid #64748b;
}

.btn-fav:hover {
  color: #fff;
  background: #475569;
  border-color: #475569;
  transform: translateY(-1px);
}

.btn-fav.on {
  color: #fff;
  background: #f59e0b;
  border-color: #f59e0b;
  box-shadow: 0 6px 14px rgba(245, 158, 11, 0.28);
}

.btn-fav.on:hover {
  background: #d97706;
  border-color: #d97706;
}

.btn-buy:disabled,
.btn-cart:disabled,
.btn-ask:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.trust-row {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: 12px 22px;
  margin: 28px 0 0;
  padding: 18px 0 0;
  border-top: 1px solid rgba(15, 23, 42, 0.06);
}

.trust-row li {
  position: relative;
  padding-left: 14px;
  font-size: 12px;
  color: var(--muted);
}

.trust-row li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
  transform: translateY(-50%);
}

.detail-tabs {
  border-radius: 22px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 12px 32px rgba(15, 23, 42, 0.06);
  margin-bottom: 32px;
  overflow: hidden;
}

.tab-bar {
  display: flex;
  gap: 8px;
  padding: 16px 20px 0;
  background:
    linear-gradient(180deg, #fafbfc 0%, #fff 100%);
  border-bottom: 1px solid rgba(15, 23, 42, 0.06);
}

.tab-btn {
  position: relative;
  height: 42px;
  padding: 0 18px 10px;
  border: none;
  background: transparent;
  color: #8b929c;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: color 0.2s;
}

.tab-btn:hover {
  color: #4b5563;
}

.tab-btn.active {
  color: var(--accent);
  font-weight: 760;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  left: 18px;
  right: 18px;
  bottom: 0;
  height: 3px;
  border-radius: 999px 999px 0 0;
  background: linear-gradient(90deg, #e63935, #ff6b4a);
  box-shadow: 0 4px 10px rgba(230, 57, 53, 0.28);
}

.tab-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.tab-btn.active .tab-count {
  background: rgba(230, 57, 53, 0.12);
  color: var(--accent);
}

.tab-panels {
  padding: 22px 24px 26px;
}

.tab-panel {
  min-height: 140px;
}

.panel-empty {
  padding: 20px 0 8px;
}

.rich {
  padding: 4px 2px 0;
  color: #374151;
  line-height: 1.85;
  font-size: 14px;
}

.rich :deep(img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 12px 0;
  display: block;
}

.rich :deep(h1),
.rich :deep(h2),
.rich :deep(h3) {
  margin: 16px 0 10px;
  color: #1a1a1a;
  font-weight: 720;
  line-height: 1.4;
}

.rich :deep(h3) {
  font-size: 18px;
}

.rich :deep(p) {
  margin: 0 0 12px;
}

.rich :deep(ul),
.rich :deep(ol) {
  margin: 0 0 12px;
  padding-left: 1.4em;
}

.rich :deep(a) {
  color: var(--accent);
}

.review-list,
.qa-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-item {
  padding: 16px 18px;
  border-radius: 16px;
  background: #fafbfc;
  border: 1px solid rgba(15, 23, 42, 0.05);
}

.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fee2e2, #fecaca);
  color: var(--accent);
  font-size: 14px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 6px 14px rgba(230, 57, 53, 0.12);
  overflow: hidden;
}

.avatar.has-img {
  background: #f3f4f6;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.08);
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.review-meta {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-meta strong {
  font-size: 14px;
  color: var(--ink);
}

.review-item p {
  margin: 0;
  color: #4b5563;
  line-height: 1.75;
  font-size: 14px;
}

.time {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

.ask-card {
  margin-bottom: 18px;
  padding: 18px 18px 16px;
  border-radius: 16px;
  background:
    linear-gradient(135deg, rgba(230, 57, 53, 0.05), rgba(255, 255, 255, 0.96));
  border: 1px solid rgba(230, 57, 53, 0.1);
}

.ask-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 700;
  color: #1a1a1a;
}

.ask-row {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.ask-row :deep(.el-input__wrapper) {
  border-radius: 12px;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08) inset;
}

.btn-ask {
  flex-shrink: 0;
  height: 40px;
  min-width: 96px;
  margin-top: 0;
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  box-shadow: 0 8px 18px rgba(230, 57, 53, 0.25);
}

.btn-ask:hover:not(:disabled) {
  transform: translateY(-1px);
}

.qa-item {
  padding: 16px 18px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.03);
}

.qa-item .q,
.qa-item .a {
  display: flex;
  gap: 10px;
  margin: 0;
  line-height: 1.7;
  font-size: 14px;
}

.qa-item .q {
  margin-bottom: 12px;
  color: var(--ink);
}

.qa-item .a {
  color: #6b7280;
}

.qa-mark {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}

.q-mark {
  background: #111827;
  color: #fff;
}

.a-mark {
  background: #fff1f0;
  color: var(--accent);
}

.similar {
  margin-top: 8px;
}

.similar-shell {
  padding: 22px 22px 20px;
  border-radius: 20px;
  background:
    radial-gradient(ellipse 48% 75% at 100% 0%, rgba(225, 37, 27, 0.07), transparent 58%),
    #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.02),
    0 14px 36px rgba(26, 26, 26, 0.045);
}

.similar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(26, 26, 26, 0.05);
}

.similar-title {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.title-mark {
  width: 4px;
  height: 30px;
  border-radius: 999px;
  background: linear-gradient(180deg, #ff8a65 0%, #e1251b 100%);
  box-shadow: 0 4px 10px rgba(225, 37, 27, 0.28);
  flex-shrink: 0;
}

.title-copy {
  min-width: 0;
}

.title-copy h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 780;
  color: var(--ink);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.title-copy p {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #9ca3af;
  line-height: 1.3;
}

.title-tag {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  color: #c2410c;
  font-size: 11px;
  font-weight: 740;
  border: 1px solid rgba(234, 88, 12, 0.12);
}

.more-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid #e8eaef;
  background: #fff;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.more-link:hover {
  color: var(--accent);
  border-color: rgba(225, 37, 27, 0.28);
  background: #fff8f7;
  transform: translateX(2px);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

@media (max-width: 960px) {
  .detail-top {
    grid-template-columns: 1fr;
    gap: 20px;
    padding: 18px;
    align-items: start;
  }

  .gallery {
    max-width: none;
    grid-template-columns: 56px minmax(0, 1fr);
    gap: 10px;
  }

  .main-img {
    height: 380px;
  }

  .thumbs-col {
    width: 56px;
    height: 380px;
  }

  .thumb {
    width: 56px;
    height: 56px;
    flex-basis: 56px;
  }

  .thumbs-more {
    width: 56px;
  }

  .nav-btn {
    opacity: 1;
  }

  .info h1 {
    font-size: 24px;
  }

  .price {
    font-size: 30px;
  }

  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .similar-shell {
    padding: 16px 14px 14px;
    border-radius: 16px;
  }

  .title-copy p,
  .title-tag {
    display: none;
  }
}

@media (max-width: 560px) {
  .detail-page {
    padding: 16px 14px 48px;
  }

  .gallery {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .main-img {
    height: 300px;
  }

  .thumbs-col {
    width: 100%;
    height: auto;
    order: 2;
  }

  .thumbs {
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    max-height: none;
    gap: 8px;
  }

  .thumb {
    width: 64px;
    height: 64px;
    flex: 0 0 64px;
  }

  .thumbs-more {
    display: none;
  }

  .actions {
    flex-direction: column;
  }

  .btn-buy,
  .btn-cart,
  .btn-fav {
    width: 100%;
  }

  .ask-row {
    flex-direction: column;
  }

  .btn-ask {
    width: 100%;
  }
}
</style>
