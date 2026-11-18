<template>
  <div class="ann-page">
    <div class="page-head">
      <div class="head-shell">
        <div class="head-glow" aria-hidden="true" />
        <div class="head-inner">
          <div class="head-copy">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <em>商城公告</em>
            </nav>
            <h1>商城公告</h1>
            <p class="head-desc">
              活动、规则与重要通知
              <template v-if="list.length"> · 共 <b>{{ list.length }}</b> 条</template>
            </p>
          </div>
        </div>
      </div>
    </div>

    <div class="ann-shell" v-loading="loading">
      <router-link
        v-for="(a, idx) in list"
        :key="a.id"
        :to="`/user/announcements/${a.id}`"
        class="ann-card reveal"
        :style="{ '--delay': `${Math.min(idx, 8) * 40}ms` }"
      >
        <span class="ann-mark" aria-hidden="true" />
        <div class="ann-body">
          <div class="ann-top">
            <h3>{{ a.title }}</h3>
            <time class="time">{{ formatTime(a.createdAt || a.publishTime) }}</time>
          </div>
          <p>{{ excerpt(a) }}</p>
        </div>
        <span class="ann-arrow" aria-hidden="true">→</span>
      </router-link>

      <EmptyState
        v-if="!loading && !list.length"
        type="notice"
        title="暂无公告"
        description="活动与规则通知会在这里发布"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import request from '@/api/request'
import { formatTime, plainTextOf } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const list = ref([])

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

function excerpt(a) {
  return plainTextOf(a.content || a.summary || '').slice(0, 120) || '点击查看详情'
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/user/announcements', { params: { page: 1, size: 50 }, silent: true })
    list.value = unwrapList(data)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.ann-page {
  --ink: #1a1a1a;
  --muted: #6b7280;
  --accent: #e63935;
  --line: rgba(26, 26, 26, 0.08);
  max-width: 980px;
  margin: 0 auto;
  padding: 0 0 72px;
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
  color: var(--ink);
  background:
    radial-gradient(ellipse 55% 30% at 100% 0%, rgba(230, 57, 53, 0.05), transparent 50%),
    transparent;
  animation: pageIn 0.45s ease;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
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

.ann-shell {
  margin-top: 20px;
  padding: 0 24px;
  display: grid;
  gap: 14px;
  min-height: 200px;
}

.ann-card {
  position: relative;
  display: grid;
  grid-template-columns: 4px minmax(0, 1fr) auto;
  align-items: center;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  text-decoration: none;
  color: inherit;
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 10px 28px rgba(26, 26, 26, 0.04);
  transition:
    transform 0.22s ease,
    box-shadow 0.22s ease,
    border-color 0.22s ease;
}

.ann-card:hover {
  transform: translateY(-3px);
  border-color: rgba(230, 57, 53, 0.16);
  box-shadow:
    0 8px 20px rgba(26, 26, 26, 0.05),
    0 20px 40px rgba(230, 57, 53, 0.08);
}

.ann-card:hover .ann-arrow {
  color: var(--accent);
  transform: translateX(3px);
}

.ann-card:hover h3 {
  color: var(--accent);
}

.ann-mark {
  align-self: stretch;
  width: 4px;
  border-radius: 999px;
  background: linear-gradient(180deg, #e63935, #ff8a75);
  opacity: 0.85;
}

.ann-body {
  min-width: 0;
}

.ann-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 8px;
}

.ann-card h3 {
  margin: 0;
  font-size: 17px;
  font-weight: 740;
  letter-spacing: -0.01em;
  line-height: 1.35;
  transition: color 0.2s ease;
}

.ann-card p {
  margin: 0;
  color: var(--muted);
  font-size: 13.5px;
  line-height: 1.65;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.time {
  flex-shrink: 0;
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  color: #9ca3af;
  font-size: 12px;
  font-weight: 600;
  background: #f3f4f6;
  white-space: nowrap;
}

.ann-arrow {
  color: #cbd5e1;
  font-size: 18px;
  font-weight: 600;
  transition: color 0.2s ease, transform 0.2s ease;
}

.reveal {
  animation: riseIn 0.5s ease both;
  animation-delay: var(--delay, 0ms);
}

@keyframes riseIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@media (max-width: 640px) {
  .page-head,
  .ann-shell {
    padding-left: 16px;
    padding-right: 16px;
  }

  .head-inner {
    padding: 22px 20px 20px;
  }

  .ann-card {
    grid-template-columns: 3px minmax(0, 1fr);
    gap: 12px;
    padding: 16px 16px 16px 14px;
  }

  .ann-arrow {
    display: none;
  }

  .ann-top {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
