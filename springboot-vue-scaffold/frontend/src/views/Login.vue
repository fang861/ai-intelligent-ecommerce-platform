<template>
  <div class="login-page portal-admin">
    <div class="login-card">
      <section class="visual-side">
        <div class="visual-bg" />
        <div class="visual-mask" />
        <div class="visual-content">
          <span class="portal-badge">管理端</span>
          <h1 class="visual-title">平台管理中心</h1>
          <p class="visual-desc">商品审核 · 订单监管 · 系统配置</p>
        </div>
      </section>

      <section class="form-side">
        <div class="form-inner">
          <div class="portal-tabs">
            <router-link class="tab" to="/login">用户端</router-link>
            <router-link class="tab" to="/merchant/login">商家端</router-link>
            <router-link class="tab active" to="/admin/login">管理端</router-link>
          </div>

          <div class="form-brand">
            <BrandLogo size="lg" class="login-brand-logo" />
            <div class="brand-text">
              <h2>管理员登录</h2>
              <p>仅限平台管理员账号进入</p>
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
              <el-input
                v-model="form.username"
                placeholder="管理员账号"
                prefix-icon="User"
                size="large"
              />
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

          <p class="demo-tip">演示账号 admin / 123456</p>
          <p class="switch-tip">
            普通用户请前往
            <router-link to="/login">用户登录</router-link>
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import BrandLogo from '@/components/BrandLogo.vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await userStore.login(form)
    if (data?.accountType !== 'ADMIN' && data?.role !== 'ADMIN') {
      userStore.logout()
      ElMessage.error('仅管理员账号可登录管理端')
      return
    }
    ElMessage.success('登录成功')
    router.push('/admin/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --portal: #334155;
  --portal-deep: #1e293b;
  position: relative;
  isolation: isolate;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  overflow: hidden;
  background:
    radial-gradient(ellipse 70% 55% at 12% 18%, rgba(51, 65, 85, 0.18), transparent 58%),
    radial-gradient(ellipse 55% 50% at 88% 82%, rgba(225, 37, 27, 0.08), transparent 55%),
    linear-gradient(155deg, #e8eef5 0%, #f1f5f9 42%, #e2e8f0 100%);
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
    0 18px 48px rgba(30, 41, 59, 0.14);
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
  background: url('https://images.unsplash.com/photo-1497366216548-37526070297c?auto=format&fit=crop&w=1200&q=80')
    center / cover no-repeat;
}

.visual-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.45) 0%, rgba(30, 41, 59, 0.88) 100%);
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
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.22);
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
  color: rgba(255, 255, 255, 0.86);
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
  background: #f1f5f9;
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
  background: var(--portal-deep);
  box-shadow: 0 6px 14px rgba(30, 41, 59, 0.28);
}

.form-brand {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.login-brand-logo {
  box-shadow: 0 4px 14px rgba(30, 41, 59, 0.22);
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
  background: #f8fafc;
  box-shadow: 0 0 0 1px #e2e8f0 inset;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(51, 65, 85, 0.28) inset;
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
  --el-button-bg-color: #1e293b;
  --el-button-border-color: #1e293b;
  --el-button-hover-bg-color: #334155;
  --el-button-hover-border-color: #334155;
  background: linear-gradient(135deg, #475569 0%, #1e293b 100%);
  box-shadow: 0 8px 18px rgba(30, 41, 59, 0.28);
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
  color: #e1251b;
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
