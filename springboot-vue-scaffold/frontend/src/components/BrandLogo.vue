<template>
  <span class="brand-logo-wrap" :class="[`size-${size}`, { 'is-image': hasCustomLogo }]">
    <img v-if="hasCustomLogo" class="brand-logo-img" :src="logoSrc" alt="logo" />
    <svg
      v-else
      class="brand-logo-svg"
      viewBox="0 0 40 40"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
    >
      <circle cx="20" cy="20" r="20" fill="#e1251b" />
      <path
        d="M12.2 15.2h15.6l-1.05 14.2a2.1 2.1 0 0 1-2.1 1.9H15.35a2.1 2.1 0 0 1-2.1-1.9L12.2 15.2Z"
        fill="#fff"
      />
      <path
        d="M15.8 15.2v-2.1a4.2 4.2 0 0 1 8.4 0v2.1"
        stroke="#fff"
        stroke-width="2.1"
        stroke-linecap="round"
        fill="none"
      />
      <path
        d="M15.8 15.2v-2.1a4.2 4.2 0 0 1 8.4 0v2.1"
        stroke="#c41a12"
        stroke-width="1.45"
        stroke-linecap="round"
        fill="none"
      />
      <path
        d="M17.5 19.6v5.6M22.5 19.6v5.6"
        stroke="#e1251b"
        stroke-width="1.5"
        stroke-linecap="round"
        opacity=".9"
      />
    </svg>
  </span>
</template>

<script setup>
import { storeToRefs } from 'pinia'
import { useSystemStore } from '@/stores/system'

defineProps({
  size: {
    type: String,
    default: 'md' // sm | md | lg
  }
})

const systemStore = useSystemStore()
const { logoSrc, hasCustomLogo } = storeToRefs(systemStore)
</script>

<style scoped>
.brand-logo-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
}

.brand-logo-wrap:not(.is-image) {
  border-radius: 50%;
}

.brand-logo-wrap.is-image {
  border-radius: 10px;
}

.size-sm {
  width: 32px;
  height: 32px;
}

.size-md {
  width: 42px;
  height: 42px;
}

.size-lg {
  width: 48px;
  height: 48px;
  border-radius: 12px;
}

.brand-logo-img,
.brand-logo-svg {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.brand-logo-wrap.is-image .brand-logo-img {
  border-radius: inherit;
}
</style>
