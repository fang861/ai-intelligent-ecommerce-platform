<template>
  <div class="page">
    <el-card shadow="never" class="panel">
      <template #header>
        <div class="header-row">
          <span>分类管理</span>
          <el-button type="primary" @click="openDialog()">新增分类</el-button>
        </div>
      </template>
      <el-table v-loading="loading" :data="tableData">
        <el-table-column prop="name" label="分类名称" min-width="160" />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="light" round>
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="visible" :title="form.id ? '编辑分类' : '新增分类'" width="460px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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

const loading = ref(false)
const submitting = ref(false)
const visible = ref(false)
const tableData = ref([])
const formRef = ref()
const form = reactive({ id: null, name: '', sortOrder: 0, status: 1 })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || data?.records || []
}

async function loadData() {
  loading.value = true
  try {
    const data = await request.get('/admin/categories')
    tableData.value = unwrapList(data)
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    name: row?.name || '',
    sortOrder: Number(row?.sortOrder || 0),
    status: row?.status ?? 1
  })
  visible.value = true
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (form.id) await request.put(`/admin/categories/${form.id}`, form)
    else await request.post('/admin/categories', form)
    ElMessage.success('已保存')
    visible.value = false
    await loadData()
  } finally {
    submitting.value = false
  }
}

async function remove(row) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await request.delete(`/admin/categories/${row.id}`)
  ElMessage.success('已删除')
  await loadData()
}

onMounted(loadData)
</script>

<style scoped>
.header-row { display: flex; justify-content: space-between; align-items: center; }
.panel { border-radius: 14px; }
</style>
