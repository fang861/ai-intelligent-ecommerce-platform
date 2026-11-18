<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>系统设置</span>
          <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
        </div>
      </template>
      <el-form label-width="88px" style="max-width: 680px" @submit.prevent>
        <el-form-item label="商城名称">
          <el-input
            v-model="form.systemName"
            maxlength="40"
            show-word-limit
            placeholder="显示在前台与后台的商城名称"
            clearable
          />
          <p class="field-tip">修改后将同步到导航栏、登录页、页脚与浏览器标题</p>
        </el-form-item>

        <el-form-item label="商城 Logo">
          <div class="logo-field">
            <ImageUpload
              v-model="form.systemLogo"
              type="avatar"
              :avatar-size="72"
              upload-text="上传 Logo"
              fallback="N"
              :placeholder-src="defaultLogoSrc"
              :clearable="false"
              :show-success="false"
            />
            <div class="logo-actions">
              <p class="field-tip">建议上传正方形图片（PNG / JPG / WebP / SVG），将同步到侧栏、顶栏、登录页与浏览器图标</p>
              <button
                v-if="form.systemLogo"
                type="button"
                class="reset-logo-btn"
                @click="resetLogo"
              >
                <el-icon><RefreshLeft /></el-icon>
                <span>恢复默认 Logo</span>
              </button>
            </div>
          </div>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft } from '@element-plus/icons-vue'
import { useSystemStore } from '@/stores/system'
import ImageUpload from '@/components/ImageUpload.vue'

const systemStore = useSystemStore()
const saving = ref(false)
const defaultLogoSrc = '/logo.svg'

const form = reactive({
  systemName: systemStore.displayName,
  systemLogo: systemStore.systemLogo || ''
})

async function syncForm() {
  await systemStore.fetchConfig()
  form.systemName = systemStore.displayName
  form.systemLogo = systemStore.systemLogo || ''
}

async function resetLogo() {
  await ElMessageBox.confirm('确定恢复为系统默认 Logo 吗？保存设置后生效。', '恢复默认 Logo', {
    type: 'warning',
    confirmButtonText: '恢复默认',
    cancelButtonText: '取消'
  })
  form.systemLogo = ''
  ElMessage.success('已切换为默认 Logo，请点击保存设置')
}

async function handleSave() {
  const name = String(form.systemName || '').trim()
  if (!name) {
    ElMessage.warning('请输入商城名称')
    return
  }
  saving.value = true
  try {
    await systemStore.saveConfig({
      systemName: name,
      systemLogo: form.systemLogo || ''
    })
    form.systemName = systemStore.displayName
    form.systemLogo = systemStore.systemLogo || ''
    ElMessage.success('系统设置已更新')
  } finally {
    saving.value = false
  }
}

onMounted(syncForm)
</script>

<style scoped>
.panel {
  border: 1px solid #e5e7eb;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.field-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #94a3b8;
  line-height: 1.5;
}

.logo-field {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.logo-actions {
  flex: 1;
  min-width: 0;
  padding-top: 4px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
}

.reset-logo-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 12px;
  border-radius: 10px;
  border: 1px solid rgba(15, 23, 42, 0.1);
  background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
  color: #475569;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
  transition: border-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease, background 0.2s ease;
}

.reset-logo-btn:hover {
  color: #0f172a;
  border-color: rgba(15, 23, 42, 0.18);
  background: #ffffff;
  box-shadow: 0 6px 14px rgba(15, 23, 42, 0.08);
}

.reset-logo-btn:active {
  transform: translateY(1px);
}
</style>
