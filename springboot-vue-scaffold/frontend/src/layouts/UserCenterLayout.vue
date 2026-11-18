<template>
  <div class="center-layout">
    <header class="mall-hero">
      <div class="hero-shell">
        <div class="hero-glow" aria-hidden="true" />
        <div class="hero-inner">
          <div class="hero-copy">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <em>我的商城</em>
              <template v-if="sectionTitle">
                <span>/</span>
                <em class="curr">{{ sectionTitle }}</em>
              </template>
            </nav>
            <h1>我的商城</h1>
            <p class="hero-desc">
              {{ sectionDesc }}
              <template v-if="sectionTitle"> · 当前 <b>{{ sectionTitle }}</b></template>
            </p>
          </div>

          <div class="hero-side">
            <div class="quick-panel">
              <router-link to="/user/orders">订单</router-link>
              <router-link to="/user/wallet">钱包</router-link>
              <router-link to="/user/favorites">收藏</router-link>
              <router-link to="/user/account">账户</router-link>
            </div>
            <p class="quick-tip">常用功能一键直达</p>
          </div>
        </div>
      </div>
    </header>

    <div class="center-body">
      <aside class="center-side">
        <router-link
          to="/user/profile"
          class="side-home"
          :class="{ active: route.path === '/user/profile' }"
        >
          <el-icon :size="16"><User /></el-icon>
          <span>我的商城</span>
        </router-link>

        <nav class="side-nav">
          <section v-for="group in menuGroups" :key="group.key" class="nav-group">
            <button type="button" class="group-head" @click="toggle(group.key)">
              <span class="group-left">
                <el-icon :size="16"><component :is="group.icon" /></el-icon>
                <em>{{ group.title }}</em>
              </span>
              <el-icon class="chev" :class="{ open: isOpen(group.key) }" :size="12">
                <ArrowDown />
              </el-icon>
            </button>

            <div v-show="isOpen(group.key)" class="group-body">
              <router-link
                v-for="item in group.children"
                :key="item.to"
                :to="item.to"
                class="nav-link"
                :class="{ active: isActive(item.to) }"
              >
                {{ item.label }}
              </router-link>
            </div>
          </section>
        </nav>
      </aside>

      <div class="center-main">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  ArrowDown,
  CreditCard,
  Document,
  Location,
  Star,
  User
} from '@element-plus/icons-vue'

const route = useRoute()

const pageMeta = {
  '/user/profile': {
    title: '',
    desc: '查看订单、管理钱包与收货信息'
  },
  '/user/orders': {
    title: '我的订单',
    desc: '查看进度 · 支付 · 收货 · 售后与评价'
  },
  '/user/wallet': {
    title: '余额充值',
    desc: '充值即时到账，可用于商城下单支付'
  },
  '/user/history': {
    title: '我的足迹',
    desc: '最近浏览过的商品都会留在这里'
  },
  '/user/favorites': {
    title: '商品收藏',
    desc: '收藏的好物都在这里，方便随时回看'
  },
  '/user/follows': {
    title: '店铺关注',
    desc: '关注的店铺都在这里，进店更方便'
  },
  '/user/account': {
    title: '个人信息',
    desc: '管理你的会员资料与登录密码'
  },
  '/user/addresses': {
    title: '收货地址',
    desc: '管理常用收货信息，结算更快捷'
  }
}

const sectionTitle = computed(() => pageMeta[route.path]?.title || '')
const sectionDesc = computed(
  () => pageMeta[route.path]?.desc || '查看订单、管理钱包与收货信息'
)

const menuGroups = [
  {
    key: 'orders',
    title: '订单中心',
    icon: Document,
    children: [{ label: '我的订单', to: '/user/orders' }]
  },
  {
    key: 'wallet',
    title: '我的钱包',
    icon: CreditCard,
    children: [{ label: '余额充值', to: '/user/wallet' }]
  },
  {
    key: 'footprint',
    title: '足迹收藏',
    icon: Star,
    children: [
      { label: '我的足迹', to: '/user/history' },
      { label: '商品收藏', to: '/user/favorites' },
      { label: '店铺关注', to: '/user/follows' }
    ]
  },
  {
    key: 'account',
    title: '账户设置',
    icon: Location,
    children: [
      { label: '个人信息', to: '/user/account' },
      { label: '收货地址', to: '/user/addresses' }
    ]
  }
]

const openMap = reactive({
  orders: true,
  wallet: true,
  footprint: true,
  account: true
})

function isOpen(key) {
  return !!openMap[key]
}

function toggle(key) {
  openMap[key] = !openMap[key]
}

function isActive(to) {
  return route.path === to || route.path.startsWith(`${to}/`)
}

function ensureGroupOpen() {
  const path = route.path
  if (path.startsWith('/user/orders')) openMap.orders = true
  if (path.startsWith('/user/wallet')) openMap.wallet = true
  if (
    path.startsWith('/user/history') ||
    path.startsWith('/user/favorites') ||
    path.startsWith('/user/follows')
  ) {
    openMap.footprint = true
  }
  if (path.startsWith('/user/account') || path.startsWith('/user/addresses')) openMap.account = true
  if (path.startsWith('/user/ai-')) openMap.service = true
}

onMounted(ensureGroupOpen)
watch(() => route.path, ensureGroupOpen)
</script>

<style scoped>
.center-layout {
  --jd-red: #e1251b;
  --jd-pink: #fdf0f0;
  --ink: #1a1a1a;
  --muted: #666;
  --line: #f0f0f0;
  --accent: #e63935;
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px 20px 48px;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
}

.mall-hero { margin-bottom: 16px; }

.hero-shell {
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

.hero-glow {
  pointer-events: none;
  position: absolute;
  width: 280px;
  height: 180px;
  right: -40px;
  top: -60px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.14), transparent 70%);
}

.hero-inner {
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
  transition: color 0.2s ease;
}
.crumb a:hover { color: var(--accent); }
.crumb em {
  font-style: normal;
  color: var(--ink);
  font-weight: 600;
}
.crumb em.curr { color: var(--accent); }

.hero-copy h1 {
  margin: 0 0 10px;
  font-size: clamp(26px, 3vw, 34px);
  font-weight: 760;
  letter-spacing: -0.02em;
  line-height: 1.2;
  color: var(--ink);
}

.hero-desc {
  margin: 0;
  color: #8b929c;
  font-size: 13.5px;
}
.hero-desc b {
  color: var(--accent);
  font-weight: 700;
}

.hero-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
  flex-shrink: 0;
}

.quick-panel {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 5px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.06);
  box-shadow:
    0 2px 8px rgba(26, 26, 26, 0.04),
    0 10px 24px rgba(26, 26, 26, 0.06);
}

.quick-panel a {
  height: 40px;
  padding: 0 16px;
  border-radius: 999px;
  color: #4b5563;
  text-decoration: none;
  font-size: 13px;
  font-weight: 650;
  display: inline-flex;
  align-items: center;
  transition: 0.18s ease;
}
.quick-panel a:hover {
  background: #fff1f0;
  color: var(--accent);
}
.quick-panel a.router-link-active {
  background: linear-gradient(135deg, #e63935, #ff6a4d);
  color: #fff;
}

.quick-tip {
  margin: 0;
  font-size: 12px;
  color: #9ca3af;
}

.center-body {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.center-side {
  position: sticky;
  top: 84px;
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 14px;
  padding: 14px 0 16px;
  box-shadow: 0 8px 24px rgba(20, 23, 28, 0.04);
}

.side-home {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 12px 10px;
  height: 40px;
  padding: 0 12px;
  border-radius: 10px;
  background: var(--jd-pink);
  color: var(--jd-red);
  text-decoration: none;
  font-size: 14px;
  font-weight: 700;
}
.side-home.active,
.side-home:hover {
  background: #fce8e8;
}

.nav-group + .nav-group { margin-top: 2px; }

.group-head {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  height: 40px;
  padding: 0 16px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: #333;
}
.group-head:hover { background: #fafafa; }
.group-left {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.group-left em {
  font-style: normal;
  font-size: 14px;
  font-weight: 650;
}
.chev {
  color: #bbb;
  transition: transform 0.2s ease;
}
.chev.open { transform: rotate(180deg); }

.group-body { padding: 2px 0 8px; }

.nav-link {
  display: flex;
  align-items: center;
  height: 34px;
  padding: 0 16px 0 42px;
  color: var(--muted);
  text-decoration: none;
  font-size: 13px;
}
.nav-link:hover {
  color: var(--jd-red);
  background: #fafafa;
}
.nav-link.active {
  color: var(--jd-red);
  font-weight: 700;
  background: var(--jd-pink);
}

.center-main {
  min-width: 0;
  min-height: 520px;
}

@media (max-width: 900px) {
  .center-layout { padding: 12px 12px 40px; }
  .center-body { grid-template-columns: 1fr; }
  .center-side { position: static; }
  .side-nav {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }
  .hero-inner {
    flex-direction: column;
    align-items: stretch;
    padding: 22px 20px;
  }
  .hero-side {
    align-items: stretch;
  }
  .quick-panel {
    justify-content: space-between;
  }
  .quick-tip { text-align: left; }
}
</style>
