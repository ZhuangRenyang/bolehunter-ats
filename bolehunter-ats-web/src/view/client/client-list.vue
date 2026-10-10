<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="page-title">客户列表</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">新建客户</el-button>
    </div>
    <div class="filters">
      <el-input v-model="filters.keyword" placeholder="客户名称" clearable style="width: 180px" @keyup.enter="handleSearch" />
      <el-select v-model="filters.industry" placeholder="行业" clearable style="width: 150px">
        <el-option v-for="(label, value) in dictMaps.industry" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.level" placeholder="客户等级" clearable style="width: 140px">
        <el-option v-for="(label, value) in dictMaps.client_level" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.client_status" placeholder="合作状态" clearable style="width: 140px">
        <el-option v-for="(label, value) in dictMaps.client_status" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.owner_id" placeholder="负责人" clearable filterable style="width: 140px">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
      <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="name" label="客户名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="city" label="城市" width="110" />
      <el-table-column label="行业" width="140">
        <template #default="scope">{{ dictMaps.industry[scope.row.industry] || '—' }}</template>
      </el-table-column>
      <el-table-column label="等级" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.level" size="small" :type="levelTagType(scope.row.level)">
            {{ dictMaps.client_level[scope.row.level] || scope.row.level }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="合作状态" width="110" align="center">
        <template #default="scope">
          <el-tag size="small" :type="statusTagType(scope.row.client_status)">
            {{ dictMaps.client_status[scope.row.client_status] || scope.row.client_status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="负责人" width="110" align="center">
        <template #default="scope">{{ ownerMap[scope.row.owner_id] || '未分配' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleDetail(scope.row)">详情</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑客户' : '新建客户'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="外文名称">
          <el-input v-model="form.foreignName" maxlength="100" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" maxlength="50" />
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="form.industry" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.industry" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="form.level" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_level" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="form.clientStatus" style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_status" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createClient, getClientOwners, getClientPage, updateClient } from '@/api/client'
import { getDictDataByType } from '@/api/setting'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageCount = 10
const owners = ref([])
const ownerMap = ref({})
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({ id: null, name: '', foreignName: '', city: '', industry: '', level: '', clientStatus: 'potential', ownerId: null, description: '' })
const rules = {
  name: [{ required: true, message: '请输入客户公司名称', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
}
const filters = reactive({ keyword: '', industry: '', level: '', client_status: '', owner_id: '' })
const dictMaps = reactive({ industry: {}, client_level: {}, client_status: {} })

function toMap(list) {
  const map = {}
  ;(list || []).forEach(item => { map[item.dict_value] = item.dict_label })
  return map
}

function levelTagType(level) {
  if (level === 'A') return 'danger'
  if (level === 'B') return 'warning'
  return 'info'
}

function statusTagType(status) {
  if (status === 'signing') return 'success'
  if (status === 'suspended') return 'warning'
  if (status === 'lost') return 'danger'
  return 'info'
}

async function getAll() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, count: pageCount }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.industry) params.industry = filters.industry
    if (filters.level) params.level = filters.level
    if (filters.client_status) params.client_status = filters.client_status
    if (filters.owner_id) params.owner_id = filters.owner_id
    const res = await getClientPage(params)
    tableData.value = res.items
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  getAll()
}

function handleReset() {
  Object.assign(filters, { keyword: '', industry: '', level: '', client_status: '', owner_id: '' })
  handleSearch()
}

async function handlePageChange(val) {
  currentPage.value = val
  await getAll()
}

function handleCreate() {
  Object.assign(form, { id: null, name: '', foreignName: '', city: '', industry: '', level: '', clientStatus: 'potential', ownerId: null, description: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, {
    id: row.id, name: row.name, foreignName: row.foreign_name, city: row.city, industry: row.industry,
    level: row.level, clientStatus: row.client_status, ownerId: row.owner_id, description: row.description,
  })
  dialogVisible.value = true
}

function confirmSubmit() {
  formRef.value.validate(async valid => {
    if (!valid) return
    const res = form.id ? await updateClient(form.id, form) : await createClient(form)
    ElMessage.success(res.message || '保存成功')
    dialogVisible.value = false
    await getAll()
  })
}

function handleDetail(row) {
  router.push({ path: '/client/detail', query: { id: row.id } })
}

onMounted(async () => {
  await getAll()
  owners.value = await getClientOwners()
  const map = {}
  owners.value.forEach(u => { map[u.id] = u.nickname })
  ownerMap.value = map
  const [industry, level, status] = await Promise.all([
    getDictDataByType('industry'), getDictDataByType('client_level'), getDictDataByType('client_status'),
  ])
  dictMaps.industry = toMap(industry)
  dictMaps.client_level = toMap(level)
  dictMaps.client_status = toMap(status)
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.page-title {
  font-size: 15px;
  font-weight: 500;
}
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
