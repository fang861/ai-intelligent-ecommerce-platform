<template>
  <div class="login-page portal-user">
    <div class="login-card">
      <section class="visual-side">
        <div class="visual-bg" />
        <div class="visual-mask" />
        <div class="visual-content">
          <span class="portal-badge">用户端</span>
          <h1 class="visual-title">{{ systemStore.displayName }}</h1>
          <p class="visual-desc">精选好物 · AI 智能导购 · 安心下单</p>
        </div>
      </section>

      <section class="form-side">
        <div class="form-inner">
          <div class="portal-tabs">
            <router-link class="tab active" to="/login">用户端</router-link>
            <router-link class="tab" to="/merchant/login">商家端</router-link>
            <router-link class="tab" to="/admin/login">管理端</router-link>
          </div>

          <div class="form-brand">
            <BrandLogo size="lg" class="login-brand-logo" />
            <div class="brand-text">
              <h2>用户登录</h2>
              <p>登录后可购物、收藏与 AI 导购</p>
            </div>
          </div>

          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-width="0"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>

          <p class="demo-tip">演示账号 user / 123456</p>
          <p class="switch-tip">
            还没有账号？
            <router-link to="/register">立即注册</router-link>
            ·
            <router-link to="/user/home">先逛逛商城</router-link>
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import BrandLogo from '@/components/BrandLogo.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const systemStore = useSystemStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'user',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

function accountTypeOf(data) {
  return data?.accountType || data?.role || ''
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await userStore.login(form)
    const type = accountTypeOf(data)
    if (type === 'ADMIN') {
      userStore.logout()
      ElMessage.error('管理员请使用管理端登录')
      return
    }
    if (type === 'MERCHANT') {
      userStore.logout()
      ElMessage.error('商家请使用商家端登录')
      return
    }
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    router.push(redirect && redirect.startsWith('/user') ? redirect : '/user/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --portal: #e1251b;
  --portal-deep: #c9181a;
  position: relative;
  isolation: isolate;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  overflow: hidden;
  background:
    radial-gradient(ellipse 70% 55% at 12% 18%, rgba(225, 37, 27, 0.14), transparent 58%),
    radial-gradient(ellipse 55% 50% at 88% 82%, rgba(15, 55, 70, 0.08), transparent 55%),
    linear-gradient(155deg, #f6f3f2 0%, #f7f8fa 42%, #eceff3 100%);
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
}

.login-card {
  position: relative;
  display: flex;
  width: 920px;
  max-width: 100%;
  min-height: 520px;
  border-radius: 18px;
  overflow: hidden;
  background: #fff;
  border: 1px solid rgba(255, 255, 255, 0.7);
  box-shadow:
    0 4px 6px rgba(15, 23, 42, 0.04),
    0 18px 48px rgba(15, 23, 42, 0.12);
}

.visual-side {
  position: relative;
  flex: 1;
  min-height: 520px;
  overflow: hidden;
}

.visual-bg {
  position: absolute;
  inset: 0;
  background: url('https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1200&q=80')
    center / cover no-repeat;
}

.visual-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(26, 10, 10, 0.28) 0%, rgba(180, 20, 20, 0.72) 100%);
}

.visual-content {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  text-align: center;
}

.portal-badge {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  margin-bottom: 16px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.28);
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
}

.visual-title {
  margin: 0 0 12px;
  font-size: 34px;
  font-weight: 780;
  color: #fff;
  letter-spacing: 0.04em;
}

.visual-desc {
  margin: 0;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.88);
}

.form-side {
  width: 420px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px 36px 40px;
  background: #fff;
}

.form-inner {
  width: 100%;
}

.portal-tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 28px;
  padding: 4px;
  border-radius: 12px;
  background: #f5f6f8;
}

.portal-tabs .tab {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 34px;
  border-radius: 9px;
  color: #6b7280;
  text-decoration: none;
  font-size: 13px;
  font-weight: 650;
  transition: background 0.15s ease, color 0.15s ease, box-shadow 0.15s ease;
}

.portal-tabs .tab:hover {
  color: var(--portal);
}

.portal-tabs .tab.active {
  color: #fff;
  background: var(--portal);
  box-shadow: 0 6px 14px rgba(225, 37, 27, 0.25);
}

.form-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.login-brand-logo {
  box-shadow: 0 4px 14px rgba(225, 37, 27, 0.28);
}

.brand-text h2 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 760;
  color: #1f2937;
}

.brand-text p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  background: #f7f8fa;
  box-shadow: 0 0 0 1px #e8ecf0 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(225, 37, 27, 0.28) inset;
}

.submit-btn {
  width: 100%;
  margin-top: 6px;
  height: 44px;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 4px;
  border: none;
}

.login-form :deep(.submit-btn.el-button--primary) {
  --el-button-bg-color: #e1251b;
  --el-button-border-color: #e1251b;
  --el-button-hover-bg-color: #c9181a;
  --el-button-hover-border-color: #c9181a;
  background: linear-gradient(135deg, #e63935 0%, #c9181a 100%);
  box-shadow: 0 8px 18px rgba(225, 37, 27, 0.28);
}

.demo-tip,
.switch-tip {
  margin: 14px 0 0;
  text-align: center;
  font-size: 13px;
  color: #b0b8c4;
}

.demo-tip {
  margin-top: 24px;
}

.switch-tip a {
  color: var(--portal);
  text-decoration: none;
  font-weight: 650;
}

@media (max-width: 820px) {
  .login-card {
    flex-direction: column;
    min-height: auto;
  }

  .visual-side {
    min-height: 200px;
  }

  .form-side {
    width: 100%;
    padding: 28px 22px 36px;
  }
}
</style>
