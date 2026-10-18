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
  <div class="trace-stage">
    <svg viewBox="0 0 320 320" ref="svgRef" class="trace-svg">
      <defs>
        <radialGradient id="pearlGrad" cx="35%" cy="30%" r="80%">
          <stop offset="0%" stop-color="#ffffff" />
          <stop offset="55%" stop-color="#eef2fb" />
          <stop offset="100%" stop-color="#d9e3f6" />
        </radialGradient>
      </defs>
      <!-- 流转轨迹：按时间顺序连接各节点 -->
      <polyline :points="linePoints" class="trace-line" ref="lineEl" fill="none" />
      <!-- 中心到最新节点的脉冲连线 -->
      <line v-if="lastNode" :x1="CX" :y1="CY" :x2="lastNode.x" :y2="lastNode.y" class="trace-pulse" />
      <!-- 历史节点（12 点方向顺时针按时间排布） -->
      <g v-for="(n, i) in nodes" :key="i" class="trace-node" :transform="`translate(${n.x},${n.y})`">
        <circle r="10" :class="['node-ring', catClass(n.stage)]" />
        <circle r="4" class="node-core" />
        <text :x="n.labelDx" :y="n.labelDy" class="node-label" :text-anchor="n.anchor">{{ n.label }}</text>
        <text :x="n.labelDx" :y="n.labelDy + 12" class="node-date" :text-anchor="n.anchor">{{ n.date }}</text>
      </g>
      <!-- 中心珍珠球：当前阶段 -->
      <g class="center-ball" :transform="`translate(${CX},${CY})`">
        <circle r="30" class="ball-halo" />
        <circle r="24" class="ball-body" />
        <circle r="24" class="ball-scan" />
        <text y="-2" class="ball-count num">{{ nodes.length }}</text>
        <text y="14" class="ball-sub">{{ t('kanban.traceCount') }}</text>
      </g>
    </svg>
    <div class="trace-caption">
      {{ t('kanban.traceCurrent') }}<b>{{ stageLabel(currentStage) }}</b>
      <span v-if="history.length"> · {{ t('kanban.traceFirst', { date: firstDate }) }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { drawLine, popIn } from '@/composables/motion'

const props = defineProps({
  /** 流转记录（stage_history，按时间升序） */
  history: { type: Array, default: () => [] },
  /** 当前阶段枚举 */
  currentStage: { type: String, default: '' },
})

const { t } = useI18n()
const CX = 160
const CY = 160
const R = 112

const svgRef = ref()
const lineEl = ref()
const nodeEls = []

/** 与看板一致的分类色映射（只区分类别） */
const STAGE_CAT = {
  PENDING: 'c2', RECOMMENDED: 'c1', CLIENT_READ: 'c3', INTERVIEW_PENDING: 'c4',
  INTERVIEWING: 'c5', OFFER_PENDING: 'c2', OFFERED: 'c1', ONBOARDED: 'c3',
  PAID: 'c5', ARCHIVED: 'c4',
}
function catClass(stage) {
  return STAGE_CAT[stage] || 'c2'
}
function stageLabel(stage) {
  const translated = t(`stage.${stage}`)
  return translated === `stage.${stage}` ? stage : translated
}

/** 节点：每次流转一个，12 点方向顺时针按时间均布 */
const nodes = computed(() => {
  const list = props.history || []
  const n = Math.max(list.length, 1)
  return list.map((h, i) => {
    const angle = (-90 + (360 / n) * i) * (Math.PI / 180)
    const x = CX + Math.cos(angle) * R
    const y = CY + Math.sin(angle) * R
    // 标签放节点外侧：按象限决定锚点方向
    const right = Math.cos(angle) >= 0
    return {
      x, y,
      label: stageLabel(h.to_stage),
      stage: h.to_stage,
      date: String(h.create_time || '').replace('T', ' ').slice(5, 16),
      labelDx: right ? 16 : -16,
      labelDy: Math.sin(angle) > 0.9 ? 14 : (Math.sin(angle) < -0.9 ? -8 : 4),
      anchor: right ? 'start' : 'end',
    }
  })
})

const linePoints = computed(() => nodes.value.map(n => `${n.x},${n.y}`).join(' '))
const lastNode = computed(() => nodes.value[nodes.value.length - 1] || null)
const firstDate = computed(() => {
  const h = (props.history || [])[0]
  return h ? String(h.create_time || '').replace('T', ' ').slice(0, 16) : ''
})

onMounted(() => {
  // "导"语态：轨迹描边生长 → "结"语态：节点级联落定
  drawLine(lineEl.value, 1.2, 0.15)
  nodeEls.forEach((el, i) => popIn(el, 0.2 + i * 0.12))
})
</script>

<style scoped>
.trace-stage {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 14px;
}
.trace-svg {
  width: 100%;
  max-width: 340px;
}
.trace-line {
  stroke: var(--blue);
  stroke-width: 1.5;
  opacity: 0.55;
  stroke-linejoin: round;
}
.trace-pulse {
  stroke: var(--blue);
  stroke-width: 1.5;
  stroke-dasharray: 4 4;
  opacity: 0.7;
}
.node-ring {
  fill: var(--pearl-solid);
  stroke-width: 2;
}
.node-ring.c1 { stroke: var(--cat-1); fill: var(--cat-1-bg); }
.node-ring.c2 { stroke: var(--cat-2); fill: var(--cat-2-bg); }
.node-ring.c3 { stroke: var(--cat-3); fill: var(--cat-3-bg); }
.node-ring.c4 { stroke: var(--cat-4); fill: var(--cat-4-bg); }
.node-ring.c5 { stroke: var(--cat-5); fill: var(--cat-5-bg); }
.node-core {
  fill: var(--ink-soft);
}
.node-label {
  font-size: 11px;
  font-weight: 600;
  fill: var(--ink);
}
.node-date {
  font-size: 10px;
  fill: var(--ink-faint);
  font-family: var(--font-mono);
}
/* 中心珍珠球 */
.ball-halo {
  fill: var(--ambient-b);
}
.ball-body {
  fill: url(#pearlGrad);
  stroke: var(--glass-line-strong);
  stroke-width: 1;
}
.ball-scan {
  fill: none;
  stroke: var(--blue);
  stroke-width: 1;
  opacity: 0.5;
  stroke-dasharray: 30 121;
  transform-origin: center;
  animation: ball-scan 6s linear infinite;
}
@keyframes ball-scan {
  to { transform: rotate(360deg); }
}
.ball-count {
  font-size: 20px;
  font-weight: 700;
  fill: var(--ink);
  text-anchor: middle;
  font-family: var(--font-mono);
}
.ball-sub {
  font-size: 10px;
  fill: var(--ink-soft);
  text-anchor: middle;
}
.trace-caption {
  font-size: 12px;
  color: var(--ink-soft);
  margin-top: 4px;
}
.trace-caption b {
  color: var(--ink);
}
</style>
