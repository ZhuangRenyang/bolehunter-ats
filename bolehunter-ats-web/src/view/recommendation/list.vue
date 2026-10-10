<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="page-title">推荐列表</span>
    </div>
    <div class="filters">
      <el-select v-model="filters.job_id" placeholder="职位" clearable filterable style="width: 260px" @change="handleSearch">
        <el-option v-for="j in jobOptions" :key="j.id" :value="j.id"
          :label="`${j.title}${j.company_name ? '（' + j.company_name + '）' : ''}`" />
      </el-select>
      <el-select v-model="filters.stage" placeholder="阶段" clearable style="width: 130px" @change="handleSearch">
        <el-option v-for="s in stages" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
      <el-select v-model="filters.owner_id" placeholder="顾问" clearable filterable style="width: 130px" @change="handleSearch">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
      <el-button @click="handleReset">重置</el-button>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column label="候选人" width="120">
        <template #default="scope">
          <div>{{ scope.row.candidate_name || '#' + scope.row.candidate_id }}</div>
          <div class="sub-text">{{ scope.row.candidate_company || '—' }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="job_title" label="职位" min-width="140" show-overflow-tooltip />
      <el-table-column prop="client_name" label="客户" min-width="130" show-overflow-tooltip />
      <el-table-column label="阶段" width="100" align="center">
        <template #default="scope">
          <el-tag size="small" :type="stageTagType(scope.row.current_stage)">
            {{ stageLabelMap[scope.row.current_stage] || scope.row.current_stage }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="最近客户反馈" min-width="180" show-overflow-tooltip>
        <template #default="scope">{{ scope.row.feedback || '—' }}</template>
      </el-table-column>
      <el-table-column label="反馈时间" width="110" align="center">
        <template #default="scope">{{ formatDate(scope.row.feedback_time) }}</template>
      </el-table-column>
      <el-table-column label="进入阶段" width="110" align="center">
        <template #default="scope">{{ formatDate(scope.row.stage_time) }}</template>
      </el-table-column>
      <el-table-column label="顾问" width="90" align="center">
        <template #default="scope">{{ scope.row.owner_name || '未分配' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="170" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" plain @click="handleEdit(scope.row)">流转/反馈</el-button>
          <el-button size="small" type="danger" plain @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, jumper" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="editDialogVisible" title="阶段流转与客户反馈" width="460px">
      <el-form v-if="current" label-width="90px" size="small">
        <el-form-item label="候选人">{{ current.candidate_name || '#' + current.candidate_id }}</el-form-item>
        <el-form-item label="职位">{{ current.job_title || '—' }}（{{ current.client_name || '—' }}）</el-form-item>
        <el-form-item label="流转到">
          <el-select v-model="editStage" style="width: 100%">
            <el-option v-for="s in stages" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户反馈">
          <el-input v-model="editFeedback" type="textarea" :rows="4" maxlength="5000" placeholder="顾问代录：客户 HR 的反馈意见" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmEdit">保 存</el-button>
        <el-button @click="editDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  STAGES, deleteRecommendation, exportCsv, getJobOptions, getRecommendationPage, updateRecommendationStage,
} from '@/api/recommendation'
import { getClientOwners } from '@/api/client'
import moment from 'moment'

const stages = STAGES
const stageLabelMap = Object.fromEntries(STAGES.map(s => [s.value, s.label]))
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
  ElMessageBox.confirm(stageChanged ? '确认流转推荐阶段？' : '确认保存客户反馈？', '提示')
    .then(async () => {
      const res = await updateRecommendationStage(current.value.id, {
        stage: editStage.value,
        feedback: editFeedback.value,
      })
      ElMessage.success(res.message || '保存成功')
      editDialogVisible.value = false
      await getAll()
    })
    .catch(() => {})
}

function handleDelete(row) {
  ElMessageBox.confirm('是否删除该推荐记录？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteRecommendation(row.id)
    ElMessage.success(res.message || '删除成功')
    if (total.value % pageCount === 1 && currentPage.value !== 1) {
      currentPage.value--
    }
    await getAll()
  })
}

async function handleExport() {
  if (!filters.job_id && !filters.owner_id) {
    ElMessage.info('请先按职位筛选，或按顾问筛选后导出')
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
