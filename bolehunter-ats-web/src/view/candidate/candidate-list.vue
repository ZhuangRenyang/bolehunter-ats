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
    <div class="toolbar">
      <span class="page-title">{{ t('candidate.title') }}</span>
      <el-space>
        <el-button plain size="small" icon="PriceTag" @click="tagDialogVisible = true">{{ t('candidate.tagMgmt') }}</el-button>
        <el-button type="primary" plain size="small" icon="Upload" @click="importDialogVisible = true">{{ t('candidate.import') }}</el-button>
        <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">{{ t('candidate.create') }}</el-button>
      </el-space>
    </div>
    <div class="filters">
      <el-input v-model="filters.keyword" :placeholder="t('candidate.filterKeyword')" clearable style="width: 220px"
        @keyup.enter="handleSearch" />
      <el-select v-model="filters.education" :placeholder="t('candidate.filterEdu')" clearable style="width: 120px">
        <el-option v-for="(label, value) in dictMaps.education" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.source" :placeholder="t('candidate.filterSource')" clearable style="width: 130px">
        <el-option v-for="(label, value) in dictMaps.candidate_source" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.tag_ids" :placeholder="t('candidate.filterTag')" clearable multiple filterable collapse-tags style="width: 200px">
        <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
      </el-select>
      <el-select v-model="filters.owner_id" :placeholder="t('common.owner')" clearable filterable style="width: 130px">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
      <el-button type="primary" icon="Search" @click="handleSearch">{{ t('common.search') }}</el-button>
      <el-button @click="handleReset">{{ t('candidate.reset') }}</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column :label="t('candidate.colName')" width="110">
        <template #default="scope">
          <el-link type="primary" :underline="false" @click="handleDetail(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="phone" :label="t('candidate.colPhone')" width="130" />
      <el-table-column prop="email" :label="t('candidate.colEmail')" min-width="160" show-overflow-tooltip />
      <el-table-column prop="current_company" :label="t('candidate.colCompany')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="current_position" :label="t('candidate.colPosition')" min-width="130" show-overflow-tooltip />
      <el-table-column :label="t('candidate.colEdu')" width="80" align="center">
        <template #default="scope">{{ dictMaps.education[scope.row.education] || '—' }}</template>
      </el-table-column>
      <el-table-column prop="city" :label="t('candidate.colCity')" width="100" />
      <el-table-column :label="t('candidate.colTag')" min-width="150">
        <template #default="scope">
          <el-tag v-for="tagName in scope.row.tag_names" :key="tagName" size="small" class="tag-item">{{ tagName }}</el-tag>
          <span v-if="!scope.row.tag_names || scope.row.tag_names.length === 0">—</span>
        </template>
      </el-table-column>
      <el-table-column :label="t('common.owner')" width="100" align="center">
        <template #default="scope">{{ ownerMap[scope.row.owner_id] || t('common.notAssigned') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="210" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleDetail(scope.row)">{{ t('common.detail') }}</el-button>
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">{{ t('common.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDeleteCandidate(scope.row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <!-- 新建/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? t('candidate.editTitle') : t('candidate.createTitle')" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12"><el-form-item :label="t('candidate.formName')" prop="name"><el-input v-model="form.name" maxlength="50" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('candidate.colPhone')"><el-input v-model="form.phone" maxlength="20" @blur="checkDuplicate" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item :label="t('candidate.colEmail')"><el-input v-model="form.email" maxlength="100" @blur="checkDuplicate" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('candidate.formCity')"><el-input v-model="form.city" maxlength="50" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item :label="t('candidate.colCompany')"><el-input v-model="form.currentCompany" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item :label="t('candidate.colPosition')"><el-input v-model="form.currentPosition" maxlength="100" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="t('candidate.colEdu')">
              <el-select v-model="form.education" clearable style="width: 100%">
                <el-option v-for="(label, value) in dictMaps.education" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('candidate.filterSource')">
              <el-select v-model="form.source" style="width: 100%">
                <el-option v-for="(label, value) in dictMaps.candidate_source" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item :label="t('common.owner')">
              <el-select v-model="form.ownerId" clearable filterable style="width: 100%">
                <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('candidate.filterTag')">
              <el-select v-model="form.tagIds" multiple filterable style="width: 100%">
                <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('common.remark')">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-alert v-if="duplicateWarning" :title="duplicateWarning" type="warning" show-icon :closable="false" />
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">{{ t('common.confirm') }}</el-button>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 导入 -->
    <el-dialog v-model="importDialogVisible" :title="t('candidate.importTitle')" width="560px" @closed="handleImportClosed">
      <el-tabs v-model="importTab">
        <el-tab-pane :label="t('candidate.tabResume')" name="resume">
          <div class="import-tip">{{ t('candidate.resumeTip') }}</div>
          <input type="file" accept=".pdf,.doc,.docx" @change="e => (resumeFile = e.target.files[0] || null)" />
          <el-button type="primary" size="small" class="import-btn" :disabled="!resumeFile" :loading="importing"
            @click="submitImport('resume')">{{ t('candidate.startImport') }}</el-button>
        </el-tab-pane>
        <el-tab-pane :label="t('candidate.tabExcel')" name="excel">
          <div class="import-tip">
            {{ t('candidate.excelTip') }}
            <el-link type="primary" :underline="false" @click="handleDownloadTemplate">{{ t('candidate.downloadTemplate') }}</el-link>
          </div>
          <input type="file" accept=".xls,.xlsx" @change="e => (excelFile = e.target.files[0] || null)" />
          <el-button type="primary" size="small" class="import-btn" :disabled="!excelFile" :loading="importing"
            @click="submitImport('excel')">{{ t('candidate.startImport') }}</el-button>
        </el-tab-pane>
      </el-tabs>
      <div v-if="importResult" class="import-result">
        <el-alert :title="t('candidate.importDone', { total: importResult.total, ok: importResult.success_count, dup: importResult.duplicate_count, fail: importResult.fail_count })"
          :type="importResult.fail_count > 0 ? 'warning' : 'success'" show-icon :closable="false" />
        <div v-if="importResult.duplicates && importResult.duplicates.length" class="report-block">
          <div class="report-title">{{ t('candidate.dupDetail') }}</div>
          <div v-for="(item, index) in importResult.duplicates" :key="'d' + index" class="report-line">{{ item }}</div>
        </div>
        <div v-if="importResult.failures && importResult.failures.length" class="report-block">
          <div class="report-title">{{ t('candidate.failDetail') }}</div>
          <div v-for="(item, index) in importResult.failures" :key="'f' + index" class="report-line">{{ item }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 标签管理 -->
    <el-dialog v-model="tagDialogVisible" :title="t('candidate.tagMgmtTitle')" width="420px">
      <div class="tag-add">
        <el-input v-model="newTagName" :placeholder="t('candidate.tagPlaceholder')" maxlength="50" style="width: 220px" />
        <el-button type="primary" @click="handleAddTag">{{ t('kanban.commAdd') }}</el-button>
      </div>
      <el-table :data="tags" size="small" border max-height="320">
        <el-table-column :label="t('candidate.tagName')">
          <template #default="scope">
            <el-input v-if="editingTagId === scope.row.id" v-model="editingTagName" size="small" />
            <span v-else>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="150" align="center">
          <template #default="scope">
            <template v-if="editingTagId === scope.row.id">
              <el-button size="small" type="primary" plain @click="handleSaveTag(scope.row)">{{ t('common.save') }}</el-button>
              <el-button size="small" @click="editingTagId = null">{{ t('common.cancel') }}</el-button>
            </template>
            <template v-else>
              <el-button size="small" type="primary" plain @click="handleEditTag(scope.row)">{{ t('common.edit') }}</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteTag(scope.row)">{{ t('common.delete') }}</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  createCandidate, createTag, deleteCandidate, deleteTag, duplicateCheck, getCandidatePage,
  getTags, importExcel, importResume, updateCandidate, updateTag,
} from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import { getDictDataByType } from '@/api/setting'
import defaultAxios from '@/api/request'

const { t } = useI18n()
const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageCount = 10
const owners = ref([])
const ownerMap = ref({})
const tags = ref([])
const dictMaps = reactive({ education: {}, candidate_source: {} })
const filters = reactive({ keyword: '', education: '', source: '', tag_ids: [], owner_id: '' })
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({})
const rules = computed(() => ({ name: [{ required: true, message: t('candidate.ruleName'), trigger: 'blur' }] }))
const duplicateWarning = ref('')
const importDialogVisible = ref(false)
const importTab = ref('resume')
const resumeFile = ref(null)
const excelFile = ref(null)
const importing = ref(false)
const importResult = ref(null)
const tagDialogVisible = ref(false)
const newTagName = ref('')
const editingTagId = ref(null)
const editingTagName = ref('')

function toMap(list) {
  const map = {}
  ;(list || []).forEach(item => { map[item.dict_value] = item.dict_label })
  return map
}

async function getAll() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, count: pageCount }
    if (filters.keyword) params.keyword = filters.keyword
    if (filters.education) params.education = filters.education
    if (filters.source) params.source = filters.source
    if (filters.owner_id) params.owner_id = filters.owner_id
    if (filters.tag_ids && filters.tag_ids.length) params.tag_ids = filters.tag_ids.join(',')
    const res = await getCandidatePage(params)
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
  Object.assign(filters, { keyword: '', education: '', source: '', tag_ids: [], owner_id: '' })
  handleSearch()
}

async function handlePageChange(val) {
  currentPage.value = val
  await getAll()
}

function handleDetail(row) {
  router.push({ path: '/candidate/detail', query: { id: row.id } })
}

let duplicateTimer = null
function checkDuplicate() {
  if (duplicateTimer) clearTimeout(duplicateTimer)
  duplicateTimer = setTimeout(async () => {
    const phone = (form.phone || '').trim()
    const email = (form.email || '').trim()
    if (!phone && !email) {
      duplicateWarning.value = ''
      return
    }
    try {
      const params = {}
      if (phone) params.phone = phone
      if (email) params.email = email
      if (form.id) params.exclude_id = form.id
      const conflicts = await duplicateCheck(params)
      duplicateWarning.value = conflicts && conflicts.length ? t('candidate.dupWarning', { names: conflicts.join('、') }) : ''
    } catch (e) { /* ignore */ }
  }, 400)
}

function handleCreate() {
  Object.assign(form, {
    id: null, name: '', phone: '', email: '', currentCompany: '', currentPosition: '',
    education: '', city: '', source: 'sourcing', ownerId: null, remark: '', tagIds: [],
  })
  duplicateWarning.value = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  Object.assign(form, {
    id: row.id, name: row.name, phone: row.phone, email: row.email, currentCompany: row.current_company,
    currentPosition: row.current_position, education: row.education, city: row.city, source: row.source,
    ownerId: row.owner_id, remark: row.remark, tagIds: row.tag_ids || [],
  })
  duplicateWarning.value = ''
  dialogVisible.value = true
}

function confirmSubmit() {
  formRef.value.validate(async valid => {
    if (!valid) return
    const res = form.id ? await updateCandidate(form.id, form) : await createCandidate(form)
    ElMessage.success(res.message || t('common.saveOk'))
    dialogVisible.value = false
    await getAll()
  })
}

async function submitImport(type) {
  importing.value = true
  importResult.value = null
  try {
    const formData = new FormData()
    formData.append('file', type === 'resume' ? resumeFile.value : excelFile.value)
    importResult.value = type === 'resume' ? await importResume(formData) : await importExcel(formData)
    ElMessage.success(t('candidate.importOk'))
    await getAll()
  } catch (e) {
    // 请求层已提示
  } finally {
    importing.value = false
  }
}

async function handleDownloadTemplate() {
  const blob = await defaultAxios.get('recruit/candidate/import/template', { responseType: 'blob' })
  const url = window.URL.createObjectURL(new Blob([blob]))
  const link = document.createElement('a')
  link.href = url
  link.download = 'candidate_import_template.xlsx'
  link.click()
  window.URL.revokeObjectURL(url)
}

function handleImportClosed() {
  resumeFile.value = null
  excelFile.value = null
  importResult.value = null
}

async function handleAddTag() {
  if (!newTagName.value.trim()) return
  const res = await createTag(newTagName.value.trim())
  ElMessage.success(res.message || t('kanban.addOk'))
  newTagName.value = ''
  tags.value = await getTags()
}

function handleEditTag(row) {
  editingTagId.value = row.id
  editingTagName.value = row.name
}

async function handleSaveTag(row) {
  const res = await updateTag(row.id, editingTagName.value.trim())
  ElMessage.success(res.message || t('common.saveOk'))
  editingTagId.value = null
  tags.value = await getTags()
  await getAll()
}

function handleDeleteTag(row) {
  ElMessageBox.confirm(t('candidate.confirmDeleteTag'), t('common.tip'), { type: 'warning' }).then(async () => {
    const res = await deleteTag(row.id)
    ElMessage.success(res.message || t('common.deleted'))
    tags.value = await getTags()
    await getAll()
  })
}

/**
 * 删除候选人（迭代24 操作审计增强）：二次确认后才发起请求。
 * 该操作会同时移除候选人的标签关联；后端已接 @Logger 审计。
 */
function handleDeleteCandidate(row) {
  ElMessageBox.confirm(
    t('candidate.confirmDelete', { name: row.name }),
    t('candidate.deleteTitle'),
    { type: 'warning', confirmButtonText: t('common.delete'), cancelButtonText: t('common.cancel') },
  ).then(async () => {
    try {
      const res = await deleteCandidate(row.id)
      ElMessage.success(res.message || t('common.deleted'))
      await getAll()
    } catch (e) {
      /* 错误提示由请求层统一处理 */
    }
  }).catch(() => {})
}

onMounted(async () => {
  await getAll()
  owners.value = await getClientOwners()
  const map = {}
  owners.value.forEach(u => { map[u.id] = u.nickname })
  ownerMap.value = map
  tags.value = await getTags()
  const [education, source] = await Promise.all([
    getDictDataByType('education'), getDictDataByType('candidate_source'),
  ])
  dictMaps.education = toMap(education)
  dictMaps.candidate_source = toMap(source)
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
.tag-item {
  margin-right: 4px;
}
.import-tip {
  font-size: 12px;
  color: #909399;
  margin-bottom: 10px;
  line-height: 1.6;
}
.import-btn {
  margin-top: 10px;
}
.import-result {
  margin-top: 14px;
}
.report-block {
  margin-top: 8px;
}
.report-title {
  font-size: 12px;
  font-weight: 500;
  color: #606266;
  margin-bottom: 4px;
}
.report-line {
  font-size: 12px;
  color: #909399;
  line-height: 1.8;
}
.tag-add {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
</style>
