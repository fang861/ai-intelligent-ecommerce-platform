<template>
  <div class="center-page" v-loading="loading">
    <div class="center-shell">
      <section class="profile-card">
        <div class="profile-grid">
          <!-- 左：身份 -->
          <div class="id-col">
            <div class="avatar-wrap">
              <ImageUpload
                v-model="form.avatar"
                type="avatar"
                :avatar-size="64"
                upload-text="更换"
                :fallback="avatarFallback"
                :clearable="false"
                :show-success="false"
                @success="onAvatarUploaded"
              />
            </div>
            <div class="id-body">
              <div class="id-top">
                <h2 class="id-name">{{ form.nickname || form.username || '用户' }}</h2>
                <p class="id-meta">
                  <span>{{ form.username || '-' }}</span>
                  <template v-if="form.phone">
                    <i />
                    <span>{{ form.phone }}</span>
                  </template>
                </p>
              </div>
              <nav class="id-actions">
                <router-link to="/user/account">编辑资料</router-link>
                <router-link to="/user/account">修改密码</router-link>
                <router-link to="/user/addresses">地址管理</router-link>
                <router-link to="/user/wallet">账户明细</router-link>
              </nav>
            </div>
          </div>

          <!-- 中：资产数据 -->
          <div class="asset-col">
            <div class="asset-stats">
              <router-link to="/user/favorites" class="asset-item">
                <strong>{{ favorites.length }}</strong>
                <em>收藏</em>
              </router-link>
              <router-link to="/user/history" class="asset-item">
                <strong>{{ histories.length }}</strong>
                <em>足迹</em>
              </router-link>
              <router-link to="/user/follows" class="asset-item">
                <strong>{{ follows.length }}</strong>
                <em>关注店铺</em>
              </router-link>
              <router-link to="/user/addresses" class="asset-item">
                <strong>{{ addresses.length }}</strong>
                <em>收货地址</em>
              </router-link>
            </div>
            <div class="asset-banner">
              <div class="banner-left">
                <span class="banner-icon">单</span>
                <div class="banner-copy">
                  <strong>待处理订单 {{ pendingOrderTotal }} 笔</strong>
                  <p>付款、发货、收货与售后均可在此处理</p>
                </div>
              </div>
              <router-link class="banner-btn" to="/user/orders">去处理</router-link>
            </div>
          </div>

          <!-- 右：充值中心 -->
          <div class="charge-col">
            <router-link to="/user/wallet" class="charge-head">
              <span class="charge-icon">充</span>
              <div>
                <strong>充值中心</strong>
                <p>账户余额 {{ formatPrice(form.balance) }}</p>
              </div>
            </router-link>
            <div class="charge-phone">
              <span>{{ maskedPhone }}</span>
              <router-link to="/user/wallet">立即充值 ›</router-link>
            </div>
          </div>
        </div>
      </section>

      <!-- 订单 -->
      <section class="panel orders-panel">
        <header class="panel-head">
          <h2>我的订单</h2>
          <router-link class="panel-link" to="/user/orders">全部订单 ›</router-link>
        </header>

        <div class="status-rail">
          <button
            v-for="item in orderStatusItems"
            :key="item.key"
            type="button"
            class="status-chip"
            @click="goOrders(item.status)"
          >
            <span class="status-icon" :class="item.tone">
              <el-icon :size="22"><component :is="item.icon" /></el-icon>
              <em v-if="orderStats[item.key]" class="status-badge">{{ displayCount(orderStats[item.key]) }}</em>
            </span>
            <span class="status-label">{{ item.label }}</span>
          </button>
        </div>

        <div v-if="recentOrders.length" class="recent-list">
          <article v-for="order in recentOrders" :key="order.id" class="recent-item">
            <img :src="orderCover(order)" alt="" />
            <div class="recent-meta">
              <div class="recent-top">
                <strong>{{ order.shopName || '商城订单' }}</strong>
                <b>{{ formatPrice(order.totalAmount) }}</b>
              </div>
              <p>
                <span class="tone">{{ orderStatusLabel(order.status) }}</span>
                <i />
                <span class="ono">{{ order.orderNo || order.id }}</span>
              </p>
            </div>
            <router-link class="view-btn" to="/user/orders">查看</router-link>
          </article>
        </div>
        <p v-else class="quiet pad">暂无近期订单，去首页逛逛吧</p>
      </section>

      <!-- 收藏 / 足迹 / 关注 -->
      <div class="triple">
        <section class="panel mini">
          <header class="panel-head compact">
            <h2>我的收藏</h2>
            <router-link v-if="favorites.length" class="text-action" to="/user/favorites">全部 ›</router-link>
          </header>
          <div v-if="favorites.length" class="thumb-row">
            <router-link
              v-for="item in favorites.slice(0, 4)"
              :key="item.id"
              :to="`/user/product/${item.productId || item.product?.id}`"
              class="thumb"
              :title="nameOf(item.product || item)"
            >
              <img :src="coverOf(item.product || item)" alt="" />
            </router-link>
          </div>
          <p v-else class="quiet">还没有收藏好物</p>
          <router-link class="panel-link bottom" to="/user/favorites">查看全部收藏 ›</router-link>
        </section>

        <section class="panel mini">
          <header class="panel-head compact">
            <h2>我的足迹</h2>
            <router-link v-if="histories.length" class="text-action" to="/user/history">全部 ›</router-link>
          </header>
          <div v-if="histories.length" class="thumb-row">
            <router-link
              v-for="item in histories.slice(0, 4)"
              :key="item.id"
              :to="`/user/product/${item.productId || item.product?.id}`"
              class="thumb"
              :title="nameOf(item.product || item)"
            >
              <img :src="coverOf(item.product || item)" alt="" />
            </router-link>
          </div>
          <p v-else class="quiet">浏览商品后会出现在这里</p>
          <router-link class="panel-link bottom" to="/user/history">查看全部足迹 ›</router-link>
        </section>

        <section class="panel mini">
          <header class="panel-head compact">
            <h2>关注店铺</h2>
            <span class="count-pill">{{ follows.length }}</span>
          </header>
          <div v-if="follows.length" class="follow-list">
            <router-link
              v-for="f in follows.slice(0, 3)"
              :key="f.id"
              :to="`/user/shop/${f.merchantId}`"
              class="follow-row"
            >
              <span class="shop-mark" :class="{ 'has-img': !!shopAvatarOf(f) }">
                <img v-if="shopAvatarOf(f)" :src="shopAvatarOf(f)" alt="" />
                <template v-else>{{ String(f.shop?.shopName || '店').slice(0, 1) }}</template>
              </span>
              <span class="shop-name">{{ f.shop?.shopName || '店铺' }}</span>
              <em>进店</em>
            </router-link>
          </div>
          <p v-else class="quiet">去店铺详情关注喜欢的店</p>
          <router-link class="panel-link bottom" to="/user/follows">管理关注 ›</router-link>
        </section>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Box,
  CreditCard,
  RefreshRight,
  Van
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import {
  formatPrice,
  mediaUrl,
  orderStatusLabel,
  productCover
} from '@/utils/format'
import ImageUpload from '@/components/ImageUpload.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  role: '',
  balance: 0,
  createdAt: ''
})

const orderStats = reactive({
  unpaid: 0,
  unshipped: 0,
  unreceived: 0,
  afterSale: 0,
  completed: 0
})
const recentOrders = ref([])
const favorites = ref([])
const histories = ref([])
const follows = ref([])
const addresses = ref([])

const orderStatusItems = [
  { key: 'unpaid', label: '待付款', status: '0', icon: CreditCard, tone: 'warn' },
  { key: 'unshipped', label: '待发货', status: '1', icon: Box, tone: 'info' },
  { key: 'unreceived', label: '待收货', status: '2', icon: Van, tone: 'ship' },
  { key: 'afterSale', label: '退换/售后', status: '', icon: RefreshRight, tone: 'mute' }
]

const avatarFallback = computed(() => (form.nickname || form.username || 'U').slice(0, 1).toUpperCase())

const pendingOrderTotal = computed(() =>
  Number(orderStats.unpaid || 0) +
  Number(orderStats.unshipped || 0) +
  Number(orderStats.unreceived || 0) +
  Number(orderStats.afterSale || 0)
)

const maskedPhone = computed(() => {
  const phone = String(form.phone || '').replace(/\s/g, '')
  if (phone.length >= 7) {
    return `${phone.slice(0, 3)} **** ${phone.slice(-4)}`
  }
  return phone || form.username || '未绑定手机'
})

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function displayCount(n) {
  const num = Number(n || 0)
  return num > 99 ? '99+' : String(num)
}

function coverOf(item) {
  return (
    productCover(item) ||
    mediaUrl(item?.cover || item?.productCover) ||
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=160&q=80'
  )
}

function shopAvatarOf(f) {
  return mediaUrl(f?.shop?.avatar || f?.avatar) || ''
}

function nameOf(item) {
  return item?.name || item?.productName || item?.title || '商品'
}

function orderCover(order) {
  const item = order?.items?.[0] || order?.orderItems?.[0]
  return coverOf(item || {})
}

function goOrders(status) {
  router.push({ path: '/user/orders', query: status === '' || status == null ? {} : { status } })
}

async function loadProfile() {
  const user = await userStore.fetchUser()
  Object.assign(form, {
    username: user?.username || '',
    nickname: user?.nickname || '',
    phone: user?.phone || '',
    avatar: user?.avatar || '',
    role: user?.role || user?.accountType || '',
    balance: user?.balance ?? 0,
    createdAt: user?.createdAt || ''
  })
}

async function loadHub() {
  loading.value = true
  try {
    await loadProfile()
    const [stats, orders, favs, hist, flws, addrs] = await Promise.all([
      request.get('/user/orders/stats', { silent: true }).catch(() => ({})),
      request.get('/user/orders', { params: { page: 1, size: 2 }, silent: true }).catch(() => []),
      request.get('/user/favorites', { silent: true }).catch(() => []),
      request.get('/user/history', { params: { page: 1, size: 8 }, silent: true }).catch(() => []),
      request.get('/user/follows', { silent: true }).catch(() => []),
      request.get('/user/addresses', { silent: true }).catch(() => [])
    ])
    Object.assign(orderStats, {
      unpaid: Number(stats?.unpaid || 0),
      unshipped: Number(stats?.unshipped || 0),
      unreceived: Number(stats?.unreceived || 0),
      afterSale: Number(stats?.afterSale || 0),
      completed: Number(stats?.completed || 0)
    })
    recentOrders.value = unwrapList(orders).slice(0, 2)
    favorites.value = unwrapList(favs)
    histories.value = unwrapList(hist)
    follows.value = unwrapList(flws)
    addresses.value = unwrapList(addrs)
  } finally {
    loading.value = false
  }
}

async function onAvatarUploaded() {
  await request.put('/auth/profile', { avatar: form.avatar, nickname: form.nickname, phone: form.phone })
  await userStore.fetchUser()
  ElMessage.success('头像已更新')
}

onMounted(loadHub)
</script>
<style scoped>
.center-page {
  --ink: #222;
  --muted: #999;
  --accent: #e1251b;
  --line: #ececec;
  --surface: #ffffff;
  max-width: none;
  margin: 0;
  padding: 0 0 24px;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.center-shell {
  display: grid;
  gap: 14px;
}

.profile-card,
.panel {
  background: #fff;
  border: 1px solid #e8eaed;
  border-radius: 12px;
}

.profile-card {
  overflow: hidden;
  padding: 16px;
  background: #fff6f5;
  border-color: rgba(225, 37, 27, 0.06);
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.03);
}

.profile-grid {
  display: grid;
  grid-template-columns: minmax(250px, 1.15fr) minmax(300px, 1.55fr) minmax(170px, 0.72fr);
  gap: 12px;
  align-items: stretch;
}

.id-col {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.04);
  box-shadow: 0 1px 2px rgba(26, 26, 26, 0.03);
}

.avatar-wrap {
  position: relative;
  flex-shrink: 0;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  overflow: hidden;
  box-shadow: inset 0 0 0 1px rgba(26, 26, 26, 0.06);
}

.id-body {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.id-top { min-width: 0; }
.id-name {
  margin: 0 0 4px;
  font-size: 18px;
  font-weight: 740;
  letter-spacing: -0.02em;
  line-height: 1.25;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.id-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
  margin: 0;
  color: #9ca3af;
  font-size: 12px;
}
.id-meta i {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #d1d5db;
  font-style: normal;
}

.id-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px;
}
.id-actions a {
  height: 30px;
  padding: 0 8px;
  border-radius: 8px;
  background: #fff5f4;
  border: 1px solid rgba(225, 37, 27, 0.06);
  color: #4b5563;
  text-decoration: none;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: 0.15s ease;
}
.id-actions a:hover {
  background: #fff;
  border-color: rgba(225, 37, 27, 0.28);
  color: #e1251b;
}

.asset-col {
  display: flex;
  flex-direction: column;
  gap: 10px;
  min-width: 0;
  padding: 14px 16px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.04);
  box-shadow: 0 1px 2px rgba(26, 26, 26, 0.03);
}

.asset-stats {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 4px;
}
.asset-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 8px 4px 10px;
  text-decoration: none;
  color: inherit;
  border-radius: 8px;
  transition: background 0.15s ease;
}
.asset-item:hover { background: #fafafa; }
.asset-item strong {
  font-size: 20px;
  font-weight: 760;
  line-height: 1;
  letter-spacing: -0.02em;
  color: #1a1a1a;
}
.asset-item em {
  font-style: normal;
  font-size: 12px;
  color: #9ca3af;
}

.asset-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 10px;
  background: #fff5f4;
  border: 1px solid rgba(225, 37, 27, 0.08);
}
.banner-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
.banner-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(145deg, #e1251b, #ff6a4d);
  color: #fff;
  font-size: 13px;
  font-weight: 760;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.banner-copy {
  min-width: 0;
}
.banner-copy strong {
  display: block;
  font-size: 13px;
  font-weight: 700;
  color: #1a1a1a;
  line-height: 1.3;
}
.banner-copy p {
  margin: 2px 0 0;
  font-size: 11px;
  color: #9ca3af;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.banner-btn {
  flex-shrink: 0;
  height: 28px;
  padding: 0 14px;
  border-radius: 14px;
  background: #e1251b;
  color: #fff;
  text-decoration: none;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}
.banner-btn:hover { background: #c81e16; }

.charge-col {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 12px;
  min-width: 0;
  padding: 14px 14px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.04);
  box-shadow: 0 1px 2px rgba(26, 26, 26, 0.03);
}

.charge-head {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: inherit;
}
.charge-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(145deg, #ff8a3d, #f97316);
  color: #fff;
  font-size: 18px;
  font-weight: 800;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 14px rgba(249, 115, 22, 0.28);
}
.charge-head strong {
  display: block;
  font-size: 14px;
  font-weight: 740;
  color: #1a1a1a;
}
.charge-head p {
  margin: 4px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.charge-phone {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f5f6f8;
  font-size: 12px;
  color: #6b7280;
}
.charge-phone a {
  color: #e1251b;
  text-decoration: none;
  font-weight: 650;
  white-space: nowrap;
}
.charge-phone a:hover { opacity: 0.85; }

.panel {
  padding: 20px 22px;
  background: #fff;
  border: 1px solid #e8eaed;
  border-radius: 12px;
}
.panel.mini {
  padding: 16px 18px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}
.panel-head.compact { margin-bottom: 0; }
.panel-head h2 {
  margin: 0;
  position: relative;
  padding-left: 10px;
  font-size: 15px;
  font-weight: 720;
  letter-spacing: 0.01em;
}
.panel-head h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 3px;
  height: 14px;
  margin-top: -7px;
  border-radius: 2px;
  background: var(--accent);
}
.panel-link, .soft-link {
  color: #999;
  text-decoration: none;
  font-size: 12px;
  transition: color 0.15s ease;
}
.panel-link:hover, .soft-link:hover { color: var(--accent); }
.panel-link.bottom {
  margin-top: 2px;
  padding-top: 10px;
  border-top: 1px solid #f3f4f6;
}

.count-pill {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 10px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.text-action {
  border: none;
  background: transparent;
  color: #999;
  font-size: 12px;
  cursor: pointer;
  padding: 0;
  text-decoration: none;
}
.text-action:hover { color: var(--accent); }

.status-rail {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  padding: 4px 0 2px;
}
.status-chip {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 12px 8px 14px;
  border: none;
  border-radius: 10px;
  background: transparent;
  cursor: pointer;
  transition: background 0.15s ease;
}
.status-chip:hover { background: #fafafa; }
.status-icon {
  position: relative;
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f7f8fa;
  color: #4b5563;
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.status-chip:hover .status-icon {
  transform: translateY(-1px);
  box-shadow: 0 6px 14px rgba(26, 26, 26, 0.06);
}
.status-icon.warn { color: #d97706; background: #fff8ef; }
.status-icon.info { color: #2563eb; background: #f0f5ff; }
.status-icon.ship { color: #0f766e; background: #f0fdfa; }
.status-icon.mute { color: #64748b; background: #f1f5f9; }
.status-label {
  font-size: 13px;
  color: #374151;
  font-weight: 560;
}
.status-badge {
  position: absolute;
  top: -4px;
  right: -6px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--accent);
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 700;
  line-height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 2px #fff;
}

.recent-list {
  margin-top: 14px;
  border-top: 1px solid #f0f0f0;
}
.recent-item {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  padding: 14px 4px;
  border-bottom: 1px solid #f5f5f5;
  transition: background 0.15s ease;
}
.recent-item:last-child { border-bottom: none; }
.recent-item:hover { background: #fcfcfd; }
.recent-item img {
  width: 52px;
  height: 52px;
  border-radius: 8px;
  object-fit: cover;
  background: #f3f4f6;
  border: 1px solid rgba(26, 26, 26, 0.04);
}
.recent-meta { min-width: 0; }
.recent-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
}
.recent-top strong {
  font-size: 14px;
  font-weight: 680;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.recent-top b {
  flex-shrink: 0;
  font-size: 14px;
  font-weight: 720;
  color: #1a1a1a;
  letter-spacing: -0.02em;
}
.recent-meta p {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #9ca3af;
}
.recent-meta .tone {
  color: var(--accent);
  font-weight: 650;
}
.recent-meta i {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #d1d5db;
  font-style: normal;
}
.recent-meta .ono {
  max-width: 220px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.view-btn {
  flex-shrink: 0;
  height: 28px;
  padding: 0 12px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #4b5563;
  text-decoration: none;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  transition: 0.15s ease;
}
.view-btn:hover {
  border-color: rgba(225, 37, 27, 0.35);
  color: var(--accent);
}

.quiet {
  margin: 0;
  color: #a8b0bb;
  font-size: 13px;
  line-height: 1.6;
}
.quiet.pad {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
.panel.mini .quiet {
  min-height: 56px;
  display: flex;
  align-items: center;
}

.triple {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  align-items: start;
}

.thumb-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 56px;
}
.thumb {
  flex: 0 0 56px;
  width: 56px;
  height: 56px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f6f8;
  border: 1px solid rgba(26, 26, 26, 0.04);
  transition: transform 0.18s ease, box-shadow 0.18s ease;
}
.thumb:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 14px rgba(26, 26, 26, 0.08);
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.follow-list {
  display: grid;
  gap: 8px;
  min-height: 56px;
}
.follow-row {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 8px 10px;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  background: #fafafa;
  border: 1px solid transparent;
  transition: 0.15s ease;
}
.follow-row:hover {
  background: #fff;
  border-color: #f0f0f0;
}
.shop-mark {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 720;
  background: #3d4f5f;
  overflow: hidden;
  flex-shrink: 0;
}
.shop-mark.has-img {
  background: #f3f4f6;
}
.shop-mark img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.shop-name {
  font-size: 13px;
  font-weight: 650;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.follow-row em {
  font-style: normal;
  font-size: 12px;
  color: var(--accent);
  font-weight: 650;
}

.split {
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 16px;
  align-items: start;
}

.live-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  background: #ecfdf5;
  color: #059669;
  font-size: 12px;
  font-weight: 680;
}
.live-dot::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

.balance-board {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 20px;
  margin-bottom: 18px;
  border-radius: 16px;
  background: #14171c;
  color: #fff;
}
.balance-board-copy em {
  display: block;
  margin-bottom: 6px;
  font-style: normal;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.55);
  font-weight: 600;
}
.balance-board-copy strong {
  display: block;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.04em;
  line-height: 1;
  color: #fff;
}
.balance-board-tip {
  flex-shrink: 0;
  max-width: 120px;
  text-align: right;
  font-size: 12px;
  line-height: 1.45;
  color: rgba(255, 255, 255, 0.45);
}

.recharge-block { margin-bottom: 20px; }
.field-label {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 680;
  color: #475569;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 14px;
}
.amount-card {
  height: 64px;
  border-radius: 14px;
  border: 1.5px solid rgba(20, 23, 28, 0.08);
  background: #f8fafc;
  color: var(--ink);
  cursor: pointer;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  transition: 0.2s ease;
}
.amount-card .yen {
  font-size: 13px;
  font-weight: 700;
  color: #94a3b8;
}
.amount-card b {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
}
.amount-card:hover {
  border-color: rgba(230, 57, 53, 0.28);
  background: #fff;
}
.amount-card.active {
  border-color: var(--accent);
  background: #fff1f0;
  box-shadow: inset 0 0 0 1px rgba(230, 57, 53, 0.12);
}
.amount-card.active .yen,
.amount-card.active b { color: var(--accent); }

.custom-amount {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  margin-bottom: 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid rgba(20, 23, 28, 0.05);
}
.custom-amount label {
  font-size: 13px;
  font-weight: 650;
  color: #64748b;
  white-space: nowrap;
}
.custom-input {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 0;
}
.custom-input > span {
  font-size: 16px;
  font-weight: 760;
  color: var(--accent);
}
.custom-input :deep(.el-input-number) { width: 120px; }
.custom-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  padding: 0;
}
.custom-input :deep(.el-input__inner) {
  text-align: right;
  font-size: 18px;
  font-weight: 760;
  color: var(--ink);
}

.recharge-cta {
  height: 44px;
  border-radius: 14px;
  font-size: 14px;
}

.ledger-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding-top: 4px;
  border-top: 1px dashed rgba(20, 23, 28, 0.08);
}
.ledger-head h3 {
  margin: 0;
  padding-top: 16px;
  font-size: 14px;
  font-weight: 720;
}
.ledger-head span {
  padding-top: 16px;
  font-size: 12px;
  color: #94a3b8;
  font-weight: 650;
}
.ledger-list {
  display: grid;
  gap: 0;
  max-height: 240px;
  overflow: auto;
  scrollbar-width: thin;
}
.ledger-row {
  position: relative;
  display: grid;
  grid-template-columns: 14px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: start;
  padding: 12px 4px 12px 0;
  border-bottom: 1px solid rgba(20, 23, 28, 0.04);
}
.ledger-row:last-child { border-bottom: none; }
.ledger-dot {
  width: 8px;
  height: 8px;
  margin-top: 6px;
  border-radius: 50%;
  background: #fecaca;
  box-shadow: 0 0 0 3px #fff1f0;
}
.ledger-main strong {
  display: block;
  color: #dc2626;
  font-size: 15px;
  font-weight: 760;
}
.ledger-main span {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.4;
}
.ledger-time {
  font-style: normal;
  font-size: 11.5px;
  color: #a8b0bb;
  white-space: nowrap;
  padding-top: 2px;
}

.add-addr-btn {
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid rgba(230, 57, 53, 0.18);
  background: #fff1f0;
  color: var(--accent);
  font-size: 12.5px;
  font-weight: 680;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  transition: 0.2s ease;
}
.add-addr-btn i {
  font-style: normal;
  font-weight: 800;
  font-size: 15px;
  line-height: 1;
}
.add-addr-btn:hover {
  background: var(--accent);
  color: #fff;
  border-color: var(--accent);
}

.addr-list { display: grid; gap: 12px; min-height: 120px; }
.addr-card {
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(20, 23, 28, 0.06);
  overflow: hidden;
  transition: 0.2s ease;
}
.addr-card:hover {
  border-color: rgba(230, 57, 53, 0.16);
  box-shadow: 0 10px 24px rgba(20, 23, 28, 0.05);
}
.addr-card.default {
  border-color: rgba(230, 57, 53, 0.22);
  background: linear-gradient(180deg, #fffafa, #fff);
}
.addr-body { padding: 14px 16px 10px; }
.addr-top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.addr-top strong { font-size: 15px; font-weight: 740; }
.addr-top span { color: #64748b; font-size: 13px; }
.addr-top em {
  font-style: normal;
  height: 20px;
  padding: 0 8px;
  border-radius: 6px;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  font-weight: 720;
  display: inline-flex;
  align-items: center;
}
.addr-body p {
  margin: 0;
  color: #64748b;
  font-size: 13px;
  line-height: 1.55;
}
.addr-ops {
  display: flex;
  gap: 8px;
  padding: 10px 14px;
  border-top: 1px solid rgba(20, 23, 28, 0.05);
  background: #fafbfc;
}
.addr-ops .op {
  height: 28px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid rgba(20, 23, 28, 0.08);
  background: #fff;
  color: #475569;
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
  transition: 0.15s ease;
}
.addr-ops .op:hover {
  border-color: rgba(230, 57, 53, 0.25);
  color: var(--accent);
}
.addr-ops .op.danger {
  color: #ef4444;
  border-color: rgba(239, 68, 68, 0.18);
  margin-left: auto;
}
.addr-ops .op.danger:hover {
  background: #fef2f2;
}

.empty-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 28px 16px;
  text-align: center;
}
.empty-block.tall { padding: 48px 16px; min-height: 220px; }
.empty-ico {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: #f1f5f9;
  color: #94a3b8;
  font-size: 18px;
  font-weight: 800;
}
.empty-ico.pin {
  background: #fff1f0;
  color: var(--accent);
}
.empty-block p {
  margin: 0;
  color: #a8b0bb;
  font-size: 13px;
}
.empty-action {
  margin-top: 6px;
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  font-weight: 680;
  cursor: pointer;
}

.region-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
  width: 100%;
}

@media (max-width: 1100px) {
  .profile-grid {
    grid-template-columns: 1fr 1fr;
  }
  .charge-col {
    grid-column: 1 / -1;
    flex-direction: row;
    align-items: center;
  }
  .charge-phone { flex: 1; }
}

@media (max-width: 960px) {
  .triple,
  .split {
    grid-template-columns: 1fr;
  }
  .profile-grid {
    grid-template-columns: 1fr;
  }
  .charge-col {
    grid-column: auto;
    flex-direction: column;
    align-items: stretch;
  }
  .status-rail { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}

@media (max-width: 640px) {
  .panel { padding: 16px; }
  .profile-card { padding: 12px; }
  .asset-stats { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .id-name { font-size: 18px; }
  .banner-copy p { display: none; }
}
</style>
