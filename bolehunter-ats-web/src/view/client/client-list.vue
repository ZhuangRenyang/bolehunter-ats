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
  <el-card shadow="never">
    <div class="page-header">
      <el-button type="primary" icon="Plus" @click="handleCreate">{{ t('client.create') }}</el-button>
    </div>
    <div class="filters">
      <el-input v-model="filters.keyword" :placeholder="t('client.filterKeyword')" clearable style="width: 180px" @keyup.enter="handleSearch" />
      <el-select v-model="filters.industry" :placeholder="t('client.filterIndustry')" clearable style="width: 150px">
        <el-option v-for="(label, value) in dictMaps.industry" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.level" :placeholder="t('client.filterLevel')" clearable style="width: 140px">
        <el-option v-for="(label, value) in dictMaps.client_level" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.client_status" :placeholder="t('client.filterStatus')" clearable style="width: 140px">
        <el-option v-for="(label, value) in dictMaps.client_status" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.owner_id" :placeholder="t('common.owner')" clearable filterable style="width: 140px">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
      <el-button type="primary" icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button @click="handleReset">{{ t('client.reset') }}</el-button>
    </div>
    <el-table v-if="!isMobile" :data="tableData" border v-loading="loading">
      <el-table-column prop="name" :label="t('client.colName')" min-width="160" show-overflow-tooltip />
      <el-table-column prop="city" :label="t('client.colCity')" width="110" />
      <el-table-column :label="t('client.colIndustry')" width="140">
        <template #default="scope">{{ dictMaps.industry[scope.row.industry] || '—' }}</template>
      </el-table-column>
      <el-table-column :label="t('client.colLevel')" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.level" size="small" :type="levelTagType(scope.row.level)">
            {{ dictMaps.client_level[scope.row.level] || scope.row.level }}
          </el-tag>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('client.colStatus')" width="110" align="center">
        <template #default="scope">
          <el-tag size="small" :type="statusTagType(scope.row.client_status)">
            {{ dictMaps.client_status[scope.row.client_status] || scope.row.client_status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.owner')" width="110" align="center">
        <template #default="scope">{{ ownerMap[scope.row.owner_id] || t('common.notAssigned') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="220" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleDetail(scope.row)">{{ t('common.detail') }}</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 移动端：卡片流替代表格，避免 7 列必须横向滚动 -->
    <div v-else class="m-list" v-loading="loading">
      <div v-for="row in tableData" :key="row.id" class="m-item" @click="handleDetail(row)">
        <div class="m-item__head">
          <span class="m-item__title">{{ row.name }}</span>
          <el-tag size="small" :type="statusTagType(row.client_status)">
            {{ dictMaps.client_status[row.client_status] || row.client_status }}
          </el-tag>
        </div>
        <div class="m-item__meta">
          {{ [row.city, dictMaps.industry[row.industry], dictMaps.client_level[row.level]].filter(Boolean).join(' · ') || '—' }}
        </div>
        <div class="m-item__meta">{{ t('common.owner') }}：{{ ownerMap[row.owner_id] || t('common.notAssigned') }}</div>
        <div class="m-item__ops" @click.stop>
          <el-button size="small" type="primary" plain @click="handleDetail(row)">{{ t('common.detail') }}</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
        </div>
      </div>
      <el-empty v-if="tableData.length === 0" :description="t('common.noData')" :image-size="50" />
    </div>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="form.id ? t('client.editTitle') : t('client.createTitle')" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item :label="t('client.formName')" prop="name">
          <el-input v-model="form.name" maxlength="100" />
        </el-form-item>
        <el-form-item :label="t('client.colForeignName')">
          <el-input v-model="form.foreignName" maxlength="100" />
        </el-form-item>
        <el-form-item :label="t('client.colCity')" prop="city">
          <el-input v-model="form.city" maxlength="50" />
        </el-form-item>
        <el-form-item :label="t('client.colIndustry')">
          <el-select v-model="form.industry" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.industry" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('client.filterLevel')">
          <el-select v-model="form.level" clearable style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_level" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('client.formCoopStatus')">
          <el-select v-model="form.clientStatus" style="width: 100%">
            <el-option v-for="(label, value) in dictMaps.client_status" :key="value" :label="label" :value="value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.owner')">
          <el-select v-model="form.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('client.colDescription')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">{{ t('common.confirm') }}</el-button>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { createClient, deleteClient, getClientOwners, getClientPage, updateClient } from '@/api/client'
import { getDictDataByType } from '@/api/setting'
import { useIsMobile } from '@/composables/useIsMobile'

const { t } = useI18n()
const router = useRouter()
// 窄屏改卡片流（无需横向滚动），宽屏保持表格
const isMobile = useIsMobile()
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
const rules = computed(() => ({
  name: [{ required: true, message: t('client.ruleName'), trigger: 'blur' }],
  city: [{ required: true, message: t('client.ruleCity'), trigger: 'blur' }],
}))
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

/**
 * 删除客户（迭代24 操作审计增强）。
 * 高危操作用强确认：必须手动输入与客户名称完全一致的文字才会发起请求；
 * 后端还有第二道防线——名下存在推荐记录时直接拒绝（80290）。
 */
function handleDelete(row) {
  ElMessageBox.prompt(
    t('client.deletePrompt', { name: row.name }),
    t('client.deleteTitle'),
    { type: 'warning', confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel') },
  ).then(async ({ value }) => {
    if ((value || '').trim() !== row.name) {
      ElMessage.error(t('client.deleteConfirmWord'))
      return
    }
    try {
      const res = await deleteClient(row.id)
      ElMessage.success(res.message || t('common.deleted'))
      await getAll()
    } catch (e) {
      /* 错误提示由请求层统一处理（含 80290 有推荐拒绝） */
    }
  }).catch(() => {})
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
    ElMessage.success(res.message || t('common.saveOk'))
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
