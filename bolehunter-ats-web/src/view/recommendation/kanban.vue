<!--
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 -->
<template>
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar">
      <span class="page-title">推荐看板</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">新增推荐</el-button>
    </div>
    <div class="filters">
      <el-select v-model="filters.job_id" placeholder="按职位筛选" clearable filterable style="width: 280px" @change="getAll">
        <el-option v-for="j in jobOptions" :key="j.id" :value="j.id"
          :label="`${j.title}${j.company_name ? '（' + j.company_name + '）' : ''}`" />
      </el-select>
      <el-select v-model="filters.owner_id" placeholder="按顾问筛选" clearable filterable style="width: 150px" @change="getAll">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
    </div>
    <div class="kanban">
      <div v-for="stage in stages" :key="stage.value" class="kanban-column"
        @dragover.prevent @drop.prevent="onDrop(stage.value)">
        <div class="column-header">
          <span class="column-title">{{ stage.label }}</span>
          <span class="column-count">{{ groupItems(stage.value).length }}</span>
        </div>
        <div class="column-body">
          <div v-for="item in groupItems(stage.value)" :key="item.id" class="card" draggable="true"
            @dragstart="onDragStart(item, $event)" @click="handleCardClick(item)">
            <div class="card-name">{{ item.candidate_name || '候选人#' + item.candidate_id }}</div>
            <div class="card-sub">{{ item.candidate_company || '—' }} · {{ item.candidate_position || '—' }}</div>
            <div class="card-sub">{{ item.job_title || '—' }}</div>
            <div class="card-footer">
              <span>{{ formatDate(item.stage_time) }}</span>
              <span v-if="item.feedback" class="feedback-dot">反馈</span>
            </div>
          </div>
          <div v-if="groupItems(stage.value).length === 0" class="empty-column">拖拽卡片到此</div>
        </div>
      </div>
    </div>

    <!-- 新增推荐 -->
    <el-dialog v-model="createDialogVisible" title="新增推荐" width="460px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item label="职位" prop="jobId">
          <el-select v-model="createForm.jobId" filterable style="width: 100%">
            <el-option v-for="j in jobOptions" :key="j.id" :value="j.id"
              :label="`${j.title}${j.company_name ? '（' + j.company_name + '）' : ''}`" />
          </el-select>
        </el-form-item>
        <el-form-item label="候选人" prop="candidateId">
          <el-select v-model="createForm.candidateId" filterable style="width: 100%">
            <el-option v-for="c in candidateOptions" :key="c.id" :value="c.id"
              :label="`${c.name}（${c.current_company || '—'}）`" />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="createForm.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmCreate">确 定</el-button>
        <el-button @click="createDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>

    <!-- 详情/流转/反馈 -->
    <el-dialog v-model="detailDialogVisible" title="推荐详情" width="560px">
      <el-form v-if="current" label-width="90px" size="small">
        <el-form-item label="候选人">{{ current.candidate_name || '#' + current.candidate_id }}
          （{{ current.candidate_company || '—' }} · {{ current.candidate_position || '—' }}）</el-form-item>
        <el-form-item label="职位">{{ current.job_title || '—' }}</el-form-item>
        <el-form-item label="客户">{{ current.client_name || '—' }}</el-form-item>
        <el-form-item label="负责人">{{ current.owner_name || '未分配' }}</el-form-item>
        <el-form-item label="当前阶段">
          <el-select v-model="editStage" style="width: 100%">
            <el-option v-for="s in stages" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户反馈">
          <el-input v-model="editFeedback" type="textarea" :rows="3" maxlength="5000"
            placeholder="顾问代录：微信/电话沟通后记录客户 HR 的反馈意见" />
        </el-form-item>
      </el-form>
      <el-tabs v-if="current" v-model="detailTab" style="margin-top: 4px">
        <el-tab-pane label="沟通记录" name="comm">
          <div class="comm-add">
            <el-select v-model="commForm.type" size="small" style="width: 90px">
              <el-option label="电话" value="phone" />
              <el-option label="微信" value="wechat" />
              <el-option label="邮件" value="email" />
              <el-option label="面谈" value="meeting" />
              <el-option label="备注" value="note" />
            </el-select>
            <el-input v-model="commForm.content" size="small" maxlength="1000" placeholder="记录与客户 HR 的沟通内容"
              @keyup.enter="confirmAddComm" />
            <el-button type="primary" size="small" @click="confirmAddComm">添加</el-button>
          </div>
          <div v-for="comm in communications" :key="comm.id" class="comm-row">
            <el-tag size="small" class="comm-type">{{ commTypeLabel(comm.type) }}</el-tag>
            <span class="comm-content">{{ comm.content }}</span>
            <span class="comm-meta">{{ comm.communicator_name || '' }} · {{ formatDateTime(comm.communicate_time) }}</span>
          </div>
          <el-empty v-if="communications.length === 0" description="暂无沟通记录，来记录第一次沟通吧" :image-size="50" />
        </el-tab-pane>
        <el-tab-pane label="跟进任务" name="tasks">
          <div style="margin-bottom: 8px">
            <el-button type="primary" size="small" plain icon="Plus" @click="confirmFollowTask">创建跟进任务（明天09:00）</el-button>
          </div>
          <div v-for="task in relatedTasks" :key="task.id" class="task-line">
            <span class="task-line-title" :class="{ done: task.status === 1 }">{{ task.title }}</span>
            <span class="task-line-meta">{{ formatDateTime(task.due_time) }} · {{ task.status === 1 ? '已完成' : '待办' }}</span>
            <el-button v-if="task.status === 0" size="small" type="success" plain @click="handleDoneTask(task)">完成</el-button>
          </div>
          <el-empty v-if="relatedTasks.length === 0" description="暂无跟进任务" :image-size="50" />
        </el-tab-pane>
        <el-tab-pane label="流转历史" name="history">
          <el-timeline v-if="stageHistory.length" style="padding-left: 6px">
            <el-timeline-item v-for="h in stageHistory" :key="h.id" :timestamp="formatDateTime(h.create_time)">
              {{ h.from_stage ? (stageLabelMap[h.from_stage] || h.from_stage) + ' → ' : '创建：' }}{{ stageLabelMap[h.to_stage] || h.to_stage }}
              <span class="history-operator">（{{ h.operator_name || '—' }}）</span>
              <div v-if="h.feedback" class="history-feedback">{{ h.feedback }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无流转历史" :image-size="50" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button type="primary" @click="confirmDetailSave">保 存</el-button>
        <el-button @click="detailDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  STAGES, createRecommendation, getJobOptions, getKanban, updateRecommendationFeedback,
  updateRecommendationStage,
} from '@/api/recommendation'
import { getCandidateOptions } from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import {
  createCommunication, createTask, doneTask, getCommunications, getRelatedTasks, getStageHistory,
} from '@/api/workbench'

const stages = STAGES
const stageLabelMap = Object.fromEntries(STAGES.map(s => [s.value, s.label]))
const loading = ref(false)
const items = ref([])
const jobOptions = ref([])
const candidateOptions = ref([])
const owners = ref([])
const filters = reactive({ job_id: '', owner_id: '' })
const dragging = ref(null)
const createDialogVisible = ref(false)
const createFormRef = ref()
const createForm = reactive({ jobId: null, candidateId: null, ownerId: null, remark: '' })
const createRules = {
  jobId: [{ required: true, message: '请选择职位', trigger: 'change' }],
  candidateId: [{ required: true, message: '请选择候选人', trigger: 'change' }],
}
const detailDialogVisible = ref(false)
const current = ref(null)
const editStage = ref('')
const editFeedback = ref('')
const detailTab = ref('comm')
const communications = ref([])
const stageHistory = ref([])
const commForm = reactive({ type: 'phone', content: '' })
const relatedTasks = ref([])

const COMM_TYPE_LABELS = {
  phone: '电话', wechat: '微信', email: '邮件', meeting: '面谈', note: '备注', other: '其他',
}

function commTypeLabel(type) {
  return COMM_TYPE_LABELS[type] || type
}

function formatDateTime(val) {
  if (!val) return ''
  return String(val).replace('T', ' ').slice(0, 16)
}

function groupItems(stage) {
  return items.value.filter(item => item.current_stage === stage)
}

function formatDate(val) {
  if (!val) return ''
  const d = new Date(val)
  return `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

async function getAll() {
  loading.value = true
  try {
    const params = {}
    if (filters.job_id) params.job_id = filters.job_id
    if (filters.owner_id) params.owner_id = filters.owner_id
    items.value = await getKanban(params)
  } finally {
    loading.value = false
  }
}

function onDragStart(item, event) {
  dragging.value = item
  if (event && event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

function onDrop(stage) {
  const item = dragging.value
  dragging.value = null
  if (!item || item.current_stage === stage) return
  ElMessageBox.confirm(`确认将「${item.candidate_name || '该候选人'}」流转到「${stageLabelMap[stage]}」？`, '提示')
    .then(async () => {
      const res = await updateRecommendationStage(item.id, { stage })
      ElMessage.success(res.message || '流转成功')
      await getAll()
    })
    .catch(() => {})
}

function handleCreate() {
  Object.assign(createForm, { jobId: null, candidateId: null, ownerId: null, remark: '' })
  createDialogVisible.value = true
}

function confirmCreate() {
  createFormRef.value.validate(async valid => {
    if (!valid) return
    const res = await createRecommendation(createForm)
    ElMessage.success(res.message || '新增成功')
    createDialogVisible.value = false
    await getAll()
  })
}

function handleCardClick(item) {
  current.value = item
  editStage.value = item.current_stage
  editFeedback.value = item.feedback || ''
  detailTab.value = 'comm'
  communications.value = []
  stageHistory.value = []
  detailDialogVisible.value = true
  loadCommunications(item.id)
  loadStageHistory(item.id)
  loadRelatedTasks(item.id)
}

async function loadCommunications(recommendationId) {
  try {
    const res = await getCommunications(recommendationId)
    communications.value = res.items || []
  } catch (e) {
    communications.value = []
  }
}

async function loadRelatedTasks(recommendationId) {
  try {
    relatedTasks.value = await getRelatedTasks('recommendation', recommendationId)
  } catch (e) {
    relatedTasks.value = []
  }
}

async function confirmFollowTask() {
  const due = new Date(Date.now() + 24 * 3600 * 1000)
  const pad = n => String(n).padStart(2, '0')
  const dueStr = `${due.getFullYear()}-${pad(due.getMonth() + 1)}-${pad(due.getDate())} 09:00:00`
  const res = await createTask({
    title: `跟进：${current.value.candidate_name || '候选人'}`,
    dueTime: dueStr,
    relatedType: 'recommendation',
    relatedId: current.value.id,
  })
  ElMessage.success(res.message || '已创建跟进任务')
  await loadRelatedTasks(current.value.id)
}

async function handleDoneTask(task) {
  const res = await doneTask(task.id)
  ElMessage.success(res.message || '任务已完成')
  await loadRelatedTasks(current.value.id)
}

async function loadStageHistory(recommendationId) {
  try {
    stageHistory.value = await getStageHistory(recommendationId)
  } catch (e) {
    stageHistory.value = []
  }
}

async function confirmAddComm() {
  if (!commForm.content.trim()) {
    ElMessage.warning('请输入沟通内容')
    return
  }
  const res = await createCommunication({
    recommendation_id: current.value.id,
    type: commForm.type,
    content: commForm.content.trim(),
  })
  ElMessage.success(res.message || '添加成功')
  commForm.content = ''
  await loadCommunications(current.value.id)
}

async function confirmDetailSave() {
  if (!current.value) return
  const stageChanged = editStage.value !== current.value.current_stage
  const feedbackChanged = editFeedback.value !== (current.value.feedback || '')
  if (!stageChanged && !feedbackChanged) {
    detailDialogVisible.value = false
    return
  }
  let res
  if (stageChanged) {
    res = await updateRecommendationStage(current.value.id, {
      stage: editStage.value,
      feedback: feedbackChanged ? editFeedback.value : undefined,
    })
  } else {
    res = await updateRecommendationFeedback(current.value.id, editFeedback.value)
  }
  ElMessage.success(res.message || '保存成功')
  detailDialogVisible.value = false
  await getAll()
}

onMounted(async () => {
  await getAll()
  jobOptions.value = await getJobOptions()
  candidateOptions.value = await getCandidateOptions()
  owners.value = await getClientOwners()
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
  gap: 10px;
  margin-bottom: 12px;
}
.kanban {
  display: flex;
  align-items: flex-start;
  overflow-x: auto;
  padding-bottom: 15px;
}
.kanban-column {
  flex: 0 0 178px;
  margin-right: 10px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}
.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
}
.column-title {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
}
.column-count {
  font-size: 12px;
  color: #909399;
  background: #e4e7ed;
  border-radius: 8px;
  padding: 0 7px;
}
.column-body {
  padding: 0 6px 8px;
  min-height: 120px;
}
.card {
  background: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 8px 10px;
  margin-bottom: 8px;
  cursor: grab;
}
.card:hover {
  border-color: #409eff;
}
.card-name {
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 3px;
}
.card-sub {
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 5px;
  font-size: 12px;
  color: #c0c4cc;
}
.feedback-dot {
  color: #e6a23c;
}
.empty-column {
  font-size: 12px;
  color: #c0c4cc;
  text-align: center;
  padding: 16px 0;
}
.comm-add {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}
.comm-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f2f5;
  font-size: 12px;
}
.comm-type {
  flex-shrink: 0;
}
.comm-content {
  flex: 1;
  color: #303133;
  line-height: 1.5;
}
.comm-meta {
  flex-shrink: 0;
  color: #c0c4cc;
}
.history-operator {
  color: #909399;
  font-size: 12px;
}
.task-line {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f2f5;
  font-size: 12px;
}
.task-line-title {
  flex: 1;
  color: #303133;
}
.task-line-title.done {
  color: #909399;
  text-decoration: line-through;
}
.task-line-meta {
  color: #c0c4cc;
}
.history-feedback {
  font-size: 12px;
  color: #606266;
  margin-top: 2px;
  background: #f5f7fa;
  border-radius: 4px;
  padding: 4px 8px;
}
</style>
