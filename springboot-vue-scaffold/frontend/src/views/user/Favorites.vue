<template>
  <div class="page">
    <div class="content" v-loading="loading">
      <div v-if="list.length" class="product-grid">
        <ProductCard
          v-for="(item, idx) in list"
          :key="item.id"
          :product="item.product || item"
          show-unfavorite
          class="reveal"
          :style="{ '--delay': `${Math.min(idx, 8) * 35}ms` }"
          @unfavorite="remove(item)"
        />
      </div>
      <EmptyState
        v-else-if="!loading"
        type="favorite"
        title="还没有收藏"
        description="逛逛商城，把心动好物收藏起来，方便随时回看"
      >
        <router-link to="/user/category" class="empty-cta">去逛逛</router-link>
      </EmptyState>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const list = ref([])

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/user/favorites', { silent: true })
    list.value = unwrapList(data)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function remove(item) {
  const productId = item.productId || item.product?.id || item.id
  await request.delete(`/user/favorites/${productId}`)
  ElMessage.success('已取消收藏')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page {
  --accent: #e1251b;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
}

.content { min-height: 280px; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.reveal {
  animation: rise 0.45s ease both;
  animation-delay: var(--delay, 0ms);
}
@keyframes rise {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

.empty-cta {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 16px;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  text-decoration: none;
  font-size: 13px;
}

@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(3, minmax(0, 1fr)); }
}
@media (max-width: 720px) {
  .product-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
}
</style>
