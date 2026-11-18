<template>
  <el-container class="layout merchant-layout">
    <el-aside width="220px" class="aside">
      <div class="aside-brand">
        <BrandLogo size="sm" class="aside-brand-logo" />
        <div class="brand-copy">
          <span class="logo">{{ systemStore.displayName }}</span>
          <span class="logo-sub">商家中心</span>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="side-menu"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="content-wrap">
      <el-header class="top-bar" height="56px">
        <div class="top-left">
          <span class="page-title">{{ route.meta?.title || '商家中心' }}</span>
        </div>
        <div class="top-toolbar">
          <ThemeSwitcher />
          <span class="toolbar-divider" />
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="toolbar-item user-item">
              <el-avatar :size="28" :src="headerAvatarUrl" class="header-avatar">
                {{ avatarText }}
              </el-avatar>
              <span class="username">{{ displayName }}</span>
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">店铺资料</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowDown,
  ChatDotRound,
  Goods,
  HomeFilled,
  List,
  Shop,
  Star
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import BrandLogo from '@/components/BrandLogo.vue'
import ThemeSwitcher from '@/components/ThemeSwitcher.vue'
import { resolveImageUrl } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()

const menuItems = [
  { path: '/merchant/home', title: '数据概览', icon: HomeFilled },
  { path: '/merchant/products', title: '商品管理', icon: Goods },
  { path: '/merchant/orders', title: '订单管理', icon: List },
  { path: '/merchant/reviews', title: '评价管理', icon: Star },
  { path: '/merchant/qa', title: '问答管理', icon: ChatDotRound },
  { path: '/merchant/profile', title: '店铺资料', icon: Shop }
]

const activeMenu = computed(() => route.path || '/merchant/home')
const displayName = computed(
  () => userStore.user?.shopName || userStore.user?.nickname || userStore.user?.username || '商家'
)
const avatarText = computed(() => displayName.value.slice(0, 1).toUpperCase())
const headerAvatarUrl = computed(() => resolveImageUrl(userStore.user?.avatar))

function handleCommand(command) {
  if (command === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
    return
  }
  if (command === 'profile') {
    router.push('/merchant/profile')
  }
}

onMounted(() => {
  document.documentElement.classList.add('is-merchant')
  document.documentElement.classList.remove('is-portal', 'is-admin')
})

onUnmounted(() => {
  document.documentElement.classList.remove('is-merchant')
})
</script>

<style scoped>
.merchant-layout {
  height: 100vh;
  overflow: hidden;
  background: var(--app-page-bg, #f5f7fa);
}

.aside {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
  background: var(--app-shell-bg);
  border-right: 1px solid var(--app-shell-border);
}

.aside-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--app-shell-border);
  flex-shrink: 0;
}

.aside-brand-logo {
  width: 34px !important;
  height: 34px !important;
  border-radius: 10px;
  box-shadow: 0 6px 14px color-mix(in srgb, var(--app-primary, #3b82f6) 35%, transparent);
  flex-shrink: 0;
}

.brand-copy {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.logo {
  font-size: 14px;
  font-weight: 700;
  color: #f8fafc;
  letter-spacing: 0.4px;
  white-space: nowrap;
  line-height: 1.2;
}

.logo-sub {
  font-size: 10px;
  font-weight: 400;
  color: rgba(248, 250, 252, 0.52);
  letter-spacing: 0.05em;
}

.side-menu {
  flex: 1;
  min-height: 0;
  border-right: none;
  background: transparent;
  overflow-y: auto;
}

.side-menu :deep(.el-menu-item) {
  position: relative;
  color: var(--app-menu-text) !important;
}

.side-menu :deep(.el-menu-item .el-icon) {
  color: var(--app-menu-text) !important;
}

.side-menu :deep(.el-menu-item:hover) {
  background: var(--app-menu-hover-bg) !important;
  color: var(--app-menu-text) !important;
}

.side-menu :deep(.el-menu-item.is-active) {
  background: var(--app-menu-hover-bg) !important;
  color: var(--app-menu-active) !important;
}

.side-menu :deep(.el-menu-item.is-active .el-icon) {
  color: var(--app-menu-active) !important;
}

.side-menu :deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  bottom: 10px;
  width: 3px;
  border-radius: 0 2px 2px 0;
  background: var(--app-menu-active-bar);
}

.content-wrap {
  flex-direction: column;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
}

.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #ffffff;
  border-bottom: 1px solid #e8ecf0;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.top-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-toolbar :deep(.toolbar-item) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border-radius: 8px;
  color: #64748b;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
  outline: none;
}

.top-toolbar :deep(.toolbar-item:hover) {
  background: #f1f5f9;
  color: var(--app-primary);
}

.toolbar-divider {
  width: 1px;
  height: 18px;
  margin: 0 2px;
  background: #e2e8f0;
}

.user-item {
  padding-right: 10px;
}

.header-avatar {
  flex-shrink: 0;
  background: #f1f5f9;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  border: 1px solid #e2e8f0;
}

.username {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  color: #334155;
}

.arrow-icon {
  font-size: 12px;
  color: #94a3b8;
}

.main {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px;
  background: var(--app-page-bg, #f5f7fa);
}
</style>
