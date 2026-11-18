<template>
  <div class="page" v-loading="loading">
    <div class="toolbar">
      <div class="tool-copy">
        <strong>收货地址</strong>
        <span v-if="list.length">已保存 {{ list.length }} 条常用地址</span>
        <span v-else>添加后结算更快捷</span>
      </div>
      <button type="button" class="add-btn" @click="openDialog()">
        <i>+</i> 新增地址
      </button>
    </div>

    <div v-if="list.length" class="addr-list">
      <article
        v-for="a in list"
        :key="a.id"
        class="addr-card"
        :class="{ default: isDefault(a) }"
      >
        <div class="pin" aria-hidden="true">
          <span />
        </div>
        <div class="addr-main">
          <div class="top">
            <div class="who">
              <strong>{{ a.receiver || a.receiverName || a.name }}</strong>
              <span>{{ a.phone }}</span>
              <em v-if="isDefault(a)">默认</em>
            </div>
            <div class="quick">
              <button type="button" @click="openDialog(a)">编辑</button>
              <button type="button" class="danger" @click="remove(a)">删除</button>
            </div>
          </div>
          <p class="detail">{{ a.province }}{{ a.city }}{{ a.district }}{{ a.detail || a.address }}</p>
          <div class="foot" v-if="!isDefault(a)">
            <button type="button" class="set-default" @click="setDefault(a)">设为默认地址</button>
          </div>
        </div>
      </article>
    </div>

    <EmptyState
      v-else-if="!loading"
      type="address"
      title="暂无收货地址"
      description="添加常用地址，下单结算更快捷"
    >
      <button type="button" class="empty-cta" @click="openDialog()">新增地址</button>
    </EmptyState>

    <el-dialog v-model="visible" :title="form.id ? '编辑地址' : '新增地址'" width="520px" destroy-on-close align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="收货人" prop="receiverName">
          <el-input v-model="form.receiverName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>
        <el-form-item label="省市区" prop="province">
          <el-cascader
            v-model="regionPath"
            class="region-cascader"
            :options="regionOptions"
            :props="cascaderProps"
            placeholder="请选择省 / 市 / 区"
            clearable
            filterable
            style="width: 100%"
            @change="onRegionChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="设为默认地址">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pcaTextArr } from 'element-china-area-data'
import request from '@/api/request'
import EmptyState from '@/components/EmptyState.vue'

const loading = ref(false)
const submitting = ref(false)
const visible = ref(false)
const list = ref([])
const formRef = ref()
const regionPath = ref([])
const regionOptions = pcaTextArr
const cascaderProps = {
  expandTrigger: 'hover'
}

const form = reactive({
  id: null,
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: false
})

const rules = {
  receiverName: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  province: [
    {
      validator: (_r, _v, cb) => {
        if (!form.province || !form.city || !form.district) cb(new Error('请选择完整省市区'))
        else cb()
      },
      trigger: 'change'
    }
  ],
  detail: [{ required: true, message: '请填写详细地址', trigger: 'blur' }]
}

function unwrapList(data) {
  if (Array.isArray(data)) return data
  return data?.list || []
}

function isDefault(a) {
  return Number(a?.isDefault) === 1 || a?.isDefault === true || !!a?.defaultAddress
}

function matchRegionNode(nodes, name) {
  if (!name || !nodes?.length) return null
  const raw = String(name).trim()
  const hit =
    nodes.find((n) => n.value === raw || n.label === raw) ||
    nodes.find((n) => n.label.includes(raw) || raw.includes(n.label.replace(/(省|市|区|县|自治州|地区|盟|壮族|回族|维吾尔|特别行政区|自治区)$/g, '')))
  return hit || null
}

function resolveRegionPath(province, city, district) {
  const p = matchRegionNode(regionOptions, province)
  if (!p) return []
  const c = matchRegionNode(p.children, city)
  if (!c) return [p.value]
  const d = matchRegionNode(c.children, district)
  if (!d) return [p.value, c.value]
  return [p.value, c.value, d.value]
}

function onRegionChange(val) {
  const path = Array.isArray(val) ? val : []
  form.province = path[0] || ''
  form.city = path[1] || ''
  form.district = path[2] || ''
  formRef.value?.validateField?.('province')
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/user/addresses', { silent: true })
    list.value = unwrapList(data)
  } catch {
    list.value = []
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  Object.assign(form, {
    id: row?.id || null,
    receiverName: row?.receiver || row?.receiverName || row?.name || '',
    phone: row?.phone || '',
    province: row?.province || '',
    city: row?.city || '',
    district: row?.district || '',
    detail: row?.detail || row?.address || '',
    isDefault: isDefault(row)
  })
  regionPath.value = resolveRegionPath(form.province, form.city, form.district)
  visible.value = true
}

async function submit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const payload = {
      receiver: form.receiverName,
      phone: form.phone,
      province: form.province,
      city: form.city,
      district: form.district,
      detail: form.detail,
      isDefault: form.isDefault ? 1 : 0
    }
    if (form.id) await request.put(`/user/addresses/${form.id}`, payload)
    else await request.post('/user/addresses', payload)
    ElMessage.success('已保存')
    visible.value = false
    await load()
  } finally {
    submitting.value = false
  }
}

async function setDefault(a) {
  await request.put(`/user/addresses/${a.id}/default`)
  ElMessage.success('已设为默认')
  await load()
}

async function remove(a) {
  await ElMessageBox.confirm('确定删除该地址？', '提示', { type: 'warning' })
  await request.delete(`/user/addresses/${a.id}`)
  ElMessage.success('已删除')
  await load()
}

onMounted(load)
</script>

<style scoped>
.page {
  --accent: #e1251b;
  --ink: #14171c;
  --muted: #8b929c;
  font-family: 'Microsoft YaHei', 'PingFang SC', 'Noto Sans SC', sans-serif;
  color: var(--ink);
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 14px;
  padding: 16px 18px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid rgba(20, 23, 28, 0.05);
  box-shadow: 0 8px 22px rgba(20, 23, 28, 0.03);
}
.tool-copy strong {
  display: block;
  font-size: 15px;
  font-weight: 740;
  margin-bottom: 4px;
}
.tool-copy span {
  font-size: 12.5px;
  color: var(--muted);
}
.add-btn {
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 11px;
  background: linear-gradient(135deg, #e1251b, #ff5d48);
  color: #fff;
  font-size: 13px;
  font-weight: 720;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 10px 20px rgba(225, 37, 27, 0.22);
  white-space: nowrap;
}
.add-btn i {
  font-style: normal;
  font-size: 16px;
  font-weight: 800;
  line-height: 1;
}

.addr-list {
  display: grid;
  gap: 12px;
}

.addr-card {
  display: grid;
  grid-template-columns: 44px minmax(0, 1fr);
  gap: 12px;
  padding: 18px 18px 16px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid rgba(20, 23, 28, 0.06);
  box-shadow: 0 10px 28px rgba(20, 23, 28, 0.04);
  transition: 0.22s ease;
}
.addr-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 34px rgba(20, 23, 28, 0.07);
}
.addr-card.default {
  background:
    linear-gradient(180deg, #fff8f7 0%, #fff 55%);
  box-shadow:
    0 10px 28px rgba(225, 37, 27, 0.08),
    inset 3px 0 0 #e1251b;
}

.pin {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  background: #f4f6f8;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.pin span {
  width: 12px;
  height: 12px;
  border-radius: 50% 50% 50% 0;
  transform: rotate(-45deg);
  background: #98a2b3;
  box-shadow: inset 0 0 0 3px #f4f6f8;
}
.addr-card.default .pin {
  background: #fff1f0;
}
.addr-card.default .pin span {
  background: var(--accent);
  box-shadow: inset 0 0 0 3px #fff1f0;
}

.top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 8px;
}
.who {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.who strong {
  font-size: 16px;
  font-weight: 760;
}
.who span {
  color: #667085;
  font-size: 13px;
}
.who em {
  font-style: normal;
  height: 20px;
  padding: 0 8px;
  border-radius: 6px;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  font-weight: 720;
  display: inline-flex;
  align-items: center;
}

.quick {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}
.quick button {
  height: 28px;
  padding: 0 10px;
  border: none;
  background: transparent;
  color: #667085;
  font-size: 12.5px;
  font-weight: 650;
  cursor: pointer;
  border-radius: 8px;
}
.quick button:hover {
  background: #f5f6f8;
  color: var(--accent);
}
.quick button.danger { color: #f04438; }
.quick button.danger:hover { background: #fef3f2; }

.detail {
  margin: 0;
  color: #475467;
  font-size: 13.5px;
  line-height: 1.65;
}

.foot {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #eef0f3;
}
.set-default {
  height: 30px;
  padding: 0 12px;
  border-radius: 8px;
  border: 1px solid #eadfdf;
  background: #fff;
  color: #667085;
  font-size: 12px;
  font-weight: 650;
  cursor: pointer;
}
.set-default:hover {
  border-color: rgba(225, 37, 27, 0.3);
  color: var(--accent);
  background: #fff8f7;
}

.region-cascader :deep(.el-input__wrapper) {
  min-height: 40px;
  border-radius: 10px;
}

.empty-cta {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 16px;
  border: none;
  border-radius: 999px;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  cursor: pointer;
}

@media (max-width: 640px) {
  .addr-card {
    grid-template-columns: 1fr;
  }
  .pin { display: none; }
  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
