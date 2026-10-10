<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="title-wrap">
        <el-button size="small" icon="Back" @click="$router.push('/client/list')">返回</el-button>
        <span class="title" v-if="company">{{ company.name }}</span>
        <el-tag v-if="company" size="small" :type="statusTagType(company.client_status)">
          {{ dictMaps.client_status[company.client_status] || company.client_status }}
        </el-tag>
      </div>
      <el-button type="primary" plain size="small" icon="Edit" @click="handleEditClient">编辑客户</el-button>
    </div>

    <div class="section" v-if="company">
      <div class="section-title">基本信息</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item label="外文名称">{{ company.foreign_name || '—' }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ company.city || '—' }}</el-descriptions-item>
        <el-descriptions-item label="行业">{{ dictMaps.industry[company.industry] || '—' }}</el-descriptions-item>
        <el-descriptions-item label="客户等级">{{ dictMaps.client_level[company.level] || '—' }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ ownerMap[company.owner_id] || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="关联职位">{{ positions.length }} 个</el-descriptions-item>
        <el-descriptions-item label="公司简介" :span="3">{{ company.description || '—' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="section">
      <div class="section-title-wrap">
        <span class="section-title">联系人（{{ contacts.length }}）</span>
        <el-button type="primary" plain size="small" icon="Plus" @click="handleContactCreate">新增联系人</el-button>
      </div>
      <el-table :data="contacts" border>
        <el-table-column label="姓名" width="140">
          <template #default="scope">
            {{ scope.row.name }}
            <el-tag v-if="scope.row.is_primary === 1" size="small" type="success">主</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职位" width="150" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="140" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="handleContactEdit(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="handleContactDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section">
      <div class="section-title">关联职位（{{ positions.length }}）</div>
      <el-table :data="positions" border>
        <el-table-column prop="title" label="职位名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="city" label="城市" width="110" />
        <el-table-column label="薪资" width="130" align="center">
          <template #default="scope">{{ scope.row.salary_down }}~{{ scope.row.salary_up }}</template>
        </el-table-column>
        <el-table-column prop="quantity" label="人数" width="70" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.state === 1 ? 'success' : 'info'">
              {{ scope.row.state === 1 ? '招聘中' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑客户 -->
    <el-dialog v-model="clientDialogVisible" title="编辑客户" width="520px">
      <el-form ref="clientFormRef" :model="clientForm" :rules="clientRules" label-width="90px">
        <el-form-item label="客户名称" prop="name">
          <el-input v-model="clientForm.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="clientForm.city" maxlength="50" />
        </el-form-item>
        <el-form-item label="行业">
          <el-select v-model="clientForm.industry" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.industry" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户等级">
          <el-select v-model="clientForm.level" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_level" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="合作状态">
          <el-select v-model="clientForm.clientStatus" style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_status" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="clientForm.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="公司简介">
          <el-input v-model="clientForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmClientSubmit">确 定</el-button>
        <el-button @click="clientDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 联系人 -->
    <el-dialog v-model="contactDialogVisible" :title="contactForm.id ? '编辑联系人' : '新增联系人'" width="440px">
      <el-form ref="contactFormRef" :model="contactForm" :rules="contactRules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="contactForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item label="职位">
          <el-input v-model="contactForm.title" maxlength="50" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="contactForm.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="contactForm.email" maxlength="100" />
        </el-form-item>
        <el-form-item label="主联系人">
          <el-switch v-model="contactForm.isPrimary" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="contactForm.remark" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmContactSubmit">确 定</el-button>
        <el-button @click="contactDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createContact, deleteContact, getClientDetail, getClientOwners, updateClient, updateContact,
} from '@/api/client'
import { getDictDataByType } from '@/api/setting'
import { STAGES, exportCsv, getRecommendationPage } from '@/api/recommendation'

const route = useRoute()
const loading = ref(false)
const companyId = ref(null)
const company = ref(null)
const contacts = ref([])
const positions = ref([])
const owners = ref([])
const ownerMap = ref({})
const dictMaps = reactive({ industry: {}, client_level: {}, client_status: {} })
const clientDialogVisible = ref(false)
const clientFormRef = ref()
const clientForm = reactive({})
const clientRules = {
  name: [{ required: true, message: '请输入客户公司名称', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
}
const contactDialogVisible = ref(false)
const contactFormRef = ref()
const contactForm = reactive({})
const contactRules = {
  name: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
}
const recItems = ref([])
const stageLabelMap = Object.fromEntries(STAGES.map(s => [s.value, s.label]))

function toMap(list) {
  const map = {}
  ;(list || []).forEach(item => { map[item.dict_value] = item.dict_label })
  return map
}

function statusTagType(status) {
  if (status === 'signing') return 'success'
  if (status === 'suspended') return 'warning'
  if (status === 'lost') return 'danger'
  return 'info'
}

async function getDetail() {
  loading.value = true
  try {
    const res = await getClientDetail(companyId.value)
    company.value = res.company
    contacts.value = res.contacts || []
    positions.value = res.positions || []
  } finally {
    loading.value = false
  }
}

function handleEditClient() {
  Object.assign(clientForm, {
    name: company.value.name,
    city: company.value.city,
    industry: company.value.industry,
    level: company.value.level,
    clientStatus: company.value.client_status,
    ownerId: company.value.owner_id,
    description: company.value.description,
  })
  clientDialogVisible.value = true
}

function confirmClientSubmit() {
  clientFormRef.value.validate(async valid => {
    if (!valid) return
    const res = await updateClient(companyId.value, clientForm)
    ElMessage.success(res.message || '保存成功')
    clientDialogVisible.value = false
    await getDetail()
  })
}

function handleContactCreate() {
  Object.assign(contactForm, { id: null, name: '', title: '', phone: '', email: '', isPrimary: 0, remark: '' })
  contactDialogVisible.value = true
}

function handleContactEdit(row) {
  Object.assign(contactForm, {
    id: row.id, name: row.name, title: row.title, phone: row.phone,
    email: row.email, isPrimary: row.is_primary, remark: row.remark,
  })
  contactDialogVisible.value = true
}

function confirmContactSubmit() {
  contactFormRef.value.validate(async valid => {
    if (!valid) return
    const res = contactForm.id
      ? await updateContact(companyId.value, contactForm.id, contactForm)
      : await createContact(companyId.value, contactForm)
    ElMessage.success(res.message || '保存成功')
    contactDialogVisible.value = false
    await getDetail()
  })
}

function handleContactDelete(row) {
  ElMessageBox.confirm('是否删除该联系人？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteContact(companyId.value, row.id)
    ElMessage.success(res.message || '删除成功')
    await getDetail()
  })
}

onMounted(async () => {
  companyId.value = Number(route.query.id)
  if (!companyId.value) {
    ElMessage.error('缺少客户id参数')
    return
  }
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
  await getDetail()
  await loadRecs()
})

async function loadRecs() {
  try {
    const res = await getRecommendationPage({ client_id: companyId.value, page: 0, count: 50 })
    recItems.value = res.items || []
  } catch (e) {
    recItems.value = []
  }
}

function stageTag(stage) {
  if (stage === 'OFFERED' || stage === 'ONBOARDED' || stage === 'PAID') return 'success'
  if (stage === 'ARCHIVED') return 'info'
  if (stage === 'PENDING') return ''
  return 'warning'
}

async function handleExportRecs() {
  const blob = await exportCsv({ client_id: companyId.value })
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.download = `recommendations_client_${companyId.value}.csv`
  link.click()
  window.URL.revokeObjectURL(url)
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}
.title {
  font-size: 16px;
  font-weight: 500;
}
.section {
  margin-top: 18px;
}
.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 10px;
}
.section-title-wrap {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
</style>
