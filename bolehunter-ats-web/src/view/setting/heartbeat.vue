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
    <div class="page-header">
      <el-tag size="small" type="info">{{ t('heartbeat.window') }}</el-tag>
    </div>
    <div class="m-surface chart-panel">
      <div class="section-label">{{ t('heartbeat.section') }}</div>
      <div ref="chartEl" class="hb-chart" />
      <p class="hb-tip">{{ t('heartbeat.tip') }}</p>
    </div>
  </el-card>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getHeartbeat } from '@/api/audit'

echarts.use([LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const { t } = useI18n()
const chartEl = ref()
let chart = null
let timer = null
let loading = false

async function refresh() {
  if (loading || !chart) return
  loading = true
  try {
    const points = await getHeartbeat()
    const times = points.map(p => String(p.time).slice(11, 19))
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: [t('heartbeat.cpu'), t('heartbeat.sysMem'), t('heartbeat.jvmHeap')] },
      grid: { left: 40, right: 20, top: 40, bottom: 30 },
      xAxis: { type: 'category', data: times },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
      series: [
        { name: t('heartbeat.cpu'), type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.cpu) },
        { name: t('heartbeat.sysMem'), type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.sysMem) },
        { name: t('heartbeat.jvmHeap'), type: 'line', smooth: true, showSymbol: false, data: points.map(p => p.jvmHeap) },
      ],
    })
  } catch (e) {
    // 请求层统一提示
  } finally {
    loading = false
  }
}

onMounted(() => {
  chart = echarts.init(chartEl.value)
  refresh()
  timer = setInterval(refresh, 30_000)
  window.addEventListener('resize', () => chart && chart.resize())
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
  if (chart) chart.dispose()
})
</script>

<style scoped>
.hb-chart {
  width: 100%;
  height: 380px;
}
.hb-tip {
  font-size: 12px;
  color: var(--ink-faint);
  margin: 10px 0 0;
}
</style>
