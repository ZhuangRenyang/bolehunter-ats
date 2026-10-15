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
      <span class="page-title">{{ t('kanban.title') }}</span>
      <el-button type="primary" plain size="small" icon="Plus" @click="handleCreate">{{ t('kanban.create') }}</el-button>
    </div>
    <div class="filters">
      <el-select v-model="filters.job_id" :placeholder="t('kanban.filterJob')" clearable filterable style="width: 280px" @change="getAll">
        <el-option v-for="j in jobOptions" :key="j.id" :value="j.id"
          :label="jobOptionLabel(j)" />
      </el-select>
      <el-select v-model="filters.owner_id" :placeholder="t('kanban.filterOwner')" clearable filterable style="width: 150px" @change="getAll">
        <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
      </el-select>
    </div>
    <div class="kanban">
      <div v-for="stage in stages" :key="stage" class="kanban-column"
        @dragover.prevent @drop.prevent="onDrop(stage)">
        <div class="column-header">
          <span class="column-title">{{ stageLabel(stage) }}</span>
          <span class="column-count">{{ groupItems(stage).length }}</span>
        </div>
        <div class="column-body">
          <div v-for="item in groupItems(stage)" :key="item.id" class="card" draggable="true"
            @dragstart="onDragStart(item, $event)" @click="handleCardClick(item)">
            <div class="card-name">{{ item.candidate_name || t('kanban.candidateNo', { id: item.candidate_id }) }}</div>
            <div class="card-sub">{{ item.candidate_company || '—' }} · {{ item.candidate_position || '—' }}</div>
            <div class="card-sub">{{ item.job_title || '—' }}</div>
            <div class="card-footer">
              <span>{{ formatDate(item.stage_time) }}</span>
              <span class="footer-badges">
                <el-tooltip :content="t('kanban.urgentTip')" placement="top">
                  <span v-if="urgentCounts[item.id]" class="task-dot">⏰ {{ urgentCounts[item.id] }}</span>
                </el-tooltip>
                <span v-if="item.feedback" class="feedback-dot">{{ t('kanban.feedbackTag') }}</span>
              </span>
            </div>
          </div>
          <div v-if="groupItems(stage).length === 0" class="empty-column">{{ t('kanban.emptyColumn') }}</div>
        </div>
      </div>
    </div>

    <!-- 新增推荐 -->
    <el-dialog v-model="createDialogVisible" :title="t('kanban.createTitle')" width="460px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="90px">
        <el-form-item :label="t('kanban.fieldJob')" prop="jobId">
          <el-select v-model="createForm.jobId" filterable style="width: 100%">
            <el-option v-for="j in jobOptions" :key="j.id" :value="j.id" :label="jobOptionLabel(j)" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('kanban.fieldCandidate')" prop="candidateId">
          <el-select v-model="createForm.candidateId" filterable style="width: 100%">
            <el-option v-for="c in candidateOptions" :key="c.id" :value="c.id"
              :label="`${c.name}（${c.current_company || '—'}）`" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.owner')">
          <el-select v-model="createForm.ownerId" clearable filterable style="width: 100%">
            <el-option v-for="u in owners" :key="u.id" :label="u.nickname" :value="u.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('common.remark')">
          <el-input v-model="createForm.remark" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmCreate">{{ t('common.confirm') }}</el-button>
        <el-button @click="createDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>

    <!-- 详情/流转/反馈 -->
    <el-dialog v-model="detailDialogVisible" :title="t('kanban.detailTitle')" width="560px">
      <el-form v-if="current" label-width="90px" size="small">
        <el-form-item :label="t('kanban.fieldCandidate')">{{ current.candidate_name || '#' + current.candidate_id }}
          （{{ current.candidate_company || '—' }} · {{ current.candidate_position || '—' }}）</el-form-item>
        <el-form-item :label="t('kanban.fieldJob')">{{ current.job_title || '—' }}</el-form-item>
        <el-form-item :label="t('kanban.fieldClient')">{{ current.client_name || '—' }}</el-form-item>
        <el-form-item :label="t('common.owner')">{{ current.owner_name || t('common.notAssigned') }}</el-form-item>
        <el-form-item :label="t('kanban.fieldStage')">
          <el-select v-model="editStage" style="width: 100%">
            <el-option v-for="s in stages" :key="s" :label="stageLabel(s)" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('kanban.fieldFeedback')">
          <el-input v-model="editFeedback" type="textarea" :rows="3" maxlength="5000"
            :placeholder="t('kanban.feedbackPlaceholder')" />
        </el-form-item>
      </el-form>
      <el-tabs v-if="current" v-model="detailTab" style="margin-top: 4px">
        <el-tab-pane :label="t('kanban.tabComm')" name="comm">
          <div class="comm-add">
            <el-select v-model="commForm.type" size="small" style="width: 90px">
              <el-option v-for="(i18nKey, key) in COMM_TYPE_KEYS" :key="key" :label="t(i18nKey)" :value="key" />
            </el-select>
            <el-input v-model="commForm.content" size="small" maxlength="1000" :placeholder="t('kanban.commPlaceholder')"
              @keyup.enter="confirmAddComm" />
            <el-button type="primary" size="small" @click="confirmAddComm">{{ t('kanban.commAdd') }}</el-button>
          </div>
          <div v-for="comm in communications" :key="comm.id" class="comm-row">
            <el-tag size="small" class="comm-type">{{ commTypeLabel(comm.type) }}</el-tag>
            <span class="comm-content">{{ comm.content }}</span>
            <span class="comm-meta">{{ comm.communicator_name || '' }} · {{ formatDateTime(comm.communicate_time) }}</span>
          </div>
          <el-empty v-if="communications.length === 0" :description="t('kanban.commEmpty')" :image-size="50" />
        </el-tab-pane>
        <el-tab-pane :label="t('kanban.tabTasks')" name="tasks">
          <div style="margin-bottom: 8px">
            <el-button type="primary" size="small" plain icon="Plus" @click="confirmFollowTask">{{ t('kanban.followTaskBtn') }}</el-button>
          </div>
          <div v-for="task in relatedTasks" :key="task.id" class="task-line">
            <span class="task-line-title" :class="{ done: task.status === 1 }">{{ task.title }}</span>
            <span class="task-line-meta">{{ formatDateTime(task.due_time) }} · {{ task.status === 1 ? t('kanban.taskDone') : t('kanban.taskTodo') }}</span>
            <el-button v-if="task.status === 0" size="small" type="success" plain @click="handleDoneTask(task)">{{ t('workbench.done') }}</el-button>
          </div>
          <el-empty v-if="relatedTasks.length === 0" :description="t('kanban.tasksEmpty')" :image-size="50" />
        </el-tab-pane>
        <el-tab-pane :label="t('kanban.tabHistory')" name="history">
          <el-timeline v-if="stageHistory.length" style="padding-left: 6px">
            <el-timeline-item v-for="h in stageHistory" :key="h.id" :timestamp="formatDateTime(h.create_time)">
              {{ h.from_stage ? stageLabel(h.from_stage) + ' → ' : t('kanban.historyCreate') }}{{ stageLabel(h.to_stage) }}
              <span class="history-operator">（{{ h.operator_name || '—' }}）</span>
              <div v-if="h.feedback" class="history-feedback">{{ h.feedback }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else :description="t('kanban.historyEmpty')" :image-size="50" />
        </el-tab-pane>
      </el-tabs>
      <template #footer>
        <el-button type="warning" plain @click="handleGenConfirmLink">{{ t('kanban.confirmLinkBtn') }}</el-button>
        <el-button type="primary" @click="confirmDetailSave">{{ t('common.save') }}</el-button>
        <el-button @click="detailDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import {
  STAGES, createRecommendation, generateConfirmToken, getJobOptions, getKanban, getUrgentTaskCounts,
  updateRecommendationFeedback, updateRecommendationStage,
} from '@/api/recommendation'
import { getCandidateOptions } from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import {
  createCommunication, createTask, doneTask, getCommunications, getRelatedTasks, getStageHistory,
} from '@/api/workbench'

const { t } = useI18n()

// STAGES 现为枚举值数组（保序），显示文案统一经 stageLabel 走语言包 stage.*
const stages = STAGES

function stageLabel(stage) {
  const translated = t(`stage.${stage}`)
  return translated === `stage.${stage}` ? stage : translated
}

function jobOptionLabel(j) {
  return `${j.title}${j.company_name ? '（' + j.company_name + '）' : ''}`
}

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
const createRules = computed(() => ({
  jobId: [{ required: true, message: t('kanban.ruleJob'), trigger: 'change' }],
  candidateId: [{ required: true, message: t('kanban.ruleCandidate'), trigger: 'change' }],
}))
const detailDialogVisible = ref(false)
const current = ref(null)
const editStage = ref('')
const editFeedback = ref('')
const detailTab = ref('comm')
const communications = ref([])
const stageHistory = ref([])
const commForm = reactive({ type: 'phone', content: '' })
const relatedTasks = ref([])
const urgentCounts = ref({})

const COMM_TYPE_KEYS = { phone: 'kanban.commType.phone', wechat: 'kanban.commType.wechat', email: 'kanban.commType.email', meeting: 'kanban.commType.meeting', note: 'kanban.commType.note', other: 'kanban.commType.other' }

function commTypeLabel(type) {
  const key = COMM_TYPE_KEYS[type]
  // 未知类型兜底展示原始值
  return key ? t(key) : type
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
  await loadUrgentCounts()
}

/** 批量加载看板卡片的紧急任务数（今日到期/已逾期） */
async function loadUrgentCounts() {
  const ids = items.value.map(item => item.id)
  if (!ids.length) {
    urgentCounts.value = {}
    return
  }
  try {
    urgentCounts.value = await getUrgentTaskCounts(ids)
  } catch (e) {
    urgentCounts.value = {}
  }
}

/** 生成/刷新免登录客户确认链接并复制到剪贴板 */
async function handleGenConfirmLink() {
  const res = await generateConfirmToken(current.value.id)
  const link = `${window.location.origin}${res.url}`
  try {
    await navigator.clipboard.writeText(link)
    ElMessageBox.alert(
      `<p>${t('kanban.linkCopied')}</p><code style="word-break:break-all">${link}</code><p style="color:#999">${t('kanban.linkHint')}</p>`,
      t('kanban.confirmLinkTitle'),
      { dangerouslyUseHTMLString: true, confirmButtonText: t('kanban.linkOk') },
    )
  } catch (e) {
    ElMessageBox.prompt(t('kanban.linkPrompt'), t('kanban.confirmLinkTitle'), {
      inputValue: link, readOnly: true, confirmButtonText: t('kanban.linkClose'), showCancelButton: false,
    })
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
  ElMessageBox.confirm(
    t('kanban.confirmMove', { name: item.candidate_name || t('kanban.fieldCandidate'), stage: stageLabel(stage) }),
    t('common.tip'),
  )
    .then(async () => {
      const res = await updateRecommendationStage(item.id, { stage })
      ElMessage.success(res.message || t('kanban.moveOk'))
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
    ElMessage.success(res.message || t('kanban.createOk'))
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
    title: t('kanban.followTaskTitle', { name: current.value.candidate_name || t('kanban.fieldCandidate') }),
    dueTime: dueStr,
    relatedType: 'recommendation',
    relatedId: current.value.id,
  })
  ElMessage.success(res.message || t('kanban.followOk'))
  await loadRelatedTasks(current.value.id)
}

async function handleDoneTask(task) {
  const res = await doneTask(task.id)
  ElMessage.success(res.message || t('workbench.taskDone'))
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
    ElMessage.warning(t('kanban.commRequired'))
    return
  }
  const res = await createCommunication({
    recommendation_id: current.value.id,
    type: commForm.type,
    content: commForm.content.trim(),
  })
  ElMessage.success(res.message || t('kanban.addOk'))
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
  ElMessage.success(res.message || t('common.saveOk'))
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
.footer-badges {
  display: inline-flex;
  gap: 6px;
  align-items: center;
}
.task-dot {
  color: #f56c6c;
  font-weight: 600;
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
