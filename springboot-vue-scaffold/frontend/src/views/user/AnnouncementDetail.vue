<template>
  <div class="ann-detail" v-loading="loading">
    <template v-if="item">
      <div class="page-head">
        <div class="head-shell">
          <div class="head-glow" aria-hidden="true" />
          <div class="head-inner">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <router-link to="/user/announcements">商城公告</router-link>
              <span>/</span>
              <em>详情</em>
            </nav>
            <h1>{{ item.title }}</h1>
            <p class="head-desc">
              发布时间 · <b>{{ formatTime(item.createdAt || item.publishTime) }}</b>
            </p>
          </div>
        </div>
      </div>

      <article class="content-card">
        <div class="content" v-html="html" />
      </article>
    </template>

    <div v-else-if="!loading" class="empty-wrap">
      <EmptyState
        type="notice"
        title="公告不存在"
        description="该公告可能已下架或链接有误"
      >
        <router-link to="/user/announcements" class="empty-cta">返回列表</router-link>
      </EmptyState>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import request from '@/api/request'
import { formatTime, toContentHtml } from '@/utils/format'
import EmptyState from '@/components/EmptyState.vue'

const route = useRoute()
const loading = ref(false)
const item = ref(null)
const html = computed(() => toContentHtml(item.value?.content))

onMounted(async () => {
  loading.value = true
  try {
    item.value = await request.get(`/user/announcements/${route.params.id}`, { silent: true })
  } catch {
    item.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.ann-detail {
  --ink: #1a1a1a;
  --muted: #6b7280;
  --accent: #e63935;
  max-width: 860px;
  margin: 0 auto;
  padding: 0 0 72px;
  min-height: 50vh;
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
  color: var(--ink);
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

.head-inner h1 {
  margin: 0 0 10px;
  font-size: clamp(24px, 3vw, 32px);
  font-weight: 760;
  letter-spacing: -0.02em;
  line-height: 1.3;
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

.content-card {
  margin: 20px 24px 0;
  padding: 28px 30px;
  border-radius: 20px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 12px 28px rgba(26, 26, 26, 0.04);
}

.content {
  color: #374151;
  line-height: 1.9;
  font-size: 15px;
}

.content :deep(h2),
.content :deep(h3) {
  margin: 1.4em 0 0.7em;
  color: #1a1a1a;
  font-weight: 740;
  line-height: 1.35;
}

.content :deep(h2) { font-size: 1.25em; }
.content :deep(h3) { font-size: 1.12em; }

.content :deep(p) {
  margin: 0 0 1em;
}

.content :deep(ul),
.content :deep(ol) {
  margin: 0 0 1em;
  padding-left: 1.35em;
}

.content :deep(li) {
  margin: 0.35em 0;
}

.content :deep(a) {
  color: var(--accent);
  text-decoration: none;
}

.content :deep(a:hover) {
  text-decoration: underline;
}

.content :deep(strong),
.content :deep(b) {
  color: #1f2937;
  font-weight: 720;
}

.content :deep(img) {
  max-width: 100%;
  border-radius: 12px;
  margin: 0.6em 0;
}

.content :deep(blockquote) {
  margin: 0 0 1em;
  padding: 10px 14px;
  border-left: 3px solid #e1251b;
  background: #fff8f7;
  color: #4b5563;
  border-radius: 0 8px 8px 0;
}

.empty-wrap {
  padding: 40px 24px;
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
  font-weight: 650;
}

@media (max-width: 640px) {
  .page-head,
  .content-card,
  .empty-wrap {
    padding-left: 16px;
    padding-right: 16px;
  }

  .content-card {
    margin-left: 16px;
    margin-right: 16px;
    padding: 20px;
  }

  .head-inner {
    padding: 22px 20px 20px;
  }
}
</style>
