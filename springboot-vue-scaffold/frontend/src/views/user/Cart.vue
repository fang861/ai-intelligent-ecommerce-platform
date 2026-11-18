<template>
  <div class="cart-page">
    <div class="page-head">
      <div class="head-shell">
        <div class="head-glow" aria-hidden="true" />
        <div class="head-inner">
          <div class="head-copy">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <em>购物车</em>
            </nav>
            <h1>购物车</h1>
            <p class="head-desc">
              按店铺分组结算 · 共 <b>{{ items.length }}</b> 件商品
              <template v-if="shopGroups.length"> · <b>{{ shopGroups.length }}</b> 家店铺</template>
            </p>
          </div>
          <div v-if="items.length" class="head-actions">
            <el-checkbox
              v-model="allChecked"
              class="action-select"
              :indeterminate="indeterminate"
              @change="toggleAll"
            >
              <span class="action-select-text">
                <strong>全选</strong>
                <em>全部店铺</em>
              </span>
            </el-checkbox>
            <span class="action-divider" aria-hidden="true" />
            <div class="action-meta">
              <span class="action-count">
                已选 <b>{{ selectedIds.length }}</b>
              </span>
              <button
                type="button"
                class="action-delete"
                :disabled="!selectedIds.length"
                @click="removeSelected"
              >
                删除选中
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="cart-shell" v-loading="loading">
      <template v-if="items.length">
        <div v-for="group in shopGroups" :key="group.merchantId" class="shop-card">
          <div class="shop-head">
            <el-checkbox
              :model-value="isShopChecked(group)"
              :indeterminate="isShopIndeterminate(group)"
              @change="(v) => toggleShop(group, v)"
            />
            <router-link :to="`/user/shop/${group.merchantId}`" class="shop-name">
              <span class="shop-avatar" :class="{ 'has-img': !!group.shopAvatar }">
                <img v-if="group.shopAvatar" :src="group.shopAvatar" alt="" />
                <template v-else>{{ String(group.shopName || '店').slice(0, 1) }}</template>
              </span>
              <span class="shop-text">
                <strong>{{ group.shopName }}</strong>
                <em>{{ group.items.length }} 件商品</em>
              </span>
            </router-link>
            <router-link :to="`/user/shop/${group.merchantId}`" class="enter-shop">进店逛逛</router-link>
          </div>

          <div v-for="item in group.items" :key="item.id" class="cart-row">
            <el-checkbox v-model="item._checked" class="row-check" />
            <router-link :to="`/user/product/${item.productId || item.product?.id}`" class="cover">
              <img :src="coverOf(item)" alt="" />
            </router-link>
            <div class="info">
              <router-link :to="`/user/product/${item.productId || item.product?.id}`" class="name">
                {{ item.productName || item.product?.name || '商品' }}
              </router-link>
              <div class="meta-line">
                <span class="unit-price">{{ formatPrice(item.price ?? item.product?.price) }}</span>
                <span v-if="item.product?.stock != null" class="stock">库存 {{ item.product.stock }}</span>
              </div>
            </div>
            <div class="row-ops">
              <div class="qty-wrap">
                <span class="ops-label">数量</span>
                <el-input-number
                  v-model="item.quantity"
                  :min="1"
                  :max="Math.max(1, item.product?.stock || 999)"
                  size="small"
                  @change="(v) => updateQty(item, v)"
                />
              </div>
              <div class="subtotal">
                <span class="sub-label">小计</span>
                <strong>{{ formatPrice(((item.price ?? item.product?.price) || 0) * item.quantity) }}</strong>
              </div>
              <button type="button" class="row-del" title="删除" @click="removeOne(item)">删除</button>
            </div>
          </div>
        </div>

        <div class="checkout-bar">
          <div class="sum">
            <span>已选 <b>{{ selectedIds.length }}</b> 件</span>
            <span class="sum-total">
              合计
              <em>{{ formatPrice(selectedTotal) }}</em>
            </span>
          </div>
          <button type="button" class="checkout-btn" :disabled="checkingOut" @click="checkout">
            {{ checkingOut ? '提交中…' : '去结算' }}
          </button>
        </div>
      </template>

      <EmptyState
        v-else-if="!loading"
        type="cart"
        title="购物车空空如也"
        description="精选好物等你带回，去分类页逛逛吧"
      >
        <router-link to="/user/category" class="empty-cta">去逛逛</router-link>
      </EmptyState>
    </div>

    <el-dialog v-model="addrVisible" title="选择收货地址" width="520px">
      <el-radio-group v-model="addressId" class="addr-group">
        <el-radio v-for="a in addresses" :key="a.id" :value="a.id" class="addr-radio">
          <div>
            <strong>{{ a.receiver || a.receiverName || a.name }} {{ a.phone }}</strong>
            <p>{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail || a.address }}</p>
          </div>
        </el-radio>
      </el-radio-group>
      <EmptyState
        v-if="!addresses.length"
        compact
        type="address"
        title="还没有收货地址"
        description="请先添加收货地址后再下单"
      />
      <template #footer>
        <el-button @click="$router.push('/user/addresses')">管理地址</el-button>
        <el-button type="primary" :loading="checkingOut" :disabled="!addressId" @click="confirmOrder">
          确认下单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { formatPrice, mediaUrl, productCover } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const loading = ref(false)
const checkingOut = ref(false)
const items = ref([])
const addresses = ref([])
const addrVisible = ref(false)
const addressId = ref(null)

const selectedIds = computed(() => items.value.filter((i) => i._checked).map((i) => i.id))
const selectedTotal = computed(() =>
  items.value
    .filter((i) => i._checked)
    .reduce((sum, i) => sum + Number((i.price ?? i.product?.price) || 0) * Number(i.quantity || 1), 0)
)

const allChecked = computed({
  get: () => items.value.length > 0 && items.value.every((i) => i._checked),
  set: () => {}
})

const indeterminate = computed(
  () => items.value.some((i) => i._checked) && !items.value.every((i) => i._checked)
)

const shopGroups = computed(() => {
  const map = new Map()
  for (const item of items.value) {
    const merchantId = item.product?.merchantId || item.merchantId || 0
    const shopName = item.product?.shopName || item.shopName || '未知店铺'
    const shopAvatar =
      mediaUrl(item.product?.shopAvatar || item.shopAvatar) || ''
    if (!map.has(merchantId)) {
      map.set(merchantId, {
        merchantId,
        shopName,
        shopAvatar,
        items: []
      })
    } else if (!map.get(merchantId).shopAvatar && shopAvatar) {
      map.get(merchantId).shopAvatar = shopAvatar
    }
    map.get(merchantId).items.push(item)
  }
  return [...map.values()]
})

function coverOf(item) {
  return (
    productCover(item.product || item) ||
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=200&q=80'
  )
}

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || data?.items || []
}

function isShopChecked(group) {
  return group.items.length > 0 && group.items.every((i) => i._checked)
}

function isShopIndeterminate(group) {
  return group.items.some((i) => i._checked) && !group.items.every((i) => i._checked)
}

function toggleShop(group, val) {
  group.items.forEach((i) => {
    i._checked = !!val
  })
}

function toggleAll(val) {
  items.value.forEach((i) => {
    i._checked = !!val
  })
}

async function loadCart() {
  loading.value = true
  try {
    const data = await request.get('/user/cart', { silent: true })
    items.value = unwrapList(data).map((i) => ({
      ...i,
      _checked: true,
      quantity: Number(i.quantity || 1)
    }))
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function updateQty(item, val) {
  try {
    await request.put(`/user/cart/${item.id}`, { quantity: val })
  } catch {
    await loadCart()
  }
}

async function removeOne(item) {
  await ElMessageBox.confirm('确定删除该商品？', '提示', { type: 'warning' })
  await request.delete(`/user/cart/${item.id}`)
  ElMessage.success('已删除')
  await loadCart()
}

async function removeSelected() {
  await ElMessageBox.confirm(`删除选中的 ${selectedIds.value.length} 件商品？`, '提示', { type: 'warning' })
  await Promise.all(selectedIds.value.map((id) => request.delete(`/user/cart/${id}`)))
  ElMessage.success('已删除')
  await loadCart()
}

async function checkout() {
  if (!selectedIds.value.length) {
    ElMessage.warning('请先选择商品')
    return
  }
  const data = await request.get('/user/addresses', { silent: true }).catch(() => [])
  addresses.value = unwrapList(data)
  const def = addresses.value.find((a) => a.isDefault || a.defaultAddress) || addresses.value[0]
  addressId.value = def?.id || null
  addrVisible.value = true
}

async function confirmOrder() {
  if (!addressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }
  checkingOut.value = true
  try {
    await request.post('/user/orders', {
      addressId: addressId.value,
      cartItemIds: selectedIds.value
    })
    ElMessage.success('下单成功')
    addrVisible.value = false
    router.push('/user/orders')
  } finally {
    checkingOut.value = false
  }
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page {
  --accent: #e63935;
  --ink: #1a1a1a;
  --muted: #8b929c;
  max-width: 1180px;
  margin: 0 auto;
  padding: 0 0 72px;
}

.page-head {
  padding: 20px 24px 0;
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
  color: var(--ink);
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

.head-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-shrink: 0;
  padding: 10px 12px 10px 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, #fbfcfd, #f6f7f9);
  border: 1px solid rgba(15, 23, 42, 0.06);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.9),
    0 10px 28px rgba(15, 23, 42, 0.05);
}

.action-select {
  height: auto;
  margin: 0;
  align-items: center;
}

.action-select :deep(.el-checkbox__label) {
  padding-left: 10px;
}

.action-select-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  line-height: 1.2;
}

.action-select-text strong {
  font-size: 13px;
  font-weight: 720;
  color: var(--ink);
}

.action-select-text em {
  font-style: normal;
  font-size: 11px;
  color: var(--muted);
}

.action-divider {
  width: 1px;
  height: 28px;
  background: linear-gradient(180deg, transparent, rgba(15, 23, 42, 0.12), transparent);
  flex-shrink: 0;
}

.action-meta {
  display: inline-flex;
  align-items: center;
  gap: 10px;
}

.action-count {
  font-size: 12px;
  color: var(--muted);
  white-space: nowrap;
}

.action-count b {
  color: var(--accent);
  font-weight: 760;
}

.action-delete {
  height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(230, 57, 53, 0.16);
  border-radius: 999px;
  background: #fff;
  color: var(--accent);
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition:
    background 0.2s ease,
    border-color 0.2s ease,
    box-shadow 0.2s ease;
}

.action-delete:hover:not(:disabled) {
  background: #fff1f0;
  border-color: rgba(230, 57, 53, 0.28);
  box-shadow: 0 6px 16px rgba(230, 57, 53, 0.12);
}

.action-delete:disabled {
  opacity: 0.42;
  cursor: not-allowed;
  color: #b0b6bf;
  border-color: rgba(15, 23, 42, 0.08);
  background: #fff;
}

.cart-shell {
  max-width: 1180px;
  margin: 0 auto;
  padding: 18px 24px 0;
  min-height: 240px;
}

.shop-card {
  position: relative;
  margin-bottom: 20px;
  border-radius: 22px;
  background:
    linear-gradient(180deg, #ffffff 0%, #fcfcfd 100%);
  border: 1px solid rgba(15, 23, 42, 0.05);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.02),
    0 10px 28px rgba(15, 23, 42, 0.05),
    0 28px 56px rgba(230, 57, 53, 0.035);
  overflow: hidden;
}

.shop-card::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #e63935 0%, #ff8a75 55%, rgba(255, 138, 117, 0.2) 100%);
}

.shop-card::after {
  content: '';
  position: absolute;
  right: -40px;
  top: -48px;
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.07), transparent 68%);
  pointer-events: none;
}

.shop-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 18px 20px 12px 22px;
  background: linear-gradient(90deg, rgba(230, 57, 53, 0.04), transparent 42%);
  border-bottom: 1px solid rgba(15, 23, 42, 0.035);
}

.shop-name {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  color: inherit;
  text-decoration: none;
}

.shop-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(145deg, #1f2937, #4b5563);
  color: #fff;
  font-size: 15px;
  font-weight: 760;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  box-shadow:
    0 0 0 2px #fff,
    0 0 0 3px rgba(230, 57, 53, 0.18),
    0 6px 14px rgba(15, 23, 42, 0.12);
}

.shop-avatar.has-img {
  background: #f3f4f6;
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.shop-text {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.shop-text strong {
  font-size: 15.5px;
  font-weight: 760;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  letter-spacing: -0.01em;
}

.shop-name:hover .shop-text strong {
  color: var(--accent);
}

.shop-text em {
  font-style: normal;
  font-size: 12px;
  color: var(--muted);
  font-weight: 550;
}

.enter-shop {
  position: relative;
  z-index: 1;
  margin-left: auto;
  height: 34px;
  padding: 0 16px;
  border-radius: 999px;
  background: linear-gradient(180deg, #fff7f6, #ffe9e6);
  color: var(--accent);
  font-size: 12px;
  font-weight: 720;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  border: 1px solid rgba(230, 57, 53, 0.14);
  box-shadow: 0 4px 12px rgba(230, 57, 53, 0.08);
  transition: transform 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.enter-shop:hover {
  transform: translateY(-1px);
  background: linear-gradient(180deg, #fff, #ffe4e1);
  box-shadow: 0 8px 18px rgba(230, 57, 53, 0.12);
}

.cart-row {
  position: relative;
  z-index: 1;
  display: grid;
  grid-template-columns: 28px 88px minmax(0, 1fr) auto;
  gap: 14px 18px;
  align-items: center;
  margin: 12px 16px 16px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fb 100%);
  border: 1px solid rgba(15, 23, 42, 0.05);
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.02);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.cart-row:hover {
  transform: translateY(-1px);
  border-color: rgba(230, 57, 53, 0.16);
  box-shadow:
    0 8px 20px rgba(15, 23, 42, 0.04),
    0 14px 28px rgba(230, 57, 53, 0.06);
}

.cart-row:last-child {
  margin-bottom: 16px;
}

.row-check {
  display: flex;
  justify-content: center;
}

.cover {
  width: 88px;
  height: 88px;
  border-radius: 14px;
  overflow: hidden;
  background: #f3f4f6;
  box-shadow:
    inset 0 0 0 1px rgba(15, 23, 42, 0.04),
    0 8px 18px rgba(15, 23, 42, 0.08);
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.cart-row:hover .cover {
  transform: scale(1.03);
  box-shadow:
    inset 0 0 0 1px rgba(230, 57, 53, 0.08),
    0 10px 22px rgba(230, 57, 53, 0.12);
}

.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.info {
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding-right: 8px;
}

.name {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  color: var(--ink);
  text-decoration: none;
  font-weight: 720;
  font-size: 15px;
  line-height: 1.4;
  letter-spacing: -0.01em;
}

.name:hover {
  color: var(--accent);
}

.meta-line {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0;
}

.unit-price {
  color: var(--accent);
  font-size: 17px;
  font-weight: 800;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.03em;
}

.stock {
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 650;
  color: #6b7280;
  background: rgba(15, 23, 42, 0.05);
  border: 1px solid rgba(15, 23, 42, 0.04);
}

.row-ops {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-shrink: 0;
  padding: 8px 8px 8px 18px;
  margin-left: 4px;
  border-left: 1px dashed rgba(15, 23, 42, 0.08);
  background: linear-gradient(90deg, rgba(248, 250, 252, 0.5), transparent);
  border-radius: 0 12px 12px 0;
}

.qty-wrap {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  min-width: 124px;
}

.ops-label,
.sub-label {
  font-size: 11px;
  font-weight: 700;
  color: #9ca3af;
  line-height: 1;
  letter-spacing: 0.04em;
}

.qty-wrap :deep(.el-input-number) {
  width: 124px;
}

.qty-wrap :deep(.el-input-number .el-input__wrapper) {
  height: 34px;
  border-radius: 999px;
  padding-left: 2px;
  padding-right: 2px;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08) inset;
  background: #fff;
}

.qty-wrap :deep(.el-input-number__decrease),
.qty-wrap :deep(.el-input-number__increase) {
  background: #f3f4f6;
  color: #4b5563;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  top: 3px;
}

.qty-wrap :deep(.el-input-number__decrease) {
  left: 3px;
}

.qty-wrap :deep(.el-input-number__increase) {
  right: 3px;
}

.qty-wrap :deep(.el-input-number__decrease:hover),
.qty-wrap :deep(.el-input-number__increase:hover) {
  color: var(--accent);
  background: #fff1f0;
}

.qty-wrap :deep(.el-input-number .el-input__inner) {
  font-weight: 720;
  color: var(--ink);
}

.subtotal {
  min-width: 96px;
  text-align: right;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.subtotal strong {
  color: var(--accent);
  font-size: 18px;
  font-weight: 820;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.03em;
  line-height: 1.1;
}

.row-del {
  height: 34px;
  padding: 0 14px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 999px;
  background: #fff;
  color: #6b7280;
  font-size: 12px;
  font-weight: 680;
  cursor: pointer;
  transition: color 0.15s ease, border-color 0.15s ease, background 0.15s ease, box-shadow 0.15s ease;
}

.row-del:hover {
  color: var(--accent);
  border-color: rgba(230, 57, 53, 0.28);
  background: #fff5f5;
  box-shadow: 0 4px 12px rgba(230, 57, 53, 0.1);
}

.checkout-bar {
  position: sticky;
  bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  margin-top: 8px;
  padding: 16px 22px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 12px 32px rgba(15, 23, 42, 0.1),
    0 1px 2px rgba(15, 23, 42, 0.04);
  backdrop-filter: blur(10px);
}

.sum {
  display: flex;
  align-items: baseline;
  flex-wrap: wrap;
  gap: 14px;
  color: #6b7280;
  font-size: 14px;
}

.sum b {
  color: var(--ink);
}

.sum-total {
  display: inline-flex;
  align-items: baseline;
  gap: 8px;
}

.sum-total em {
  font-style: normal;
  color: var(--accent);
  font-size: 28px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.checkout-btn {
  height: 48px;
  min-width: 148px;
  padding: 0 28px;
  border: none;
  border-radius: 999px;
  color: #fff;
  font-size: 15px;
  font-weight: 740;
  cursor: pointer;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  box-shadow: 0 10px 24px rgba(230, 57, 53, 0.3);
}

.checkout-btn:hover:not(:disabled) {
  transform: translateY(-1px);
}

.checkout-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.empty-wrap {
  padding: 56px 20px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.05);
}

.go-shop {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  color: #fff;
  text-decoration: none;
  font-weight: 700;
}

.addr-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.addr-radio {
  height: auto;
  margin: 0;
  padding: 12px;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  width: 100%;
}

.addr-radio p {
  margin: 4px 0 0;
  color: #6b7280;
  font-size: 13px;
  white-space: normal;
}

@media (max-width: 900px) {
  .head-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .head-actions {
    justify-content: space-between;
    border-radius: 16px;
  }

  .cart-row {
    grid-template-columns: 24px 68px minmax(0, 1fr);
    gap: 10px 12px;
    padding: 12px;
  }

  .cover {
    width: 68px;
    height: 68px;
    border-radius: 10px;
  }

  .row-ops {
    grid-column: 1 / -1;
    width: 100%;
    justify-content: space-between;
    padding: 10px 0 0;
    margin-top: 2px;
    border-left: none;
    border-top: 1px solid rgba(26, 26, 26, 0.06);
  }

  .qty-wrap {
    min-width: 0;
  }

  .subtotal {
    min-width: 0;
    text-align: right;
  }
}
</style>
