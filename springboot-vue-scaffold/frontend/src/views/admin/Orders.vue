<template>
  <div class="page orders-page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>订单管理</span>
          <el-button type="primary" plain @click="loadData">刷新</el-button>
        </div>
      </template>

      <div class="filter-bar">
        <button
          v-for="tab in statusTabs"
          :key="tab.value === '' ? 'all' : tab.value"
          type="button"
          class="filter-pill"
          :class="{ active: query.status === tab.value }"
          @click="selectStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
        <el-input
          v-model="query.keyword"
          clearable
          placeholder="订单号"
          class="keyword-input"
          @keyup.enter="onFilter"
          @clear="onFilter"
        />
        <el-button type="primary" plain @click="onFilter">查询</el-button>
      </div>

      <div v-loading="loading" class="order-list">
        <article v-for="row in tableData" :key="row.id" class="order-card">
          <header class="card-head">
            <div class="head-main">
              <span class="order-no"># {{ row.orderNo || row.id }}</span>
              <span class="dot" aria-hidden="true" />
              <span class="time">{{ formatTime(row.createdAt) }}</span>
              <span v-if="row.shopName" class="shop-tag">{{ row.shopName }}</span>
            </div>
            <div class="status-stack">
              <span class="status-chip" :class="`tone-${statusTone(row.status)}`">
                {{ orderStatusLabel(row.status) }}
              </span>
              <span
                v-if="afterSaleStatusOf(row)"
                class="as-chip"
                :class="`as-${afterSaleStatusOf(row)}`"
              >
                {{ afterSaleStatusLabel(row) }}
              </span>
            </div>
          </header>

          <div class="meta-grid">
            <div class="meta-block buyer-block">
              <div class="buyer-avatar" :class="{ 'has-img': !!buyerAvatar(row) }" :style="buyerAvatar(row) ? undefined : avatarStyle(row)">
                <img v-if="buyerAvatar(row)" :src="buyerAvatar(row)" alt="" />
                <template v-else>{{ buyerInitial(row) }}</template>
              </div>
              <div class="meta-copy">
                <em>买家</em>
                <strong>{{ buyerName(row) }}</strong>
                <span>{{ row.buyerPhone || row.buyerUsername || '暂无联系方式' }}</span>
              </div>
            </div>
            <div class="meta-block">
              <em>收货信息</em>
              <strong>{{ receiverName(row) }} {{ receiverPhone(row) }}</strong>
              <span class="addr">{{ receiverAddress(row) }}</span>
            </div>
            <div class="meta-block amount-block">
              <em>订单金额</em>
              <strong class="money">{{ formatPrice(row.payAmount || row.totalAmount) }}</strong>
              <span>{{ itemCount(row) }} 件商品 · {{ row.shopName || '店铺' }}</span>
            </div>
          </div>

          <div class="goods-list">
            <div v-for="item in orderItems(row)" :key="item.id || item.productId" class="goods-row">
              <img :src="coverOf(item)" alt="" />
              <div class="goods-copy">
                <p class="name">{{ item.productName || item.name || '商品' }}</p>
                <p class="sku">单价 {{ formatPrice(item.price) }}</p>
              </div>
              <span class="qty">×{{ item.quantity || 1 }}</span>
              <strong class="subtotal">
                {{ formatPrice(Number(item.price || 0) * Number(item.quantity || 1)) }}
              </strong>
            </div>
            <p v-if="!orderItems(row).length" class="empty-goods">暂无商品明细</p>
          </div>

          <footer class="card-foot">
            <div class="foot-links">
              <button type="button" class="link-btn" @click="openDetail(row)">查看详情</button>
              <button
                v-if="normalizeOrderStatus(row.status) >= 2 && normalizeOrderStatus(row.status) !== 4"
                type="button"
                class="link-btn"
                @click="openLogistics(row)"
              >
                物流轨迹
              </button>
            </div>
            <span class="action-hint">{{ viewHint(row) }}</span>
          </footer>
          <p v-if="afterSaleBrief(row)" class="as-brief">{{ afterSaleBrief(row) }}</p>
        </article>

        <div v-if="!loading && !tableData.length" class="empty-wrap">
          <el-empty description="暂无符合条件的订单" />
        </div>
      </div>

      <div class="pager">
        <el-pagination
          v-model:current-page="query.page"
          :page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          background
          @current-change="loadData"
        />
      </div>
    </el-card>

    <el-drawer v-model="detailVisible" title="订单详情" size="440px">
      <div v-if="current" class="detail" v-loading="detailLoading">
        <div class="detail-hero">
          <span class="status-chip" :class="`tone-${statusTone(current.status)}`">
            {{ orderStatusLabel(current.status) }}
          </span>
          <strong>{{ formatPrice(current.payAmount || current.totalAmount) }}</strong>
          <p>订单号 {{ current.orderNo || current.id }}</p>
        </div>

        <div class="detail-block">
          <h4>店铺</h4>
          <p>{{ current.shopName || '—' }}</p>
        </div>

        <div class="detail-block">
          <h4>物流轨迹</h4>
          <LogisticsTimeline
            :company="current.expressCompany"
            :tracking-no="current.trackingNo"
            :trace="current.logisticsTrace || []"
          />
        </div>

        <div class="detail-block">
          <h4>买家</h4>
          <div class="detail-buyer">
            <div class="buyer-avatar sm" :class="{ 'has-img': !!buyerAvatar(current) }" :style="buyerAvatar(current) ? undefined : avatarStyle(current)">
              <img v-if="buyerAvatar(current)" :src="buyerAvatar(current)" alt="" />
              <template v-else>{{ buyerInitial(current) }}</template>
            </div>
            <div>
              <strong>{{ buyerName(current) }}</strong>
              <p>{{ current.buyerUsername || '—' }} · {{ current.buyerPhone || '无手机号' }}</p>
            </div>
          </div>
        </div>

        <div class="detail-block">
          <h4>收货信息</h4>
          <p class="addr-full">
            {{ receiverName(current) }} {{ receiverPhone(current) }}<br />
            {{ receiverAddress(current) }}
          </p>
        </div>

        <div class="detail-block">
          <h4>商品明细</h4>
          <div
            v-for="item in orderItems(current)"
            :key="item.id || item.productId"
            class="detail-goods"
          >
            <img :src="coverOf(item)" alt="" />
            <div>
              <p>{{ item.productName || item.name }}</p>
              <span>{{ formatPrice(item.price) }} × {{ item.quantity || 1 }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-drawer v-model="logisticsVisible" title="物流轨迹" size="440px">
      <LogisticsTimeline
        v-if="logisticsOrder"
        :company="logisticsOrder.expressCompany"
        :tracking-no="logisticsOrder.trackingNo"
        :trace="logisticsOrder.logisticsTrace || []"
      />
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '@/api/request'
import LogisticsTimeline from '@/components/LogisticsTimeline.vue'
import {
  formatPrice,
  formatTime,
  mediaUrl,
  normalizeOrderStatus,
  orderStatusLabel,
  productCover
} from '@/utils/format'

const loading = ref(false)
const detailLoading = ref(false)
const tableData = ref([])
const total = ref(0)
const detailVisible = ref(false)
const logisticsVisible = ref(false)
const current = ref(null)
const logisticsOrder = ref(null)
const query = reactive({ status: '', keyword: '', page: 1, size: 10 })

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 0 },
  { label: '待发货', value: 1 },
  { label: '待收货', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 }
]

const AVATAR_COLORS = [
  ['#0f172a', '#334155'],
  ['#1e3a8a', '#2563eb'],
  ['#14532d', '#16a34a'],
  ['#7c2d12', '#c2410c'],
  ['#4c1d95', '#7c3aed'],
  ['#9f1239', '#e11d48']
]

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function orderItems(order) {
  return order?.items || order?.orderItems || []
}

function itemCount(order) {
  return orderItems(order).reduce((sum, i) => sum + Number(i.quantity || 1), 0)
}

function coverOf(item) {
  return (
    productCover(item) ||
    mediaUrl(item?.productCover) ||
    'data:image/svg+xml,' +
      encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="#f3f4f6" width="80" height="80"/></svg>'
      )
  )
}

function buyerName(row) {
  return row?.buyerNickname || row?.buyerUsername || row?.username || '买家'
}

function buyerAvatar(row) {
  return mediaUrl(row?.buyerAvatar || row?.avatar || row?.userAvatar) || ''
}

function buyerInitial(row) {
  return String(buyerName(row)).slice(0, 1)
}

function avatarStyle(row) {
  const key = String(buyerName(row))
  let hash = 0
  for (let i = 0; i < key.length; i += 1) hash = (hash + key.charCodeAt(i) * (i + 1)) % 997
  const [a, b] = AVATAR_COLORS[hash % AVATAR_COLORS.length]
  return { background: `linear-gradient(135deg, ${a}, ${b})` }
}

function parseAddress(row) {
  const raw = row?.addressSnapshot
  if (!raw) return null
  try {
    const obj = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (obj && typeof obj === 'object') return obj
  } catch {
    return { detail: String(raw) }
  }
  return { detail: String(raw) }
}

function receiverName(row) {
  const obj = parseAddress(row)
  return obj?.receiver || obj?.receiverName || buyerName(row)
}

function receiverPhone(row) {
  const obj = parseAddress(row)
  return obj?.phone || row?.buyerPhone || ''
}

function receiverAddress(row) {
  const obj = parseAddress(row)
  if (!obj) return '暂无收货地址'
  const addr = [obj.province, obj.city, obj.district, obj.detail || obj.address]
    .filter(Boolean)
    .join('')
  return addr || String(row.addressSnapshot || '暂无收货地址')
}

function statusTone(s) {
  const code = normalizeOrderStatus(s)
  return { 0: 'warn', 1: 'info', 2: 'ship', 3: 'ok', 4: 'mute' }[code] || 'mute'
}

function afterSaleStatusOf(order) {
  const s = Number(order?.afterSaleStatus ?? 0)
  return s > 0 ? s : 0
}

function afterSaleTypeLabel(type) {
  return { 1: '仅退款', 2: '退货退款', 3: '换货' }[Number(type)] || '售后'
}

function afterSaleStatusLabel(order) {
  return { 1: '售后待处理', 2: '售后已同意', 3: '售后已拒绝' }[afterSaleStatusOf(order)] || ''
}

function afterSaleBrief(order) {
  const as = afterSaleStatusOf(order)
  if (!as) return ''
  const type = afterSaleTypeLabel(order.afterSaleType)
  if (as === 1) return `买家申请${type}：${order.afterSaleReason || '—'}`
  if (as === 2) return `${type}已同意${order.afterSaleReply ? ' · ' + order.afterSaleReply : ''}`
  if (as === 3) return `${type}已拒绝${order.afterSaleReply ? '：' + order.afterSaleReply : ''}`
  return ''
}

function viewHint(row) {
  const as = afterSaleStatusOf(row)
  if (as === 1) return '售后待商家处理'
  if (as === 2) return '售后已同意'
  if (as === 3) return '售后已拒绝'
  const code = normalizeOrderStatus(row?.status)
  return (
    {
      0: '等待买家付款',
      1: '待商家发货',
      2: '运输中，等待确认收货',
      3: '订单已完成',
      4: '订单已取消'
    }[code] || '仅可查看'
  )
}

function selectStatus(value) {
  query.status = value
  query.page = 1
  loadData()
}

function onFilter() {
  query.page = 1
  loadData()
}

async function openDetail(row) {
  detailVisible.value = true
  current.value = row
  if (!row?.id) return
  detailLoading.value = true
  try {
    const data = await request.get(`/admin/orders/${row.id}`, { silent: true })
    if (data) current.value = data
  } catch {
    /* keep list row */
  } finally {
    detailLoading.value = false
  }
}

function openLogistics(row) {
  logisticsOrder.value = row
  logisticsVisible.value = true
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/orders', {
      params: {
        status: query.status === '' || query.status == null ? undefined : query.status,
        keyword: query.keyword?.trim() || undefined,
        page: query.page,
        size: query.size
      }
    })
    tableData.value = unwrapList(data)
    total.value = Number(data?.total ?? tableData.value.length) || 0
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.orders-page {
  --ink: #0f172a;
  --muted: #94a3b8;
  --line: rgba(15, 23, 42, 0.06);
  --accent: #3b82f6;
}

.panel {
  border-radius: 14px;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid var(--line);
}

.filter-pill {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #64748b;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  transition: 0.2s ease;
}

.filter-pill:hover {
  background: #fff;
  color: var(--ink);
}

.filter-pill.active {
  color: #fff;
  background: #334155;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.16);
}

.keyword-input {
  width: 200px;
  margin-left: auto;
}

.order-list {
  min-height: 240px;
  display: grid;
  gap: 14px;
}

.order-card {
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
  overflow: hidden;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 18px;
  background: linear-gradient(180deg, #fbfcfd, #fff);
  border-bottom: 1px solid var(--line);
}

.head-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 10px;
  min-width: 0;
}

.order-no {
  font-size: 13px;
  font-weight: 740;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.dot {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #cbd5e1;
}

.time {
  font-size: 12px;
  color: var(--muted);
}

.shop-tag {
  height: 22px;
  padding: 0 8px;
  border-radius: 6px;
  background: #eff6ff;
  color: #2563eb;
  font-size: 12px;
  font-weight: 650;
  display: inline-flex;
  align-items: center;
}

.status-stack {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.status-chip {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 720;
  display: inline-flex;
  align-items: center;
}

.tone-warn {
  color: #c2410c;
  background: #fff7ed;
}

.tone-info {
  color: #1d4ed8;
  background: #eff6ff;
}

.tone-ship {
  color: #7c3aed;
  background: #f5f3ff;
}

.tone-ok {
  color: #15803d;
  background: #f0fdf4;
}

.tone-mute {
  color: #64748b;
  background: #f1f5f9;
}

.as-chip {
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.as-chip.as-1 {
  color: #c2410c;
  background: #fff7ed;
}

.as-chip.as-2 {
  color: #15803d;
  background: #f0fdf4;
}

.as-chip.as-3 {
  color: #64748b;
  background: #f1f5f9;
}

.as-brief {
  margin: 0;
  padding: 0 18px 14px;
  font-size: 12px;
  color: #9a3412;
  line-height: 1.5;
}

.meta-grid {
  display: grid;
  grid-template-columns: 1.1fr 1.5fr 0.9fr;
  gap: 0;
  padding: 0 6px;
  border-bottom: 1px solid var(--line);
}

.meta-block {
  padding: 16px 14px;
  min-width: 0;
}

.meta-block + .meta-block {
  border-left: 1px solid var(--line);
}

.meta-block em {
  display: block;
  margin-bottom: 8px;
  font-style: normal;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--muted);
}

.meta-block strong {
  display: block;
  margin-bottom: 4px;
  font-size: 14px;
  font-weight: 720;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.meta-block span {
  display: block;
  font-size: 12px;
  color: #64748b;
  line-height: 1.5;
}

.meta-block .addr {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  white-space: normal;
}

.buyer-block {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.buyer-block em {
  margin-bottom: 4px;
}

.buyer-avatar {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 16px;
  font-weight: 760;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.16);
  overflow: hidden;
}

.buyer-avatar.has-img {
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.1);
  background: #f1f5f9;
}

.buyer-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.buyer-avatar.sm {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  font-size: 14px;
}

.amount-block .money {
  color: var(--accent);
  font-size: 22px;
  font-weight: 820;
  letter-spacing: -0.03em;
}

.goods-list {
  padding: 8px 18px;
}

.goods-row {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) 48px 90px;
  gap: 12px;
  align-items: center;
  padding: 10px 0;
}

.goods-row + .goods-row {
  border-top: 1px dashed rgba(15, 23, 42, 0.06);
}

.goods-row img {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  object-fit: cover;
  background: #f1f5f9;
}

.goods-copy .name {
  margin: 0;
  font-size: 13.5px;
  font-weight: 650;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-copy .sku {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--muted);
}

.qty {
  text-align: center;
  color: #64748b;
  font-size: 13px;
}

.subtotal {
  text-align: right;
  font-size: 13px;
  color: var(--ink);
}

.empty-goods {
  margin: 12px 0;
  color: var(--muted);
  font-size: 13px;
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 18px 14px;
  background: #fbfcfd;
  border-top: 1px solid var(--line);
}

.foot-links {
  display: flex;
  flex-wrap: wrap;
  gap: 8px 14px;
}

.link-btn {
  border: none;
  background: transparent;
  color: #475569;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
}

.link-btn:hover {
  color: var(--accent);
}

.action-hint {
  font-size: 12px;
  color: var(--muted);
}

.empty-wrap {
  padding: 40px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid var(--line);
}

.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.detail {
  display: grid;
  gap: 18px;
}

.detail-hero {
  padding: 16px;
  border-radius: 14px;
  background: linear-gradient(135deg, #1e293b, #334155);
  color: #fff;
}

.detail-hero .status-chip {
  margin-bottom: 10px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.detail-hero strong {
  display: block;
  font-size: 28px;
  font-weight: 820;
  letter-spacing: -0.03em;
}

.detail-hero p {
  margin: 6px 0 0;
  opacity: 0.72;
  font-size: 12px;
}

.detail-block h4 {
  margin: 0 0 10px;
  font-size: 13px;
  color: var(--ink);
}

.detail-block > p {
  margin: 0;
  font-size: 14px;
  color: #334155;
}

.detail-buyer {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-buyer strong {
  display: block;
  font-size: 14px;
  color: var(--ink);
}

.detail-buyer p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.addr-full {
  line-height: 1.6;
  word-break: break-all;
}

.detail-goods {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.detail-goods img {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  object-fit: cover;
}

.detail-goods p {
  margin: 0 0 4px;
  font-size: 13px;
  color: var(--ink);
}

.detail-goods span {
  color: var(--muted);
  font-size: 12px;
}

@media (max-width: 960px) {
  .meta-grid {
    grid-template-columns: 1fr;
  }

  .meta-block + .meta-block {
    border-left: none;
    border-top: 1px solid var(--line);
  }

  .goods-row {
    grid-template-columns: 48px minmax(0, 1fr) auto;
  }

  .qty {
    display: none;
  }

  .keyword-input {
    margin-left: 0;
    width: 100%;
  }
}
</style>
