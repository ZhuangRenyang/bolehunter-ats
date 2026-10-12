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
          <span class="page-title">职位详情：{{ position.title || '加载中…' }}</span>
        </template>
      </el-page-header>
    </div>

    <el-card shadow="never" style="margin-bottom: 14px">
      <template #header>基本信息</template>
      <el-descriptions :column="3" border v-loading="loading">
        <el-descriptions-item label="职位名称">{{ position.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="所属公司">{{ position.company_name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="职位分类">{{ position.category_name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作城市">{{ position.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ salaryText(position) }}</el-descriptions-item>
        <el-descriptions-item label="招聘人数">{{ position.quantity ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ position.edu_back || '-' }}</el-descriptions-item>
        <el-descriptions-item label="浏览次数">{{ position.hits ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布日期">{{ (position.release_date || '').slice(0, 10) || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="position.state === 1 ? 'success' : position.state === 2 ? 'danger' : 'warning'" size="small">
            {{ position.state === 1 ? '已通过' : position.state === 2 ? '不通过' : '未审核' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="职位要求" :span="2">
          <div class="requirement-text">{{ position.requirement || '-' }}</div>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card shadow="never">
      <template #header>该职位的推荐记录</template>
      <el-table :data="recommendations" border stripe v-loading="loadingRec">
        <el-table-column label="推荐编号" prop="id" width="90" align="center" />
        <el-table-column label="当前阶段" width="130">
          <template #default="scope">
            <el-tag size="small">{{ stageLabel(scope.row.stage) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐日期" width="120">
          <template #default="scope">{{ (scope.row.createTime || '').slice(0, 10) || '-' }}</template>
        </el-table-column>
        <el-table-column label="备注" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.feedback || '-' }}</template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loadingRec && recommendations.length === 0" description="该职位暂无推荐记录" :image-size="60" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getPositionById } from '@/api/position'
import { getRecommendationPage, STAGES } from '@/api/recommendation'

const route = useRoute()
const position = ref({})
const recommendations = ref([])
const loading = ref(false)
const loadingRec = ref(false)

function salaryText(row) {
  if (!row.salary_down && !row.salary_up) return '-'
  return `${row.salary_down || 0} - ${row.salary_up || 0} K`
}

function stageLabel(stage) {
  return (STAGES.find(s => s.value === stage) || {}).label || stage
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
