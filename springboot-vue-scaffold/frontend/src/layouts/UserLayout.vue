<template>
  <div class="user-shell" :class="{ 'is-fill-page': route.meta.fillHeight }">
    <header class="mall-header" :class="{ scrolled: scrolled }">
      <div class="topbar">
        <div class="topbar-inner">
          <p class="topbar-hello">
            欢迎来到<span>{{ systemStore.displayName }}</span>
            <template v-if="isLoggedIn">，{{ displayName }}</template>
          </p>
          <div class="topbar-links">
            <router-link to="/user/orders">我的订单</router-link>
            <router-link to="/user/shops">店铺街</router-link>
            <router-link to="/user/announcements">商城公告</router-link>
            <router-link v-if="!isLoggedIn" to="/login">请登录</router-link>
            <router-link v-if="!isLoggedIn" to="/register" class="topbar-reg">免费注册</router-link>
          </div>
        </div>
      </div>

      <div class="nav-main">
        <div class="nav-main-inner">
          <router-link to="/user/home" class="brand">
            <BrandLogo size="md" class="nav-brand-logo" />
            <span class="brand-text">
              <span class="brand-name">{{ systemStore.displayName }}</span>
              <span class="brand-tagline">品质好物 · 智能导购</span>
            </span>
          </router-link>

          <form class="search-box" @submit.prevent="goSearch">
            <el-icon class="search-ico" :size="16"><Search /></el-icon>
            <input
              v-model="keyword"
              type="text"
              class="search-input"
              placeholder="搜索商品、品牌或关键词"
              maxlength="60"
              autocomplete="off"
            />
            <button
              v-if="keyword"
              type="button"
              class="search-clear"
              aria-label="清空"
              @click="keyword = ''"
            >
              <el-icon :size="14"><CircleClose /></el-icon>
            </button>
            <button type="submit" class="search-btn">搜索</button>
          </form>

          <div v-if="isLoggedIn" class="nav-tools">
            <el-dropdown
              trigger="click"
              placement="bottom-end"
              popper-class="user-account-popper"
              @command="onCommand"
            >
              <button type="button" class="user-chip" aria-label="账户菜单">
                <span class="avatar-ring">
                  <el-avatar :size="34" :src="avatarSrc">{{ avatarFallback }}</el-avatar>
                </span>
                <span class="user-meta">
                  <span class="user-name">{{ displayName }}</span>
                  <span class="user-role">会员中心</span>
                </span>
                <el-icon class="chev" :size="12"><ArrowDown /></el-icon>
              </button>
              <template #dropdown>
                <el-dropdown-menu class="user-account-menu">
                  <el-dropdown-item command="profile" class="item-profile">
                    <span class="item-ico" aria-hidden="true">
                      <el-icon :size="15"><User /></el-icon>
                    </span>
                    <span class="item-txt">
                      <strong>个人中心</strong>
                      <em>资料 · 订单 · 资产</em>
                    </span>
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout" class="item-logout">
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </header>

    <main class="user-main" :class="{ 'is-fill': route.meta.fillHeight }">
      <router-view />
    </main>

    <footer v-if="!route.meta.hideFooter" class="user-footer">
      <div class="footer-inner">
        <div class="footer-brand">
          <BrandLogo size="sm" />
          <div>
            <strong>{{ systemStore.displayName }}</strong>
            <p>智能电商商城 · 精选好物与 AI 导购</p>
          </div>
        </div>
        <div class="footer-cols">
          <div>
            <h4>购物指南</h4>
            <router-link to="/user/category">全部商品</router-link>
            <router-link to="/user/shops">店铺街</router-link>
            <router-link to="/user/cart">购物车</router-link>
          </div>
          <div>
            <h4>服务支持</h4>
            <router-link to="/user/announcements">商城公告</router-link>
            <router-link to="/user/orders">我的订单</router-link>
            <router-link to="/user/ai-guide">AI 导购</router-link>
          </div>
          <div>
            <h4>会员中心</h4>
            <router-link to="/user/profile">个人中心</router-link>
            <router-link to="/login">登录账号</router-link>
            <router-link to="/register">注册会员</router-link>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span class="footer-tip">精选好物 · AI 智能导购 · 安心下单</span>
        <p class="footer-copy">© {{ year }} {{ systemStore.displayName }}</p>
      </div>
    </footer>

    <nav class="side-rail" aria-label="快捷导航">
      <router-link
        v-for="item in sideItems"
        :key="item.path"
        :to="item.path"
        class="side-item"
        :class="{ 'is-hot': item.hot }"
      >
        <span class="side-ico">
          <el-icon :size="20"><component :is="item.icon" /></el-icon>
          <em v-if="item.badge && cartCount > 0">{{ cartCount > 99 ? '99+' : cartCount }}</em>
        </span>
        <span class="side-txt">{{ item.title }}</span>
        <i v-if="item.hot" class="side-hot">HOT</i>
      </router-link>
    </nav>

    <AiAssistantPanel />
    <button
      type="button"
      class="ai-float"
      :class="{ open: aiPanelVisible }"
      aria-label="AI客服"
      @click="toggleAiPanel"
    >
      <span v-if="!aiPanelVisible" class="ai-float-pulse" />
      <el-icon :size="22"><ChatDotRound /></el-icon>
      <span class="ai-float-label">{{ aiPanelVisible ? '收起' : 'AI客服' }}</span>
    </button>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ArrowDown,
  Bell,
  ChatDotRound,
  CircleClose,
  Cpu,
  Goods,
  HomeFilled,
  Search,
  ShoppingCart,
  Shop,
  User
} from '@element-plus/icons-vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import { mediaUrl } from '@/utils/format'
import BrandLogo from '@/components/BrandLogo.vue'
import AiAssistantPanel from '@/components/AiAssistantPanel.vue'
import { useAiAssistantPanel } from '@/composables/useAiAssistantPanel'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const systemStore = useSystemStore()
const scrolled = ref(false)
const keyword = ref('')
const cartCount = ref(0)
const year = new Date().getFullYear()
const { visible: aiPanelVisible, toggle: toggleAiPanel } = useAiAssistantPanel()

const sideItems = [
  { path: '/user/home', title: '首页', icon: HomeFilled },
  { path: '/user/category', title: '分类', icon: Goods },
  { path: '/user/shops', title: '店铺', icon: Shop },
  { path: '/user/ai-guide', title: 'AI导购', icon: Cpu, hot: true },
  { path: '/user/announcements', title: '公告', icon: Bell },
  { path: '/user/cart', title: '购物车', icon: ShoppingCart, badge: true },
  { path: '/user/profile', title: '我的', icon: User }
]

const displayName = computed(() => userStore.user?.nickname || userStore.user?.username || '用户')
const avatarSrc = computed(() => mediaUrl(userStore.user?.avatar))
const avatarFallback = computed(() => displayName.value.slice(0, 1).toUpperCase())
const isLoggedIn = computed(() => !!userStore.token)

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || data?.items || []
}

async function loadCartCount() {
  if (!userStore.token) {
    cartCount.value = 0
    return
  }
  try {
    const data = await request.get('/user/cart', { silent: true })
    cartCount.value = unwrapList(data).reduce((n, i) => n + Number(i.quantity || 1), 0)
  } catch {
    cartCount.value = 0
  }
}

function goSearch() {
  const q = keyword.value.trim()
  const next = {
    path: '/user/category',
    query: q ? { keyword: q } : {}
  }
  // 已在分类页且参数相同会静默失败，强制带时间戳触发刷新
  const samePage =
    route.path === '/user/category' &&
    String(route.query.keyword || '') === q &&
    !route.query.categoryId
  if (samePage) {
    next.query = { ...(q ? { keyword: q } : {}), _: String(Date.now()) }
  }
  router.push(next).catch(() => {})
}

function onScroll() {
  scrolled.value = window.scrollY > 8
}

function onCommand(cmd) {
  if (cmd === 'profile') router.push('/user/profile')
  if (cmd === 'logout') {
    userStore.logout()
    cartCount.value = 0
    router.push('/user/home')
  }
}

function syncKeywordFromRoute() {
  if (route.path.startsWith('/user/category') && typeof route.query.keyword === 'string') {
    keyword.value = route.query.keyword
    return
  }
  // 离开分类搜索页时清空顶栏搜索框
  keyword.value = ''
}

watch(
  () => route.fullPath,
  () => {
    syncKeywordFromRoute()
    if (route.path.startsWith('/user/cart') || route.path.startsWith('/user/')) {
      loadCartCount()
    }
  }
)

watch(
  () => userStore.token,
  () => loadCartCount()
)

onMounted(async () => {
  window.addEventListener('scroll', onScroll, { passive: true })
  onScroll()
  syncKeywordFromRoute()
  if (userStore.token && !userStore.user) {
    try {
      await userStore.fetchUser()
    } catch {
      userStore.logout()
    }
  }
  await loadCartCount()
})

onUnmounted(() => {
  window.removeEventListener('scroll', onScroll)
})
</script>

<style scoped>
.user-shell {
  --ink: #1a1a1a;
  --muted: #8a8f98;
  --line: #eceef2;
  --accent: #e1251b;
  --accent-deep: #c9181a;
  --accent-soft: rgba(225, 37, 27, 0.08);
  --bg: #f5f5f5;
  min-height: 100vh;
  color: var(--ink);
  background: var(--bg);
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
}

.user-shell.is-fill-page {
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.user-shell.is-fill-page .mall-header {
  flex-shrink: 0;
}

.user-shell.is-fill-page .user-main.is-fill {
  flex: 1;
  height: auto;
  min-height: 0;
}

.mall-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: #fff;
  transition: box-shadow 0.25s ease;
}

.mall-header.scrolled {
  box-shadow: 0 6px 20px rgba(26, 26, 26, 0.06);
}

.topbar {
  background: #f7f8fa;
  border-bottom: 1px solid #eceef2;
  font-size: 12px;
  color: #8b929c;
}

.topbar-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 20px;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.topbar-hello {
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar-hello span {
  color: var(--accent);
  font-weight: 650;
  margin: 0 2px;
}

.topbar-links {
  display: flex;
  align-items: center;
  gap: 0;
  flex-shrink: 0;
}

.topbar-links a {
  position: relative;
  color: #6b7280;
  text-decoration: none;
  padding: 0 12px;
  transition: color 0.15s ease;
}

.topbar-links a + a::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  width: 1px;
  height: 10px;
  margin-top: -5px;
  background: #d8dce3;
}

.topbar-links a:first-child {
  padding-left: 0;
}

.topbar-links a:hover,
.topbar-links a.router-link-active {
  color: var(--accent);
}

.topbar-reg {
  color: var(--accent) !important;
  font-weight: 600;
}

.nav-main {
  background: #fff;
  padding-bottom: 4px;
}

.nav-main-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding: 18px 20px 16px;
  display: grid;
  grid-template-columns: auto minmax(280px, 1fr) auto;
  align-items: center;
  gap: 36px;
}

.brand {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  text-decoration: none;
  color: var(--ink);
  flex-shrink: 0;
}

.brand:hover .brand-name {
  color: var(--accent);
}

.nav-brand-logo {
  filter: drop-shadow(0 4px 12px rgba(225, 37, 27, 0.28));
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.brand-name {
  font-size: 22px;
  font-weight: 800;
  line-height: 1.15;
  letter-spacing: 0.02em;
  color: var(--accent);
  transition: color 0.15s ease;
}

.brand-tagline {
  font-size: 11px;
  color: var(--muted);
  letter-spacing: 0.04em;
  white-space: nowrap;
}

.search-box {
  display: flex;
  align-items: center;
  max-width: 640px;
  width: 100%;
  margin: 0 auto;
  height: 40px;
  border: 2px solid var(--accent);
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
  transition: box-shadow 0.2s ease;
}

.search-box:focus-within {
  box-shadow: 0 0 0 3px rgba(225, 37, 27, 0.12);
}

.search-ico {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding-left: 12px;
  color: #b0b6c0;
  flex-shrink: 0;
  line-height: 1;
}

.search-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  border: none;
  outline: none;
  padding: 0 8px;
  font-size: 14px;
  font-family: inherit;
  color: var(--ink);
  background: transparent;
  line-height: 1.4;
}

.search-input::placeholder {
  color: #b0b6c0;
}

.search-clear {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  margin-right: 4px;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #c0c4cc;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.15s ease, background 0.15s ease;
}

.search-clear:hover {
  color: #909399;
  background: #f5f5f5;
}

.search-btn {
  align-self: stretch;
  flex-shrink: 0;
  min-width: 84px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-size: 15px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: background 0.15s ease;
}

.search-btn:hover {
  background: var(--accent-deep);
}

.nav-tools {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.btn-login,
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 36px;
  padding: 0 16px;
  border-radius: 6px;
  text-decoration: none;
  font-size: 13px;
  font-weight: 650;
  transition: background 0.15s ease, border-color 0.15s ease, color 0.15s ease;
}

.btn-login {
  color: var(--ink);
  background: #fff;
  border: 1px solid #dde1e8;
}

.btn-login:hover {
  border-color: var(--accent);
  color: var(--accent);
}

.btn-primary {
  color: #fff;
  background: var(--accent);
  border: 1px solid var(--accent);
}

.btn-primary:hover {
  background: var(--accent-deep);
  border-color: var(--accent-deep);
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 42px;
  padding: 3px 10px 3px 3px;
  border: 1px solid rgba(26, 26, 26, 0.08);
  border-radius: 999px;
  background: #fff;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.user-chip:hover {
  border-color: rgba(225, 37, 27, 0.28);
  box-shadow: 0 4px 14px rgba(225, 37, 27, 0.08);
}

.avatar-ring {
  display: inline-flex;
  padding: 2px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ff6a4d, #e1251b 55%, #c9181a);
  flex-shrink: 0;
}

.avatar-ring :deep(.el-avatar) {
  background: #fff1f0;
  color: #e1251b;
  font-weight: 740;
  border: 2px solid #fff;
}

.user-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1px;
  min-width: 0;
  text-align: left;
}

.user-name {
  font-size: 13px;
  font-weight: 700;
  color: var(--ink);
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  font-weight: 600;
  color: var(--accent);
  opacity: 0.85;
}

.chev {
  color: #b0b6c0;
}

.side-rail {
  position: fixed;
  right: 0;
  top: 50%;
  z-index: 55;
  transform: translateY(-54%);
  display: flex;
  flex-direction: column;
  width: 58px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.06);
  border-right: none;
  border-radius: 12px 0 0 12px;
  box-shadow:
    -4px 0 16px rgba(26, 26, 26, 0.06),
    -12px 8px 28px rgba(26, 26, 26, 0.05);
}

.side-item {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  min-height: 62px;
  padding: 10px 4px 8px;
  text-decoration: none;
  color: #4b5563;
  background: #fff;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.15s ease, color 0.15s ease;
}

.side-item:last-child {
  border-bottom: none;
}

.side-item:hover {
  color: var(--accent);
  background: #fff8f7;
}

.side-item.router-link-active {
  color: #fff;
  background: #ff6b63;
}

.side-item.router-link-active .side-txt {
  color: #fff;
}

.side-ico {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
}

.side-ico em {
  position: absolute;
  top: -7px;
  right: -10px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: 10px;
  font-style: normal;
  font-weight: 700;
  line-height: 16px;
  text-align: center;
  box-shadow: 0 0 0 2px #fff;
}

.side-item.router-link-active .side-ico em {
  box-shadow: 0 0 0 2px #ff6b63;
}

.side-txt {
  font-size: 11px;
  font-weight: 650;
  line-height: 1.2;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.side-hot {
  position: absolute;
  top: 4px;
  right: 2px;
  font-style: normal;
  font-size: 8px;
  font-weight: 800;
  letter-spacing: 0.02em;
  line-height: 1;
  padding: 2px 3px;
  border-radius: 3px;
  color: var(--accent);
  background: #ffe566;
}

.side-item.router-link-active .side-hot {
  color: #fff;
  background: rgba(0, 0, 0, 0.2);
}

.user-main {
  min-height: calc(100vh - 140px);
}

.user-main.is-fill {
  height: calc(100vh - 148px);
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.user-main.is-fill > * {
  flex: 1;
  min-height: 0;
}

.user-footer {
  margin-top: 24px;
  padding: 36px 20px 0;
  background: #f7f8fa;
  border-top: 1px solid var(--line);
  color: var(--muted);
}

.footer-inner {
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: 28px;
  display: grid;
  grid-template-columns: 1.2fr 2fr;
  gap: 40px;
}

.footer-brand {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.footer-brand strong {
  display: block;
  color: var(--ink);
  font-size: 16px;
  margin-bottom: 4px;
}

.footer-brand p {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
}

.footer-cols {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.footer-cols h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: var(--ink);
  font-weight: 720;
}

.footer-cols a {
  display: block;
  margin-bottom: 8px;
  color: #8b929c;
  text-decoration: none;
  font-size: 13px;
}

.footer-cols a:hover {
  color: var(--accent);
}

.footer-bottom {
  max-width: 1280px;
  margin: 0 auto;
  padding: 14px 0 18px;
  border-top: 1px solid #e8eaee;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.footer-tip {
  font-size: 12px;
  color: #a0a7b2;
}

.footer-copy {
  margin: 0;
  font-size: 12px;
  color: #9aa1ab;
  white-space: nowrap;
}

.ai-float {
  position: fixed;
  right: 72px;
  bottom: 36px;
  z-index: 60;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 52px;
  padding: 0 18px 0 16px;
  border: none;
  border-radius: 999px;
  color: #fff;
  background: linear-gradient(135deg, #e63935 0%, #c9181a 100%);
  box-shadow: 0 14px 32px rgba(230, 57, 53, 0.38);
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  font-family: inherit;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.ai-float:hover {
  transform: translateY(-2px);
  box-shadow: 0 18px 36px rgba(230, 57, 53, 0.45);
}

.ai-float-pulse {
  position: absolute;
  inset: -4px;
  border-radius: 999px;
  border: 2px solid rgba(230, 57, 53, 0.35);
  animation: pulseRing 2s ease infinite;
  pointer-events: none;
}

@keyframes pulseRing {
  0% { transform: scale(0.96); opacity: 0.8; }
  70% { transform: scale(1.08); opacity: 0; }
  100% { opacity: 0; }
}

.ai-float-label {
  letter-spacing: 0.02em;
}

@media (max-width: 1100px) {
  .nav-main-inner {
    grid-template-columns: auto 1fr;
    gap: 14px 18px;
  }

  .nav-tools {
    grid-column: 1 / -1;
    justify-content: flex-end;
  }

  .brand-tagline {
    display: none;
  }

  .side-rail {
    width: 52px;
  }

  .side-item {
    min-height: 56px;
    padding: 8px 2px;
  }
}

@media (max-width: 900px) {
  .topbar-links a:nth-child(2),
  .topbar-links a:nth-child(3) {
    display: none;
  }

  .nav-main-inner {
    padding: 12px 16px;
  }

  .user-meta,
  .chev {
    display: none;
  }

  .user-chip {
    padding: 3px;
  }

  .search-btn {
    min-width: 68px;
  }

  .footer-inner {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .footer-bottom {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }

  .side-rail {
    top: auto;
    bottom: 88px;
    transform: none;
    width: 50px;
    max-height: calc(100vh - 160px);
    overflow-y: auto;
    scrollbar-width: none;
  }

  .side-rail::-webkit-scrollbar {
    display: none;
  }

  .side-item {
    min-height: 52px;
  }

  .side-hot {
    display: none;
  }

  .ai-float-label {
    display: none;
  }

  .ai-float {
    width: 52px;
    padding: 0;
    justify-content: center;
    right: 58px;
    bottom: 24px;
  }
}

@media (max-width: 640px) {
  .brand-name {
    font-size: 18px;
  }
}
</style>

<style>
.user-account-popper.el-popper {
  border: none !important;
  border-radius: 16px !important;
  padding: 0 !important;
  background: transparent !important;
  box-shadow: none !important;
}

.user-account-popper .el-popper__arrow::before {
  background: #fff !important;
  border: 1px solid rgba(26, 26, 26, 0.06) !important;
}

.user-account-menu.el-dropdown-menu {
  min-width: 196px;
  margin: 0 !important;
  padding: 8px !important;
  border: 1px solid rgba(26, 26, 26, 0.06) !important;
  border-radius: 16px !important;
  background: #fff !important;
  box-shadow:
    0 4px 12px rgba(26, 26, 26, 0.04),
    0 18px 40px rgba(26, 26, 26, 0.1) !important;
}

.user-account-menu .el-dropdown-menu__item {
  padding: 10px 12px !important;
  border-radius: 12px !important;
  line-height: 1.3 !important;
  color: #3b414a !important;
}

.user-account-menu .el-dropdown-menu__item:not(.is-disabled):hover,
.user-account-menu .el-dropdown-menu__item:not(.is-disabled):focus {
  background: #fff5f4 !important;
  color: #e1251b !important;
}

.user-account-menu .item-profile {
  display: flex !important;
  align-items: center;
  gap: 10px;
}

.user-account-menu .item-ico {
  width: 32px;
  height: 32px;
  border-radius: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(145deg, #fff1f0, #ffe4e1);
  color: #e1251b;
  flex-shrink: 0;
}

.user-account-menu .item-txt {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.user-account-menu .item-txt strong {
  font-size: 13.5px;
  font-weight: 720;
  color: #1a1a1a;
}

.user-account-menu .el-dropdown-menu__item:hover .item-txt strong {
  color: #e1251b;
}

.user-account-menu .item-txt em {
  font-style: normal;
  font-size: 11px;
  color: #9ca3af;
  font-weight: 560;
}

.user-account-menu .el-dropdown-menu__item--divided {
  margin-top: 6px !important;
  border-top: 1px solid rgba(26, 26, 26, 0.06) !important;
}

.user-account-menu .el-dropdown-menu__item--divided::before {
  display: none !important;
}

.user-account-menu .item-logout {
  justify-content: center !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  color: #8b929c !important;
}

.user-account-menu .item-logout:hover {
  color: #e1251b !important;
  background: #fff8f7 !important;
}
</style>
