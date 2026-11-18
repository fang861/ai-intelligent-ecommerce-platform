<template>
  <router-link :to="`/user/product/${product.id}`" class="product-card">
    <div class="cover-wrap">
      <img :src="cover" :alt="product.name || '商品'" loading="lazy" />
      <span v-if="displayBadge" class="badge" :class="badgeTone">{{ displayBadge }}</span>
      <button
        v-if="showUnfavorite"
        type="button"
        class="unfav-btn"
        title="取消收藏"
        aria-label="取消收藏"
        @click.prevent.stop="$emit('unfavorite')"
      >
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
          <path
            d="M12 20.2S4.5 15.1 2.4 10.6C1.1 7.7 2.4 4.2 5.8 3.4c2-.5 4 .4 5.1 2 1.1-1.6 3.1-2.5 5.1-2 3.4.8 4.7 4.3 3.4 7.2C19.5 15.1 12 20.2 12 20.2Z"
            fill="currentColor"
          />
        </svg>
      </button>
    </div>
    <div class="body">
      <h3 class="name">{{ product.name || '未命名商品' }}</h3>

      <div class="tags" v-if="product.categoryName || product.merchantId || product.shopName">
        <span v-if="product.categoryName" class="tag">{{ product.categoryName }}</span>
        <router-link
          v-if="product.merchantId"
          :to="`/user/shop/${product.merchantId}`"
          class="shop"
          @click.stop
        >{{ product.shopName || '进店逛逛' }}</router-link>
        <span v-else-if="product.shopName" class="shop">{{ product.shopName }}</span>
      </div>

      <div class="price-row">
        <span class="price">{{ formatPrice(product.price) }}</span>
        <span
          v-if="hasOriginal"
          class="origin"
        >{{ formatPrice(product.originalPrice) }}</span>
      </div>

      <div class="meta">
        <span class="sales">已售 {{ product.sales ?? 0 }}</span>
        <span v-if="product.stock != null" :class="{ low: isLowStock }">
          {{ stockText }}
        </span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { formatPrice, productCover } from '@/utils/format'

const props = defineProps({
  product: { type: Object, required: true },
  badge: { type: String, default: '' },
  showUnfavorite: { type: Boolean, default: false }
})

defineEmits(['unfavorite'])

const cover = computed(
  () =>
    productCover(props.product) ||
    'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80'
)

const hasOriginal = computed(() => {
  const price = Number(props.product.price)
  const origin = Number(props.product.originalPrice)
  return Number.isFinite(origin) && Number.isFinite(price) && origin > price
})

const displayBadge = computed(() => {
  if (props.badge) return props.badge
  if ((props.product.sales || 0) >= 200) return '热销'
  if (hasOriginal.value) return '特惠'
  return ''
})

const badgeTone = computed(() => {
  if (props.badge === 'AI' || props.badge === '相似' || props.badge === '推荐' || props.badge === 'ItemCF' || props.badge === 'UserCF') {
    return 'tone-rec'
  }
  if (displayBadge.value === '热销') return 'tone-hot'
  return 'tone-deal'
})

const isLowStock = computed(() => {
  const stock = Number(props.product.stock)
  return Number.isFinite(stock) && stock > 0 && stock <= 30
})

const stockText = computed(() => {
  const stock = Number(props.product.stock)
  if (!Number.isFinite(stock)) return ''
  if (stock <= 0) return '暂时缺货'
  if (stock <= 30) return `仅剩 ${stock}`
  return `库存 ${stock}`
})
</script>

<style scoped>
.product-card {
  display: block;
  text-decoration: none;
  color: inherit;
  border-radius: 14px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 2px 10px rgba(26, 26, 26, 0.03);
  transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  border-color: rgba(225, 37, 27, 0.16);
  box-shadow:
    0 8px 20px rgba(26, 26, 26, 0.08),
    0 0 0 1px rgba(225, 37, 27, 0.05);
}

.cover-wrap {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;
  background: #f5f6f8;
}

.cover-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.4s ease;
}

.product-card:hover .cover-wrap img {
  transform: scale(1.05);
}

.badge {
  position: absolute;
  left: 8px;
  top: 8px;
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
  letter-spacing: 0.02em;
}

.tone-deal {
  background: #e1251b;
}

.tone-hot {
  background: linear-gradient(135deg, #ff7a45, #e1251b);
}

.tone-rec {
  background: linear-gradient(135deg, #ff9a4d, #e1251b);
}

.tone-ai {
  background: linear-gradient(135deg, #ff9a4d, #e1251b);
}

.unfav-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  display: grid;
  place-items: center;
  cursor: pointer;
  color: #e1251b;
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 12px rgba(26, 26, 26, 0.12);
  transition: transform 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.unfav-btn svg {
  width: 15px;
  height: 15px;
}

.unfav-btn:hover {
  transform: scale(1.06);
  color: #fff;
  background: #e1251b;
}

.body {
  padding: 12px 12px 13px;
}

.name {
  margin: 0;
  font-size: 13.5px;
  font-weight: 650;
  line-height: 1.45;
  color: #1a1a1a;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 2.9em;
}

.tags {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 8px;
  min-width: 0;
}

.tag {
  flex-shrink: 0;
  height: 20px;
  padding: 0 6px;
  border-radius: 4px;
  background: #fff5f4;
  color: #e1251b;
  font-size: 11px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
}

.shop {
  min-width: 0;
  font-size: 11.5px;
  color: #9ca3af;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-decoration: none;
  transition: color 0.2s;
}

a.shop:hover {
  color: #e1251b;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 6px;
  margin-top: 10px;
}

.price {
  color: #e1251b;
  font-size: 18px;
  font-weight: 780;
  letter-spacing: -0.03em;
  line-height: 1;
}

.origin {
  font-size: 12px;
  color: #b0b5bd;
  text-decoration: line-through;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed rgba(26, 26, 26, 0.06);
  font-size: 11.5px;
  color: #9ca3af;
}

.meta .low {
  color: #f59e0b;
  font-weight: 600;
}

.meta .sales {
  color: #6b7280;
  font-weight: 600;
}
</style>
