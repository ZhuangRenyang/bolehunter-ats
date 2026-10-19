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
    </div>
    <div class="filters">
      <el-select v-model="filters.job_id" :placeholder="t('rlist.filterJob')" clearable filterable style="width: 260px" @change="handleSearch">
        <el-option v-for="j in jobOptions" :key="j.id" :value="j.id"
          :label="`${j.title}${j.company_name ? '（' + j.company_name + '）' : ''}`" />
      </el-select>
      <el-select v-model="filters.stage" :placeholder="t('rlist.filterStage')" clearable style="width: 130px" @change="handleSearch">
        <el-option v-for="s in stages" :key="s" :label="stageLabel(s)" :value="s" />
      </el-select>
      <el-select v-model="filters.owner_id" :placeholder="t('rlist.filterOwner')" clearable filterable style="width: 130px" @change="handleSearch">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
      <el-button @click="handleReset">{{ t('rlist.reset') }}</el-button>
    </div>
    <el-table v-if="!isMobile" :data="tableData" border v-loading="loading">
      <el-table-column :label="t('rlist.colCandidate')" width="120">
        <template #default="scope">
          <div>{{ scope.row.candidate_name || '#' + scope.row.candidate_id }}</div>
          <div class="sub-text">{{ scope.row.candidate_company || '—' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="job_title" :label="t('rlist.colJob')" min-width="140" show-overflow-tooltip />
      <el-table-column prop="client_name" :label="t('rlist.colClient')" min-width="130" show-overflow-tooltip />
      <el-table-column :label="t('rlist.colStage')" width="100" align="center">
        <template #default="scope">
          <el-tag size="small" :type="stageTagType(scope.row.current_stage)">
            {{ stageLabel(scope.row.current_stage) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="t('rlist.colFeedback')" min-width="180" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.feedback || '—' }}</template>
      </el-table-column>
      <el-table-column :label="t('rlist.colFeedbackTime')" width="110" align="center">
        <template #default="scope">{{ formatDate(scope.row.feedback_time) }}</template>
      </el-table-column>
      <el-table-column :label="t('rlist.colStageTime')" width="110" align="center">
        <template #default="scope">{{ formatDate(scope.row.stage_time) }}</template>
      </el-table-column>
      <el-table-column :label="t('rlist.filterOwner')" width="90" align="center">
        <template #default="scope">{{ scope.row.owner_name || t('common.notAssigned') }}</template>
      </el-table-column>
      <el-table-column :label="t('common.operation')" width="170" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">{{ t('rlist.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">{{ t('common.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 移动端：卡片流替代表格，避免 9 列必须横向滚动 -->
    <div v-else class="m-list" v-loading="loading">
      <div v-for="row in tableData" :key="row.id" class="m-item">
        <div class="m-item__head">
          <span class="m-item__title">{{ row.candidate_name || '#' + row.candidate_id }}</span>
          <el-tag size="small" :type="stageTagType(row.current_stage)">{{ stageLabel(row.current_stage) }}</el-tag>
        </div>
        <div class="m-item__meta">{{ [row.job_title, row.client_name].filter(Boolean).join(' · ') || '—' }}</div>
        <div class="m-item__meta">负责人：{{ row.owner_name || t('common.notAssigned') }}</div>
        <div v-if="row.feedback" class="m-item__meta">{{ row.feedback }}</div>
        <div class="m-item__ops">
          <el-button size="small" type="primary" plain @click="handleEdit(row)">{{ t('rlist.edit') }}</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(row)">{{ t('common.delete') }}</el-button>
        </div>
      </div>
      <el-empty v-if="tableData.length === 0" :description="t('common.noData')" :image-size="50" />
    </div>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="editDialogVisible" :title="t('rlist.dialogTitle')" width="460px">
      <el-form v-if="current" label-width="90px" size="small">
        <el-form-item :label="t('rlist.colCandidate')">{{ current.candidate_name || '#' + current.candidate_id }}</el-form-item>
        <el-form-item :label="t('rlist.colJob')">{{ current.job_title || '—' }}（{{ current.client_name || '—' }}）</el-form-item>
        <el-form-item :label="t('rlist.moveTo')">
          <el-select v-model="editStage" style="width: 100%">
            <el-option v-for="s in stages" :key="s" :label="stageLabel(s)" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('rlist.feedback')">
          <el-input v-model="editFeedback" type="textarea" :rows="4" maxlength="5000" :placeholder="t('rlist.feedbackPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmEdit">{{ t('common.save') }}</el-button>
        <el-button @click="editDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  STAGES, deleteRecommendation, exportCsv, getJobOptions, getRecommendationPage, updateRecommendationStage,
} from '@/api/recommendation'
import { getClientOwners } from '@/api/client'
import { useIsMobile } from '@/composables/useIsMobile'
import moment from 'moment'

const { t } = useI18n()
// 窄屏改卡片流（无需横向滚动），宽屏保持表格
const isMobile = useIsMobile()

// STAGES 现为枚举值数组（保序），显示文案统一经 stageLabel 走语言包 stage.*
const stages = STAGES

function stageLabel(stage) {
  const translated = t(`stage.${stage}`)
  return translated === `stage.${stage}` ? stage : translated
}

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageCount = 10
const jobOptions = ref([])
const owners = ref([])
const filters = reactive({ job_id: '', stage: '', owner_id: '' })
const editDialogVisible = ref(false)
const current = ref(null)
const editStage = ref('')
const editFeedback = ref('')

function stageTagType(stage) {
  if (stage === 'OFFERED' || stage === 'ONBOARDED' || stage === 'PAID') return 'success'
  if (stage === 'ARCHIVED') return 'info'
  if (stage === 'PENDING') return ''
  return 'warning'
}

function formatDate(val) {
  if (!val) return '—'
  return moment(val).format('YYYY-MM-DD')
}

async function getAll() {
  loading.value = true
  try {
    const params = { page: currentPage.value - 1, count: pageCount }
    if (filters.job_id) params.job_id = filters.job_id
    if (filters.stage) params.stage = filters.stage
    if (filters.owner_id) params.owner_id = filters.owner_id
    const res = await getRecommendationPage(params)
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
  Object.assign(filters, { job_id: '', stage: '', owner_id: '' })
  handleSearch()
}

async function handlePageChange(val) {
  currentPage.value = val
  await getAll()
}

function handleEdit(row) {
  current.value = row
  editStage.value = row.current_stage
  editFeedback.value = row.feedback || ''
  editDialogVisible.value = true
}

function confirmEdit() {
  const stageChanged = current.value && editStage.value !== current.value.current_stage
  const feedbackChanged = editFeedback.value !== (current.value ? current.value.feedback || '' : '')
  if (!stageChanged && !feedbackChanged) {
    editDialogVisible.value = false
    return
  }
  ElMessageBox.confirm(stageChanged ? t('rlist.confirmStage') : t('rlist.confirmFeedback'), t('common.tip'))
    .then(async () => {
      const res = await updateRecommendationStage(current.value.id, {
        stage: editStage.value,
        feedback: editFeedback.value,
      })
      ElMessage.success(res.message || t('common.saveOk'))
      editDialogVisible.value = false
      await getAll()
    })
    .catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm(t('rlist.confirmDelete'), t('common.tip'), { type: 'warning' }).then(async () => {
    const res = await deleteRecommendation(row.id)
    ElMessage.success(res.message || t('common.deleted'))
    if (total.value % pageCount === 1 && currentPage.value !== 1) {
      currentPage.value--
    }
    await getAll()
  })
}

async function handleExport() {
  if (!filters.job_id && !filters.owner_id) {
    ElMessage.info(t('rlist.exportNeedFilter'))
    return
  }
  try {
    const params = {}
    if (filters.job_id) params.job_id = filters.job_id
    if (filters.owner_id) params.owner_id = filters.owner_id
    const blob = await exportCsv(params)
    const url = window.URL.createObjectURL(new Blob([blob]))
    const link = document.createElement('a')
    link.href = url
    link.download = `recommendations_${filters.job_id || 'all'}.csv`
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (e) {
    console.log(e)
  }
}

onMounted(async () => {
  await getAll()
  jobOptions.value = await getJobOptions()
  owners.value = await getClientOwners()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 14px;
}
.page-title {
  font-size: 15px;
  font-weight: 500;
}
.filters {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.sub-text {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}
</style>
