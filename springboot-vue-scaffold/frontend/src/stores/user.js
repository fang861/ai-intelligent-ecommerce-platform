import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import request from '@/api/request'

function resolveAccountType(data) {
  if (data?.accountType) return data.accountType
  if (data?.role === 'ADMIN') return 'ADMIN'
  if (data?.role === 'MERCHANT') return 'MERCHANT'
  return 'USER'
}

export const useUserStore = defineStore('user', () => {
  const user = ref(null)
  const token = ref(localStorage.getItem('token') || '')

  const isLoggedIn = computed(() => !!token.value)
  const displayName = computed(() => user.value?.nickname || user.value?.username || '同学')
  const isAdmin = computed(() => user.value?.accountType === 'ADMIN' || user.value?.role === 'ADMIN')
  const isMerchant = computed(
    () => user.value?.accountType === 'MERCHANT' || user.value?.role === 'MERCHANT'
  )
  const isUser = computed(
    () => !isAdmin.value && !isMerchant.value && (!!user.value || !!token.value)
  )

  async function login(form) {
    const data = await request.post('/auth/login', form)
    applySession(data)
    return data
  }

  async function register(form) {
    const data = await request.post('/auth/register', form)
    // 商家注册：等待审核，后端可能不返回 token
    if (form.role === 'MERCHANT' || form.accountType === 'MERCHANT') {
      if (!data?.token) {
        return { ...data, pendingAudit: true }
      }
    }
    if (data?.token) {
      applySession(data)
    }
    return data
  }

  function applySession(data) {
    if (!data?.token) return
    token.value = data.token
    localStorage.setItem('token', data.token)
    const accountType = resolveAccountType(data)
    user.value = {
      id: data.userId ?? data.id,
      username: data.username,
      nickname: data.nickname,
      role: data.role || accountType,
      avatar: data.avatar,
      balance: data.balance,
      shopName: data.shopName,
      shopDesc: data.shopDesc,
      accountType
    }
  }

  async function fetchUser() {
    const profile = await request.get('/auth/me', { skipAuthRedirect: true, silent: true })
    const accountType = resolveAccountType(profile)
    user.value = {
      ...profile,
      role: profile.role || accountType,
      accountType
    }
    return user.value
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return {
    user,
    token,
    isLoggedIn,
    displayName,
    isAdmin,
    isMerchant,
    isUser,
    login,
    register,
    applySession,
    fetchUser,
    logout
  }
})
