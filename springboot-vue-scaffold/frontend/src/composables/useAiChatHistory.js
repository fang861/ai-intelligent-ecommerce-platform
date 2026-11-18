import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'

function uid() {
  return `${Date.now().toString(36)}_${Math.random().toString(36).slice(2, 8)}`
}

function slimProducts(list) {
  if (!Array.isArray(list)) return []
  return list.slice(0, 8).map((p) => ({
    id: p.id,
    name: p.name,
    price: p.price,
    cover: p.cover,
    categoryName: p.categoryName || '',
    images: Array.isArray(p.images) ? p.images.slice(0, 1) : p.images
  }))
}

function slimMessages(list) {
  return (list || []).map((m) => ({
    role: m.role,
    content: m.content,
    imageUrl: m.imageUrl || '',
    products: slimProducts(m.products)
  }))
}

/**
 * 本地持久化多会话聊天历史（按用户 + 场景隔离）
 * @param {'guide'|'service'} type
 * @param {{ role: string, content: string }} welcome
 */
export function useAiChatHistory(type, welcome) {
  const userStore = useUserStore()
  const storageKey = computed(() => {
    const uidPart = userStore.user?.id || userStore.user?.userId || (userStore.token ? 'auth' : 'guest')
    return `zg_ai_chat_v1_${type}_${uidPart}`
  })

  const sessions = ref([])
  const currentId = ref('')
  const messages = ref([])

  function sessionTitle(list) {
    const firstUser = (list || []).find((m) => m.role === 'user')
    if (!firstUser) return '新的对话'
    if (firstUser.imageUrl && (!firstUser.content || firstUser.content.includes('图片'))) {
      return '识图导购'
    }
    return firstUser.content?.slice(0, 18) || '新的对话'
  }

  function createSession(seedMessages) {
    return {
      id: uid(),
      title: sessionTitle(seedMessages),
      updatedAt: Date.now(),
      messages: slimMessages(seedMessages?.length ? seedMessages : [welcome])
    }
  }

  function persist() {
    try {
      const payload = {
        currentId: currentId.value,
        sessions: sessions.value.slice(0, 30).map((s) => ({
          ...s,
          messages: slimMessages(s.messages).slice(-80)
        }))
      }
      localStorage.setItem(storageKey.value, JSON.stringify(payload))
    } catch {
      /* ignore quota */
    }
  }

  function load() {
    let raw = null
    try {
      raw = JSON.parse(localStorage.getItem(storageKey.value) || 'null')
    } catch {
      raw = null
    }
    if (raw?.sessions?.length) {
      sessions.value = raw.sessions
      currentId.value = raw.currentId || raw.sessions[0].id
      const cur = sessions.value.find((s) => s.id === currentId.value) || sessions.value[0]
      currentId.value = cur.id
      messages.value = [...(cur.messages || [welcome])]
      return
    }
    const fresh = createSession([welcome])
    sessions.value = [fresh]
    currentId.value = fresh.id
    messages.value = [...fresh.messages]
    persist()
  }

  function syncCurrentToSession() {
    const idx = sessions.value.findIndex((s) => s.id === currentId.value)
    if (idx < 0) return
    const firstUser = messages.value.find((m) => m.role === 'user')
    sessions.value[idx] = {
      ...sessions.value[idx],
      title: firstUser?.content?.slice(0, 18) || sessions.value[idx].title || '新的对话',
      updatedAt: Date.now(),
      messages: slimMessages(messages.value)
    }
    // 最近使用的置顶
    const [cur] = sessions.value.splice(idx, 1)
    sessions.value.unshift(cur)
    persist()
  }

  function selectSession(id) {
    syncCurrentToSession()
    const target = sessions.value.find((s) => s.id === id)
    if (!target) return
    currentId.value = id
    messages.value = [...(target.messages || [welcome])]
    persist()
  }

  function newSession() {
    syncCurrentToSession()
    const fresh = createSession([welcome])
    sessions.value.unshift(fresh)
    currentId.value = fresh.id
    messages.value = [...fresh.messages]
    persist()
  }

  function clearSession() {
    messages.value = [welcome]
    syncCurrentToSession()
  }

  function removeSession(id) {
    const next = sessions.value.filter((s) => s.id !== id)
    if (!next.length) {
      const fresh = createSession([welcome])
      sessions.value = [fresh]
      currentId.value = fresh.id
      messages.value = [...fresh.messages]
      persist()
      return
    }
    sessions.value = next
    if (currentId.value === id) {
      currentId.value = next[0].id
      messages.value = [...(next[0].messages || [welcome])]
    }
    persist()
  }

  function pushMessage(msg) {
    messages.value.push({
      role: msg.role,
      content: msg.content,
      imageUrl: msg.imageUrl || '',
      products: slimProducts(msg.products),
      streaming: !!msg.streaming
    })
    syncCurrentToSession()
  }

  function patchLastMessage(patch) {
    const last = messages.value[messages.value.length - 1]
    if (!last) return
    Object.assign(last, patch)
    if (patch.products) last.products = slimProducts(patch.products)
    if (!last.streaming) syncCurrentToSession()
  }

  watch(storageKey, () => load(), { immediate: true })

  const historyList = computed(() =>
    sessions.value.map((s) => ({
      id: s.id,
      title: s.title || '新的对话',
      updatedAt: s.updatedAt,
      active: s.id === currentId.value
    }))
  )

  return {
    messages,
    historyList,
    currentId,
    selectSession,
    newSession,
    clearSession,
    removeSession,
    pushMessage,
    patchLastMessage,
    persist,
    syncCurrentToSession
  }
}
