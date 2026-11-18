<template>
  <div class="empty-state" :class="[`tone-${type}`, { compact }]">
    <div class="orb orb-a" aria-hidden="true" />
    <div class="orb orb-b" aria-hidden="true" />
    <div class="visual" aria-hidden="true">
      <div class="icon-ring">
        <svg v-if="type === 'cart'" viewBox="0 0 48 48" fill="none">
          <path d="M14 16h24l-2.4 14.2a3 3 0 0 1-3 2.5H19.2a3 3 0 0 1-3-2.4L14 16Z" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
          <path d="M14 16 12.2 9.8A2 2 0 0 0 10.3 8.5H8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
          <circle cx="20" cy="38" r="2.2" fill="currentColor"/>
          <circle cx="33" cy="38" r="2.2" fill="currentColor"/>
        </svg>
        <svg v-else-if="type === 'favorite'" viewBox="0 0 48 48" fill="none">
          <path d="M24 38s-12.5-7.8-16.2-14.4C5.2 19.4 6.8 13 12.2 11.6c3.2-.8 6.3.6 8 3.1 1.7-2.5 4.8-3.9 8-3.1 5.4 1.4 7 7.8 4.4 12C36.5 30.2 24 38 24 38Z" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
        </svg>
        <svg v-else-if="type === 'order'" viewBox="0 0 48 48" fill="none">
          <rect x="12" y="8" width="24" height="32" rx="4" stroke="currentColor" stroke-width="2.2"/>
          <path d="M18 16h12M18 23h12M18 30h8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
        </svg>
        <svg v-else-if="type === 'shop'" viewBox="0 0 48 48" fill="none">
          <path d="M10 20h28v18a3 3 0 0 1-3 3H13a3 3 0 0 1-3-3V20Z" stroke="currentColor" stroke-width="2.2"/>
          <path d="M10 20 13.5 11h21L38 20" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
          <path d="M20 41V28h8v13" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
        </svg>
        <svg v-else-if="type === 'address'" viewBox="0 0 48 48" fill="none">
          <path d="M24 40s-12-9.2-12-18a12 12 0 1 1 24 0c0 8.8-12 18-12 18Z" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
          <circle cx="24" cy="22" r="4" stroke="currentColor" stroke-width="2.2"/>
        </svg>
        <svg v-else-if="type === 'notice'" viewBox="0 0 48 48" fill="none">
          <path d="M16 34h16l4 6H12l4-6Z" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
          <path d="M24 8a10 10 0 0 1 10 10v8l3 4H11l3-4v-8A10 10 0 0 1 24 8Z" stroke="currentColor" stroke-width="2.2" stroke-linejoin="round"/>
        </svg>
        <svg v-else-if="type === 'search'" viewBox="0 0 48 48" fill="none">
          <circle cx="22" cy="22" r="10" stroke="currentColor" stroke-width="2.2"/>
          <path d="m30 30 8 8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
        </svg>
        <svg v-else viewBox="0 0 48 48" fill="none">
          <rect x="10" y="12" width="28" height="24" rx="5" stroke="currentColor" stroke-width="2.2"/>
          <path d="M18 22h12M18 28h8" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/>
        </svg>
      </div>
    </div>
    <h3 v-if="title" class="title">{{ title }}</h3>
    <p v-if="description" class="desc">{{ description }}</p>
    <div v-if="$slots.default" class="actions">
      <slot />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  description: { type: String, default: '' },
  type: {
    type: String,
    default: 'default'
  },
  compact: { type: Boolean, default: false }
})
</script>

<style scoped>
.empty-state {
  --accent: #e63935;
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  width: 100%;
  grid-column: 1 / -1;
  min-height: 280px;
  padding: 48px 28px;
  border-radius: 22px;
  background:
    radial-gradient(circle at 18% 20%, rgba(230, 57, 53, 0.07), transparent 42%),
    radial-gradient(circle at 86% 78%, rgba(15, 23, 42, 0.04), transparent 40%),
    #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow:
    0 1px 2px rgba(15, 23, 42, 0.03),
    0 14px 36px rgba(15, 23, 42, 0.05);
}

.empty-state.compact {
  min-height: 180px;
  padding: 32px 20px;
  border-radius: 16px;
  box-shadow: none;
}

.orb {
  pointer-events: none;
  position: absolute;
  border-radius: 50%;
  filter: blur(2px);
}

.orb-a {
  width: 120px;
  height: 120px;
  right: -24px;
  top: -36px;
  background: radial-gradient(circle, rgba(230, 57, 53, 0.12), transparent 70%);
}

.orb-b {
  width: 90px;
  height: 90px;
  left: -18px;
  bottom: -24px;
  background: radial-gradient(circle, rgba(15, 23, 42, 0.06), transparent 70%);
}

.visual {
  position: relative;
  z-index: 1;
  margin-bottom: 18px;
}

.icon-ring {
  width: 88px;
  height: 88px;
  border-radius: 28px;
  display: grid;
  place-items: center;
  color: var(--accent);
  background: linear-gradient(160deg, #fff5f4, #ffe8e6);
  border: 1px solid rgba(230, 57, 53, 0.1);
  box-shadow:
    0 10px 28px rgba(230, 57, 53, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
}

.compact .icon-ring {
  width: 68px;
  height: 68px;
  border-radius: 22px;
}

.icon-ring svg {
  width: 42px;
  height: 42px;
}

.compact .icon-ring svg {
  width: 32px;
  height: 32px;
}

.title {
  position: relative;
  z-index: 1;
  margin: 0 0 8px;
  font-size: 18px;
  font-weight: 740;
  letter-spacing: -0.02em;
  color: #1a1a1a;
}

.desc {
  position: relative;
  z-index: 1;
  margin: 0;
  max-width: 320px;
  font-size: 13.5px;
  line-height: 1.6;
  color: #8b929c;
}

.actions {
  position: relative;
  z-index: 1;
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.actions :deep(a),
.actions :deep(.empty-cta) {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 40px;
  padding: 0 20px;
  border-radius: 999px;
  background: linear-gradient(135deg, #e63935, #ff5a4f);
  color: #fff;
  text-decoration: none;
  font-size: 13px;
  font-weight: 720;
  border: none;
  cursor: pointer;
  box-shadow: 0 10px 22px rgba(230, 57, 53, 0.28);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.actions :deep(a:hover),
.actions :deep(.empty-cta:hover) {
  transform: translateY(-1px);
  box-shadow: 0 14px 28px rgba(230, 57, 53, 0.34);
}

.tone-cart .icon-ring,
.tone-favorite .icon-ring,
.tone-order .icon-ring {
  color: #e63935;
}
</style>
