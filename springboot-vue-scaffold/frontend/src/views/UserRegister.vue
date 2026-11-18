<template>
  <div class="register-page">
    <div class="register-card" :class="{ 'is-merchant': form.role === 'MERCHANT' }">
      <section class="visual-side">
        <div class="visual-bg" />
        <div class="visual-mask" />
        <div class="visual-content">
          <span class="visual-badge">欢迎加入</span>
          <h1 class="visual-title">{{ systemStore.displayName }}</h1>
          <p class="visual-desc">注册用户或商家，开启智能电商体验</p>
        </div>
      </section>

      <section class="form-side">
        <div class="form-inner">
          <div v-if="merchantPending" class="pending-box">
            <el-result icon="success" title="注册成功" sub-title="商家账号已提交，请等待平台审核通过后再登录。">
              <template #extra>
                <el-button type="primary" @click="$router.push('/merchant/login')">去商家登录</el-button>
                <el-button @click="$router.push('/user/home')">逛逛商城</el-button>
              </template>
            </el-result>
          </div>

          <template v-else>
            <header class="form-head">
              <BrandLogo size="md" class="brand-logo" />
              <div class="head-copy">
                <h2>创建账号</h2>
                <p>{{ roleHint }}</p>
              </div>
            </header>

            <div class="role-toggle" role="tablist">
              <button
                type="button"
                class="role-btn"
                :class="{ active: form.role === 'USER' }"
                @click="switchRole('USER')"
              >
                我是买家
              </button>
              <button
                type="button"
                class="role-btn"
                :class="{ active: form.role === 'MERCHANT' }"
                @click="switchRole('MERCHANT')"
              >
                我是商家
              </button>
            </div>

            <div v-if="form.role === 'MERCHANT'" class="step-bar">
              <button
                type="button"
                class="step-item"
                :class="{ active: merchantStep === 1, done: merchantStep > 1 }"
                @click="merchantStep = 1"
              >
                <i>1</i>
                <span>账号密码</span>
              </button>
              <span class="step-line" :class="{ active: merchantStep > 1 }" />
              <button
                type="button"
                class="step-item"
                :class="{ active: merchantStep === 2 }"
                @click="goStep2"
              >
                <i>2</i>
                <span>店铺证件</span>
              </button>
            </div>

            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
              class="register-form"
              require-asterisk-position="right"
              @keyup.enter="onEnter"
            >
              <template v-if="form.role === 'USER' || merchantStep === 1">
                <div class="field-row">
                  <el-form-item label="用户名" prop="username">
                    <el-input v-model="form.username" placeholder="3-32 位" size="large" />
                  </el-form-item>
                  <el-form-item label="昵称" prop="nickname">
                    <el-input v-model="form.nickname" placeholder="选填" size="large" />
                  </el-form-item>
                </div>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="form.phone" placeholder="选填" size="large" />
                </el-form-item>
                <div class="field-row">
                  <el-form-item label="密码" prop="password">
                    <el-input
                      v-model="form.password"
                      type="password"
                      placeholder="至少 6 位"
                      size="large"
                      show-password
                    />
                  </el-form-item>
                  <el-form-item label="确认密码" prop="confirmPassword">
                    <el-input
                      v-model="form.confirmPassword"
                      type="password"
                      placeholder="再次输入"
                      size="large"
                      show-password
                    />
                  </el-form-item>
                </div>
              </template>

              <template v-if="form.role === 'MERCHANT' && merchantStep === 2">
                <el-form-item label="店铺名称" prop="shopName">
                  <el-input v-model="form.shopName" placeholder="例如：智购官方旗舰店" size="large" />
                </el-form-item>
                <el-form-item label="店铺简介" prop="shopDesc">
                  <el-input
                    v-model="form.shopDesc"
                    type="textarea"
                    :rows="2"
                    maxlength="120"
                    show-word-limit
                    placeholder="一句话介绍店铺特色（选填）"
                  />
                </el-form-item>

                <div class="docs-block">
                  <div class="docs-head">
                    <strong>入驻证件</strong>
                    <span>审核必填</span>
                  </div>
                  <div class="docs-grid">
                    <el-form-item prop="licenseImage" class="doc-item">
                      <ImageUpload
                        v-model="form.licenseImage"
                        type="cover"
                        width="100%"
                        :height="52"
                        upload-text="营业执照"
                        tip="清晰完整"
                        :clearable="true"
                        :hide-actions="true"
                        :show-success="false"
                      />
                    </el-form-item>
                    <el-form-item prop="idCardFront" class="doc-item">
                      <ImageUpload
                        v-model="form.idCardFront"
                        type="cover"
                        width="100%"
                        :height="52"
                        upload-text="身份证正面"
                        tip="人像面"
                        :clearable="true"
                        :hide-actions="true"
                        :show-success="false"
                      />
                    </el-form-item>
                    <el-form-item prop="idCardBack" class="doc-item">
                      <ImageUpload
                        v-model="form.idCardBack"
                        type="cover"
                        width="100%"
                        :height="52"
                        upload-text="身份证反面"
                        tip="国徽面"
                        :clearable="true"
                        :hide-actions="true"
                        :show-success="false"
                      />
                    </el-form-item>
                  </div>
                </div>
              </template>

              <div class="form-actions">
                <el-button
                  v-if="form.role === 'MERCHANT' && merchantStep === 2"
                  size="large"
                  class="back-btn"
                  @click="merchantStep = 1"
                >
                  上一步
                </el-button>
                <el-button
                  v-if="form.role === 'MERCHANT' && merchantStep === 1"
                  type="primary"
                  size="large"
                  class="submit-btn"
                  @click="goStep2"
                >
                  下一步
                </el-button>
                <el-button
                  v-else
                  type="primary"
                  size="large"
                  class="submit-btn"
                  :loading="loading"
                  @click="handleRegister"
                >
                  {{ form.role === 'MERCHANT' ? '提交入驻申请' : '立即注册' }}
                </el-button>
              </div>
            </el-form>

            <p class="switch-tip">
              已有账号？
              <router-link :to="form.role === 'MERCHANT' ? '/merchant/login' : '/login'">返回登录</router-link>
            </p>
          </template>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import BrandLogo from '@/components/BrandLogo.vue'
import ImageUpload from '@/components/ImageUpload.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const systemStore = useSystemStore()
const formRef = ref()
const loading = ref(false)
const merchantPending = ref(false)
const merchantStep = ref(1)

const form = reactive({
  role: 'USER',
  username: '',
  nickname: '',
  phone: '',
  shopName: '',
  shopDesc: '',
  licenseImage: '',
  idCardFront: '',
  idCardBack: '',
  password: '',
  confirmPassword: ''
})

onMounted(() => {
  const role = String(route.query.role || '').toUpperCase()
  if (role === 'MERCHANT') form.role = 'MERCHANT'
})

watch(
  () => form.role,
  () => {
    merchantStep.value = 1
  }
)

const roleHint = computed(() => {
  if (form.role !== 'MERCHANT') return '创建买家账号，即可购物与收藏'
  return merchantStep.value === 1 ? '先设置账号与登录密码' : '填写店铺信息并上传入驻证件'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 32, message: '用户名长度为 3-32 位', trigger: 'blur' }
  ],
  phone: [{ pattern: /^1\d{10}$|^$/, message: '请输入正确的手机号', trigger: 'blur' }],
  shopName: [
    {
      validator: (_r, v, cb) => {
        if (form.role === 'MERCHANT' && merchantStep.value === 2 && !String(v || '').trim()) {
          cb(new Error('请填写店铺名称'))
        } else cb()
      },
      trigger: 'blur'
    }
  ],
  licenseImage: [
    {
      validator: (_r, v, cb) => {
        if (form.role === 'MERCHANT' && merchantStep.value === 2 && !String(v || '').trim()) {
          cb(new Error('请上传营业执照'))
        } else cb()
      },
      trigger: 'change'
    }
  ],
  idCardFront: [
    {
      validator: (_r, v, cb) => {
        if (form.role === 'MERCHANT' && merchantStep.value === 2 && !String(v || '').trim()) {
          cb(new Error('请上传身份证正面'))
        } else cb()
      },
      trigger: 'change'
    }
  ],
  idCardBack: [
    {
      validator: (_r, v, cb) => {
        if (form.role === 'MERCHANT' && merchantStep.value === 2 && !String(v || '').trim()) {
          cb(new Error('请上传身份证反面'))
        } else cb()
      },
      trigger: 'change'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次输入的密码不一致'))
        else callback()
      },
      trigger: 'blur'
    }
  ]
}

function switchRole(role) {
  form.role = role
  merchantStep.value = 1
}

async function goStep2() {
  await formRef.value.validateField(['username', 'password', 'confirmPassword', 'phone'])
  merchantStep.value = 2
}

function onEnter() {
  if (form.role === 'MERCHANT' && merchantStep.value === 1) goStep2()
  else handleRegister()
}

async function handleRegister() {
  await formRef.value.validate()
  loading.value = true
  try {
    const payload = {
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || undefined,
      phone: form.phone.trim() || undefined,
      role: form.role,
      accountType: form.role
    }
    if (form.role === 'MERCHANT') {
      payload.shopName = form.shopName.trim()
      payload.shopDesc = form.shopDesc.trim() || undefined
      payload.licenseImage = form.licenseImage
      payload.idCardFront = form.idCardFront
      payload.idCardBack = form.idCardBack
    }
    const data = await userStore.register(payload)
    if (form.role === 'MERCHANT' && (data?.pendingAudit || !data?.token)) {
      merchantPending.value = true
      ElMessage.success('注册成功，等待审核')
      return
    }
    ElMessage.success('注册成功')
    router.push(form.role === 'MERCHANT' ? '/merchant/home' : '/user/home')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  --portal: #e1251b;
  --portal-deep: #c9181a;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  background:
    radial-gradient(ellipse 70% 55% at 12% 18%, rgba(225, 37, 27, 0.14), transparent 58%),
    radial-gradient(ellipse 55% 50% at 88% 82%, rgba(15, 55, 70, 0.08), transparent 55%),
    linear-gradient(155deg, #f6f3f2 0%, #f7f8fa 42%, #eceff3 100%);
  font-family: 'DM Sans', 'Noto Sans SC', sans-serif;
}

.register-card {
  display: grid;
  grid-template-columns: 1fr 1fr;
  width: min(920px, 100%);
  min-height: 520px;
  max-height: min(640px, calc(100vh - 40px));
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
  min-width: 0;
  min-height: 100%;
  overflow: hidden;
}

.visual-bg {
  position: absolute;
  inset: 0;
  background: url('https://images.unsplash.com/photo-1441986300917-64674bd600d8?auto=format&fit=crop&w=1400&q=80')
    center / cover no-repeat;
}

.visual-mask {
  position: absolute;
  inset: 0;
  background: linear-gradient(160deg, rgba(28, 10, 10, 0.32) 0%, rgba(20, 8, 8, 0.78) 100%);
}

.visual-content {
  position: relative;
  z-index: 1;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding: 36px 32px;
  color: #fff;
}

.visual-badge {
  align-self: flex-start;
  margin-bottom: 12px;
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 650;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.visual-title {
  margin: 0 0 8px;
  font-size: 30px;
  font-weight: 760;
  letter-spacing: -0.02em;
  line-height: 1.2;
}

.visual-desc {
  margin: 0;
  max-width: 280px;
  font-size: 14px;
  line-height: 1.55;
  color: rgba(255, 255, 255, 0.84);
}

.form-side {
  min-width: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 36px 36px 40px;
  background: #fff;
}

.form-inner {
  width: 100%;
  max-height: 100%;
  overflow-y: auto;
  scrollbar-width: thin;
  scrollbar-color: #d6dbe3 transparent;
}

.form-head {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 28px;
}

.brand-logo {
  box-shadow: 0 4px 14px rgba(225, 37, 27, 0.28);
}

.head-copy h2 {
  margin: 0 0 4px;
  font-size: 20px;
  font-weight: 760;
  color: #1f2937;
}

.head-copy p {
  margin: 0;
  font-size: 13px;
  color: #9ca3af;
}

.role-toggle {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
  margin-bottom: 28px;
  padding: 4px;
  border-radius: 12px;
  background: #f5f6f8;
}

.role-btn {
  height: 34px;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: #6b7280;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  font-family: inherit;
}

.role-btn.active {
  background: #fff;
  color: #e1251b;
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.08);
}

.step-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 22px;
}

.step-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  font-family: inherit;
  color: #94a3b8;
}

.step-item i {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: grid;
  place-items: center;
  font-style: normal;
  font-size: 12px;
  font-weight: 700;
  background: #eef2f7;
  color: #94a3b8;
}

.step-item span {
  font-size: 12px;
  font-weight: 650;
}

.step-item.active,
.step-item.done {
  color: #e1251b;
}

.step-item.active i,
.step-item.done i {
  background: #e1251b;
  color: #fff;
}

.step-line {
  flex: 1;
  height: 2px;
  border-radius: 999px;
  background: #eef2f7;
}

.step-line.active {
  background: rgba(225, 37, 27, 0.35);
}

.field-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.register-form :deep(.el-form-item__label) {
  margin-bottom: 4px !important;
  padding: 0 !important;
  line-height: 1.2;
  font-size: 12px;
  font-weight: 650;
  color: #64748b;
}

.register-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 4px 12px;
  background: #f7f8fa;
  box-shadow: 0 0 0 1px #e8ecf0 inset;
}

.register-form :deep(.el-input__wrapper.is-focus) {
  background: #fff;
  box-shadow: 0 0 0 2px rgba(225, 37, 27, 0.28) inset;
}

.register-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  background: #f8fafc;
  box-shadow: 0 0 0 1px #e8edf2 inset;
  padding: 10px 12px;
}

.docs-block {
  margin: 2px 0 4px;
  padding: 10px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #eef2f7;
}

.docs-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.docs-head strong {
  font-size: 12px;
  font-weight: 700;
  color: #334155;
}

.docs-head span {
  font-size: 11px;
  font-weight: 650;
  color: #e1251b;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(225, 37, 27, 0.08);
}

.docs-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.doc-item {
  margin-bottom: 0 !important;
}

.doc-item :deep(.image-upload),
.doc-item :deep(.el-upload) {
  width: 100%;
  display: block;
}

.doc-item :deep(.cover-box) {
  width: 100% !important;
  height: 52px !important;
  min-height: 52px !important;
  max-height: 52px !important;
  border-radius: 10px;
  border: 1px dashed #d7dee8;
  background: #fff;
  box-shadow: none;
}

.doc-item :deep(.cover-box:hover),
.doc-item :deep(.cover-box.is-empty:hover) {
  border-color: rgba(225, 37, 27, 0.4);
  background: #fffafa;
}

.doc-item :deep(.cover-empty) {
  display: grid;
  grid-template-columns: 22px 1fr;
  grid-template-rows: auto auto;
  align-items: center;
  column-gap: 8px;
  row-gap: 1px;
  padding: 0 10px;
  text-align: left;
}

.doc-item :deep(.cover-plus) {
  grid-row: 1 / 3;
  grid-column: 1;
  flex-shrink: 0;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: rgba(225, 37, 27, 0.1);
  color: #e1251b;
  border: none;
  box-shadow: none;
}

.doc-item :deep(.cover-plus .el-icon) {
  font-size: 14px;
}

.doc-item :deep(.cover-title) {
  grid-column: 2;
  grid-row: 1;
  font-size: 11px;
  font-weight: 700;
  color: #475569;
  line-height: 1.2;
}

.doc-item :deep(.cover-hint) {
  grid-column: 2;
  grid-row: 2;
  font-size: 10px;
  color: #94a3b8;
  line-height: 1.2;
}

.doc-item :deep(.el-form-item__error) {
  position: static;
  text-align: center;
  padding-top: 4px;
  font-size: 11px;
}

.form-actions {
  display: flex;
  gap: 10px;
  margin-top: 6px;
}

.back-btn {
  flex: 0 0 auto;
  min-width: 96px;
  height: 44px;
  border-radius: 10px;
}

.submit-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.register-form :deep(.submit-btn.el-button--primary) {
  --el-button-bg-color: #e1251b;
  --el-button-border-color: #e1251b;
  --el-button-hover-bg-color: #c9181a;
  --el-button-hover-border-color: #c9181a;
  background: linear-gradient(135deg, #e63935 0%, #c9181a 100%);
  border-color: #e63935;
  box-shadow: 0 8px 18px rgba(225, 37, 27, 0.28);
}

.switch-tip {
  margin: 14px 0 0;
  text-align: center;
  font-size: 13px;
  color: #b0b8c4;
}

.switch-tip a {
  color: #e1251b;
  text-decoration: none;
  font-weight: 650;
}

.pending-box {
  padding: 40px 8px;
}

@media (max-width: 860px) {
  .register-card,
  .register-card.is-merchant {
    grid-template-columns: 1fr;
    min-height: auto;
    max-height: none;
  }

  .visual-side {
    min-height: 200px;
  }

  .visual-content {
    padding: 28px 22px;
  }

  .visual-title {
    font-size: 24px;
  }

  .form-side {
    padding: 28px 22px 36px;
  }

  .field-row {
    grid-template-columns: 1fr;
    gap: 0;
  }
}
</style>
