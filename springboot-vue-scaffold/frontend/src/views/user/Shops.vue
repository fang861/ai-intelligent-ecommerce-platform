<template>
  <div class="shops-page">
    <div class="page-head">
      <div class="head-shell">
        <div class="head-glow" aria-hidden="true" />
        <div class="head-inner">
          <div class="head-copy">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <em>店铺街</em>
            </nav>
            <h1>精选店铺</h1>
            <p class="head-desc">发现优质商家 · 共 <b>{{ total }}</b> 家在售店铺</p>
          </div>
          <div class="head-search">
            <div class="search-panel">
              <el-input
                v-model="keyword"
                placeholder="搜索店铺名称"
                clearable
                class="search-input"
                @keyup.enter="search"
                @clear="search"
              >
                <template #prefix>
                  <el-icon><Search /></el-icon>
                </template>
              </el-input>
              <button type="button" class="search-btn" @click="search">搜索</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="shop-grid" v-loading="loading">
      <router-link
        v-for="shop in shops"
        :key="shop.id"
        :to="`/user/shop/${shop.id}`"
        class="shop-card"
      >
        <div class="shop-avatar" :class="{ 'has-img': !!shopAvatar(shop) }">
          <img v-if="shopAvatar(shop)" :src="shopAvatar(shop)" :alt="shop.shopName || '店铺'" />
          <span v-else>{{ avatarText(shop) }}</span>
        </div>
        <div class="shop-body">
          <h3>{{ shop.shopName }}</h3>
          <p>{{ shop.shopDesc || '品质好物，值得信赖' }}</p>
          <div class="shop-meta">
            <span>{{ shop.productCount ?? 0 }} 件商品</span>
            <span>已售 {{ shop.salesCount ?? 0 }}</span>
          </div>
        </div>
        <span class="enter">进店</span>
      </router-link>

      <EmptyState
        v-if="!loading && !shops.length"
        type="shop"
        title="暂无店铺"
        description="稍后再来看看，优质商家正在入驻"
      />
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadShops"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { Search } from '@element-plus/icons-vue'
import request from '@/api/request'
import { mediaUrl } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const shops = ref([])
const total = ref(0)
const keyword = ref('')
const page = ref(1)
const size = 12

function avatarText(shop) {
  return String(shop.shopName || shop.nickname || '店').slice(0, 1)
}

function shopAvatar(shop) {
  return mediaUrl(shop?.avatar) || ''
}

async function loadShops() {
  loading.value = true
  try {
    const data = await request.get('/user/shops', {
      params: {
        keyword: keyword.value.trim() || undefined,
        page: page.value,
        size
      },
      silent: true
    })
    shops.value = data?.list || data?.records || (Array.isArray(data) ? data : [])
    total.value = Number(data?.total ?? shops.value.length)
  } catch {
    shops.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  loadShops()
}

onMounted(loadShops)
</script>

<style scoped>
.shops-page {
  --accent: #e63935;
  max-width: 1180px;
  margin: 0 auto;
  padding: 22px 24px 64px;
}

.page-head {
  margin-bottom: 24px;
}

.head-shell {
  position: relative;
  border-radius: 24px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 12px 36px rgba(15, 23, 42, 0.07);
  overflow: hidden;
}

.head-glow {
  position: absolute;
  right: -60px;
  top: -80px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.14), transparent 68%);
  pointer-events: none;
}

.head-inner {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 28px 30px;
}

.crumb {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #9ca3af;
}

.crumb a {
  color: inherit;
  text-decoration: none;
}

.crumb a:hover {
  color: var(--accent);
}

.crumb em {
  font-style: normal;
  color: #4b5563;
}

.head-copy h1 {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 760;
  color: #1a1a1a;
}

.head-desc {
  margin: 0;
  color: #8b929c;
  font-size: 14px;
}

.head-desc b {
  color: var(--accent);
  font-weight: 700;
}

.search-panel {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 8px 8px 14px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
  min-width: 360px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  box-shadow: none !important;
  background: transparent;
}

.search-btn {
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  box-shadow: 0 8px 18px rgba(230, 57, 53, 0.28);
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  min-height: 180px;
}

.shop-card {
  display: grid;
  grid-template-columns: 64px 1fr auto;
  gap: 16px;
  align-items: center;
  padding: 20px;
  border-radius: 18px;
  background: #fff;
  text-decoration: none;
  color: inherit;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 10px 28px rgba(15, 23, 42, 0.06);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.shop-card:hover {
  transform: translateY(-4px);
  box-shadow:
    0 4px 10px rgba(15, 23, 42, 0.04),
    0 18px 40px rgba(15, 23, 42, 0.1);
}

.shop-avatar {
  width: 64px;
  height: 64px;
  border-radius: 18px;
  background: linear-gradient(135deg, #ff6b63, #e1251b);
  color: #fff;
  font-size: 24px;
  font-weight: 760;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 8px 18px rgba(225, 37, 27, 0.22);
}

.shop-avatar.has-img {
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.06);
  box-shadow: 0 6px 16px rgba(26, 26, 26, 0.08);
}

.shop-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.shop-body h3 {
  margin: 0 0 6px;
  font-size: 18px;
  font-weight: 720;
  color: #1a1a1a;
}

.shop-body p {
  margin: 0;
  font-size: 13px;
  color: #8b929c;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.shop-meta {
  display: flex;
  gap: 14px;
  margin-top: 10px;
  font-size: 12px;
  color: #9ca3af;
}

.enter {
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  background: #fff1f0;
  color: var(--accent);
  font-size: 13px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.empty-wrap {
  grid-column: 1 / -1;
  padding: 48px 0;
}

.pager {
  display: flex;
  justify-content: center;
  margin-top: 28px;
}

@media (max-width: 900px) {
  .head-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .search-panel {
    min-width: 0;
    width: 100%;
  }

  .shop-grid {
    grid-template-columns: 1fr;
  }
}
</style>
