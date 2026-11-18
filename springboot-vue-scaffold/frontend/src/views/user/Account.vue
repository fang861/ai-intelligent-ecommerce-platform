<template>
  <div class="page" v-loading="loading">
    <header class="id-bar">
      <div class="id-left">
        <div class="avatar-wrap">
          <ImageUpload
            v-model="form.avatar"
            type="avatar"
            :avatar-size="64"
            upload-text="换头像"
            :fallback="avatarFallback"
            :clearable="false"
            :show-success="false"
            @success="onAvatarUploaded"
          />
        </div>
        <div class="id-copy">
          <div class="name-row">
            <h2>{{ form.nickname || form.username || '用户' }}</h2>
            <span class="member">普通会员</span>
          </div>
          <p>
            <em>{{ form.username || '-' }}</em>
            <i v-if="form.phone" />
            <span v-if="form.phone">{{ form.phone }}</span>
          </p>
        </div>
      </div>
      <p class="id-tip">头像上传后自动保存，资料与密码请在下方分别提交</p>
    </header>

    <div class="shell">
      <section class="block">
        <header class="block-head">
          <h3>基础资料</h3>
          <span>账号不可修改，可更新昵称与手机号</span>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="88px"
          class="info-form"
          @submit.prevent
        >
          <el-form-item label="账号">
            <el-input :model-value="form.username" disabled />
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" maxlength="32" show-word-limit placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" maxlength="11" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item>
            <button type="button" class="btn primary" :disabled="submitting" @click="handleSubmit">
              {{ submitting ? '保存中…' : '保存资料' }}
            </button>
          </el-form-item>
        </el-form>
      </section>

      <section class="block">
        <header class="block-head">
          <h3>修改密码</h3>
          <span>定期更换密码，保障账号安全</span>
        </header>

        <el-form
          ref="pwdRef"
          :model="pwdForm"
          :rules="pwdRules"
          label-width="88px"
          class="info-form"
          @submit.prevent
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <button type="button" class="btn" :disabled="pwdSubmitting" @click="handlePasswordSubmit">
              {{ pwdSubmitting ? '更新中…' : '更新密码' }}
            </button>
          </el-form-item>
        </el-form>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/api/request'
import { useUserStore } from '@/stores/user'
import ImageUpload from '@/components/ImageUpload.vue'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const pwdSubmitting = ref(false)
const formRef = ref()
const pwdRef = ref()

const form = reactive({
  username: '',
  nickname: '',
  phone: '',
  avatar: ''
})
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const avatarFallback = computed(() => (form.nickname || form.username || 'U').slice(0, 1).toUpperCase())

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$|^$/, message: '请输入正确的手机号', trigger: 'blur' }]
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '至少 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (_r, v, cb) => {
        if (!v) cb(new Error('请确认密码'))
        else if (v !== pwdForm.newPassword) cb(new Error('两次密码不一致'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

async function load() {
  loading.value = true
  try {
    const user = await userStore.fetchUser()
    Object.assign(form, {
      username: user?.username || '',
      nickname: user?.nickname || '',
      phone: user?.phone || '',
      avatar: user?.avatar || ''
    })
  } finally {
    loading.value = false
  }
}

async function onAvatarUploaded() {
  await request.put('/auth/profile', {
    avatar: form.avatar,
    nickname: form.nickname,
    phone: form.phone
  })
  await userStore.fetchUser()
  ElMessage.success('头像已更新')
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    await request.put('/auth/profile', {
      nickname: form.nickname,
      phone: form.phone,
      avatar: form.avatar
    })
    await userStore.fetchUser()
    ElMessage.success('资料已保存')
  } finally {
    submitting.value = false
  }
}

async function handlePasswordSubmit() {
  await pwdRef.value.validate()
  pwdSubmitting.value = true
  try {
    await request.post('/auth/password', {
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    ElMessage.success('密码已更新')
  } finally {
    pwdSubmitting.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.page {
  --accent: #e1251b;
  --ink: #1a1a1a;
  --muted: #8b929c;
  --line: #ececec;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.id-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 14px;
  padding: 18px 20px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--line);
}

.id-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.avatar-wrap { flex-shrink: 0; }

.id-copy { min-width: 0; }
.name-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.name-row h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 720;
  line-height: 1.2;
}
.member {
  height: 22px;
  padding: 0 8px;
  border-radius: 4px;
  background: #f5f5f5;
  color: #666;
  font-size: 12px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
}
.id-copy p {
  margin: 0;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--muted);
}
.id-copy em {
  font-style: normal;
  color: #666;
}
.id-copy i {
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #d1d5db;
  font-style: normal;
}
.id-tip {
  margin: 0;
  max-width: 220px;
  text-align: right;
  font-size: 12px;
  line-height: 1.6;
  color: #a8b0bb;
}

.shell {
  border-radius: 12px;
  background: #fff;
  border: 1px solid var(--line);
  overflow: hidden;
}

.block {
  padding: 22px 24px 8px;
}
.block + .block {
  border-top: 1px solid #f0f0f0;
}

.block-head {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 10px;
  margin-bottom: 18px;
  padding-left: 10px;
  border-left: 3px solid var(--accent);
}
.block-head h3 {
  margin: 0;
  font-size: 15px;
  font-weight: 720;
}
.block-head span {
  font-size: 12px;
  color: var(--muted);
}

.info-form {
  max-width: 520px;
}
.info-form :deep(.el-form-item) {
  margin-bottom: 18px;
}
.info-form :deep(.el-form-item__label) {
  color: #666;
  font-weight: 600;
}
.info-form :deep(.el-input__wrapper) {
  border-radius: 6px;
  min-height: 38px;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
  background: #fff;
}
.info-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #d1d5db inset !important;
}
.info-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--accent) inset !important;
}
.info-form :deep(.el-input.is-disabled .el-input__wrapper) {
  background: #f7f8fa;
}

.btn {
  height: 36px;
  padding: 0 18px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: #fff;
  color: #333;
  font-size: 13px;
  font-weight: 650;
  cursor: pointer;
  transition: 0.15s ease;
}
.btn:hover:not(:disabled) {
  border-color: #bbb;
  color: #111;
}
.btn.primary {
  border-color: var(--accent);
  background: var(--accent);
  color: #fff;
}
.btn.primary:hover:not(:disabled) {
  background: #c81e16;
  border-color: #c81e16;
}
.btn:disabled {
  opacity: 0.65;
  cursor: wait;
}

@media (max-width: 720px) {
  .id-bar {
    flex-direction: column;
    align-items: flex-start;
  }
  .id-tip {
    max-width: none;
    text-align: left;
  }
  .block { padding: 18px 16px 4px; }
  .info-form { max-width: none; }
}
</style>
