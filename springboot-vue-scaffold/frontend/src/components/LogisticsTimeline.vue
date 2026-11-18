<template>
  <div class="logistics">
    <div v-if="company || trackingNo" class="logistics-head">
      <div class="express">
        <strong>{{ company || '快递配送' }}</strong>
        <span v-if="trackingNo">运单号 {{ trackingNo }}</span>
      </div>
      <button v-if="trackingNo" type="button" class="copy-btn" @click="copyNo">复制单号</button>
    </div>

    <div v-if="nodes.length" class="timeline">
      <div
        v-for="(n, idx) in nodes"
        :key="`${n.code}-${idx}`"
        class="tl-item"
        :class="{ current: n.current, done: n.done && !n.current, pending: !n.done }"
      >
        <div class="tl-axis">
          <span class="dot" />
          <span v-if="idx < nodes.length - 1" class="line" />
        </div>
        <div class="tl-body">
          <div class="tl-title-row">
            <strong>{{ n.title }}</strong>
            <em v-if="n.time">{{ formatTime(n.time) }}</em>
          </div>
          <p>{{ n.desc }}</p>
        </div>
      </div>
    </div>
    <div v-else class="empty">暂无物流信息</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { formatTime } from '@/utils/format'

const props = defineProps({
  company: { type: String, default: '' },
  trackingNo: { type: String, default: '' },
  trace: { type: Array, default: () => [] }
})

const nodes = computed(() => (Array.isArray(props.trace) ? props.trace : []).filter(Boolean))

async function copyNo() {
  try {
    await navigator.clipboard.writeText(props.trackingNo)
    ElMessage.success('运单号已复制')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.logistics-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 14px;
  background: #f8fafc;
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.express {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.express strong {
  font-size: 14px;
  color: #0f172a;
}

.express span {
  font-size: 12px;
  color: #64748b;
  word-break: break-all;
}

.copy-btn {
  flex-shrink: 0;
  height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: #fff;
  color: #334155;
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
}

.timeline {
  display: flex;
  flex-direction: column;
}

.tl-item {
  display: grid;
  grid-template-columns: 22px minmax(0, 1fr);
  gap: 12px;
  min-height: 64px;
}

.tl-axis {
  position: relative;
  display: flex;
  justify-content: center;
}

.dot {
  width: 10px;
  height: 10px;
  margin-top: 4px;
  border-radius: 50%;
  background: #cbd5e1;
  box-shadow: 0 0 0 4px #f1f5f9;
  z-index: 1;
}

.line {
  position: absolute;
  top: 18px;
  bottom: -4px;
  width: 2px;
  background: #e2e8f0;
}

.tl-body {
  padding-bottom: 18px;
}

.tl-title-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 4px;
}

.tl-title-row strong {
  font-size: 14px;
  color: #64748b;
}

.tl-title-row em {
  font-style: normal;
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

.tl-body p {
  margin: 0;
  font-size: 12.5px;
  color: #94a3b8;
  line-height: 1.5;
}

.tl-item.current .dot {
  background: #e63935;
  box-shadow: 0 0 0 4px rgba(230, 57, 53, 0.16);
}

.tl-item.current .tl-title-row strong {
  color: #0f172a;
}

.tl-item.current .tl-body p {
  color: #475569;
}

.tl-item.done .dot {
  background: #94a3b8;
}

.tl-item.done .tl-title-row strong {
  color: #334155;
}

.empty {
  padding: 24px 0;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}
</style>
