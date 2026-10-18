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
    <!-- 仪器化读数卡（迭代 29b 珍珠玻璃）：等宽大数字 + 单位上标 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="6">
        <div class="m-surface stat-card">
          <div class="metric-label">{{ t('workbench.metricClient') }}</div>
          <div class="metric-value num">{{ summary.clientTotal ?? 0 }}<span class="unit">{{ t('workbench.unitClient') }}</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="m-surface stat-card">
          <div class="metric-label">{{ t('workbench.metricCandidate') }}</div>
          <div class="metric-value num">{{ summary.candidateTotal ?? 0 }}<span class="unit">{{ t('workbench.unitCandidate') }}</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="m-surface stat-card">
          <div class="metric-label">{{ t('workbench.metricRecommendation') }}</div>
          <div class="metric-value num">{{ summary.recommendationTotal ?? 0 }}<span class="unit">{{ t('workbench.unitRecommendation') }}</span></div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="m-surface stat-card" :class="{ 'stat-warn': overdueCount > 0 }">
          <div class="metric-label">{{ t('workbench.metricOverdue') }}</div>
          <div class="metric-value num" :class="{ 'text-danger': overdueCount > 0 }">{{ overdueCount }}<span class="unit">{{ t('workbench.unitOverdue') }}</span></div>
        </div>
      </el-col>
    </el-row>

    <!-- 系统规格铭牌（迭代 29b）：来自健康检查接口的只读汇总 -->
    <div class="m-plate sys-plate">
      <span class="cell"><span class="k">SERVICE</span><span class="dot" :class="{ ok: sys.service === 'UP' }"></span>{{ sys.service || '—' }}</span>
      <span class="cell"><span class="k">DATABASE</span><span class="dot" :class="{ ok: sys.database === 'UP' }"></span>{{ sys.database || '—' }}</span>
      <span class="cell"><span class="k">UPTIME</span>{{ sys.uptime || '—' }}</span>
      <span class="cell"><span class="k">MODE</span>{{ t('appearance.' + (theme === 'dark' ? 'dark' : 'light')) }}</span>
    </div>

    <el-row :gutter="16" class="row-gap">
      <!-- 今日/逾期任务 -->
      <el-col :span="13">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>{{ t('workbench.myTasks') }}</span>
              <el-button type="primary" size="small" plain icon="Plus" @click="taskDialogVisible = true">{{ t('workbench.newTask') }}</el-button>
            </div>
          </template>
          <el-empty v-if="tasks.length === 0" :description="t('workbench.taskEmpty')" :image-size="60" />
          <div v-for="task in tasks" :key="task.id" class="task-row">
            <div class="task-main">
              <div class="task-title" :class="{ overdue: isOverdue(task) }">{{ task.title }}</div>
              <div class="task-sub">
                {{ formatTime(task.due_time) }} {{ t('workbench.duePrefix') }}
                <el-tag v-if="isOverdue(task)" size="small" type="danger">{{ t('workbench.tagOverdue') }}</el-tag>
                <el-tag v-else size="small" type="warning">{{ t('workbench.tagToday') }}</el-tag>
              </div>
            </div>
            <div class="task-ops">
              <el-button size="small" type="success" plain @click="handleDone(task)">{{ t('workbench.done') }}</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteTask(task)">{{ t('common.delete') }}</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 阶段分布漏斗 -->
      <el-col :span="11">
        <el-card shadow="never">
          <template #header>{{ t('workbench.funnel') }}</template>
          <div ref="funnelRef" class="funnel-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据看板增强：漏斗时间对比 / 顾问业绩 / 客户转化 -->
    <el-row :gutter="16" class="row-gap">
      <el-col :span="11">
        <el-card shadow="never">
          <template #header>{{ t('workbench.trend') }}</template>
          <div ref="trendRef" class="funnel-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="13">
        <el-card shadow="never">
          <template #header>{{ t('workbench.ranking') }}</template>
          <el-table :data="ranking" size="small" :empty-text="t('workbench.rankingEmpty')">
            <el-table-column type="index" label="#" width="44" />
            <el-table-column prop="nickname" :label="t('workbench.colConsultant')" min-width="90" show-overflow-tooltip />
            <el-table-column prop="total" :label="t('workbench.colNew')" width="84" sortable />
            <el-table-column prop="read_cnt" :label="t('workbench.colRead')" width="84" sortable />
            <el-table-column prop="interview_cnt" :label="t('workbench.colInterview')" width="68" sortable />
            <el-table-column prop="offer_cnt" :label="t('workbench.colOffer')" width="72" sortable />
            <el-table-column prop="paid_cnt" :label="t('workbench.colPaid')" width="68" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row class="row-gap">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>
            <div class="card-head">
              <span>{{ t('workbench.conversion') }}</span>
              <el-button type="primary" size="small" plain icon="Download" @click="handleExportConversion">{{ t('common.export') }}</el-button>
            </div>
          </template>
          <el-table :data="conversion" size="small" :empty-text="t('workbench.conversionEmpty')">
            <el-table-column prop="client_name" :label="t('workbench.colClient')" min-width="140" show-overflow-tooltip />
            <el-table-column prop="total" :label="t('workbench.colTotal')" width="76" sortable />
            <el-table-column prop="read_cnt" :label="t('workbench.colRead')" width="84" sortable />
            <el-table-column prop="interview_cnt" :label="t('workbench.colInterviewAfter')" width="96" sortable />
            <el-table-column prop="offer_cnt" :label="t('workbench.colOfferAfter')" width="100" sortable />
            <el-table-column prop="paid_cnt" :label="t('workbench.colPaid')" width="72" sortable />
            <el-table-column :label="t('workbench.colRate')" width="180">
              <template #default="{ row }">
                <div class="rate-cell">
                  <el-progress :percentage="Number(row.paid_rate) || 0" :stroke-width="10"
                    :color="Number(row.paid_rate) >= 30 ? '#67c23a' : Number(row.paid_rate) >= 10 ? '#e6a23c' : '#f56c6c'" />
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- AI 找人（迭代20 RAG） -->
    <el-row class="row-gap">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header>{{ t('workbench.aiTitle') }}</template>
          <div class="ai-ask-bar">
            <el-input v-model="aiQuestion" maxlength="200" :placeholder="t('workbench.aiPlaceholder')"
              @keyup.enter="handleAiAsk" />
            <el-button type="primary" :loading="aiAsking" @click="handleAiAsk">{{ t('common.search') }}</el-button>
          </div>
          <template v-if="aiResult">
            <el-alert v-if="aiResult.answer" type="success" :closable="false" style="margin: 12px 0">
              <div style="white-space: pre-wrap">{{ aiResult.answer }}</div>
            </el-alert>
            <el-alert v-else-if="!aiResult.llm" type="warning" :closable="false" style="margin: 12px 0"
              :title="t('workbench.aiDegraded')" />
            <el-empty v-if="(aiResult.candidates || []).length === 0 && !aiAsking" :description="t('workbench.aiEmpty')" :image-size="50" />
            <el-table v-else :data="aiResult.candidates || []" size="small">
              <el-table-column prop="name" :label="t('workbench.colName')" width="110" />
              <el-table-column prop="current_company" :label="t('workbench.colCompany')" min-width="140" show-overflow-tooltip />
              <el-table-column prop="current_position" :label="t('workbench.colPosition')" min-width="120" show-overflow-tooltip />
              <el-table-column prop="city" :label="t('workbench.colCity')" width="90" />
              <el-table-column prop="phone" :label="t('workbench.colPhone')" width="130" />
            </el-table>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建任务 -->
    <el-dialog v-model="taskDialogVisible" :title="t('workbench.taskDialogTitle')" width="440px">
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="90px">
        <el-form-item :label="t('workbench.fieldTitle')" prop="title">
          <el-input v-model="taskForm.title" maxlength="100" :placeholder="t('workbench.titlePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('workbench.fieldContent')">
          <el-input v-model="taskForm.content" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-form-item :label="t('workbench.fieldDue')" prop="dueTime">
          <el-date-picker v-model="taskForm.dueTime" type="datetime" :placeholder="t('workbench.duePlaceholder')"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmCreateTask">{{ t('common.confirm') }}</el-button>
        <el-button @click="taskDialogVisible = false">{{ t('common.cancel') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { TASK_STATUS } from '@/constants'
// ECharts 按需引入（迭代17 性能加固）：仅注册项目用到的图表与组件，主包瘦身
import * as echarts from 'echarts/core'
import { BarChart, FunnelChart } from 'echarts/charts'
import {
  GridComponent, LegendComponent, TooltipComponent,
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([BarChart, FunnelChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])
import { getWorkbenchSummary, getWorkbenchDashboard, exportClientConversionCsv, aiAskCandidate,
  getMyTasks, createTask, doneTask, deleteTask } from '@/api/workbench'
import { get } from '@/api/request'
import { countTo, revealCascade } from '@/composables/motion'
import { getTheme } from '@/composables/appearance'

const { t, locale } = useI18n()

// 推荐阶段名走 i18n 顶层 stage.* 命名空间（看板/列表/详情页共用同一份）
function stageLabel(stage) {
  const key = `stage.${stage}`
  const translated = t(key)
  // 未知阶段（如后端新增）兜底展示原始枚举值
  return translated === key ? stage : translated
}

// 漏斗按业务流程顺序展示（归档不计入漏斗）
const FUNNEL_STAGES = [
  'PENDING', 'RECOMMENDED', 'CLIENT_READ', 'INTERVIEW_PENDING', 'INTERVIEWING',
  'OFFER_PENDING', 'OFFERED', 'ONBOARDED', 'PAID',
]

// ---------- 珍珠玻璃：系统规格铭牌数据（迭代 29b） ----------
const sys = reactive({ service: '', database: '', uptime: '' })
const theme = ref(getTheme())

function formatUptime(seconds) {
  const s = Number(seconds) || 0
  if (s < 60) return `${s}s`
  const m = Math.floor(s / 60)
  if (m < 60) return `${m}m`
  const h = Math.floor(m / 60)
  if (h < 24) return `${h}h ${m % 60}m`
  return `${Math.floor(h / 24)}d ${h % 24}h`
}

async function loadHealth() {
  try {
    const res = await get('recruit/health')
    sys.service = res?.status || ''
    sys.database = res?.database || ''
    sys.uptime = formatUptime(res?.uptime_seconds)
  } catch (e) {
    sys.service = '—'
  }
}

// ---------- 图表配色跟随设计令牌（深浅主题切换后重渲染） ----------
function cssVar(name) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim()
}

const summary = reactive({})
const tasks = ref([])
const taskDialogVisible = ref(false)
const taskFormRef = ref()
const taskForm = reactive({ title: '', content: '', dueTime: '' })
// computed 包一层：切换语言后校验文案即时跟随
const taskRules = computed(() => ({
  title: [{ required: true, message: t('workbench.ruleTitle'), trigger: 'blur' }],
  dueTime: [{ required: true, message: t('workbench.ruleDue'), trigger: 'change' }],
}))

const overdueCount = computed(() => (summary.overdueTasks || []).length)

function stagePercent(count) {
  const total = (summary.stageStats || []).reduce((sum, item) => sum + item.count, 0)
  if (!total) return 0
  return Math.round((count / total) * 100)
}

function isOverdue(task) {
  return task.due_time && new Date(task.due_time).getTime() < Date.now()
}

function formatTime(val) {
  if (!val) return '—'
  return String(val).replace('T', ' ').slice(0, 16)
}

async function getAll() {
  const res = await getWorkbenchSummary()
  Object.assign(summary, res)
  const today = res.todayTasks || []
  const overdue = res.overdueTasks || []
  tasks.value = [...overdue, ...today]
  await nextTick()
  // 迭代 29e：读数滚动落定（'结'语态）+ 统计卡行级联进场
  const vals = [summary.clientTotal || 0, summary.candidateTotal || 0, summary.recommendationTotal || 0, overdueCount.value]
  document.querySelectorAll('.stat-card .metric-value').forEach((el, i) => countTo(el, vals[i]))
  revealCascade(document.querySelector('.stat-row'), '.stat-card')
  renderFunnel()
  await loadDashboard()
}

// ---------- 数据看板增强（迭代14） ----------
const trendRef = ref()
const ranking = ref([])
const conversion = ref([])
let trendChart = null
// 保留最近一次趋势数据：语言切换时用它整体重渲染图表文案
let lastTrend = []

async function loadDashboard() {
  try {
    const res = await getWorkbenchDashboard()
    ranking.value = res.consultantRanking || []
    conversion.value = res.clientConversion || []
    await nextTick()
    renderTrend(res.funnelTrend || [])
  } catch (e) {
    ranking.value = []
    conversion.value = []
  }
}

function renderTrend(trend) {
  lastTrend = trend
  if (!trendRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendRef.value)
    window.addEventListener('resize', () => trendChart && trendChart.resize())
  }
  const inkSoft = cssVar('--ink-soft')
  const stages = trend.filter(x => x.stage !== 'ARCHIVED')
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: [t('workbench.legendRecent'), t('workbench.legendPrev')], bottom: 0, textStyle: { color: inkSoft } },
    grid: { left: 8, right: 12, top: 24, bottom: 34, containLabel: true },
    xAxis: {
      type: 'category',
      data: stages.map(x => stageLabel(x.stage)),
      axisLabel: { fontSize: 10, interval: 0, rotate: 30, color: inkSoft },
      axisLine: { lineStyle: { color: cssVar('--tick-strong') } },
    },
    yAxis: { type: 'value', minInterval: 1, axisLabel: { color: inkSoft }, splitLine: { lineStyle: { color: cssVar('--tick') } } },
    series: [
      { name: t('workbench.legendRecent'), type: 'bar', barMaxWidth: 18, itemStyle: { color: cssVar('--blue') }, data: stages.map(x => x.recent) },
      { name: t('workbench.legendPrev'), type: 'bar', barMaxWidth: 18, itemStyle: { color: cssVar('--ink-faint') }, data: stages.map(x => x.prev) },
    ],
  })
}

async function handleExportConversion() {
  const blob = await exportClientConversionCsv()
  const url = window.URL.createObjectURL(new Blob([blob], { type: 'text/csv;charset=utf-8' }))
  const a = document.createElement('a')
  a.href = url
  a.download = 'client-conversion.csv'
  a.click()
  window.URL.revokeObjectURL(url)
}

// ---------- AI 找人（迭代20 RAG） ----------
const aiQuestion = ref('')
const aiAsking = ref(false)
const aiResult = ref(null)

async function handleAiAsk() {
  if (!aiQuestion.value.trim()) return
  aiAsking.value = true
  try {
    aiResult.value = await aiAskCandidate(aiQuestion.value.trim())
  } catch (e) {
    aiResult.value = null
  } finally {
    aiAsking.value = false
  }
}

// ---------- 漏斗图 ----------
const funnelRef = ref()
let chart = null

function renderFunnel() {
  if (!funnelRef.value) return
  if (!chart) {
    chart = echarts.init(funnelRef.value)
    window.addEventListener('resize', () => chart && chart.resize())
  }
  const stats = summary.stageStats || []
  const data = FUNNEL_STAGES.map(stage => {
    const item = stats.find(x => x.stage === stage)
    return { name: stageLabel(stage), value: item ? item.count : 0 }
  })
  chart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}' },
    series: [{
      type: 'funnel',
      left: '8%',
      width: '84%',
      top: 10,
      bottom: 10,
      minSize: '18%',
      sort: 'none',
      gap: 2,
      label: { show: true, position: 'inside', formatter: '{b}: {c}', color: '#fff' },
      itemStyle: {
        borderColor: cssVar('--pearl-solid') || '#fff',
        borderWidth: 1,
        // 蓝色系按阶段深度渐变（主色 → 浅）
        color: ({ dataIndex }) => {
          const blues = ['#1c57ba', '#236de9', '#4a86ec', '#6c9bf2', '#8fb0f5', '#a9c3f7', '#c2d4f9', '#d6e2fb', '#e9f0fc']
          return blues[dataIndex] || '#236de9'
        },
      },
      data,
    }],
  })
}

// 语言切换时重渲染两张图表（阶段名/图例即时跟随）
watch(locale, async () => {
  await nextTick()
  renderFunnel()
  renderTrend(lastTrend)
})

// 深浅主题切换时重渲染图表（文字/轴线颜色跟随设计令牌）
let themeObserver = null
onMounted(() => {
  themeObserver = new MutationObserver(() => {
    theme.value = getTheme()
    nextTick(() => {
      renderFunnel()
      renderTrend(lastTrend)
    })
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (themeObserver) themeObserver.disconnect()
})

function handleDone(task) {
  ElMessageBox.confirm(t('workbench.confirmDone', { name: task.title }), t('common.tip')).then(async () => {
    const res = await doneTask(task.id)
    ElMessage.success(res.message || t('workbench.taskDone'))
    await getAll()
  }).catch(() => {})
}

function handleDeleteTask(task) {
  ElMessageBox.confirm(t('workbench.confirmDeleteTask'), t('common.tip'), { type: 'warning' }).then(async () => {
    const res = await deleteTask(task.id)
    ElMessage.success(res.message || t('workbench.deleteOk'))
    await getAll()
  }).catch(() => {})
}

function confirmCreateTask() {
  taskFormRef.value.validate(async valid => {
    if (!valid) return
    const res = await createTask(taskForm)
    ElMessage.success(res.message || t('workbench.createOk'))
    taskDialogVisible.value = false
    Object.assign(taskForm, { title: '', content: '', dueTime: '' })
    await getAll()
  })
}

onMounted(() => {
  getAll()
  loadHealth()
})
</script>

<style scoped>
.stat-card {
  padding: 16px 18px;
  transition: transform 0.2s, box-shadow 0.2s;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--e2);
}
.metric-label {
  font-size: 12px;
  letter-spacing: var(--track-eyebrow);
  color: var(--ink-soft);
  margin-bottom: 6px;
}
.metric-value {
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  font-size: var(--fs-display-m);
  font-weight: 700;
  color: var(--ink);
  line-height: 1.1;
}
.metric-value .unit {
  font-size: 12px;
  font-weight: 500;
  color: var(--ink-soft);
  margin-left: 5px;
  vertical-align: super;
}
.metric-value.text-danger {
  color: var(--red-text);
}
.stat-warn {
  border-color: rgba(224, 68, 59, 0.25);
}

/* 系统规格铭牌 */
.sys-plate {
  display: flex;
  gap: 28px;
  margin-top: 14px;
  padding: 10px 16px;
  font-family: var(--font-mono);
  font-variant-numeric: tabular-nums;
  font-size: 12px;
  flex-wrap: wrap;
}
.sys-plate .cell { display: inline-flex; align-items: center; gap: 7px; }
.sys-plate .k {
  font-family: var(--font-sans);
  font-size: 10px;
  letter-spacing: var(--track-eyebrow);
  color: var(--plate-soft);
}
.sys-plate .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--ink-faint);
  display: inline-block;
}
.sys-plate .dot.ok {
  background: var(--leaf);
  box-shadow: 0 0 6px var(--leaf);
}

.row-gap {
  margin-top: 16px;
}
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.task-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px;
  border-bottom: 1px solid var(--glass-line);
}
.task-row:last-child {
  border-bottom: none;
}
.task-title {
  font-size: 13px;
  color: var(--ink);
}
.task-title.overdue {
  color: var(--red-text);
}
.task-sub {
  font-size: 12px;
  color: var(--ink-soft);
  margin-top: 2px;
  display: flex;
  gap: 6px;
  align-items: center;
}
.task-ops {
  display: flex;
}
.funnel-chart {
  height: 300px;
  width: 100%;
}
.rate-cell {
  width: 90%;
}
.ai-ask-bar {
  display: flex;
  gap: 10px;
}
</style>
