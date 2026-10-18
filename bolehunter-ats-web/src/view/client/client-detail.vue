<!--
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 -->
<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <div class="title-wrap">
        <el-button size="small" icon="Back" @click="$router.push('/client/list')">{{ t('client.back') }}</el-button>
        <span class="title" v-if="company">{{ company.name }}</span>
        <el-tag v-if="company" size="small" :type="statusTagType(company.client_status)">
          {{ dictMaps.client_status[company.client_status] || company.client_status }}
        </el-tag>
      </div>
      <el-button size="small" plain icon="Clock" @click="changeHistoryVisible = true">{{ t('changeLog.openBtn') }}</el-button>
        <el-button type="primary" plain size="small" icon="Edit" @click="handleEditClient">{{ t('client.editClient') }}</el-button>
    </div>

    <div class="section" v-if="company">
      <div class="section-title">{{ t('client.baseInfo') }}</div>
      <el-descriptions :column="3" border>
        <el-descriptions-item :label="t('client.colForeignName')">{{ company.foreign_name || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('client.colCity')">{{ company.city || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('client.colIndustry')">{{ dictMaps.industry[company.industry] || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('client.colLevel')">{{ dictMaps.client_level[company.level] || '—' }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.owner')">{{ ownerMap[company.owner_id] || t('common.notAssigned') }}</el-descriptions-item>
        <el-descriptions-item :label="t('client.colPositions')">{{ t('client.positionCount', { n: positions.length }) }}</el-descriptions-item>
        <el-descriptions-item :label="t('client.colDescription')" :span="3">{{ company.description || '—' }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <div class="section">
      <div class="section-title-wrap">
        <span class="section-title">{{ t('client.contactsSection') }}（{{ contacts.length }}）</span>
        <el-button type="primary" plain size="small" icon="Plus" @click="handleContactCreate">{{ t('client.addContact') }}</el-button>
      </div>
      <el-table :data="contacts" border>
        <el-table-column :label="t('client.contactColName')" width="140">
          <template #default="scope">
            {{ scope.row.name }}
            <el-tag v-if="scope.row.is_primary === 1" size="small" type="success">{{ t('client.primaryTag') }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" :label="t('client.contactColTitle')" width="150" />
        <el-table-column prop="phone" :label="t('client.contactColPhone')" width="150" />
        <el-table-column prop="email" :label="t('client.contactColEmail')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="remark" :label="t('common.remark')" min-width="140" show-overflow-tooltip />
        <el-table-column :label="t('common.operation')" width="140" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="handleContactEdit(scope.row)">{{ t('common.edit') }}</el-button>
            <el-button size="small" type="danger" plain @click="handleContactDelete(scope.row)">{{ t('common.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="section">
      <div class="section-title">{{ t('client.positionsSection') }}（{{ positions.length }}）</div>
      <el-table :data="positions" border>
        <el-table-column prop="title" :label="t('client.colPosTitle')" min-width="180" show-overflow-tooltip />
        <el-table-column prop="city" :label="t('client.colCity')" width="110" />
        <el-table-column :label="t('client.colSalary')" width="130" align="center">
          <template #default="scope">{{ scope.row.salary_down }}~{{ scope.row.salary_up }}</template>
        </el-table-column>
        <el-table-column prop="quantity" :label="t('client.colQuantity')" width="70" align="center" />
        <el-table-column :label="t('common.status')" width="90" align="center">
          <template #default="scope">
            <el-tag size="small" :type="scope.row.state === 1 ? 'success' : 'info'">
              {{ scope.row.state === 1 ? t('client.recruiting') : t('client.offShelf') }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 编辑客户 -->
    <el-dialog v-model="clientDialogVisible" :title="t('client.editClient')" width="520px">
      <el-form ref="clientFormRef" :model="clientForm" :rules="clientRules" label-width="90px">
        <el-form-item :label="t('client.formName')" prop="name">
          <el-input v-model="clientForm.name" maxlength="100" />
        </el-form-item>
        <el-form-item :label="t('client.colCity')" prop="city">
          <el-input v-model="clientForm.city" maxlength="50" />
        </el-form-item>
        <el-form-item :label="t('client.colIndustry')">
          <el-select v-model="clientForm.industry" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.industry" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('client.colLevel')">
          <el-select v-model="clientForm.level" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_level" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('client.formCoopStatus')">
          <el-select v-model="clientForm.clientStatus" style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_status" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.owner')">
          <el-select v-model="clientForm.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('client.colDescription')">
          <el-input v-model="clientForm.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmClientSubmit">{{ t('common.confirm') }}</el-button>
        <el-button @click="clientDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 联系人 -->
    <el-dialog v-model="contactDialogVisible" :title="contactForm.id ? t('client.editContact') : t('client.addContact')" width="440px">
      <el-form ref="contactFormRef" :model="contactForm" :rules="contactRules" label-width="90px">
        <el-form-item :label="t('client.contactColName')" prop="name">
          <el-input v-model="contactForm.name" maxlength="50" />
        </el-form-item>
        <el-form-item :label="t('client.contactColTitle')">
          <el-input v-model="contactForm.title" maxlength="50" />
        </el-form-item>
        <el-form-item :label="t('client.contactColPhone')">
          <el-input v-model="contactForm.phone" maxlength="20" />
        </el-form-item>
        <el-form-item :label="t('client.contactColEmail')">
          <el-input v-model="contactForm.email" maxlength="100" />
        </el-form-item>
        <el-form-item :label="t('client.primaryContact')">
          <el-switch v-model="contactForm.isPrimary" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item :label="t('common.remark')">
          <el-input v-model="contactForm.remark" type="textarea" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmContactSubmit">{{ t('common.confirm') }}</el-button>
        <el-button @click="contactDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
  <DataChangeHistory v-model="changeHistoryVisible" table-name="company" :record-id="$route.params.id" />
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import DataChangeHistory from '@/components/DataChangeHistory.vue'
import {
  createContact, deleteContact, getClientDetail, getClientOwners, updateClient, updateContact,
} from '@/api/client'
import { getDictDataByType } from '@/api/setting'

const { t } = useI18n()
const route = useRoute()
const loading = ref(false)
const changeHistoryVisible = ref(false)
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
const clientRules = computed(() => ({
  name: [{ required: true, message: t('client.ruleName'), trigger: 'blur' }],
  city: [{ required: true, message: t('client.ruleCity'), trigger: 'blur' }],
}))
const contactDialogVisible = ref(false)
const contactFormRef = ref()
const contactForm = reactive({})
const contactRules = computed(() => ({
  name: [{ required: true, message: t('client.ruleContactName'), trigger: 'blur' }],
}))

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
    ElMessage.success(res.message || t('common.saveOk'))
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
    ElMessage.success(res.message || t('common.saveOk'))
    contactDialogVisible.value = false
    await getDetail()
  })
}

function handleContactDelete(row) {
  ElMessageBox.confirm(t('client.confirmDeleteContact'), t('common.tip'), { type: 'warning' }).then(async () => {
    const res = await deleteContact(companyId.value, row.id)
    ElMessage.success(res.message || t('common.deleted'))
    await getDetail()
  })
}

onMounted(async () => {
  companyId.value = Number(route.query.id)
  if (!companyId.value) {
    ElMessage.error(t('client.missingId'))
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
})
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
