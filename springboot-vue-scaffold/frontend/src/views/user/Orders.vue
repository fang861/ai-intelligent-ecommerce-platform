<template>
  <div class="orders-page">
    <div class="orders-shell">
      <div class="status-strip">
        <button
          v-for="tab in statusTabs"
          :key="tab.value === '' ? 'all' : tab.value"
          type="button"
          class="status-pill"
          :class="{ active: status === tab.value }"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>

      <div v-loading="loading" class="order-list">
        <div
          v-for="(order, idx) in orders"
          :key="order.id"
          class="order-card reveal"
          :style="{ '--delay': `${Math.min(idx, 8) * 40}ms` }"
        >
          <div class="order-top">
            <div class="order-shop">
              <router-link
                v-if="order.merchantId"
                :to="`/user/shop/${order.merchantId}`"
                class="shop-avatar"
                :title="order.shopName || '进店逛逛'"
              >
                <img v-if="shopAvatarOf(order)" :src="shopAvatarOf(order)" alt="" />
                <span v-else>{{ shopInitial(order) }}</span>
              </router-link>
              <div class="shop-meta">
                <router-link
                  v-if="order.merchantId"
                  :to="`/user/shop/${order.merchantId}`"
                  class="shop-link"
                >
                  <strong>{{ order.shopName || '进店逛逛' }}</strong>
                  <em>进店</em>
                </router-link>
                <span v-else class="shop-link"><strong>商城订单</strong></span>
                <span class="order-no">订单号 {{ order.orderNo || order.id }}</span>
              </div>
            </div>
            <div class="status-col">
              <span class="status-badge" :class="`tone-${statusTone(order.status)}`">
                {{ orderStatusLabel(order.status) }}
              </span>
              <span
                v-if="afterSaleStatusOf(order)"
                class="after-sale-badge"
                :class="`as-${afterSaleStatusOf(order)}`"
              >
                {{ afterSaleStatusLabel(order) }}
              </span>
            </div>
          </div>

          <div class="order-items">
            <component
              :is="item.productId ? 'router-link' : 'div'"
              v-for="item in orderItems(order)"
              :key="item.id || item.productId"
              class="order-item"
              :class="{ linkable: !!item.productId }"
              v-bind="item.productId ? { to: `/user/product/${item.productId}` } : {}"
            >
              <img :src="coverOf(item)" alt="" />
              <div class="item-info">
                <p class="name">{{ item.productName || item.name }}</p>
                <p class="qty">数量 {{ item.quantity || 1 }}</p>
              </div>
              <span class="price">{{ formatPrice(item.price) }}</span>
            </component>
          </div>

          <div class="order-foot">
            <div class="amount-wrap">
              <span class="amount-label">实付</span>
              <strong class="amount">{{ formatPrice(order.payAmount || order.totalAmount || order.amount) }}</strong>
            </div>
            <div class="ops">
              <button
                v-if="isPendingPay(order.status)"
                type="button"
                class="btn btn-primary"
                @click="openPay(order)"
              >
                去支付
              </button>
              <button
                v-if="isPendingPay(order.status) || canCancelPaid(order)"
                type="button"
                class="btn btn-ghost"
                :disabled="acting === order.id + 'cancel'"
                @click="cancel(order)"
              >
                {{ acting === order.id + 'cancel' ? '取消中…' : '取消订单' }}
              </button>
              <button
                v-if="canViewLogistics(order.status)"
                type="button"
                class="btn btn-ghost"
                @click="openLogistics(order)"
              >
                查看物流
              </button>
              <button
                v-if="isShipped(order.status)"
                type="button"
                class="btn btn-dark"
                :disabled="acting === order.id + 'confirm'"
                @click="confirm(order)"
              >
                {{ acting === order.id + 'confirm' ? '提交中…' : '确认收货' }}
              </button>
              <button
                v-if="canApplyAfterSale(order)"
                type="button"
                class="btn btn-ghost"
                @click="openAfterSale(order)"
              >
                申请售后
              </button>
              <button
                v-if="canReview(order)"
                type="button"
                class="btn btn-ghost"
                @click="openReview(order)"
              >
                评价
              </button>
              <span v-else-if="isReviewed(order)" class="reviewed-tag">已评价</span>
            </div>
          </div>
          <p v-if="afterSaleHint(order)" class="after-sale-hint">{{ afterSaleHint(order) }}</p>
        </div>

        <EmptyState
          v-if="!loading && !orders.length"
          type="order"
          title="暂无订单"
          description="下单后可在这里查看物流与进度"
        >
          <router-link to="/user/category" class="empty-cta">去选购</router-link>
        </EmptyState>
      </div>
    </div>

    <el-dialog
      v-model="payVisible"
      title="确认支付"
      width="480px"
      class="pay-dialog"
      destroy-on-close
      align-center
    >
      <div v-if="payOrder" class="pay-body">
        <div class="pay-shop-card">
          <div class="pay-shop-avatar">
            <img v-if="shopAvatarOf(payOrder)" :src="shopAvatarOf(payOrder)" alt="" />
            <span v-else>{{ shopInitial(payOrder) }}</span>
          </div>
          <div class="pay-shop-meta">
            <strong>{{ payOrder.shopName || '商城订单' }}</strong>
            <span>订单号 {{ payOrder.orderNo || payOrder.id }}</span>
          </div>
          <span class="pay-shop-tag">待付款</span>
        </div>

        <div class="pay-items">
          <div
            v-for="item in orderItems(payOrder)"
            :key="item.id || item.productId"
            class="pay-item"
          >
            <img :src="coverOf(item)" alt="" />
            <div class="pay-item-info">
              <p class="name">{{ item.productName || item.name }}</p>
              <p class="qty">x{{ item.quantity || 1 }}</p>
            </div>
            <span class="price">{{ formatPrice(item.price) }}</span>
          </div>
        </div>

        <div class="pay-total">
          <span>应付金额</span>
          <strong>{{ formatPrice(payAmount(payOrder)) }}</strong>
        </div>

        <div class="pay-qr-wrap">
          <div class="pay-qr">
            <img :src="payQrUrl" alt="支付二维码" />
          </div>
          <p class="pay-qr-tip">请使用微信 / 支付宝扫码，或点击下方确认完成余额支付</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="danger" :loading="paying" @click="confirmPay">确定支付</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="reviewVisible"
      title="发表评价"
      width="520px"
      class="review-dialog"
      destroy-on-close
      align-center
    >
      <div v-if="currentOrder" class="review-body">
        <div class="review-shop-card">
          <div class="review-shop-avatar">
            <img v-if="shopAvatarOf(currentOrder)" :src="shopAvatarOf(currentOrder)" alt="" />
            <span v-else>{{ shopInitial(currentOrder) }}</span>
          </div>
          <div class="review-shop-meta">
            <strong>{{ currentOrder.shopName || '商城订单' }}</strong>
            <span>订单号 {{ currentOrder.orderNo || currentOrder.id }}</span>
            <span v-if="currentOrder.createdAt || currentOrder.createTime">
              下单 {{ currentOrder.createdAt || currentOrder.createTime }}
            </span>
          </div>
          <span class="review-shop-tag">{{ orderStatusLabel(currentOrder.status) }}</span>
        </div>

        <div v-if="reviewItems.length > 1" class="review-pick">
          <p class="pick-label">选择要评价的商品</p>
          <div class="pick-list">
            <button
              v-for="item in reviewItems"
              :key="item.id || item.productId"
              type="button"
              class="pick-item"
              :class="{ active: String(reviewForm.orderItemId) === String(item.id) }"
              @click="selectReviewItem(item)"
            >
              <img :src="coverOf(item)" alt="" />
              <div class="pick-meta">
                <span class="pick-name">{{ item.productName || item.name }}</span>
                <span class="pick-sub">
                  {{ formatPrice(item.price) }} · x{{ item.quantity || 1 }}
                </span>
              </div>
            </button>
          </div>
        </div>

        <div v-if="reviewTarget" class="review-product">
          <img :src="coverOf(reviewTarget)" alt="" />
          <div class="review-product-info">
            <strong>{{ reviewTarget.productName || reviewTarget.name || '商品' }}</strong>
            <p class="meta-line">
              <span>购买数量 {{ reviewTarget.quantity || 1 }}</span>
              <em>·</em>
              <span>成交单价 {{ formatPrice(reviewTarget.price) }}</span>
            </p>
            <p class="price-line">
              <span>该商品小计</span>
              <b>{{ formatPrice(Number(reviewTarget.price || 0) * Number(reviewTarget.quantity || 1)) }}</b>
            </p>
          </div>
        </div>

        <div class="review-order-sum">
          <span>订单实付</span>
          <strong>{{ formatPrice(payAmount(currentOrder)) }}</strong>
        </div>

        <el-form label-position="top" class="review-form">
          <el-form-item label="商品评分">
            <div class="score-row">
              <el-rate v-model="reviewForm.score" :colors="['#e63935', '#e63935', '#e63935']" />
              <span class="score-text">{{ scoreText }}</span>
            </div>
          </el-form-item>
          <el-form-item label="评价内容">
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="说说包装、物流、商品质量怎么样，给其他买家一点参考吧"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="danger" :loading="reviewing" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="logisticsVisible"
      title="物流轨迹"
      width="480px"
      destroy-on-close
      align-center
    >
      <LogisticsTimeline
        v-if="logisticsOrder"
        :company="logisticsOrder.expressCompany"
        :tracking-no="logisticsOrder.trackingNo"
        :trace="logisticsOrder.logisticsTrace || []"
      />
    </el-dialog>

    <el-dialog
      v-model="afterSaleVisible"
      title="申请售后"
      width="520px"
      class="after-sale-dialog"
      destroy-on-close
      align-center
    >
      <div v-if="afterSaleOrder" class="after-sale-body">
        <div class="pay-shop-card">
          <div class="pay-shop-avatar">
            <img v-if="shopAvatarOf(afterSaleOrder)" :src="shopAvatarOf(afterSaleOrder)" alt="" />
            <span v-else>{{ shopInitial(afterSaleOrder) }}</span>
          </div>
          <div class="pay-shop-meta">
            <strong>{{ afterSaleOrder.shopName || '商城订单' }}</strong>
            <span>订单号 {{ afterSaleOrder.orderNo || afterSaleOrder.id }}</span>
          </div>
          <span class="pay-shop-tag">{{ orderStatusLabel(afterSaleOrder.status) }}</span>
        </div>
        <p class="as-policy">支持 7 天无理由退换（不影响二次销售）。退款同意后将退回账户余额。</p>
        <el-form label-position="top">
          <el-form-item label="售后类型" required>
            <el-radio-group v-model="afterSaleForm.type">
              <el-radio :value="1">仅退款</el-radio>
              <el-radio :value="2">退货退款</el-radio>
              <el-radio :value="3">换货</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="申请原因" required>
            <el-input
              v-model="afterSaleForm.reason"
              type="textarea"
              :rows="4"
              maxlength="500"
              show-word-limit
              placeholder="请说明退换货原因，便于商家尽快处理"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="afterSaleVisible = false">取消</el-button>
        <el-button type="danger" :loading="afterSaleSubmitting" @click="submitAfterSale">提交申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import {
  formatPrice,
  isCompletedStatus,
  isPendingPayStatus,
  isShippedStatus,
  mediaUrl,
  normalizeOrderStatus,
  orderStatusLabel,
  productCover
} from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'
import LogisticsTimeline from '@/components/LogisticsTimeline.vue'

const route = useRoute()
const loading = ref(false)
const acting = ref('')
const reviewing = ref(false)
const paying = ref(false)
const status = ref('')
const orders = ref([])
const reviewVisible = ref(false)
const payVisible = ref(false)
const logisticsVisible = ref(false)
const afterSaleVisible = ref(false)
const afterSaleSubmitting = ref(false)
const currentOrder = ref(null)
const payOrder = ref(null)
const logisticsOrder = ref(null)
const afterSaleOrder = ref(null)
const afterSaleForm = reactive({
  type: 2,
  reason: ''
})
const reviewForm = reactive({
  score: 5,
  content: '',
  orderItemId: null,
  productId: null
})

const reviewItems = computed(() =>
  orderItems(currentOrder.value).filter((item) => item.reviewed !== true)
)

const reviewTarget = computed(() => {
  const list = reviewItems.value
  if (!list.length) return null
  return list.find((i) => String(i.id) === String(reviewForm.orderItemId)) || list[0]
})

const scoreText = computed(() => {
  const map = { 1: '很差', 2: '一般', 3: '还行', 4: '满意', 5: '非常好' }
  return map[reviewForm.score] || ''
})

const statusTabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: '0' },
  { label: '待发货', value: '1' },
  { label: '待收货', value: '2' },
  { label: '已完成', value: '3' },
  { label: '已取消', value: '4' }
]

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || data?.content || []
}

function orderItems(order) {
  return order?.items || order?.orderItems || order?.products || []
}

function coverOf(item) {
  return (
    productCover(item) ||
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=160&q=80'
  )
}

function shopAvatarOf(order) {
  const url = order?.shopAvatar || order?.merchantAvatar || order?.avatar
  return url ? mediaUrl(url) : ''
}

function shopInitial(order) {
  const name = String(order?.shopName || '店')
  return name.slice(0, 1)
}

function payAmount(order) {
  return order?.payAmount || order?.totalAmount || order?.amount || 0
}

function statusTone(s) {
  const code = normalizeOrderStatus(s)
  const map = { 0: 'warn', 1: 'info', 2: 'ship', 3: 'ok', 4: 'mute' }
  return map[code] || 'mute'
}

const payQrUrl = computed(() => {
  if (!payOrder.value) return ''
  const amount = Number(payAmount(payOrder.value) || 0).toFixed(2)
  const orderNo = payOrder.value.orderNo || payOrder.value.id
  const payload = `ai-mall-pay|order=${orderNo}|amount=${amount}`
  return `https://api.qrserver.com/v1/create-qr-code/?size=200x200&margin=8&data=${encodeURIComponent(payload)}`
})

function isPendingPay(s) {
  return isPendingPayStatus(s)
}

function isShipped(s) {
  return isShippedStatus(s)
}

function canReview(order) {
  if (!order) return false
  if (order.canReview === true) return true
  if (order.canReview === false) return false
  if (!isCompletedStatus(order.status)) return false
  const items = orderItems(order)
  if (!items.length) return true
  return items.some((item) => item.reviewed !== true)
}

function isReviewed(order) {
  if (!order || !isCompletedStatus(order.status)) return false
  if (order.reviewed === true) return true
  if (order.canReview === false) return true
  const items = orderItems(order)
  return items.length > 0 && items.every((item) => item.reviewed === true)
}

function canViewLogistics(s) {
  const code = normalizeOrderStatus(s)
  return code === 2 || code === 3
}

function canCancelPaid(order) {
  return normalizeOrderStatus(order?.status) === 1 && afterSaleStatusOf(order) !== 1
}

function afterSaleStatusOf(order) {
  const s = Number(order?.afterSaleStatus ?? 0)
  return s > 0 ? s : 0
}

function afterSaleTypeLabel(type) {
  return { 1: '仅退款', 2: '退货退款', 3: '换货' }[Number(type)] || '售后'
}

function afterSaleStatusLabel(order) {
  const map = { 1: '售后处理中', 2: '售后已同意', 3: '售后已拒绝' }
  return map[afterSaleStatusOf(order)] || ''
}

function canApplyAfterSale(order) {
  if (!order) return false
  if (order.canApplyAfterSale === true) return true
  if (order.canApplyAfterSale === false) return false
  const code = normalizeOrderStatus(order.status)
  if (code !== 2 && code !== 3) return false
  const as = afterSaleStatusOf(order)
  return as === 0 || as === 3
}

function afterSaleHint(order) {
  const as = afterSaleStatusOf(order)
  if (!as) return ''
  const type = afterSaleTypeLabel(order.afterSaleType)
  if (as === 1) return `${type}申请已提交，等待商家处理`
  if (as === 2) {
    const reply = order.afterSaleReply ? ` · ${order.afterSaleReply}` : ''
    return `${type}已同意${reply}`
  }
  if (as === 3) {
    const reply = order.afterSaleReply ? `：${order.afterSaleReply}` : ''
    return `${type}已拒绝${reply}`
  }
  return ''
}

function openAfterSale(order) {
  afterSaleOrder.value = order
  afterSaleForm.type = 2
  afterSaleForm.reason = ''
  afterSaleVisible.value = true
}

async function submitAfterSale() {
  if (!afterSaleOrder.value?.id) return
  if (!afterSaleForm.type) {
    ElMessage.warning('请选择售后类型')
    return
  }
  if (!String(afterSaleForm.reason || '').trim()) {
    ElMessage.warning('请填写售后原因')
    return
  }
  afterSaleSubmitting.value = true
  try {
    await request.post(`/user/orders/${afterSaleOrder.value.id}/after-sale`, {
      type: afterSaleForm.type,
      reason: String(afterSaleForm.reason).trim()
    })
    ElMessage.success('售后申请已提交')
    afterSaleVisible.value = false
    await loadOrders()
  } finally {
    afterSaleSubmitting.value = false
  }
}

function openLogistics(order) {
  logisticsOrder.value = order
  logisticsVisible.value = true
}

function changeStatus(value) {
  status.value = value
  loadOrders()
}

async function loadOrders() {
  loading.value = true
  try {
    const data = await request.get('/user/orders', {
      params: {
        status: status.value === '' || status.value == null ? undefined : Number(status.value),
        page: 1,
        size: 50
      },
      silent: true
    })
    orders.value = unwrapList(data)
  } catch {
    orders.value = []
  } finally {
    loading.value = false
  }
}

function openPay(order) {
  payOrder.value = order
  payVisible.value = true
}

async function confirmPay() {
  if (!payOrder.value?.id) return
  paying.value = true
  try {
    await request.post(`/user/orders/${payOrder.value.id}/pay`)
    ElMessage.success('支付成功')
    payVisible.value = false
    payOrder.value = null
    await loadOrders()
  } finally {
    paying.value = false
  }
}

async function cancel(order) {
  const paid = normalizeOrderStatus(order.status) === 1
  await ElMessageBox.confirm(
    paid ? '取消后将退回支付金额到账户余额，确定取消？' : '确定取消该订单？',
    '提示',
    { type: 'warning' }
  )
  acting.value = order.id + 'cancel'
  try {
    await request.post(`/user/orders/${order.id}/cancel`)
    ElMessage.success(paid ? '已取消，款项已退回余额' : '已取消')
    await loadOrders()
  } finally {
    acting.value = ''
  }
}

async function confirm(order) {
  await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
  acting.value = order.id + 'confirm'
  try {
    await request.post(`/user/orders/${order.id}/confirm`)
    ElMessage.success('已确认收货')
    await loadOrders()
  } finally {
    acting.value = ''
  }
}

function openReview(order) {
  currentOrder.value = order
  const items = orderItems(order).filter((item) => item.reviewed !== true)
  const first = items[0]
  if (!first) {
    ElMessage.info('该订单商品已全部评价')
    return
  }
  reviewForm.score = 5
  reviewForm.content = ''
  reviewForm.orderItemId = first?.id || null
  reviewForm.productId = first?.productId || first?.id || null
  reviewVisible.value = true
}

function selectReviewItem(item) {
  reviewForm.orderItemId = item.id
  reviewForm.productId = item.productId || item.id
}

async function submitReview() {
  if (!reviewForm.content.trim()) {
    ElMessage.warning('请填写评价内容')
    return
  }
  const target = reviewTarget.value
  if (!target?.id && !reviewForm.orderItemId) {
    ElMessage.warning('请选择要评价的商品')
    return
  }
  reviewing.value = true
  try {
    await request.post('/user/reviews', {
      orderId: currentOrder.value.id,
      orderItemId: reviewForm.orderItemId || target.id,
      productId: reviewForm.productId || target.productId,
      rating: reviewForm.score,
      score: reviewForm.score,
      content: reviewForm.content.trim()
    })
    ElMessage.success('评价成功')
    reviewVisible.value = false
    await loadOrders()
  } finally {
    reviewing.value = false
  }
}

onMounted(() => {
  if (route.query.status != null && route.query.status !== '') {
    status.value = String(route.query.status)
  }
  loadOrders()
})
</script>

<style scoped>
.orders-page {
  --accent: #e1251b;
  --ink: #1a1a1a;
  --muted: #8b929c;
  max-width: none;
  margin: 0;
  padding: 0;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.orders-shell {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 18px;
  box-shadow: 0 8px 24px rgba(26, 26, 26, 0.03);
  overflow: hidden;
  padding: 18px 24px 0;
}

.status-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
  padding: 10px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.04);
}

.status-pill {
  height: 34px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease;
}

.status-pill:hover {
  color: var(--ink);
  background: #f3f4f6;
}

.status-pill.active {
  color: #fff;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  box-shadow: 0 8px 18px rgba(230, 57, 53, 0.28);
}

.order-list {
  min-height: 240px;
  display: grid;
  gap: 16px;
}

.reveal {
  animation: rise 0.45s ease both;
  animation-delay: var(--delay, 0ms);
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.order-card {
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.04);
  padding: 0;
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 12px 32px rgba(15, 23, 42, 0.05);
}

.order-card::before {
  content: '';
  position: absolute;
  left: 14px;
  top: 18px;
  width: 3px;
  height: 28px;
  border-radius: 999px;
  background: linear-gradient(180deg, #e63935, #ff8a75);
}

.order-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px 18px 14px 28px;
  background: linear-gradient(90deg, rgba(230, 57, 53, 0.035), transparent 52%);
}

.order-shop {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.shop-avatar {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  text-decoration: none;
  color: #fff;
  font-size: 16px;
  font-weight: 760;
  background: linear-gradient(135deg, #111827, #374151);
  box-shadow:
    0 8px 18px rgba(15, 23, 42, 0.16),
    inset 0 0 0 1px rgba(255, 255, 255, 0.08);
  transition: transform 0.2s ease;
}

.shop-avatar:hover {
  transform: translateY(-1px) scale(1.03);
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.shop-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.shop-link {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.shop-link strong {
  font-size: 15px;
  font-weight: 740;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.shop-link em {
  flex-shrink: 0;
  height: 20px;
  padding: 0 8px;
  border-radius: 999px;
  font-style: normal;
  font-size: 11px;
  font-weight: 700;
  color: var(--accent);
  background: #fff1f0;
  display: inline-flex;
  align-items: center;
}

.shop-link:hover strong {
  color: var(--accent);
}

.order-no {
  color: #9ca3af;
  font-size: 12px;
  letter-spacing: 0.01em;
}

.status-badge {
  flex-shrink: 0;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.status-col {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
  flex-shrink: 0;
}

.after-sale-badge {
  height: 22px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.after-sale-badge.as-1 {
  color: #c2410c;
  background: #fff7ed;
}

.after-sale-badge.as-2 {
  color: #15803d;
  background: #f0fdf4;
}

.after-sale-badge.as-3 {
  color: #6b7280;
  background: #f3f4f6;
}

.after-sale-hint {
  margin: 0;
  padding: 0 18px 14px;
  font-size: 12px;
  color: #9a3412;
  line-height: 1.5;
}

.as-policy {
  margin: 0 0 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff7ed;
  color: #9a3412;
  font-size: 13px;
  line-height: 1.55;
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
  color: #6b7280;
  background: #f3f4f6;
}

.order-items {
  padding: 4px 18px 6px;
}

.order-item {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr) auto;
  gap: 14px;
  align-items: center;
  padding: 10px 0;
  color: inherit;
  text-decoration: none;
  border-radius: 12px;
  transition: background 0.15s ease;
}

.order-item.linkable {
  cursor: pointer;
}

.order-item.linkable:hover {
  background: rgba(225, 37, 27, 0.04);
}

.order-item.linkable:hover .name {
  color: #e1251b;
}

.order-item + .order-item {
  border-top: none;
}

.order-item img {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  object-fit: cover;
  background: #f3f4f6;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.08);
}

.name {
  margin: 0;
  font-size: 14px;
  font-weight: 650;
  color: var(--ink);
  line-height: 1.45;
}

.qty {
  margin: 6px 0 0;
  font-size: 12px;
  color: #9ca3af;
}

.price {
  color: var(--ink);
  font-weight: 720;
  font-size: 14px;
}

.order-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-top: 4px;
  padding: 14px 18px 16px;
  background: linear-gradient(180deg, #fcfcfd, #fff);
}

.amount-wrap {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
}

.amount-label {
  font-size: 12px;
  color: #9ca3af;
}

.amount {
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--accent);
}

.ops {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  align-items: center;
  gap: 8px;
}

.reviewed-tag {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 14px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 650;
  color: #94a3b8;
  background: #f1f5f9;
}

.btn {
  height: 36px;
  padding: 0 16px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  border: 1px solid transparent;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    background 0.2s ease;
}

.btn:disabled {
  opacity: 0.65;
  cursor: not-allowed;
}

.btn-primary {
  color: #fff;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  box-shadow: 0 8px 18px rgba(230, 57, 53, 0.28);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-dark {
  color: #fff;
  background: #111827;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.18);
}

.btn-ghost {
  color: #4b5563;
  background: #fff;
  border-color: rgba(15, 23, 42, 0.1);
}

.btn-ghost:hover:not(:disabled) {
  color: var(--accent);
  border-color: rgba(230, 57, 53, 0.24);
  background: #fff8f7;
}

.pay-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pay-shop-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 16px;
  background:
    linear-gradient(90deg, rgba(230, 57, 53, 0.05), transparent 55%),
    #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.05);
}

.pay-shop-avatar {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 17px;
  font-weight: 760;
  background: linear-gradient(135deg, #111827, #374151);
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.16);
}

.pay-shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.pay-shop-meta {
  min-width: 0;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.pay-shop-meta strong {
  font-size: 15px;
  font-weight: 740;
  color: #1a1a1a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pay-shop-meta span {
  font-size: 12px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pay-shop-tag {
  flex-shrink: 0;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
  color: #c2410c;
  background: #fff7ed;
  display: inline-flex;
  align-items: center;
}

.pay-items {
  padding: 4px 2px;
  border-radius: 14px;
  background: #f8fafc;
}

.pay-item {
  display: grid;
  grid-template-columns: 52px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
}

.pay-item img {
  width: 52px;
  height: 52px;
  border-radius: 10px;
  object-fit: cover;
  background: #e5e7eb;
}

.pay-item-info .name {
  font-size: 13px;
}

.pay-total {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 14px;
  background: #fff1f0;
  color: #6b7280;
  font-size: 14px;
}

.pay-total strong {
  color: var(--accent);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.pay-qr-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 8px 0 4px;
}

.pay-qr {
  width: 180px;
  height: 180px;
  padding: 10px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.06);
}

.pay-qr img {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 8px;
}

.pay-qr-tip {
  margin: 0;
  text-align: center;
  font-size: 12px;
  color: #9ca3af;
  line-height: 1.5;
  max-width: 280px;
}

.review-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.review-shop-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.05);
}

.review-shop-avatar {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  overflow: hidden;
  background: linear-gradient(145deg, #ffe4e1, #fecaca);
  display: grid;
  place-items: center;
  font-size: 16px;
  font-weight: 760;
  color: #b91c1c;
}

.review-shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.review-shop-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-shop-meta strong {
  font-size: 14px;
  font-weight: 720;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-shop-meta span {
  font-size: 12px;
  color: #94a3b8;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.review-shop-tag {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 650;
  color: #16a34a;
  background: rgba(22, 163, 74, 0.1);
}

.review-pick .pick-label {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 650;
  color: #334155;
}

.pick-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pick-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: #fff;
  cursor: pointer;
  text-align: left;
}

.pick-item img {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
  background: #f1f5f9;
  flex-shrink: 0;
}

.pick-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.pick-name {
  font-size: 13px;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pick-sub {
  font-size: 12px;
  color: #94a3b8;
}

.pick-item.active {
  border-color: rgba(230, 57, 53, 0.35);
  background: #fff8f7;
  box-shadow: 0 0 0 1px rgba(230, 57, 53, 0.12);
}

.review-product {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 14px;
  padding: 14px;
  border-radius: 14px;
  background: linear-gradient(90deg, rgba(230, 57, 53, 0.05), transparent 55%), #fff;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.review-product img {
  width: 88px;
  height: 88px;
  border-radius: 12px;
  object-fit: cover;
  background: #e2e8f0;
}

.review-product-info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.review-product-info strong {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 15px;
  font-weight: 720;
  color: #0f172a;
  line-height: 1.4;
}

.review-product-info .meta-line {
  margin: 8px 0 0;
  font-size: 12px;
  color: #64748b;
}

.review-product-info em {
  font-style: normal;
  margin: 0 6px;
  color: #cbd5e1;
}

.review-product-info .price-line {
  margin: 10px 0 0;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  font-size: 12px;
  color: #64748b;
}

.review-product-info .price-line b {
  font-size: 16px;
  color: var(--accent, #e63935);
  font-weight: 760;
}

.review-order-sum {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 12px;
  background: #fff8f7;
  border: 1px solid rgba(230, 57, 53, 0.1);
  font-size: 13px;
  color: #64748b;
}

.review-order-sum strong {
  font-size: 18px;
  font-weight: 760;
  color: var(--accent, #e63935);
}

.score-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.score-text {
  font-size: 13px;
  font-weight: 650;
  color: #e63935;
}

.review-form :deep(.el-form-item__label) {
  font-weight: 650;
  color: #334155;
}

@media (max-width: 720px) {
  .order-foot {
    flex-direction: column;
    align-items: stretch;
  }

  .ops {
    justify-content: stretch;
  }

  .ops .btn {
    flex: 1;
  }
}
</style>

<style>
/* 支付弹窗挂载到 body：可滚动但隐藏滚动条 */
.el-dialog.pay-dialog {
  margin-top: 6vh !important;
  margin-bottom: 6vh !important;
  max-height: 88vh;
  display: flex;
  flex-direction: column;
}

.el-dialog.pay-dialog .el-dialog__body {
  overflow-y: auto !important;
  overflow-x: hidden !important;
  max-height: min(62vh, 560px) !important;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.el-dialog.pay-dialog .el-dialog__body::-webkit-scrollbar {
  display: none;
  width: 0;
  height: 0;
}
</style>
