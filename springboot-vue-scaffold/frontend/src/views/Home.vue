<template>
  <div class="page home">
    <header class="page-head">
      <div class="head-copy">
        <h2>数据概览</h2>
        <p>{{ todayText }} · 你好，{{ displayName }}</p>
      </div>
      <el-button type="primary" plain :loading="loading" @click="load">刷新数据</el-button>
    </header>

    <div class="stat-grid" v-loading="loading">
      <article v-for="c in cards" :key="c.key" class="stat-card" :class="`tone-${c.tone}`">
        <div class="stat-top">
          <span class="stat-icon">
            <el-icon :size="18"><component :is="c.icon" /></el-icon>
          </span>
          <span class="stat-label">{{ c.label }}</span>
        </div>
        <div class="stat-value">{{ c.value }}</div>
        <div class="stat-hint">{{ c.hint }}</div>
      </article>
    </div>

    <div class="panel wide">
      <div class="panel-head">
        <h3>近 30 日销售额</h3>
      </div>
      <div ref="salesRef" class="chart" />
    </div>

    <div class="charts">
      <div class="panel">
        <div class="panel-head">
          <h3>订单状态</h3>
        </div>
        <div ref="orderRef" class="chart sm" />
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>分类库存</h3>
        </div>
        <div ref="stockRef" class="chart sm" />
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>热销商品</h3>
        </div>
        <div class="product-rank">
          <div v-for="(item, idx) in topProducts" :key="item.id || idx" class="rank-item">
            <span class="rank-no" :class="{ hot: idx < 3 }">{{ idx + 1 }}</span>
            <img :src="coverOf(item)" alt="" />
            <div class="rank-meta">
              <strong>{{ item.name || '商品' }}</strong>
              <em>{{ item.shopName ? item.shopName + ' · ' : '' }}库存 {{ item.stock ?? 0 }}</em>
            </div>
            <b class="rank-sales">{{ item.sales ?? 0 }}</b>
          </div>
          <p v-if="!loading && !topProducts.length" class="rank-empty">暂无热销数据</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import * as echarts from 'echarts'
import {
  Coin,
  ShoppingCart,
  Shop,
  User
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { primaryToRgba, useThemeStore } from '@/stores/theme'
import { formatPrice, mediaUrl, productCover } from '@/utils/format'

const userStore = useUserStore()
const themeStore = useThemeStore()
const loading = ref(false)
const stats = ref({})
const topProducts = ref([])
const salesRef = ref()
const orderRef = ref()
const stockRef = ref()

let salesChart
let orderChart
let stockChart
let lastSales = []
let lastOrders = []
let lastStock = []

const themePrimary = computed(() => themeStore.primary)
const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '管理员')
const todayText = computed(() => {
  const d = new Date()
  const w = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 星期${w}`
})

const cards = computed(() => [
  {
    key: 'sales',
    label: '今日销售额',
    value: formatPrice(stats.value.todaySales ?? 0),
    hint: `累计营收 ${formatPrice(stats.value.totalRevenue ?? stats.value.salesAmount ?? 0)}`,
    icon: Coin,
    tone: 'blue'
  },
  {
    key: 'orders',
    label: '订单总数',
    value: stats.value.orderCount ?? stats.value.orders ?? 0,
    hint: '含全部状态订单',
    icon: ShoppingCart,
    tone: 'slate'
  },
  {
    key: 'users',
    label: '用户数',
    value: stats.value.userCount ?? stats.value.users ?? 0,
    hint: '平台注册买家',
    icon: User,
    tone: 'cyan'
  },
  {
    key: 'merchants',
    label: '商家数',
    value: stats.value.merchantCount ?? stats.value.merchants ?? 0,
    hint: '已审核通过店铺',
    icon: Shop,
    tone: 'amber'
  }
])

const axisStyle = {
  axisLine: { lineStyle: { color: '#e5e7eb' } },
  axisTick: { show: false },
  axisLabel: { color: '#9ca3af', fontSize: 11 },
  splitLine: { lineStyle: { color: '#f3f4f6' } }
}

function coverOf(item) {
  return (
    productCover(item) ||
    mediaUrl(item?.cover) ||
    'data:image/svg+xml,' +
      encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80"><rect fill="#f3f4f6" width="80" height="80"/></svg>'
      )
  )
}

function shortDate(date) {
  if (!date) return ''
  const s = String(date)
  return s.length >= 10 ? s.slice(5) : s
}

function initSales(series = []) {
  if (!salesRef.value) return
  if (!salesChart) salesChart = echarts.init(salesRef.value)
  const dates = series.map((i) => shortDate(i.date || i.day || i.label))
  const values = series.map((i) => Number(i.amount ?? i.value ?? i.sales ?? 0))
  const hasData = values.some((v) => v > 0)
  salesChart.setOption(
    {
      color: [themePrimary.value],
      grid: { left: 48, right: 16, top: 28, bottom: 32 },
      tooltip: {
        trigger: 'axis',
        backgroundColor: '#fff',
        borderColor: '#e5e7eb',
        borderWidth: 1,
        textStyle: { color: '#374151', fontSize: 12 },
        formatter: (params) => {
          const p = params?.[0]
          if (!p) return ''
          return `${p.axisValue}<br/>销售额 ¥${Number(p.data || 0).toFixed(2)}`
        }
      },
      xAxis: {
        type: 'category',
        data: dates.length ? dates : ['暂无'],
        boundaryGap: false,
        ...axisStyle
      },
      yAxis: { type: 'value', ...axisStyle },
      series: [
        {
          type: 'line',
          smooth: true,
          symbol: 'circle',
          symbolSize: 5,
          showSymbol: !hasData || values.length <= 14,
          lineStyle: { width: 2.5 },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: primaryToRgba(themePrimary.value, 0.18) },
              { offset: 1, color: primaryToRgba(themePrimary.value, 0.02) }
            ])
          },
          data: values.length ? values : [0]
        }
      ]
    },
    true
  )
}

function initOrders(list = []) {
  if (!orderRef.value) return
  if (!orderChart) orderChart = echarts.init(orderRef.value)
  const raw = (list.length ? list : []).map((i) => ({
    name: i.name || i.label,
    value: Number(i.value ?? i.count ?? 0)
  }))
  const data = raw.filter((i) => i.value > 0)
  orderChart.setOption(
    {
      color: ['#94a3b8', '#f59e0b', themePrimary.value, '#10b981', '#cbd5e1'],
      tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
      legend: {
        bottom: 0,
        icon: 'circle',
        itemWidth: 8,
        itemHeight: 8,
        textStyle: { color: '#6b7280', fontSize: 11 }
      },
      series: [
        {
          type: 'pie',
          radius: ['42%', '66%'],
          center: ['50%', '44%'],
          itemStyle: { borderColor: '#fff', borderWidth: 2 },
          label: { show: false },
          data: data.length ? data : [{ name: '暂无', value: 1 }]
        }
      ]
    },
    true
  )
}

function initStock(list = []) {
  if (!stockRef.value) return
  if (!stockChart) stockChart = echarts.init(stockRef.value)
  const names = list.map((i) => i.name || i.categoryName || i.label)
  const values = list.map((i) => Number(i.stock ?? i.value ?? i.count ?? 0))
  stockChart.setOption(
    {
      grid: { left: 44, right: 12, top: 20, bottom: names.length > 5 ? 52 : 32 },
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      xAxis: {
        type: 'category',
        data: names.length ? names : ['暂无'],
        axisLabel: {
          color: '#9ca3af',
          fontSize: 11,
          rotate: names.length > 5 ? 28 : 0,
          interval: 0
        },
        axisTick: { show: false },
        axisLine: { lineStyle: { color: '#e5e7eb' } }
      },
      yAxis: { type: 'value', ...axisStyle },
      series: [
        {
          type: 'bar',
          barMaxWidth: 22,
          data: values.length ? values : [0],
          itemStyle: {
            borderRadius: [4, 4, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: primaryToRgba(themePrimary.value, 0.72) },
              { offset: 1, color: themePrimary.value }
            ])
          }
        }
      ]
    },
    true
  )
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/admin/stats', { silent: true })
    stats.value = data || {}
    topProducts.value = (data?.topProducts || []).slice(0, 6)
    lastSales = data?.salesTrend || data?.salesLast30Days || data?.dailySales || []
    lastOrders = data?.orderStatusStats || []
    lastStock = data?.stockOverview || data?.stockByCategory || []
    await nextTick()
    initSales(lastSales)
    initOrders(lastOrders)
    initStock(lastStock)
  } catch {
    stats.value = {}
    topProducts.value = []
    lastSales = []
    lastOrders = []
    lastStock = []
    await nextTick()
    initSales([])
    initOrders([])
    initStock([])
  } finally {
    loading.value = false
  }
}

function onResize() {
  salesChart?.resize()
  orderChart?.resize()
  stockChart?.resize()
}

watch(
  () => themeStore.currentKey,
  () => {
    initSales(lastSales)
    initOrders(lastOrders)
    initStock(lastStock)
  }
)

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  salesChart?.dispose()
  orderChart?.dispose()
  stockChart?.dispose()
})
</script>

<style scoped>
.home {
  --ink: #1f2937;
  --muted: #64748b;
  --line: rgba(15, 23, 42, 0.06);
  --panel: #fff;
  --accent: var(--app-primary, #3b82f6);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.head-copy h2 {
  margin: 0;
  padding-left: 12px;
  position: relative;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
}

.head-copy h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 16px;
  border-radius: 2px;
  background: var(--accent);
}

.head-copy p {
  margin: 8px 0 0;
  padding-left: 12px;
  font-size: 13px;
  color: var(--muted);
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.stat-card {
  padding: 18px 18px 16px;
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.stat-top {
  display: flex;
  align-items: center;
  gap: 10px;
}

.stat-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--muted);
}

.stat-value {
  margin-top: 14px;
  font-size: 26px;
  font-weight: 740;
  line-height: 1.15;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.stat-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.tone-blue .stat-icon {
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, #ffffff);
}

.tone-slate .stat-icon {
  color: #475569;
  background: #f1f5f9;
}

.tone-cyan .stat-icon {
  color: #0891b2;
  background: #ecfeff;
}

.tone-amber .stat-icon {
  color: #d97706;
  background: #fffbeb;
}

.panel {
  background: var(--panel);
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 16px 18px 12px;
  min-width: 0;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.04);
}

.panel.wide {
  padding-bottom: 10px;
}

.panel-head h3 {
  margin: 0;
  padding-left: 12px;
  position: relative;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.panel-head h3::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: var(--accent);
}

.charts {
  display: grid;
  grid-template-columns: 1fr 1fr 1.1fr;
  gap: 14px;
  align-items: stretch;
}

.charts > .panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 320px;
}

.chart {
  height: 280px;
}

.chart.sm {
  flex: 1;
  min-height: 260px;
  height: auto;
}

.product-rank {
  flex: 1;
  min-height: 260px;
  max-height: 280px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 0 4px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.product-rank::-webkit-scrollbar {
  display: none;
}

.rank-item {
  display: grid;
  grid-template-columns: 24px 42px minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
  padding: 8px;
  border-radius: 10px;
  background: #f8fafc;
  transition: background 0.15s;
}

.rank-no {
  width: 22px;
  height: 22px;
  border-radius: 6px;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
  background: #e2e8f0;
}

.rank-no.hot {
  color: #fff;
  background: var(--accent);
}

.rank-item:hover {
  background: color-mix(in srgb, var(--accent) 8%, #ffffff);
}

.rank-item img {
  width: 42px;
  height: 42px;
  border-radius: 8px;
  object-fit: cover;
  background: #e2e8f0;
  border: 1px solid var(--line);
}

.rank-meta {
  min-width: 0;
}

.rank-meta strong {
  display: block;
  font-size: 13px;
  font-weight: 650;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-meta em {
  display: block;
  margin-top: 2px;
  font-style: normal;
  font-size: 12px;
  color: #94a3b8;
}

.rank-sales {
  font-size: 14px;
  font-weight: 740;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.rank-empty {
  margin: auto;
  color: #94a3b8;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: 1fr 1fr;
  }

  .charts {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .page-head {
    flex-direction: column;
    align-items: flex-start;
  }

  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
