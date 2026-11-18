<template>
  <div class="page" v-loading="loading">
    <div class="toolbar">
      <span v-if="list.length" class="count">共 {{ list.length }} 家店铺</span>
      <router-link class="link" to="/user/shops">逛店铺街 ›</router-link>
    </div>

    <div v-if="list.length" class="follow-list">
      <article v-for="f in list" :key="f.id" class="shop-card">
        <router-link :to="`/user/shop/${f.merchantId}`" class="shop-main">
          <span class="shop-mark" :class="{ 'has-img': !!shopAvatar(f) }">
            <img v-if="shopAvatar(f)" :src="shopAvatar(f)" alt="" />
            <template v-else>{{ shopInitial(f) }}</template>
          </span>
          <div class="shop-copy">
            <strong>{{ f.shop?.shopName || '店铺' }}</strong>
            <p>{{ f.shop?.description || f.shop?.intro || '精选好物，进店逛逛' }}</p>
          </div>
        </router-link>
        <div class="shop-ops">
          <router-link :to="`/user/shop/${f.merchantId}`" class="enter">进店</router-link>
          <button type="button" class="unfollow" :disabled="actingId === f.merchantId" @click="unfollow(f)">
            {{ actingId === f.merchantId ? '处理中…' : '取消关注' }}
          </button>
        </div>
      </article>
    </div>

    <EmptyState
      v-else-if="!loading"
      type="product"
      title="还没有关注店铺"
      description="去店铺街发现喜欢的店，关注后方便随时回访"
    >
      <router-link to="/user/shops" class="empty-cta">去逛店铺街</router-link>
    </EmptyState>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { mediaUrl } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const actingId = ref(null)
const list = ref([])

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function shopInitial(f) {
  return String(f.shop?.shopName || '店').slice(0, 1)
}

function shopAvatar(f) {
  return mediaUrl(f.shop?.avatar) || ''
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/user/follows', { silent: true })
    list.value = unwrapList(data)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function unfollow(f) {
  await ElMessageBox.confirm(`确定取消关注「${f.shop?.shopName || '该店铺'}」？`, '取消关注', {
    type: 'warning'
  })
  actingId.value = f.merchantId
  try {
    await request.delete(`/user/follows/${f.merchantId}`)
    ElMessage.success('已取消关注')
    await load()
  } finally {
    actingId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  --accent: #e1251b;
  --ink: #1a1a1a;
  --muted: #8b929c;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  padding: 12px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
}
.count { font-size: 13px; color: #999; }
.link {
  margin-left: auto;
  color: #999;
  text-decoration: none;
  font-size: 13px;
  font-weight: 650;
}
.link:hover { color: var(--accent); }

.follow-list {
  display: grid;
  gap: 12px;
}

.shop-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 8px 24px rgba(26, 26, 26, 0.03);
  transition: 0.2s ease;
}
.shop-card:hover {
  border-color: rgba(225, 37, 27, 0.18);
  box-shadow: 0 14px 32px rgba(225, 37, 27, 0.08);
  transform: translateY(-1px);
}

.shop-main {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
  flex: 1;
  text-decoration: none;
  color: inherit;
}
.shop-mark {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  font-weight: 760;
  overflow: hidden;
  background: linear-gradient(135deg, #ff6b63, #e1251b);
  box-shadow: 0 8px 18px rgba(225, 37, 27, 0.2);
}
.shop-mark.has-img {
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.06);
  box-shadow: 0 6px 14px rgba(26, 26, 26, 0.08);
}
.shop-mark img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.shop-copy { min-width: 0; }
.shop-copy strong {
  display: block;
  font-size: 16px;
  font-weight: 740;
  margin-bottom: 6px;
}
.shop-copy p {
  margin: 0;
  font-size: 13px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.shop-ops {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.enter, .unfollow {
  height: 36px;
  padding: 0 16px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 680;
  cursor: pointer;
}
.enter {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  background: linear-gradient(135deg, #e1251b, #ff5a45);
  color: #fff;
  border: none;
  box-shadow: 0 8px 18px rgba(225, 37, 27, 0.2);
}
.unfollow {
  border: 1px solid #e8e8e8;
  background: #fff;
  color: #666;
}
.unfollow:hover:not(:disabled) {
  border-color: #ffc9c5;
  color: var(--accent);
}
.unfollow:disabled { opacity: 0.65; cursor: wait; }

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

@media (max-width: 720px) {
  .shop-card {
    flex-direction: column;
    align-items: stretch;
  }
  .shop-ops { justify-content: flex-end; }
}
</style>
