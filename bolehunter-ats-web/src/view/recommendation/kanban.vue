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
  <div>
    <div class="page-header">
      <el-button type="primary" icon="Plus" @click="handleCreate">{{ t('kanban.create') }}</el-button>
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
      <div v-for="stage in stages" :key="stage" class="kanban-column" :class="[stageCatClass(stage), { 'drag-over': dragStage === stage }]"
        @dragover.prevent="dragStage = stage" @dragleave.prevent="dragStage === stage && (dragStage = '')" @drop.prevent="onDrop(stage)">
        <div class="column-header">
          <span class="column-title">{{ stageLabel(stage) }}</span>
          <span class="column-count num">{{ groupItems(stage).length }}</span>
        </div>
        <div class="column-body">
          <div v-for="item in groupItems(stage)" :key="item.id" class="card" draggable="true"
            @dragstart="onDragStart(item, $event)" @click="handleCardClick(item)">
            <div class="card-name">{{ item.candidate_name || t('kanban.candidateNo', { id: item.candidate_id }) }}</div>
            <div class="card-sub">{{ item.candidate_company || '—' }} · {{ item.candidate_position || '—' }}</div>
            <div class="card-sub">{{ item.job_title || '—' }}</div>
            <div class="card-footer">
              <span class="num">{{ formatDate(item.stage_time) }}</span>
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
        <el-form-item :label="t('kanban.fieldOrderNo')"><span class="mono">{{ current.order_no || '—' }}</span></el-form-item>
        <el-form-item :label="t('kanban.fieldOrderNo')"><span class="mono">{{ current.order_no || '—' }}</span></el-form-item>
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
            <span class="task-line-title" :class="{ done: task.status === TASK_STATUS.DONE }">{{ task.title }}</span>
            <span class="task-line-meta">{{ formatDateTime(task.due_time) }} · {{ task.status === TASK_STATUS.DONE ? t('kanban.taskDone') : t('kanban.taskTodo') }}</span>
            <el-button v-if="task.status === TASK_STATUS.TODO" size="small" type="success" plain @click="handleDoneTask(task)">{{ t('workbench.done') }}</el-button>
          </div>
          <el-empty v-if="relatedTasks.length === 0" :description="t('kanban.tasksEmpty')" :image-size="50" />
        </el-tab-pane>
        <el-tab-pane :label="t('kanban.tabHistory')" name="history">
          <!-- 迭代 29e：推荐溯源图谱（珍珠球舞台） -->
          <StageTraceGraph v-if="stageHistory.length" :history="stageHistory" :current-stage="current ? current.current_stage : ''" />
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
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { TASK_STATUS } from '@/constants'
import {
  STAGES, createRecommendation, generateConfirmToken, getJobOptions, getKanban, getUrgentTaskCounts,
  updateRecommendationFeedback, updateRecommendationStage,
} from '@/api/recommendation'
import { getCandidateOptions } from '@/api/candidate'
import { getClientOwners } from '@/api/client'
import {
  createCommunication, createTask, doneTask, getCommunications, getRelatedTasks, getStageHistory,
} from '@/api/workbench'
import StageTraceGraph from './StageTraceGraph.vue'
import { revealCascade } from '@/composables/motion'

const { t } = useI18n()

// STAGES 现为枚举值数组（保序），显示文案统一经 stageLabel 走语言包 stage.*
const stages = STAGES

// 看板列分类色（迭代 29d 珍珠玻璃）：只区分类别不表达状态，低饱和浅底深字
const STAGE_CAT = {
  PENDING: 'c2', RECOMMENDED: 'c1', CLIENT_READ: 'c3', INTERVIEW_PENDING: 'c4',
  INTERVIEWING: 'c5', OFFER_PENDING: 'c2', OFFERED: 'c1', ONBOARDED: 'c3',
  PAID: 'c5', ARCHIVED: 'c4',
}
function stageCatClass(stage) {
  return STAGE_CAT[stage] || 'c2'
}

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
const firstLoad = ref(true)
const dragStage = ref('')
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
  // 迭代 29e：首次加载卡片级联落位（拖拽刷新不重复跑）
  if (firstLoad.value) {
    await nextTick()
    revealCascade(document.querySelector('.kanban'), '.card')
    firstLoad.value = false
  }
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
  dragStage.value = ''
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
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.page-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--ink);
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
  flex: 0 0 184px;
  margin-right: 10px;
  background: var(--paper-deep);
  border-radius: var(--r-md);
  border: 1px solid var(--glass-line);
  border-top: 3px solid var(--cat-color, var(--blue));
  transition: border-color 0.2s, background 0.2s;
}
/* 拖拽悬停高亮：目标列蓝色描边 + 浅蓝底 */
.kanban-column.drag-over {
  border-color: var(--blue);
  background: var(--blue-soft);
  box-shadow: var(--e2);
}
/* 分类色注入列顶色条与卡片色条（c1~c5 见 instruments/tokens） */
.kanban-column.c1 { --cat-color: var(--cat-1); }
.kanban-column.c2 { --cat-color: var(--cat-2); }
.kanban-column.c3 { --cat-color: var(--cat-3); }
.kanban-column.c4 { --cat-color: var(--cat-4); }
.kanban-column.c5 { --cat-color: var(--cat-5); }
.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 9px 10px 6px;
}
.column-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
}
.column-count {
  font-size: 11px;
  color: var(--blue);
  background: var(--blue-soft);
  border-radius: 999px;
  padding: 1px 8px;
  font-weight: 600;
}
.column-body {
  padding: 0 7px 10px;
  min-height: 120px;
}
.card {
  background: var(--pearl-solid);
  border: 1px solid var(--glass-line);
  border-left: 3px solid var(--cat-color, var(--blue));
  border-radius: var(--r-sm);
  padding: 9px 11px;
  margin-bottom: 8px;
  cursor: grab;
  box-shadow: var(--e1);
  transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;
}
.card:hover {
  transform: translateY(-1px);
  box-shadow: var(--e2);
  border-color: var(--blue);
}
.card:active {
  cursor: grabbing;
}
.card-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 3px;
}
.card-sub {
  font-size: 12px;
  color: var(--ink-soft);
  line-height: 1.5;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
  font-size: 12px;
  color: var(--ink-faint);
}
.feedback-dot {
  color: #d99a26;
}
.footer-badges {
  display: inline-flex;
  gap: 6px;
  align-items: center;
}
.task-dot {
  color: var(--red-text);
  font-weight: 600;
}
.empty-column {
  font-size: 12px;
  color: var(--ink-faint);
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
  border-bottom: 1px solid var(--glass-line);
  font-size: 12px;
}
.comm-type {
  flex-shrink: 0;
}
.comm-content {
  flex: 1;
  color: var(--ink);
  line-height: 1.5;
}
.comm-meta {
  flex-shrink: 0;
  color: var(--ink-faint);
}
.history-operator {
  color: var(--ink-soft);
  font-size: 12px;
}
.task-line {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid var(--glass-line);
  font-size: 12px;
}
.task-line-title {
  flex: 1;
  color: var(--ink);
}
.task-line-title.done {
  color: var(--ink-faint);
  text-decoration: line-through;
}
.task-line-meta {
  color: var(--ink-faint);
}
.history-feedback {
  font-size: 12px;
  color: var(--ink-muted);
  margin-top: 2px;
  background: var(--paper-deep);
  border-radius: var(--r-xs);
  padding: 4px 8px;
}
</style>
