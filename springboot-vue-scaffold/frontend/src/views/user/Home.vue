<template>
  <div class="home-page">
    <section class="hero" v-loading="bannerLoading">
      <el-carousel
        v-if="banners.length"
        ref="carouselRef"
        class="hero-carousel"
        height="100%"
        :interval="5600"
        arrow="hover"
        indicator-position="none"
        @change="onSlideChange"
      >
        <el-carousel-item v-for="item in banners" :key="item.id">
          <div class="slide">
            <div class="slide-media" :style="{ backgroundImage: `url('${coverOf(item)}')` }" />
            <div class="slide-veil" />
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="hero-fallback">
        <div class="slide-media fallback-media" />
        <div class="slide-veil" />
      </div>

      <div class="hero-copy">
        <p class="brand-signal">{{ systemStore.displayName }}</p>
        <transition name="fade-up" mode="out-in">
          <div :key="activeBanner?.id || 'empty'" class="copy-block">
            <h1>{{ activeBanner?.title || '发现品质好物' }}</h1>
            <p class="subtitle">
              {{ activeBanner?.subtitle || 'AI 智能导购，为你精选好货。' }}
            </p>
            <div class="cta-row">
              <router-link class="cta-primary" to="/user/category">去逛逛</router-link>
              <router-link class="cta-ghost" to="/user/ai-guide">AI 导购</router-link>
            </div>
          </div>
        </transition>
        <div v-if="banners.length > 1" class="hero-dots">
          <button
            v-for="(item, index) in banners"
            :key="item.id"
            type="button"
            class="dot"
            :class="{ active: index === activeIndex }"
            :aria-label="`切换到第 ${index + 1} 张`"
            @click="goSlide(index)"
          />
        </div>
      </div>
    </section>

    <div class="mall-body">
      <section v-if="announcements.length" class="announce-strip">
        <span class="announce-badge">公告</span>
        <div class="announce-track">
          <router-link
            v-for="a in announcements"
            :key="a.id"
            :to="`/user/announcements/${a.id}`"
            class="announce-item"
          >
            {{ a.title }}
          </router-link>
        </div>
        <router-link to="/user/announcements" class="announce-more">
          更多
          <el-icon :size="12"><ArrowRight /></el-icon>
        </router-link>
      </section>

      <section class="section ai-section">
        <div class="section-head plain">
          <div>
            <p class="eyebrow">UserCF Recommend</p>
            <h2>猜你喜欢</h2>
          </div>
          <router-link to="/user/ai-guide" class="ai-link">聊聊 AI 导购</router-link>
        </div>
        <div class="product-grid" v-loading="recLoading">
          <ProductCard v-for="p in recommendProducts" :key="p.id" :product="p" badge="UserCF" />
          <EmptyState
            v-if="!recLoading && !recommendProducts.length"
            compact
            type="product"
            title="暂无推荐"
            description="多逛逛、收藏几件好物后，协同过滤会更懂你"
          >
            <router-link to="/user/category" class="empty-cta">去逛逛</router-link>
          </EmptyState>
        </div>
      </section>

      <section class="section section-hot">
        <div class="section-shell">
          <div class="section-head">
            <div class="section-title">
              <span class="title-mark" aria-hidden="true" />
              <div class="title-copy">
                <h2>热销好物</h2>
                <p>高人气单品，口碑之选</p>
              </div>
              <span class="title-tag">热卖榜</span>
            </div>
            <router-link to="/user/category" class="ai-link">
              查看更多 ›
            </router-link>
          </div>
          <div class="product-grid" v-loading="hotLoading">
            <ProductCard v-for="p in hotProducts" :key="p.id" :product="p" badge="热销" />
            <EmptyState
              v-if="!hotLoading && !hotProducts.length"
              compact
              type="product"
              title="暂无热销商品"
              description="稍后再来看看，好物正在上架中"
            />
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ArrowRight } from '@element-plus/icons-vue'
import request from '@/api/request'
import { useSystemStore } from '@/stores/system'
import { mediaUrl } from '@/utils/format'
import ProductCard from '@/components/ProductCard.vue'
import EmptyState from '@/components/EmptyState.vue'

const systemStore = useSystemStore()
const bannerLoading = ref(false)
const hotLoading = ref(false)
const recLoading = ref(false)
const banners = ref([])
const hotProducts = ref([])
const recommendProducts = ref([])
const announcements = ref([])
const activeIndex = ref(0)
const carouselRef = ref(null)

const activeBanner = computed(() => banners.value[activeIndex.value] || null)

function coverOf(item) {
  return (
    mediaUrl(item?.cover) ||
    'https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1600&q=80'
  )
}

function onSlideChange(index) {
  activeIndex.value = index
}

function goSlide(index) {
  activeIndex.value = index
  carouselRef.value?.setActiveItem?.(index)
}

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || data?.content || []
}

onMounted(async () => {
  bannerLoading.value = true
  hotLoading.value = true
  recLoading.value = true
  try {
    const [bannerList, hot, rec, anns] = await Promise.all([
      request.get('/user/banners', { silent: true }).catch(() => []),
      request.get('/user/products/hot', { silent: true }).catch(() => []),
      request.get('/user/products/recommend', {
        params: { mode: 'usercf', limit: 8 },
        silent: true
      }).catch(() => []),
      request.get('/user/announcements', { params: { page: 1, size: 5 }, silent: true }).catch(() => [])
    ])
    banners.value = unwrapList(bannerList)
    hotProducts.value = unwrapList(hot).slice(0, 8)
    recommendProducts.value = unwrapList(rec).slice(0, 8)
    announcements.value = unwrapList(anns).slice(0, 6)
  } finally {
    bannerLoading.value = false
    hotLoading.value = false
    recLoading.value = false
  }
})
</script>

<style scoped>
.home-page {
  --ink: #1a1a1a;
  --muted: #6b7280;
  --accent: #e63935;
  --line: rgba(26, 26, 26, 0.07);
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
  color: var(--ink);
  background:
    radial-gradient(ellipse 70% 40% at 50% 0%, rgba(230, 57, 53, 0.04), transparent 55%),
    #f7f8fa;
  animation: pageIn 0.55s ease;
}

@keyframes pageIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.hero {
  position: relative;
  width: 100%;
  min-height: min(78vh, 720px);
  height: min(78vh, 720px);
  overflow: hidden;
  background: #1a1212;
}

.hero-carousel,
.hero-fallback {
  position: absolute;
  inset: 0;
}

.hero-carousel :deep(.el-carousel__container),
.hero-carousel :deep(.el-carousel__item) {
  height: 100% !important;
}

.hero-carousel :deep(.el-carousel__arrow) {
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.slide,
.hero-fallback {
  position: relative;
  width: 100%;
  height: 100%;
}

.slide-media {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  transform: scale(1.04);
  animation: kenburns 14s ease-in-out infinite alternate;
  filter: saturate(1.04) contrast(1.02);
}

.fallback-media {
  background-image:
    linear-gradient(135deg, #5c1a1a, #1a1a1a),
    url('https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1600&q=80');
  background-blend-mode: soft-light;
}

@keyframes kenburns {
  from { transform: scale(1.04); }
  to { transform: scale(1.1) translate3d(-1.5%, -1%, 0); }
}

.slide-veil {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(18, 10, 10, 0.8) 0%, rgba(18, 10, 10, 0.38) 52%, rgba(18, 10, 10, 0.18) 100%),
    linear-gradient(180deg, rgba(18, 10, 10, 0.12) 0%, transparent 40%, rgba(18, 10, 10, 0.58) 100%);
}

.hero-copy {
  position: relative;
  z-index: 2;
  height: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: 72px 28px 56px;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  color: #f7faf9;
}

.brand-signal {
  margin: 0 0 14px;
  font-size: clamp(30px, 4.2vw, 44px);
  font-weight: 760;
  line-height: 1.12;
  letter-spacing: -0.02em;
}

.copy-block h1 {
  margin: 0 0 12px;
  max-width: 16ch;
  font-size: clamp(20px, 2.6vw, 30px);
  font-weight: 600;
  line-height: 1.35;
}

.subtitle {
  margin: 0 0 28px;
  max-width: 36ch;
  color: rgba(247, 250, 249, 0.78);
  font-size: 15px;
  line-height: 1.7;
}

.cta-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.cta-primary,
.cta-ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  padding: 0 22px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: transform 0.2s ease, background 0.2s ease, border-color 0.2s ease;
}

.cta-primary {
  background: #fff;
  color: #1a1212;
}

.cta-ghost {
  border: 1px solid rgba(255, 255, 255, 0.45);
  color: #fff;
  background: rgba(255, 255, 255, 0.08);
}

.cta-primary:hover,
.cta-ghost:hover {
  transform: translateY(-1px);
}

.cta-ghost:hover {
  background: rgba(255, 255, 255, 0.14);
  border-color: rgba(255, 255, 255, 0.7);
}

.hero-dots {
  display: flex;
  gap: 8px;
  margin-top: 28px;
}

.dot {
  width: 8px;
  height: 8px;
  border: none;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.35);
  cursor: pointer;
  padding: 0;
  transition: width 0.25s ease, background 0.25s ease;
}

.dot.active {
  width: 22px;
  background: #fff;
}

.mall-body {
  max-width: 1180px;
  margin: 0 auto;
  padding: 28px 24px 72px;
}

.announce-strip {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  margin-bottom: 28px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.03),
    0 10px 28px rgba(26, 26, 26, 0.04);
}

.announce-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 40px;
  height: 24px;
  padding: 0 8px;
  border-radius: 6px;
  background: linear-gradient(135deg, #ff6b63, #e1251b);
  color: #fff;
  font-size: 12px;
  font-weight: 740;
  letter-spacing: 0.04em;
}

.announce-track {
  flex: 1;
  display: flex;
  gap: 20px;
  overflow-x: auto;
  scrollbar-width: none;
}

.announce-track::-webkit-scrollbar {
  display: none;
}

.announce-item {
  position: relative;
  white-space: nowrap;
  color: #374151;
  text-decoration: none;
  font-size: 13px;
  padding-right: 20px;
  transition: color 0.2s ease;
}

.announce-item:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 50%;
  width: 1px;
  height: 10px;
  margin-top: -5px;
  background: #e5e7eb;
}

.announce-item:hover {
  color: var(--accent);
}

.announce-more {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  color: var(--accent);
  font-size: 13px;
  font-weight: 650;
  text-decoration: none;
}

.section {
  margin-bottom: 28px;
}

.ai-section {
  padding: 24px;
  border-radius: 24px;
  background:
    radial-gradient(ellipse 60% 80% at 100% 0%, rgba(230, 57, 53, 0.08), transparent 55%),
    #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 12px 32px rgba(26, 26, 26, 0.03);
}

.section-shell {
  padding: 22px 22px 20px;
  border-radius: 20px;
  background:
    radial-gradient(ellipse 45% 70% at 0% 0%, rgba(255, 152, 67, 0.08), transparent 55%),
    #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(26, 26, 26, 0.02),
    0 14px 36px rgba(26, 26, 26, 0.045);
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.section-head.plain {
  align-items: flex-end;
  padding-bottom: 0;
  border-bottom: none;
}

.section-head.plain h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 740;
  color: var(--ink);
  letter-spacing: -0.02em;
}

.eyebrow {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--accent);
  font-weight: 700;
}

.ai-link {
  color: var(--accent);
  font-size: 13px;
  font-weight: 650;
  text-decoration: none;
  transition: opacity 0.2s ease;
}

.ai-link:hover {
  opacity: 0.82;
}

.section-shell .section-head {
  align-items: flex-end;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(26, 26, 26, 0.05);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.title-mark {
  width: 4px;
  height: 30px;
  border-radius: 999px;
  background: linear-gradient(180deg, #ff6b63 0%, #e1251b 100%);
  box-shadow: 0 4px 10px rgba(225, 37, 27, 0.28);
  flex-shrink: 0;
}

.title-copy {
  min-width: 0;
}

.title-copy h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 780;
  color: var(--ink);
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.title-copy p {
  margin: 4px 0 0;
  font-size: 12.5px;
  color: #9ca3af;
  line-height: 1.3;
}

.title-tag {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, #fff1f0, #ffe4e1);
  color: #e1251b;
  font-size: 11px;
  font-weight: 740;
  letter-spacing: 0.02em;
  border: 1px solid rgba(225, 37, 27, 0.1);
}

.more-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  height: 34px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid #e8eaef;
  background: #fff;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s ease, border-color 0.2s ease, background 0.2s ease, transform 0.2s ease;
}

.more-link:hover {
  color: var(--accent);
  border-color: rgba(225, 37, 27, 0.28);
  background: #fff8f7;
  transform: translateX(2px);
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  min-height: 120px;
}

.fade-up-enter-active,
.fade-up-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.fade-up-enter-from,
.fade-up-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 960px) {
  .product-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .hero {
    min-height: min(70vh, 560px);
    height: min(70vh, 560px);
  }

  .hero-copy {
    padding: 64px 18px 40px;
  }

  .mall-body {
    padding: 20px 16px 56px;
  }

  .ai-section {
    padding: 18px;
    border-radius: 18px;
  }

  .section-shell {
    padding: 16px 14px 14px;
    border-radius: 16px;
  }

  .section-shell .section-head {
    align-items: flex-start;
    padding-bottom: 12px;
    margin-bottom: 14px;
  }

  .title-copy h2 {
    font-size: 20px;
  }

  .title-copy p {
    display: none;
  }

  .title-tag {
    display: none;
  }

  .product-grid {
    gap: 10px;
  }
}
</style>
