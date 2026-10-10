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
      <span class="page-title">候选人列表</span>
      <el-space>
        <el-button plain size="small" icon="PriceTag" @click="tagDialogVisible = true">标签管理</el-button>
        <el-button type="primary" plain size="small" icon="Upload" @click="importDialogVisible = true">导入</el-button>
        <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">新建候选人</el-button>
      </el-space>
    </div>
    <div class="filters">
      <el-input v-model="filters.keyword" placeholder="姓名/电话/邮箱/公司/职位" clearable style="width: 220px"
        @keyup.enter="handleSearch" />
      <el-select v-model="filters.education" placeholder="学历" clearable style="width: 120px">
        <el-option v-for="(label, value) in dictMaps.education" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.source" placeholder="来源" clearable style="width: 130px">
        <el-option v-for="(label, value) in dictMaps.candidate_source" :key="value" :label="label" :value="value" />
      </el-select>
      <el-select v-model="filters.tag_ids" placeholder="标签" clearable multiple filterable collapse-tags style="width: 200px">
        <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
      </el-select>
      <el-select v-model="filters.owner_id" placeholder="负责人" clearable filterable style="width: 130px">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
      <el-button type="primary" icon="Search" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column label="姓名" width="110">
        <template #default="scope">
          <el-link type="primary" :underline="false" @click="handleDetail(scope.row)">{{ scope.row.name }}</el-link>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
      <el-table-column prop="current_company" label="当前公司" min-width="140" show-overflow-tooltip />
      <el-table-column prop="current_position" label="当前职位" min-width="130" show-overflow-tooltip />
      <el-table-column label="学历" width="80" align="center">
        <template #default="scope">{{ dictMaps.education[scope.row.education] || '—' }}</template>
      </el-table-column>
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column label="标签" min-width="150">
        <template #default="scope">
          <el-tag v-for="t in scope.row.tag_names" :key="t" size="small" class="tag-item">{{ t }}</el-tag>
          <span v-if="!scope.row.tag_names || scope.row.tag_names.length === 0">—</span>
        </template>
      </el-table-column>
      <el-table-column label="负责人" width="100" align="center">
        <template #default="scope">{{ ownerMap[scope.row.owner_id] || '未分配' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" align="center">
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

    <!-- 新建/编辑 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑候选人' : '新建候选人'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row>
          <el-col :span="12"><el-form-item label="姓名" prop="name"><el-input v-model="form.name" maxlength="50" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="电话"><el-input v-model="form.phone" maxlength="20" @blur="checkDuplicate" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" maxlength="100" @blur="checkDuplicate" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所在城市"><el-input v-model="form.city" maxlength="50" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12"><el-form-item label="当前公司"><el-input v-model="form.currentCompany" maxlength="100" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="当前职位"><el-input v-model="form.currentPosition" maxlength="100" /></el-form-item></el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="学历">
              <el-select v-model="form.education" clearable style="width: 100%">
                <el-option v-for="(label, value) in dictMaps.education" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="来源">
              <el-select v-model="form.source" style="width: 100%">
                <el-option v-for="(label, value) in dictMaps.candidate_source" :key="value" :label="label" :value="value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="form.ownerId" clearable filterable style="width: 100%">
                <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="标签">
              <el-select v-model="form.tagIds" multiple filterable style="width: 100%">
                <el-option v-for="t in tags" :key="t.id" :label="t.name" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-alert v-if="duplicateWarning" :title="duplicateWarning" type="warning" show-icon :closable="false" />
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmSubmit">确 定</el-button>
        <el-button @click="dialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 导入 -->
    <el-dialog v-model="importDialogVisible" title="导入候选人" width="560px" @closed="handleImportClosed">
      <el-tabs v-model="importTab">
        <el-tab-pane label="简历文件" name="resume">
          <div class="import-tip">支持 PDF / DOC / DOCX（≤10MB），自动解析姓名/电话/邮箱/学历，重复自动跳过。</div>
          <input type="file" accept=".pdf,.doc,.docx" @change="e => (resumeFile = e.target.files[0] || null)" />
          <el-button type="primary" size="small" class="import-btn" :disabled="!resumeFile" :loading="importing"
            @click="submitImport('resume')">开始导入</el-button>
        </el-tab-pane>
        <el-tab-pane label="Excel 批量" name="excel">
          <div class="import-tip">
            按模板填写后上传 XLSX/XLS，手机号或邮箱重复的行自动跳过。
            <el-link type="primary" :underline="false" @click="handleDownloadTemplate">下载导入模板</el-link>
          </div>
          <input type="file" accept=".xls,.xlsx" @change="e => (excelFile = e.target.files[0] || null)" />
          <el-button type="primary" size="small" class="import-btn" :disabled="!excelFile" :loading="importing"
            @click="submitImport('excel')">开始导入</el-button>
        </el-tab-pane>
      </el-tabs>
      <div v-if="importResult" class="import-result">
        <el-alert :title="`导入完成：共 ${importResult.total} 条，成功 ${importResult.success_count}，重复跳过 ${importResult.duplicate_count}，失败 ${importResult.fail_count}`"
          :type="importResult.fail_count > 0 ? 'warning' : 'success'" show-icon :closable="false" />
        <div v-if="importResult.duplicates && importResult.duplicates.length" class="report-block">
          <div class="report-title">重复明细</div>
          <div v-for="(item, index) in importResult.duplicates" :key="'d' + index" class="report-line">{{ item }}</div>
        </div>
        <div v-if="importResult.failures && importResult.failures.length" class="report-block">
          <div class="report-title">失败明细</div>
          <div v-for="(item, index) in importResult.failures" :key="'f' + index" class="report-line">{{ item }}</div>
        </div>
      </div>
    </el-dialog>

    <!-- 标签管理 -->
    <el-dialog v-model="tagDialogVisible" title="标签管理" width="420px">
      <div class="tag-add">
        <el-input v-model="newTagName" placeholder="输入新标签名" maxlength="50" style="width: 220px" />
        <el-button type="primary" @click="handleAddTag">添加</el-button>
      </div>
      <el-table :data="tags" size="small" border max-height="320">
        <el-table-column label="标签名">
          <template #default="scope">
            <el-input v-if="editingTagId === scope.row.id" v-model="editingTagName" size="small" />
            <span v-else>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" align="center">
          <template #default="scope">
            <template v-if="editingTagId === scope.row.id">
              <el-button size="small" type="primary" plain @click="handleSaveTag(scope.row)">保存</el-button>
              <el-button size="small" @click="editingTagId = null">取消</el-button>
            </template>
            <template v-else>
              <el-button size="small" type="primary" plain @click="handleEditTag(scope.row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteTag(scope.row)">删除</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createCandidate, createTag, deleteCandidate, deleteTag, duplicateCheck, getCandidatePage,
  getCandidateOptions, getTags, importExcel, importResume, updateCandidate, updateTag,
} from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import { getDictDataByType } from '@/api/setting'
import defaultAxios from '@/api/request'

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
const rules = { name: [{ required: true, message: '请输入候选人姓名', trigger: 'blur' }] }
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
      duplicateWarning.value = conflicts && conflicts.length ? `警告：${conflicts.join('、')}，保存将被拒绝` : ''
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
    ElMessage.success(res.message || '保存成功')
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
    ElMessage.success('导入完成')
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
  ElMessage.success(res.message || '添加成功')
  newTagName.value = ''
  tags.value = await getTags()
}

function handleEditTag(row) {
  editingTagId.value = row.id
  editingTagName.value = row.name
}

async function handleSaveTag(row) {
  const res = await updateTag(row.id, editingTagName.value.trim())
  ElMessage.success(res.message || '保存成功')
  editingTagId.value = null
  tags.value = await getTags()
  await getAll()
}

function handleDeleteTag(row) {
  ElMessageBox.confirm('删除标签将同时从候选人上移除该标签，是否继续？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteTag(row.id)
    ElMessage.success(res.message || '删除成功')
    tags.value = await getTags()
    await getAll()
  })
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
