<template>
  <div class="ai-page">
    <div class="page-head">
      <div class="head-shell">
        <div class="head-glow" aria-hidden="true" />
        <div class="head-inner">
          <div class="head-copy">
            <nav class="crumb">
              <router-link to="/user/home">首页</router-link>
              <span>/</span>
              <em>AI 导购</em>
            </nav>
            <h1>AI 导购 Agent</h1>
            <p class="head-desc">Spring AI 导购 · 可加购下单 · RAG 答疑 · 支持识图找同款</p>
          </div>
          <div class="head-side">
            <button type="button" class="side-btn" @click="openAssistant">打开 AI 客服</button>
            <button type="button" class="side-btn ghost" @click="clearSession">清空对话</button>
          </div>
        </div>
      </div>
    </div>

    <section class="chat-shell">
      <div ref="listRef" class="chat-list">
        <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
          <div v-if="m.role === 'assistant'" class="avatar ai" aria-hidden="true">
            <span>AI</span>
          </div>
          <div class="bubble">
            <img v-if="m.imageUrl" :src="mediaUrl(m.imageUrl)" class="msg-image" alt="识图" />
            <p v-if="displayText(m) || m.streaming" class="text">
              {{ displayText(m) }}<i v-if="m.streaming" class="caret" />
            </p>
            <div v-if="m.products?.length && !m.streaming" class="suggest-block">
              <div class="suggest-head">
                <em>为你精选</em>
                <span>{{ m.products.length }} 件好物</span>
              </div>
              <div class="suggest-grid">
                <div
                  v-for="(p, idx) in m.products"
                  :key="p.id"
                  class="suggest-card"
                >
                  <router-link :to="`/user/product/${p.id}`" class="suggest-main">
                    <div class="thumb">
                      <img :src="coverOf(p)" alt="" />
                      <i class="rank">{{ idx + 1 }}</i>
                    </div>
                    <div class="meta">
                      <strong>{{ p.name }}</strong>
                      <div class="meta-row">
                        <b class="price">{{ formatPrice(p.price) }}</b>
                        <span class="sales">已售 {{ p.sales ?? 0 }}</span>
                        <span v-if="p.categoryName" class="cat">{{ p.categoryName }}</span>
                      </div>
                    </div>
                  </router-link>
                  <button
                    type="button"
                    class="cart-mini"
                    :disabled="cartingId === p.id"
                    @click.stop="addCartFromAgent(p)"
                  >
                    {{ cartingId === p.id ? '…' : '加购' }}
                  </button>
                </div>
              </div>
            </div>
            <div v-if="m.actions?.length && !m.streaming" class="action-row">
              <router-link
                v-for="(a, ai) in m.actions"
                :key="ai + (a.path || '')"
                :to="a.path || '/user/home'"
                class="action-chip"
              >
                {{ a.label || '前往' }}
              </router-link>
            </div>
            <div v-if="m.orders?.length && !m.streaming" class="order-hint">
              <em>相关订单</em>
              <router-link
                v-for="o in m.orders"
                :key="o.id"
                to="/user/orders"
                class="order-link"
              >
                {{ o.orderNo || ('订单#' + o.id) }} · {{ formatPrice(o.totalAmount) }}
              </router-link>
            </div>
          </div>
          <div v-if="m.role === 'user'" class="avatar user" aria-hidden="true">
            <img v-if="userAvatar" :src="userAvatar" alt="" />
            <span v-else>{{ userLetter }}</span>
          </div>
        </div>
      </div>

      <footer class="chat-foot">
        <div class="quick-row">
          <span class="quick-label">试试</span>
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

        <div v-if="pendingPreview" class="image-preview">
          <img :src="pendingPreview" alt="待识别" />
          <div class="image-meta">
            <strong>识图导购</strong>
            <span>可补充说明后发送，或直接识图找同款</span>
          </div>
          <button type="button" class="image-clear" :disabled="sending || uploading" @click="clearPendingImage">
            移除
          </button>
        </div>

        <div class="composer">
          <input
            ref="fileInput"
            type="file"
            class="file-input"
            accept="image/jpeg,image/png,image/webp,image/gif"
            @change="onPickImage"
          />
          <button
            type="button"
            class="icon-btn"
            title="上传图片识图"
            :disabled="sending || uploading"
            @click="fileInput?.click()"
          >
            <span v-if="uploading">…</span>
            <svg v-else viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path
                fill="currentColor"
                d="M19 5H5a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2Zm0 12H5l3.5-4.5 2.5 3 3.5-4.5L19 17ZM8.5 10a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3Z"
              />
            </svg>
          </button>
          <el-input
            v-model="input"
            class="composer-input"
            :placeholder="pendingUrl ? '可选：补充颜色、预算、用途…' : '例如：预算 200 内想买台灯，或点左侧上传识图'"
            maxlength="500"
            clearable
            :disabled="sending"
            @keyup.enter="send"
          />
          <button type="button" class="send-btn" :disabled="sending || uploading" @click="send">
            {{ sending ? '回复中' : pendingUrl ? '识图' : '发送' }}
          </button>
        </div>
      </footer>
    </section>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import { formatPrice, mediaUrl, productCover } from '@/utils/format'
import { useAiChatHistory } from '@/composables/useAiChatHistory'
import { useAiAssistantPanel } from '@/composables/useAiAssistantPanel'

const userStore = useUserStore()
const { open: openAssistant } = useAiAssistantPanel()

const welcome = {
  role: 'assistant',
  content:
    '你好，我是智购 AI Agent。可以说预算/品类让我推荐，登录后还能说「加入购物车」「帮我下单支付」。也支持上传图片识图找同款。'
}

const { messages, clearSession, pushMessage, patchLastMessage, syncCurrentToSession } =
  useAiChatHistory('guide', welcome)

const input = ref('')
const sending = ref(false)
const uploading = ref(false)
const listRef = ref()
const fileInput = ref()
const pendingUrl = ref('')
const pendingPreview = ref('')
const quickAsks = [
  '推荐热销好物',
  '300元内数码',
  '送女生的礼物',
  '查看我的购物车',
  '怎么退换货',
  '帮我结算下单'
]
const cartingId = ref(null)
let streamTimer = null

const userAvatar = computed(() => mediaUrl(userStore.user?.avatar) || '')
const userLetter = computed(() =>
  (userStore.user?.nickname || userStore.user?.username || '我').slice(0, 1).toUpperCase()
)

function coverOf(p) {
  return productCover(p) || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=200&q=80'
}

/** 有商品卡片时，去掉文案末尾重复的编号清单 */
function displayText(m) {
  const raw = String(m?.content || '')
  if (!m?.products?.length || m.streaming) return raw
  return raw
    .replace(/\n*(?:为您筛到\s*\d+\s*件相关商品[：:]?\s*)?(?:\d+\.\s*.+(?:\n|$))+/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function sleep(ms) {
  return new Promise((resolve) => {
    streamTimer = setTimeout(resolve, ms)
  })
}

function clearPendingImage() {
  pendingUrl.value = ''
  if (pendingPreview.value?.startsWith('blob:')) {
    URL.revokeObjectURL(pendingPreview.value)
  }
  pendingPreview.value = ''
  if (fileInput.value) fileInput.value.value = ''
}

async function onPickImage(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (!userStore.token) {
    ElMessage.warning('识图需要先登录')
    if (fileInput.value) fileInput.value.value = ''
    return
  }
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请选择图片文件')
    return
  }
  if (file.size > 4 * 1024 * 1024) {
    ElMessage.warning('图片请小于 4MB')
    return
  }
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const baseURL = import.meta.env.VITE_API_BASE_URL || '/api'
    const response = await axios.post(`${baseURL}/files/upload`, formData, {
      headers: {
        Authorization: `Bearer ${userStore.token}`
      },
      timeout: 60000
    })
    const result = response.data
    if (result.code !== 200) {
      throw new Error(result.message || '上传失败')
    }
    const url = result.data?.url || result.data?.path || result.data
    if (!url) throw new Error('上传失败')
    if (pendingPreview.value?.startsWith('blob:')) {
      URL.revokeObjectURL(pendingPreview.value)
    }
    pendingUrl.value = url
    pendingPreview.value = mediaUrl(url) || URL.createObjectURL(file)
    ElMessage.success('图片已添加，点击识图即可')
  } catch (err) {
    ElMessage.error(err?.response?.data?.message || err?.message || '图片上传失败')
  } finally {
    uploading.value = false
    if (fileInput.value) fileInput.value.value = ''
  }
}

async function scrollBottom() {
  await nextTick()
  if (listRef.value) listRef.value.scrollTop = listRef.value.scrollHeight
}

watch(messages, () => scrollBottom(), { deep: true })

async function streamReply(fullText, payload = {}) {
  const text = String(fullText || '')
  const step = text.length > 180 ? 3 : text.length > 80 ? 2 : 1
  for (let i = 0; i <= text.length; i += step) {
    patchLastMessage({ content: text.slice(0, Math.min(i, text.length)), streaming: true })
    await scrollBottom()
    await sleep(18)
  }
  patchLastMessage({
    content: text,
    products: Array.isArray(payload.products) ? payload.products : [],
    actions: Array.isArray(payload.actions) ? payload.actions : [],
    orders: Array.isArray(payload.orders) ? payload.orders : [],
    needLogin: !!payload.needLogin,
    streaming: false
  })
  syncCurrentToSession()
}

async function addCartFromAgent(p) {
  if (!p?.id) return
  if (!userStore.token) {
    ElMessage.warning('请先登录后再加购')
    return
  }
  cartingId.value = p.id
  try {
    await request.post('/user/cart', { productId: p.id, quantity: 1 })
    ElMessage.success('已加入购物车')
  } catch (err) {
    ElMessage.error(err?.message || '加购失败')
  } finally {
    cartingId.value = null
  }
}

async function ask(text) {
  input.value = text
  clearPendingImage()
  await send()
}

async function send() {
  const message = input.value.trim()
  const imageUrl = pendingUrl.value
  if ((!message && !imageUrl) || sending.value || uploading.value) return

  pushMessage({
    role: 'user',
    content: message || (imageUrl ? '请根据这张图片帮我找相似商品' : ''),
    imageUrl
  })
  input.value = ''
  const usedImage = imageUrl
  clearPendingImage()

  sending.value = true
  pushMessage({ role: 'assistant', content: '', products: [], streaming: true })
  await scrollBottom()
  try {
    const history = messages.value
      .slice(0, -2)
      .filter((m) => (m.role === 'user' || m.role === 'assistant') && String(m.content || '').trim())
      .slice(-10)
      .map((m) => ({
        role: m.role,
        content: String(m.content).slice(0, 800)
      }))
    const data = usedImage
      ? await request.post('/ai/guide/vision', { imageUrl: usedImage, message: message || undefined }, { timeout: 90000 })
      : await request.post('/ai/agent', { message, history }, { timeout: 90000 })
    const content = data?.reply || data?.message || data?.content || '暂时没有合适推荐，换个说法试试？'
    const products = data?.products || data?.recommendProducts || data?.items || []
    let actions = Array.isArray(data?.actions) ? data.actions : []
    if (data?.needLogin && !actions.some((a) => a.type === 'login')) {
      actions = [...actions, { type: 'login', label: '去登录', path: '/login' }]
    }
    await streamReply(content, {
      products,
      actions,
      orders: data?.orders || [],
      needLogin: !!data?.needLogin
    })
  } catch {
    patchLastMessage({
      content: usedImage ? '识图服务暂时繁忙，请稍后再试或改用文字描述。' : '导购服务暂时繁忙，请稍后再试。',
      products: [],
      actions: [],
      orders: [],
      streaming: false
    })
    syncCurrentToSession()
  } finally {
    sending.value = false
    await scrollBottom()
  }
}

onBeforeUnmount(() => {
  if (streamTimer) clearTimeout(streamTimer)
  if (pendingPreview.value?.startsWith('blob:')) {
    URL.revokeObjectURL(pendingPreview.value)
  }
})
</script>

<style scoped>
.ai-page {
  --accent: #e63935;
  --ink: #1a1a1a;
  --muted: #8b929c;
  --line: #ececec;
  box-sizing: border-box;
  width: 100%;
  max-width: 1180px;
  margin: 0 auto;
  padding: 16px 24px 18px;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.page-head {
  flex-shrink: 0;
  margin-bottom: 12px;
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
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 18px 22px 16px;
}

.crumb {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #9ca3af;
}
.crumb a {
  color: #9ca3af;
  text-decoration: none;
}
.crumb a:hover { color: var(--accent); }
.crumb em {
  font-style: normal;
  color: var(--ink);
  font-weight: 600;
}

.head-copy h1 {
  margin: 0 0 6px;
  font-size: clamp(22px, 2.6vw, 28px);
  font-weight: 760;
  letter-spacing: -0.02em;
  line-height: 1.2;
}
.head-desc {
  margin: 0;
  color: var(--muted);
  font-size: 13.5px;
}

.head-side {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  flex-shrink: 0;
}
.side-btn {
  height: 36px;
  padding: 0 14px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #e63935, #ff6a4d);
  color: #fff;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
}
.side-btn.ghost {
  background: #fff;
  color: #4b5563;
  border: 1px solid rgba(26, 26, 26, 0.08);
  box-shadow: 0 2px 8px rgba(26, 26, 26, 0.04);
}
.side-btn.ghost:hover { color: var(--accent); border-color: rgba(230, 57, 53, 0.28); }

.chat-shell {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--line);
  overflow: hidden;
}

.chat-list {
  flex: 1;
  min-height: 0;
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  padding: 22px 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  background: linear-gradient(180deg, #fafbfc 0%, #f7f8fa 100%);
}
.msg {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.msg.user { justify-content: flex-end; }

.avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 740;
}
.avatar.ai {
  background: linear-gradient(145deg, #2b323c, #4b5563);
  color: #fff;
}
.avatar.user {
  background: #fff1f0;
  color: #e1251b;
  border: 1px solid rgba(225, 37, 27, 0.12);
}
.avatar.user img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bubble {
  max-width: min(82%, 620px);
  padding: 14px 16px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.05);
  box-shadow: 0 1px 2px rgba(26, 26, 26, 0.03);
}
.msg.user .bubble {
  background: #e1251b;
  border-color: #e1251b;
  color: #fff;
  border-bottom-right-radius: 4px;
  box-shadow: none;
  max-width: min(78%, 520px);
}
.msg.assistant .bubble {
  border-bottom-left-radius: 4px;
}
.msg-image {
  display: block;
  max-width: 180px;
  max-height: 180px;
  width: auto;
  height: auto;
  border-radius: 10px;
  margin-bottom: 8px;
  object-fit: cover;
  background: rgba(255, 255, 255, 0.2);
}
.msg.assistant .msg-image {
  background: #f3f4f6;
}
.text {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  min-height: 1.2em;
  color: #2a2f36;
}
.caret {
  display: inline-block;
  width: 2px;
  height: 1em;
  margin-left: 2px;
  background: var(--accent);
  vertical-align: -2px;
  animation: blink 0.9s steps(1) infinite;
}
@keyframes blink {
  50% { opacity: 0; }
}

.suggest-block {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid rgba(26, 26, 26, 0.06);
}
.suggest-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 10px;
}
.suggest-head em {
  font-style: normal;
  font-size: 13px;
  font-weight: 740;
  color: #1a1a1a;
  letter-spacing: 0.02em;
}
.suggest-head span {
  font-size: 12px;
  color: #9ca3af;
  font-weight: 600;
}
.suggest-grid {
  display: grid;
  gap: 8px;
}
.suggest-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 8px;
  align-items: center;
  padding: 8px 10px 8px 8px;
  border-radius: 14px;
  background: linear-gradient(180deg, #ffffff 0%, #fafbfc 100%);
  color: inherit;
  border: 1px solid rgba(26, 26, 26, 0.06);
  box-shadow: 0 1px 2px rgba(26, 26, 26, 0.03);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}
.suggest-main {
  display: grid;
  grid-template-columns: 64px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
  text-decoration: none;
  color: inherit;
  min-width: 0;
}
.cart-mini {
  flex-shrink: 0;
  height: 30px;
  padding: 0 12px;
  border: none;
  border-radius: 999px;
  background: #e63935;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
}
.cart-mini:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.action-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}
.action-chip {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: #fff5f5;
  border: 1px solid rgba(230, 57, 53, 0.2);
  color: #e63935;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
}
.action-chip:hover {
  background: #ffe8e8;
}
.order-hint {
  margin-top: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.order-hint em {
  font-style: normal;
  font-size: 12px;
  color: #8b929c;
  font-weight: 650;
}
.order-link {
  font-size: 13px;
  color: #1a1a1a;
  text-decoration: none;
  font-weight: 650;
}
.order-link:hover {
  color: #e63935;
}
.suggest-card:hover {
  transform: translateY(-1px);
  border-color: rgba(225, 37, 27, 0.22);
  box-shadow:
    0 4px 12px rgba(26, 26, 26, 0.05),
    0 10px 22px rgba(225, 37, 27, 0.06);
}
.suggest-card .thumb {
  position: relative;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  background: #f3f4f6;
  flex-shrink: 0;
}
.suggest-card .thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.25s ease;
}
.suggest-card:hover .thumb img {
  transform: scale(1.04);
}
.suggest-card .rank {
  position: absolute;
  left: 4px;
  top: 4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 999px;
  background: rgba(26, 26, 26, 0.72);
  color: #fff;
  font-size: 10px;
  font-weight: 740;
  font-style: normal;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  backdrop-filter: blur(4px);
}
.suggest-card:nth-child(1) .rank {
  background: linear-gradient(135deg, #e63935, #ff6a4d);
}
.suggest-card .meta {
  min-width: 0;
}
.suggest-card strong {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  font-size: 13.5px;
  font-weight: 700;
  line-height: 1.35;
  color: #1a1a1a;
  margin-bottom: 6px;
}
.meta-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.suggest-card .price {
  color: #e1251b;
  font-weight: 780;
  font-size: 15px;
  font-variant-numeric: tabular-nums;
  letter-spacing: -0.02em;
}
.suggest-card .sales {
  font-size: 11px;
  font-weight: 650;
  color: #6b7280;
}
.suggest-card .cat {
  font-size: 11px;
  font-weight: 650;
  color: #8b929c;
  background: #f3f4f6;
  border-radius: 999px;
  padding: 2px 8px;
  line-height: 1.4;
}
.suggest-card .go {
  color: #c4c9d1;
  font-size: 20px;
  font-weight: 400;
  line-height: 1;
  transition: color 0.15s ease, transform 0.15s ease;
}
.suggest-card:hover .go {
  color: #e1251b;
  transform: translateX(2px);
}

.chat-foot {
  flex-shrink: 0;
  padding: 14px 18px 16px;
  border-top: 1px solid rgba(26, 26, 26, 0.05);
  background: #fff;
}

.quick-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  overflow-x: auto;
  padding-bottom: 2px;
}
.quick-label {
  flex-shrink: 0;
  font-size: 12px;
  color: #9ca3af;
  font-weight: 600;
}
.chip {
  flex-shrink: 0;
  height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid rgba(26, 26, 26, 0.08);
  background: #f7f8fa;
  color: #4b5563;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: 0.15s ease;
}
.chip:hover:not(:disabled) {
  background: #fff1f0;
  border-color: rgba(230, 57, 53, 0.28);
  color: var(--accent);
}
.chip:disabled { opacity: 0.55; cursor: not-allowed; }

.image-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f7f8fa;
  border: 1px solid rgba(26, 26, 26, 0.05);
}
.image-preview img {
  width: 56px;
  height: 56px;
  border-radius: 10px;
  object-fit: cover;
  background: #e5e7eb;
}
.image-meta {
  flex: 1;
  min-width: 0;
}
.image-meta strong {
  display: block;
  margin-bottom: 4px;
  font-size: 13px;
  font-weight: 700;
}
.image-meta span {
  font-size: 12px;
  color: #9ca3af;
}
.image-clear {
  flex-shrink: 0;
  height: 30px;
  padding: 0 12px;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  color: #6b7280;
  font-size: 12px;
  cursor: pointer;
  font-family: inherit;
}
.image-clear:hover { color: #e1251b; border-color: rgba(225, 37, 27, 0.3); }

.composer {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 5px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid rgba(26, 26, 26, 0.06);
  box-shadow:
    0 2px 8px rgba(26, 26, 26, 0.04),
    0 10px 24px rgba(26, 26, 26, 0.05);
}
.file-input { display: none; }
.icon-btn {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  margin-left: 2px;
  border: none;
  border-radius: 999px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 13px;
  font-weight: 740;
  cursor: pointer;
  font-family: inherit;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.icon-btn:hover:not(:disabled) {
  background: #fff1f0;
  color: #e1251b;
}
.icon-btn:disabled { opacity: 0.6; cursor: wait; }
.composer-input { flex: 1; min-width: 0; }
.composer-input :deep(.el-input__wrapper) {
  border-radius: 999px;
  min-height: 40px;
  background: transparent;
  box-shadow: none !important;
  padding-left: 10px;
}
.composer-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: none !important;
}
.send-btn {
  flex-shrink: 0;
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #ef4a46, #e63935 55%, #d92b28);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
  font-family: inherit;
}
.send-btn:hover:not(:disabled) { filter: brightness(1.03); }
.send-btn:disabled { opacity: 0.65; cursor: wait; }

@media (max-width: 720px) {
  .ai-page { padding: 12px 12px 14px; }
  .head-inner {
    flex-direction: column;
    align-items: stretch;
    padding: 16px 16px;
  }
  .bubble { max-width: min(82%, 560px); }
}
</style>
