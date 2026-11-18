<template>
  <transition name="ai-pop">
    <div v-if="visible" class="ai-panel" role="dialog" aria-label="AI客服">
      <header class="panel-head">
        <div class="head-copy">
          <strong>AI 客服</strong>
          <span>订单 · 物流 · 售后咨询</span>
        </div>
        <div class="head-actions">
          <button type="button" class="icon-btn" title="清空" @click="clearSession">清空</button>
          <button type="button" class="icon-btn close" title="关闭" @click="close">×</button>
        </div>
      </header>

      <div v-if="needLogin" class="login-gate">
        <p>登录后可使用 AI 客服</p>
        <router-link class="gate-btn" to="/login" @click="close">去登录</router-link>
      </div>

      <template v-else>
        <div ref="listRef" class="chat-list">
          <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
            <div v-if="m.role === 'assistant'" class="avatar ai">AI</div>
            <div class="bubble">
              {{ m.content }}<i v-if="m.streaming" class="caret" />
            </div>
            <div v-if="m.role === 'user'" class="avatar user">
              <img v-if="userAvatar" :src="userAvatar" alt="" />
              <span v-else>{{ userLetter }}</span>
            </div>
          </div>
        </div>

        <div class="quick-row">
          <button
            v-for="q in quickAsks"
            :key="q"
            type="button"
            class="chip"
            :disabled="sending"
            @click="ask(q)"
          >
            {{ q }}
          </button>
        </div>

        <div class="composer">
          <input
            v-model="input"
            type="text"
            maxlength="500"
            placeholder="描述你的问题…"
            :disabled="sending"
            @keyup.enter="send"
          />
          <button type="button" class="send-btn" :disabled="sending" @click="send">
            {{ sending ? '…' : '发送' }}
          </button>
        </div>
      </template>
    </div>
  </transition>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { mediaUrl } from '@/utils/format'
import { useAiChatHistory } from '@/composables/useAiChatHistory'
import { useAiAssistantPanel } from '@/composables/useAiAssistantPanel'

const userStore = useUserStore()
const { visible, close } = useAiAssistantPanel()
const needLogin = computed(() => !userStore.token)
const userAvatar = computed(() => mediaUrl(userStore.user?.avatar) || '')
const userLetter = computed(() =>
  (userStore.user?.nickname || userStore.user?.username || '我').slice(0, 1).toUpperCase()
)

const welcome = {
  role: 'assistant',
  content: '您好，我是智购客服。可以咨询订单、支付、配送或退换货相关问题。'
}

const {
  messages,
  clearSession,
  pushMessage,
  patchLastMessage,
  syncCurrentToSession
} = useAiChatHistory('service', welcome)

const input = ref('')
const sending = ref(false)
const listRef = ref()
const quickAsks = ['如何下单？', '支付失败怎么办？', '多久发货？', '如何申请退款？']
let streamTimer = null

function sleep(ms) {
  return new Promise((resolve) => {
    streamTimer = setTimeout(resolve, ms)
  })
}

async function scrollBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

watch([messages, visible], () => {
  if (visible.value) scrollBottom()
}, { deep: true })

async function streamReply(fullText) {
  const text = String(fullText || '')
  const step = text.length > 120 ? 2 : 1
  for (let i = 0; i <= text.length; i += step) {
    patchLastMessage({ content: text.slice(0, Math.min(i, text.length)), streaming: true })
    await scrollBottom()
    await sleep(16)
  }
  patchLastMessage({ content: text, streaming: false })
  syncCurrentToSession()
}

async function ask(text) {
  input.value = text
  await send()
}

async function send() {
  if (needLogin.value) return
  const message = input.value.trim()
  if (!message || sending.value) return
  pushMessage({ role: 'user', content: message })
  input.value = ''
  sending.value = true
  pushMessage({ role: 'assistant', content: '', streaming: true })
  await scrollBottom()
  try {
    const data = await request.post('/ai/service', { message })
    const content = data?.reply || data?.message || data?.content || '已收到您的问题，请稍后再试。'
    await streamReply(content)
  } catch {
    patchLastMessage({ content: '客服暂时繁忙，请稍后再试。', streaming: false })
    syncCurrentToSession()
  } finally {
    sending.value = false
    await scrollBottom()
  }
}

onBeforeUnmount(() => {
  if (streamTimer) clearTimeout(streamTimer)
})
</script>

<style scoped>
.ai-panel {
  position: fixed;
  right: 28px;
  bottom: 100px;
  z-index: 70;
  width: min(380px, calc(100vw - 24px));
  height: min(560px, calc(100vh - 140px));
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #ececec;
  box-shadow: 0 16px 40px rgba(26, 26, 26, 0.14);
  overflow: hidden;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
}

.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px 14px;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
}
.head-copy strong {
  display: block;
  font-size: 14px;
  font-weight: 720;
  color: #1a1a1a;
}
.head-copy span {
  font-size: 11px;
  color: #8b929c;
}
.head-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}
.icon-btn {
  height: 28px;
  padding: 0 10px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  color: #4b5563;
  font-size: 12px;
  cursor: pointer;
  font-family: inherit;
}
.icon-btn:hover {
  border-color: rgba(225, 37, 27, 0.3);
  color: #e1251b;
}
.icon-btn.close {
  width: 28px;
  padding: 0;
  font-size: 18px;
  line-height: 1;
}

.login-gate {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 24px;
  background: #fafafa;
}
.login-gate p {
  margin: 0;
  color: #8b929c;
  font-size: 13px;
}
.gate-btn {
  height: 34px;
  padding: 0 16px;
  border-radius: 6px;
  background: #e1251b;
  color: #fff;
  text-decoration: none;
  font-size: 13px;
  font-weight: 650;
  display: inline-flex;
  align-items: center;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #fafafa;
}
.msg {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}
.msg.user { justify-content: flex-end; }
.avatar {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 740;
  overflow: hidden;
}
.avatar.ai {
  background: #3d4f5f;
  color: #fff;
}
.avatar.user {
  background: #fff1f0;
  color: #e1251b;
}
.avatar.user img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.bubble {
  max-width: 78%;
  padding: 9px 11px;
  border-radius: 10px;
  background: #fff;
  border: 1px solid #ececec;
  color: #1a1a1a;
  font-size: 13px;
  line-height: 1.55;
  white-space: pre-wrap;
  min-height: 1.2em;
}
.msg.user .bubble {
  background: #e1251b;
  border-color: #e1251b;
  color: #fff;
}
.caret {
  display: inline-block;
  width: 2px;
  height: 0.95em;
  margin-left: 2px;
  background: #e1251b;
  vertical-align: -2px;
  animation: blink 0.9s steps(1) infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}
.typing {
  color: #8b929c;
  background: #f3f4f6;
  border: none;
}

.quick-row {
  display: flex;
  gap: 6px;
  padding: 8px 12px 0;
  overflow-x: auto;
}
.chip {
  flex-shrink: 0;
  height: 26px;
  padding: 0 10px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  background: #fff;
  color: #4b5563;
  font-size: 11px;
  cursor: pointer;
  font-family: inherit;
}
.chip:hover {
  border-color: rgba(225, 37, 27, 0.3);
  color: #e1251b;
}

.composer {
  display: flex;
  gap: 8px;
  padding: 10px 12px 12px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}
.composer input {
  flex: 1;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  outline: none;
  font-size: 13px;
  font-family: inherit;
}
.composer input:focus {
  border-color: rgba(225, 37, 27, 0.45);
}
.send-btn {
  flex-shrink: 0;
  height: 36px;
  padding: 0 14px;
  border: none;
  border-radius: 6px;
  background: #e1251b;
  color: #fff;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
}
.send-btn:disabled {
  opacity: 0.65;
  cursor: wait;
}

.ai-pop-enter-active,
.ai-pop-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.ai-pop-enter-from,
.ai-pop-leave-to {
  opacity: 0;
  transform: translateY(10px) scale(0.98);
}

@media (max-width: 900px) {
  .ai-panel {
    right: 12px;
    bottom: 88px;
    width: calc(100vw - 24px);
    height: min(70vh, 560px);
  }
}
</style>
