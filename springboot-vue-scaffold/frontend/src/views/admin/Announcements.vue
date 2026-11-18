<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>公告管理</span>
          <el-button type="primary" @click="openDialog()">新增公告</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column label="摘要" min-width="260" show-overflow-tooltip>
          <template #default="{ row }">
            {{ plainTextOf(row.content).slice(0, 60) || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发布时间" width="170" align="center" />
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="visible"
      :title="form.id ? '编辑公告' : '新增公告'"
      width="860px"
      destroy-on-close
      top="6vh"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="72px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="80" show-word-limit placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <RichTextEditor
            v-model="form.content"
            placeholder="请输入公告正文，支持标题、列表、加粗、图片等"
            min-height="320px"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'
import { plainTextOf } from '@/utils/format'
import RichTextEditor from '@/components/RichTextEditor.vue'

const loading = ref(false)
const submitting = ref(false)
const visible = ref(false)
const tableData = ref([])
const formRef = ref()
const form = reactive({ id: null, title: '', content: '', status: 1 })
const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [
    {
      validator: (_r, v, cb) => {
        if (!plainTextOf(v || '').trim()) cb(new Error('请输入内容'))
        else cb()
      },
      trigger: 'blur'
    }
  ]
}

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/announcements', { params: { page: 1, size: 100 } })
    tableData.value = unwrapList(data)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    title: row?.title || '',
    content: row?.content || '',
    status: row?.status == null ? 1 : row.status
  })
  visible.value = true
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      title: form.title,
      content: form.content,
      status: form.status
    }
    if (form.id) await request.put(`/admin/announcements/${form.id}`, payload)
    else await request.post('/admin/announcements', payload)
    ElMessage.success('已保存')
    visible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' })
  await request.delete(`/admin/announcements/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.panel {
  border-radius: 14px;
}
</style>
