<template>
  <div class="page merchant-home">
    <el-card shadow="never" class="panel head-panel">
      <template #header>
        <div class="header-row">
          <span>店铺数据</span>
          <el-button type="primary" plain :loading="loading" @click="load">刷新数据</el-button>
        </div>
      </template>
      <p class="page-desc">{{ todayText }} · {{ shopName }}</p>
    </el-card>

    <div class="stat-grid" v-loading="loading">
      <article v-for="c in cards" :key="c.key" class="stat-card" :class="`tone-${c.tone}`">
        <div class="stat-top">
          <span class="stat-icon">
            <el-icon :size="18"><component :is="c.icon" /></el-icon>
          </span>
          <span class="stat-label">{{ c.label }}</span>
        </div>
        <div class="stat-value" :class="{ warn: c.warn }">{{ c.value }}</div>
        <div class="stat-hint">{{ c.hint }}</div>
      </article>
    </div>

    <div class="panel wide">
      <div class="panel-head">
        <h3>近 30 日销售趋势</h3>
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
          <h3>库存健康度</h3>
        </div>
        <div ref="stockRef" class="chart sm" />
      </div>

      <div class="panel">
        <div class="panel-head">
          <h3>热销商品</h3>
        </div>
        <div class="product-rank" v-loading="loading">
          <div v-for="(item, idx) in topProducts" :key="item.id || idx" class="rank-item">
            <span class="rank-no" :class="{ hot: idx < 3 }">{{ idx + 1 }}</span>
            <img :src="coverOf(item)" alt="" />
            <div class="rank-meta">
              <strong>{{ item.name || '商品' }}</strong>
              <em>库存 {{ item.stock ?? 0 }} · {{ formatPrice(item.price) }}</em>
            </div>
            <b class="rank-sales">{{ item.sales ?? 0 }}</b>
          </div>
          <EmptyState
            v-if="!loading && !topProducts.length"
            compact
            type="product"
            title="暂无商品数据"
            description="上架商品后可在这里看到销量排行"
          />
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
  Goods,
  ShoppingCart,
  Van
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { primaryToRgba, useThemeStore } from '@/stores/theme'
import { formatPrice, mediaUrl, productCover } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

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

const shopName = computed(
  () => userStore.user?.shopName || userStore.user?.nickname || userStore.user?.username || '商家'
)
const todayText = computed(() => {
  const d = new Date()
  const w = ['日', '一', '二', '三', '四', '五', '六'][d.getDay()]
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} 星期${w}`
})

const pendingShip = computed(
  () => stats.value.pendingShipCount ?? stats.value.waitShip ?? 0
)

const cards = computed(() => [
  {
    key: 'sales',
    label: '今日销售额',
    value: formatPrice(stats.value.todaySales ?? 0),
    hint: `累计营收 ${formatPrice(stats.value.totalRevenue ?? stats.value.salesAmount ?? 0)}`,
    icon: Coin,
    tone: 'teal'
  },
  {
    key: 'orders',
    label: '订单总数',
    value: stats.value.orderCount ?? stats.value.orders ?? 0,
    hint: '本店全部订单',
    icon: ShoppingCart,
    tone: 'slate'
  },
  {
    key: 'products',
    label: '在售商品',
    value: stats.value.productCount ?? stats.value.onSaleCount ?? 0,
    hint: `低库存 ${stats.value.lowStockCount ?? stats.value.stockStats?.lowStock ?? 0} 件`,
    icon: Goods,
    tone: 'amber'
  },
  {
    key: 'pending',
    label: '待发货',
    value: pendingShip.value,
    hint: pendingShip.value > 0 ? '请尽快处理发货' : '暂无待发货订单',
    icon: Van,
    tone: 'rose',
    warn: pendingShip.value > 0
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

function initStockHealth(stock = {}) {
  if (!stockRef.value) return
  if (!stockChart) stockChart = echarts.init(stockRef.value)
  const normal = Number(stock.normal ?? 0)
  const low = Number(stock.lowStock ?? 0)
  const out = Number(stock.outOfStock ?? 0)
  const data = [
    { name: '库存充足', value: normal },
    { name: '低库存', value: low },
    { name: '已售罄', value: out }
  ].filter((i) => i.value > 0)
  stockChart.setOption(
    {
      color: [themePrimary.value, '#f59e0b', '#ef4444'],
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
          data: data.length ? data : [{ name: '暂无商品', value: 1 }]
        }
      ]
    },
    true
  )
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/merchant/stats', { silent: true })
    stats.value = data || {}
    topProducts.value = (data?.topProducts || []).slice(0, 6)
    lastSales = data?.salesTrend || data?.salesLast30Days || data?.dailySales || []
    lastOrders = data?.orderStatusStats || []
    lastStock = data?.stockStats || {}
    await nextTick()
    initSales(lastSales)
    initOrders(lastOrders)
    initStockHealth(lastStock)
  } catch {
    stats.value = {}
    topProducts.value = []
    lastSales = []
    lastOrders = []
    lastStock = {}
    await nextTick()
    initSales([])
    initOrders([])
    initStockHealth({})
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
    initStockHealth(lastStock)
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
.merchant-home {
  --ink: #1f2937;
  --muted: #64748b;
  --line: rgba(15, 23, 42, 0.06);
  --accent: var(--app-primary, #3b82f6);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.head-panel :deep(.el-card__body) {
  padding-top: 0;
}

.page-desc {
  margin: 0;
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

.stat-value.warn {
  color: #dc2626;
}

.stat-hint {
  margin-top: 8px;
  font-size: 12px;
  color: #94a3b8;
}

.tone-teal .stat-icon {
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 12%, #ffffff);
}

.tone-slate .stat-icon {
  color: #475569;
  background: #f1f5f9;
}

.tone-amber .stat-icon {
  color: #d97706;
  background: #fffbeb;
}

.tone-rose .stat-icon {
  color: #e11d48;
  background: #fff1f2;
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

.rank-item:hover {
  background: color-mix(in srgb, var(--accent) 8%, #ffffff);
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

@media (max-width: 1100px) {
  .stat-grid {
    grid-template-columns: 1fr 1fr;
  }

  .charts {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 720px) {
  .stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
