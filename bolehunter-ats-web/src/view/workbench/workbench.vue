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
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">{{ summary.clientTotal }}</div>
          <div class="metric-label">客户公司</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">{{ summary.candidateTotal }}</div>
          <div class="metric-label">候选人库</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric">{{ summary.recommendationTotal }}</div>
          <div class="metric-label">推荐记录</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="metric warn">{{ overdueCount }}</div>
          <div class="metric-label">已逾期任务</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row-gap">
      <!-- 今日/逾期任务 -->
      <el-col :span="13">
        <el-card shadow="hover">
          <template #header>
            <div class="card-head">
              <span>我的任务（今日到期 / 已逾期）</span>
              <el-button type="primary" size="small" plain icon="Plus" @click="taskDialogVisible = true">新建任务</el-button>
            </div>
          </template>
          <el-empty v-if="tasks.length === 0" description="太棒了，没有待办任务" :image-size="60" />
          <div v-for="task in tasks" :key="task.id" class="task-row">
            <div class="task-main">
              <div class="task-title" :class="{ overdue: isOverdue(task) }">{{ task.title }}</div>
              <div class="task-sub">
                {{ formatTime(task.due_time) }} 前完成
                <el-tag v-if="isOverdue(task)" size="small" type="danger">已逾期</el-tag>
                <el-tag v-else size="small" type="warning">今日</el-tag>
              </div>
            </div>
            <div class="task-ops">
              <el-button size="small" type="success" plain @click="handleDone(task)">完成</el-button>
              <el-button size="small" type="danger" plain @click="handleDeleteTask(task)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 阶段分布漏斗 -->
      <el-col :span="11">
        <el-card shadow="hover">
          <template #header>推荐漏斗（按流程阶段）</template>
          <div ref="funnelRef" class="funnel-chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 数据看板增强：漏斗时间对比 / 顾问业绩 / 客户转化 -->
    <el-row :gutter="16" class="row-gap">
      <el-col :span="11">
        <el-card shadow="hover">
          <template #header>漏斗时间对比（近 30 天 vs 前 30 天各阶段新增）</template>
          <div ref="trendRef" class="funnel-chart"></div>
        </el-card>
      </el-col>
      <el-col :span="13">
        <el-card shadow="hover">
          <template #header>顾问业绩排行（本自然月）</template>
          <el-table :data="ranking" size="small" :empty-text="'本月暂无推荐记录'">
            <el-table-column type="index" label="#" width="44" />
            <el-table-column prop="nickname" label="顾问" min-width="90" show-overflow-tooltip />
            <el-table-column prop="total" label="新增推荐" width="84" sortable />
            <el-table-column prop="read_cnt" label="客户已读" width="84" sortable />
            <el-table-column prop="interview_cnt" label="面试" width="68" sortable />
            <el-table-column prop="offer_cnt" label="Offer" width="72" sortable />
            <el-table-column prop="paid_cnt" label="回款" width="68" sortable />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row class="row-gap">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-head">
              <span>客户转化率（按公司，取推荐量 Top 20）</span>
              <el-button type="primary" size="small" plain icon="Download" @click="handleExportConversion">导出 CSV</el-button>
            </div>
          </template>
          <el-table :data="conversion" size="small" empty-text="暂无客户推荐数据">
            <el-table-column prop="client_name" label="客户公司" min-width="140" show-overflow-tooltip />
            <el-table-column prop="total" label="推荐数" width="76" sortable />
            <el-table-column prop="read_cnt" label="客户已读" width="84" sortable />
            <el-table-column prop="interview_cnt" label="面试及之后" width="96" sortable />
            <el-table-column prop="offer_cnt" label="Offer及之后" width="100" sortable />
            <el-table-column prop="paid_cnt" label="回款" width="72" sortable />
            <el-table-column label="回款转化率" width="180">
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
        <el-card shadow="hover">
          <template #header>AI 找人 —— 用自然语言搜索候选人</template>
          <div class="ai-ask-bar">
            <el-input v-model="aiQuestion" maxlength="200" placeholder="例如：找一位在杭州有 5 年 Java 经验的候选人"
              @keyup.enter="handleAiAsk" />
            <el-button type="primary" :loading="aiAsking" @click="handleAiAsk">搜 索</el-button>
          </div>
          <template v-if="aiResult">
            <el-alert v-if="aiResult.answer" type="success" :closable="false" style="margin: 12px 0">
              <div style="white-space: pre-wrap">{{ aiResult.answer }}</div>
            </el-alert>
            <el-alert v-else-if="!aiResult.llm" type="warning" :closable="false" style="margin: 12px 0"
              title="大模型未配置或不可用，以下为关键词检索命中结果" />
            <el-empty v-if="(aiResult.candidates || []).length === 0 && !aiAsking" description="知识库中未找到相关候选人" :image-size="50" />
            <el-table v-else :data="aiResult.candidates || []" size="small">
              <el-table-column prop="name" label="姓名" width="110" />
              <el-table-column prop="current_company" label="当前公司" min-width="140" show-overflow-tooltip />
              <el-table-column prop="current_position" label="当前职位" min-width="120" show-overflow-tooltip />
              <el-table-column prop="city" label="城市" width="90" />
              <el-table-column prop="phone" label="电话" width="130" />
            </el-table>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建任务 -->
    <el-dialog v-model="taskDialogVisible" title="新建任务" width="440px">
      <el-form ref="taskFormRef" :model="taskForm" :rules="taskRules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="taskForm.title" maxlength="100" placeholder="如：给客户A打电话跟进张三推荐" />
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="taskForm.content" type="textarea" :rows="2" maxlength="500" />
        </el-form-item>
        <el-form-item label="到期时间" prop="dueTime">
          <el-date-picker v-model="taskForm.dueTime" type="datetime" placeholder="选择到期时间"
            value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="confirmCreateTask">确 定</el-button>
        <el-button @click="taskDialogVisible = false">取 消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
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

const STAGE_LABELS = {
  PENDING: '待推荐', RECOMMENDED: '已推荐', CLIENT_READ: '客户已读', INTERVIEW_PENDING: '待面试',
  INTERVIEWING: '面试中', OFFER_PENDING: '待Offer', OFFERED: 'Offer', ONBOARDED: '入职',
  PAID: '回款', ARCHIVED: '归档',
}
// 漏斗按业务流程顺序展示（归档不计入漏斗）
const FUNNEL_STAGES = [
  'PENDING', 'RECOMMENDED', 'CLIENT_READ', 'INTERVIEW_PENDING', 'INTERVIEWING',
  'OFFER_PENDING', 'OFFERED', 'ONBOARDED', 'PAID',
]

const summary = reactive({})
const tasks = ref([])
const taskDialogVisible = ref(false)
const taskFormRef = ref()
const taskForm = reactive({ title: '', content: '', dueTime: '' })
const taskRules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  dueTime: [{ required: true, message: '请选择到期时间', trigger: 'change' }],
}

const overdueCount = computed(() => (summary.overdueTasks || []).length)

function stageLabel(stage) {
  return STAGE_LABELS[stage] || stage
}

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
  renderFunnel()
  await loadDashboard()
}

// ---------- 数据看板增强（迭代14） ----------
const trendRef = ref()
const ranking = ref([])
const conversion = ref([])
let trendChart = null

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
  if (!trendRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendRef.value)
    window.addEventListener('resize', () => trendChart && trendChart.resize())
  }
  const stages = trend.filter(x => x.stage !== 'ARCHIVED')
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['近 30 天', '前 30 天'], bottom: 0 },
    grid: { left: 8, right: 12, top: 24, bottom: 34, containLabel: true },
    xAxis: {
      type: 'category',
      data: stages.map(x => STAGE_LABELS[x.stage] || x.stage),
      axisLabel: { fontSize: 10, interval: 0, rotate: 30 },
    },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '近 30 天', type: 'bar', barMaxWidth: 18, itemStyle: { color: '#409eff' }, data: stages.map(x => x.recent) },
      { name: '前 30 天', type: 'bar', barMaxWidth: 18, itemStyle: { color: '#c0c4cc' }, data: stages.map(x => x.prev) },
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
    return { name: STAGE_LABELS[stage], value: item ? item.count : 0 }
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
      label: { show: true, position: 'inside', formatter: '{b}: {c}' },
      itemStyle: { borderColor: '#fff', borderWidth: 1 },
      data,
    }],
  })
}

onBeforeUnmount(() => {
  if (chart) {
    chart.dispose()
    chart = null
  }
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
})

function handleDone(task) {
  ElMessageBox.confirm(`确认完成任务「${task.title}」？`, '提示').then(async () => {
    const res = await doneTask(task.id)
    ElMessage.success(res.message || '任务已完成')
    await getAll()
  }).catch(() => {})
}

function handleDeleteTask(task) {
  ElMessageBox.confirm('是否删除该任务？', '提示', { type: 'warning' }).then(async () => {
    const res = await deleteTask(task.id)
    ElMessage.success(res.message || '删除成功')
    await getAll()
  }).catch(() => {})
}

function confirmCreateTask() {
  taskFormRef.value.validate(async valid => {
    if (!valid) return
    const res = await createTask(taskForm)
    ElMessage.success(res.message || '新增成功')
    taskDialogVisible.value = false
    Object.assign(taskForm, { title: '', content: '', dueTime: '' })
    await getAll()
  })
}

onMounted(getAll)
</script>

<style scoped>
.metric {
  font-size: 30px;
  font-weight: 600;
  color: #303133;
}
.metric.warn {
  color: #f56c6c;
}
.metric-label {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
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
  border-bottom: 1px solid #f0f2f5;
}
.task-row:last-child {
  border-bottom: none;
}
.task-title {
  font-size: 13px;
  color: #303133;
}
.task-title.overdue {
  color: #f56c6c;
}
.task-sub {
  font-size: 12px;
  color: #909399;
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
