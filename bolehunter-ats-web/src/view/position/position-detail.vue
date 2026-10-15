<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ZhuangRenyang
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
  本文件遵循 AGPL-3.0-or-later，请勿移除版权声明。
-->
<template>
  <div class="page">
    <div class="page-title-bar">
      <el-page-header @back="$router.back()">
        <template #content>
          <span class="page-title">{{ t('position.detailTitle') }}：{{ position.title || t('position.loading') }}</span>
        </template>
      </el-page-header>
    </div>

    <el-card shadow="never" style="margin-bottom: 14px">
      <template #header>{{ t('position.baseInfo') }}</template>
      <el-descriptions :column="3" border v-loading="loading">
        <el-descriptions-item :label="t('position.colTitle')">{{ position.title || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colCompany')">{{ position.company_name || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colCategory')">{{ position.category_name || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colCity')">{{ position.city || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colSalary')">{{ salaryText(position) }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colQuantity')">{{ position.quantity ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colEdu')">{{ position.edu_back || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colHits')">{{ position.hits ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('position.colRelease')">{{ (position.release_date || '').slice(0, 10) || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('common.status')">
          <el-tag :type="position.state === 1 ? 'success' : position.state === 2 ? 'danger' : 'warning'" size="small">
            {{ stateText(position.state) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('position.colRequirement')" :span="2">
          <div class="requirement-text">{{ position.requirement || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <template #header>{{ t('position.recList') }}</template>
      <el-table :data="recommendations" border stripe v-loading="loadingRec">
        <el-table-column :label="t('position.colRecId')" prop="id" width="90" align="center" />
        <el-table-column :label="t('position.colStage')" width="130">
          <template #default="scope">
            <el-tag size="small">{{ stageLabel(scope.row.stage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('position.colRecDate')" width="120">
          <template #default="scope">{{ (scope.row.createTime || '').slice(0, 10) || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('common.remark')" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.feedback || '-' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loadingRec && recommendations.length === 0" :description="t('position.recEmpty')" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPositionById } from '@/api/position'
import { getRecommendationPage } from '@/api/recommendation'

const { t } = useI18n()
const route = useRoute()
const position = ref({})
const recommendations = ref([])
const loading = ref(false)
const loadingRec = ref(false)

function salaryText(row) {
  if (!row.salary_down && !row.salary_up) return '-'
  return `${row.salary_down || 0} - ${row.salary_up || 0} K`
}

function stateText(s) {
  const translated = t(`position.state.${s}`)
  return translated === `position.state.${s}` ? String(s) : translated
}

// 阶段名走语言包顶层 stage.*（与看板/工作台共用），未知枚举兜底原始值
function stageLabel(stage) {
  const translated = t(`stage.${stage}`)
  return translated === `stage.${stage}` ? stage : translated
}

async function loadPosition() {
  loading.value = true
  try {
    position.value = await getPositionById(route.params.id)
  } finally {
    loading.value = false
  }
}

async function loadRecommendations() {
  loadingRec.value = true
  try {
    const res = await getRecommendationPage({ job_id: route.params.id, page: 0, count: 20 })
    recommendations.value = res.items || []
  } finally {
    loadingRec.value = false
  }
}

onMounted(() => {
  loadPosition()
  loadRecommendations()
})
</script>

<style scoped>
.page-title-bar {
  margin-bottom: 12px;
}
.page-title {
  font-size: 16px;
  font-weight: 600;
}
.requirement-text {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
