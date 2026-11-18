<template>
  <div class="page" v-loading="loading">
    <header class="balance-card">
      <div class="balance-glow" aria-hidden="true" />
      <div class="balance-inner">
        <div class="balance-copy">
          <span class="label">账户余额（元）</span>
          <strong class="amount">{{ formatPrice(balance) }}</strong>
          <p>可用于商城下单支付，充值后即时到账</p>
        </div>
        <div class="balance-tags">
          <span>即时到账</span>
          <span>安全支付</span>
        </div>
      </div>
    </header>

    <section class="panel">
      <header class="panel-head">
        <div>
          <h2>选择充值金额</h2>
          <p>点选常用面额，或自定义金额后确认</p>
        </div>
      </header>

      <div class="amount-grid">
        <button
          v-for="n in presets"
          :key="n"
          type="button"
          class="amount-item"
          :class="{ active: Number(amount) === n }"
          @click="amount = n"
        >
          <span v-if="n === 100 || n === 200" class="hot-tag">热门</span>
          <span class="price">
            <em>¥</em>{{ n }}
          </span>
        </button>
      </div>

      <div class="custom-box" :class="{ focus: customFocus }">
        <div class="custom-copy">
          <strong>自定义金额</strong>
          <span>支持 1 ~ 100,000 元</span>
        </div>
        <div class="money-field">
          <em>¥</em>
          <el-input-number
            v-model="amount"
            :min="1"
            :max="100000"
            :step="10"
            :controls="false"
            placeholder="输入金额"
            @focus="customFocus = true"
            @blur="customFocus = false"
          />
        </div>
      </div>

      <button type="button" class="cta" :disabled="recharging" @click="handleRecharge">
        <span>{{ recharging ? '充值中…' : `确认充值 ${formatPrice(amount)}` }}</span>
      </button>
    </section>

    <section class="panel">
      <header class="panel-head">
        <div>
          <h2>充值记录</h2>
          <p v-if="records.length">最近 {{ records.length }} 条流水</p>
          <p v-else>充值成功后将显示在这里</p>
        </div>
      </header>

      <div v-loading="recordLoading" class="ledger">
        <article v-for="r in records" :key="r.id" class="ledger-row">
          <div class="dot" aria-hidden="true">+</div>
          <div class="ledger-main">
            <strong>+{{ formatPrice(r.amount) }}</strong>
            <p>
              <span>{{ r.remark || '余额充值' }}</span>
              <span class="sep">·</span>
              <span>余额 {{ formatPrice(r.balanceAfter) }}</span>
            </p>
          </div>
          <time>{{ formatTime(r.createdAt) }}</time>
        </article>
        <div v-if="!recordLoading && !records.length" class="empty">
          <div class="empty-ico" aria-hidden="true">¥</div>
          <p>暂无充值记录</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { formatPrice, formatTime } from '@/utils/format'

const userStore = useUserStore()
const loading = ref(false)
const recharging = ref(false)
const recordLoading = ref(false)
const balance = ref(0)
const amount = ref(100)
const records = ref([])
const customFocus = ref(false)
const presets = [50, 100, 200, 500, 1000, 2000]

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

async function load() {
  loading.value = true
  recordLoading.value = true
  try {
    const user = await userStore.fetchUser()
    balance.value = user?.balance ?? 0
    const data = await request.get('/user/recharge/records', {
      params: { page: 1, size: 20 },
      silent: true
    })
    records.value = unwrapList(data)
  } catch {
    records.value = []
  } finally {
    loading.value = false
    recordLoading.value = false
  }
}

async function handleRecharge() {
  if (!amount.value || amount.value <= 0) {
    ElMessage.warning('请输入充值金额')
    return
  }
  recharging.value = true
  try {
    const profile = await request.post('/user/recharge', { amount: amount.value })
    balance.value = profile?.balance ?? balance.value
    if (userStore.user) userStore.user.balance = balance.value
    ElMessage.success('充值成功')
    const data = await request.get('/user/recharge/records', {
      params: { page: 1, size: 20 },
      silent: true
    })
    records.value = unwrapList(data)
  } finally {
    recharging.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  --accent: #e1251b;
  --ink: #1a1a1a;
  --muted: #8b929c;
  --line: rgba(26, 26, 26, 0.06);
  display: grid;
  gap: 16px;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.balance-card {
  position: relative;
  overflow: hidden;
  border-radius: 18px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 12px 28px rgba(26, 26, 26, 0.04);
}

.balance-glow {
  pointer-events: none;
  position: absolute;
  width: 260px;
  height: 160px;
  right: -40px;
  top: -50px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(225, 37, 27, 0.12), transparent 70%);
}

.balance-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  padding: 26px 28px;
}

.balance-copy .label {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--muted);
  letter-spacing: 0.02em;
}

.balance-copy .amount {
  display: block;
  font-size: clamp(32px, 3.6vw, 40px);
  font-weight: 780;
  letter-spacing: -0.03em;
  line-height: 1;
  color: var(--accent);
  font-variant-numeric: tabular-nums;
}

.balance-copy p {
  margin: 12px 0 0;
  font-size: 12.5px;
  color: #a8b0bb;
}

.balance-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: flex-end;
}

.balance-tags span {
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: #fff5f4;
  border: 1px solid rgba(225, 37, 27, 0.12);
  color: #c81e16;
  font-size: 12px;
  font-weight: 650;
  display: inline-flex;
  align-items: center;
}

.panel {
  padding: 24px 26px 26px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid var(--line);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 10px 24px rgba(26, 26, 26, 0.035);
}

.panel-head {
  margin-bottom: 18px;
  padding-left: 12px;
  border-left: 3px solid var(--accent);
}

.panel-head h2 {
  margin: 0 0 6px;
  font-size: 16px;
  font-weight: 740;
  letter-spacing: 0.01em;
}

.panel-head p {
  margin: 0;
  font-size: 12.5px;
  color: var(--muted);
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 18px;
}

.amount-item {
  position: relative;
  height: 72px;
  border: 1.5px solid #ebecef;
  border-radius: 12px;
  background: #fff;
  color: var(--ink);
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  font-family: inherit;
  transition:
    transform 0.18s ease,
    border-color 0.18s ease,
    box-shadow 0.18s ease,
    background 0.18s ease,
    color 0.18s ease;
}

.amount-item .price {
  display: inline-flex;
  align-items: baseline;
  gap: 1px;
  font-size: 24px;
  font-weight: 780;
  letter-spacing: -0.03em;
  font-variant-numeric: tabular-nums;
  line-height: 1;
  color: #1f2937;
}

.amount-item .price em {
  font-style: normal;
  font-size: 14px;
  font-weight: 700;
  color: #9ca3af;
  margin-right: 1px;
}

.amount-item .hot-tag {
  position: absolute;
  left: 0;
  top: 0;
  height: 18px;
  padding: 0 8px 0 6px;
  border-radius: 0 0 10px 0;
  background: linear-gradient(135deg, #ff8a65, #e1251b);
  color: #fff;
  font-size: 10px;
  font-weight: 740;
  letter-spacing: 0.04em;
  display: inline-flex;
  align-items: center;
  line-height: 1;
}

.amount-item:hover {
  border-color: rgba(225, 37, 27, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 10px 20px rgba(225, 37, 27, 0.08);
}

.amount-item.active {
  border-color: #e1251b;
  background: linear-gradient(165deg, #fff8f7 0%, #ffffff 55%);
  box-shadow:
    0 0 0 1px rgba(225, 37, 27, 0.12),
    0 12px 24px rgba(225, 37, 27, 0.12);
}

.amount-item.active .price,
.amount-item.active .price em {
  color: #e1251b;
}

.amount-item.active::after {
  content: '';
  position: absolute;
  right: 0;
  bottom: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 0 22px 22px;
  border-color: transparent transparent #e1251b transparent;
}

.amount-item.active::before {
  content: '✓';
  position: absolute;
  right: 3px;
  bottom: 1px;
  z-index: 1;
  color: #fff;
  font-size: 10px;
  font-weight: 800;
  line-height: 1;
}

.custom-box {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  margin-bottom: 18px;
  border-radius: 14px;
  background: #f7f8fa;
  border: 1px solid transparent;
  transition: border-color 0.18s ease, background 0.18s ease, box-shadow 0.18s ease;
}

.custom-box.focus {
  background: #fff;
  border-color: rgba(225, 37, 27, 0.28);
  box-shadow: 0 0 0 3px rgba(225, 37, 27, 0.06);
}

.custom-copy strong {
  display: block;
  margin-bottom: 4px;
  font-size: 13.5px;
  font-weight: 720;
}

.custom-copy span {
  font-size: 12px;
  color: var(--muted);
}

.money-field {
  display: flex;
  align-items: center;
  gap: 4px;
  min-width: 140px;
  height: 42px;
  padding: 0 14px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.08);
}

.custom-box.focus .money-field {
  border-color: rgba(225, 37, 27, 0.35);
}

.money-field em {
  font-style: normal;
  color: var(--accent);
  font-size: 16px;
  font-weight: 760;
}

.money-field :deep(.el-input-number) {
  width: 100px;
}

.money-field :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
  padding: 0;
}

.money-field :deep(.el-input__inner) {
  text-align: right;
  font-size: 20px;
  font-weight: 760;
  color: var(--ink);
  font-variant-numeric: tabular-nums;
}

.cta {
  width: 100%;
  height: 48px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ef4a46, #e1251b 55%, #c9181a);
  color: #fff;
  font-size: 15px;
  font-weight: 720;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(225, 37, 27, 0.28);
  transition: filter 0.15s ease, transform 0.15s ease, box-shadow 0.15s ease;
}

.cta:hover:not(:disabled) {
  filter: brightness(1.03);
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(225, 37, 27, 0.32);
}

.cta:disabled {
  opacity: 0.65;
  cursor: wait;
  box-shadow: none;
}

.ledger {
  min-height: 88px;
}

.ledger-row {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 14px 12px;
  margin: 0 -4px;
  border-radius: 14px;
  transition: background 0.15s ease;
}

.ledger-row:hover {
  background: #fafbfc;
}

.ledger-row + .ledger-row {
  margin-top: 2px;
}

.dot {
  width: 36px;
  height: 36px;
  border-radius: 12px;
  background: #fff5f4;
  color: var(--accent);
  font-size: 18px;
  font-weight: 760;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.ledger-main strong {
  display: block;
  margin-bottom: 4px;
  font-size: 16px;
  font-weight: 760;
  color: var(--accent);
  letter-spacing: -0.02em;
  font-variant-numeric: tabular-nums;
}

.ledger-main p {
  margin: 0;
  font-size: 12.5px;
  color: var(--muted);
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.ledger-main .sep {
  opacity: 0.5;
}

.ledger-row time {
  flex-shrink: 0;
  font-size: 12px;
  color: #b0b7c3;
  font-variant-numeric: tabular-nums;
}

.empty {
  padding: 40px 0 20px;
  text-align: center;
  color: #b0b7c3;
}

.empty-ico {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  border-radius: 14px;
  background: #f7f8fa;
  color: #c4c9d1;
  font-size: 20px;
  font-weight: 760;
  display: flex;
  align-items: center;
  justify-content: center;
}

.empty p {
  margin: 0;
  font-size: 13px;
}

@media (max-width: 720px) {
  .balance-inner {
    flex-direction: column;
    align-items: flex-start;
    padding: 22px 20px;
  }

  .balance-tags {
    justify-content: flex-start;
  }

  .panel {
    padding: 20px 18px 22px;
  }

  .amount-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .custom-box {
    flex-direction: column;
    align-items: stretch;
  }

  .money-field {
    width: 100%;
    justify-content: flex-end;
  }

  .ledger-row {
    grid-template-columns: 32px minmax(0, 1fr);
    grid-template-rows: auto auto;
  }

  .ledger-row time {
    grid-column: 2;
    margin-top: -4px;
  }
}
</style>
