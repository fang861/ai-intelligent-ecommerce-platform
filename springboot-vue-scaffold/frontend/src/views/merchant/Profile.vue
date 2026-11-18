<template>
  <div class="page profile-page" v-loading="loading">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>店铺资料</span>
          <el-button type="primary" class="save-btn" :loading="submitting" @click="save">保存资料</el-button>
        </div>
      </template>
      <p class="page-desc">维护店铺对外展示信息，买家会在店铺页看到这些内容</p>

      <div class="profile-grid">
      <aside class="identity-card">
        <div class="identity-badge">店铺形象</div>
        <ImageUpload
          v-model="form.avatar"
          type="avatar"
          :avatar-size="108"
          upload-text="更换头像"
          :fallback="form.shopName || '店'"
          :clearable="false"
          :show-success="false"
        />
        <h3>{{ form.shopName || '未命名店铺' }}</h3>
        <p class="identity-desc">{{ form.shopDesc || '暂无店铺简介' }}</p>
        <ul class="identity-meta">
          <li>
            <span>联系电话</span>
            <strong>{{ form.phone || '未填写' }}</strong>
          </li>
          <li>
            <span>对外昵称</span>
            <strong>{{ form.nickname || '未填写' }}</strong>
          </li>
        </ul>
      </aside>

      <section class="form-card">
        <div class="section-title">基本信息</div>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
          <el-form-item label="店铺名称" prop="shopName">
            <el-input v-model="form.shopName" maxlength="40" show-word-limit placeholder="例如：智购官方旗舰店" />
          </el-form-item>
          <el-form-item label="店铺简介">
            <el-input
              v-model="form.shopDesc"
              type="textarea"
              :rows="4"
              maxlength="200"
              show-word-limit
              placeholder="一句话介绍店铺特色，建议 20～60 字"
            />
          </el-form-item>
          <div class="form-row">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" maxlength="11" placeholder="11 位手机号" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="form.nickname" maxlength="20" placeholder="商家账号展示名" />
            </el-form-item>
          </div>
          <div class="form-actions">
            <el-button type="primary" class="save-btn" :loading="submitting" @click="save">保存资料</el-button>
            <el-button @click="load">重置</el-button>
          </div>
        </el-form>
      </section>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import ImageUpload from '@/components/ImageUpload.vue'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const form = reactive({
  shopName: '',
  shopDesc: '',
  phone: '',
  nickname: '',
  avatar: ''
})

const rules = {
  shopName: [{ required: true, message: '请填写店铺名称', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const user = await userStore.fetchUser()
    Object.assign(form, {
      shopName: user.shopName || '',
      shopDesc: user.shopDesc || '',
      phone: user.phone || '',
      nickname: user.nickname || '',
      avatar: user.avatar || ''
    })
  } finally {
    loading.value = false
  }
}

async function save() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = await request.put('/merchant/profile', { ...form })
    userStore.user = { ...userStore.user, ...data, accountType: 'MERCHANT', role: 'MERCHANT' }
    ElMessage.success('已保存')
  } finally {
    submitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.profile-page {
  --ink: #1f2937;
  --muted: #64748b;
  --line: rgba(15, 23, 42, 0.08);
  --accent: var(--app-primary, #3b82f6);
  --accent-deep: var(--el-color-primary-dark-2, #0f766e);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-desc {
  margin: 0 0 16px;
  font-size: 13px;
  color: var(--muted);
}

.save-btn {
  --el-button-bg-color: var(--accent);
  --el-button-border-color: var(--accent);
  --el-button-hover-bg-color: var(--accent-deep);
  --el-button-hover-border-color: var(--accent-deep);
  --el-button-active-bg-color: #115e59;
  --el-button-active-border-color: #115e59;
  border-radius: 999px !important;
  padding: 0 20px;
  height: 36px;
  font-weight: 650;
}

.profile-grid {
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.identity-card,
.form-card {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: 12px;
  box-shadow: 0 8px 20px rgba(15, 118, 110, 0.04);
}

.identity-card {
  padding: 28px 22px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  background:
    linear-gradient(180deg, rgba(13, 148, 136, 0.08) 0%, transparent 42%),
    #fff;
}

.identity-badge {
  align-self: flex-start;
  margin-bottom: 18px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(13, 148, 136, 0.12);
  color: var(--accent-deep);
  font-size: 12px;
  font-weight: 700;
  display: inline-flex;
  align-items: center;
}

.identity-card :deep(.image-upload),
.identity-card :deep(.avatar-wrap) {
  margin: 0 auto;
}

.identity-card h3 {
  margin: 16px 0 8px;
  font-size: 18px;
  font-weight: 740;
  color: var(--ink);
  line-height: 1.35;
  word-break: break-word;
}

.identity-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--muted);
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.identity-meta {
  width: 100%;
  margin: 20px 0 0;
  padding: 0;
  list-style: none;
  border-top: 1px solid var(--line);
}

.identity-meta li {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
  text-align: left;
}

.identity-meta li:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.identity-meta span {
  font-size: 12px;
  color: #94a3b8;
  flex-shrink: 0;
}

.identity-meta strong {
  font-size: 13px;
  font-weight: 650;
  color: var(--ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.form-card {
  padding: 22px 24px 20px;
}

.section-title {
  margin: 0 0 18px;
  padding-left: 12px;
  position: relative;
  font-size: 15px;
  font-weight: 700;
  color: var(--ink);
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 14px;
  border-radius: 2px;
  background: var(--accent);
}

.profile-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.profile-form :deep(.el-form-item__label) {
  margin-bottom: 6px !important;
  padding: 0 !important;
  line-height: 1.4 !important;
  height: auto !important;
  font-weight: 650;
  color: #334155;
}

.profile-form :deep(.el-input__wrapper),
.profile-form :deep(.el-textarea__inner) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px rgba(15, 23, 42, 0.08) inset;
}

.profile-form :deep(.el-input__wrapper.is-focus),
.profile-form :deep(.el-textarea__inner:focus) {
  box-shadow:
    0 0 0 1px color-mix(in srgb, var(--accent) 55%, transparent) inset,
    0 0 0 3px rgba(13, 148, 136, 0.12) !important;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0 16px;
}

.form-actions {
  display: flex;
  gap: 10px;
  padding-top: 8px;
  border-top: 1px solid #f1f5f9;
  margin-top: 4px;
}

@media (max-width: 900px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
