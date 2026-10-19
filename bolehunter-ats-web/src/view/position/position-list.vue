<!--
  BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
  Copyright (C) 2026 ZhuangRenyang
  GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
  Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
  本文件遵循 AGPL-3.0-or-later，请勿移除版权声明。
-->
<template>
  <div class="page">

    <div class="page-header">
    </div>
    <el-card shadow="never">
      <div class="search-bar">
        <el-input v-model="keyword" :placeholder="t('position.searchPlaceholder')" clearable style="width: 220px" @keyup.enter="loadPage(0)" />
        <el-select v-model="state" style="width: 130px" @change="loadPage(0)">
          <el-option v-for="s in POSITION_STATES" :key="s" :label="stateText(s)" :value="s" />
        </el-select>
        <el-button type="primary" icon="Search" @click="loadPage(0)">{{ t('common.search') }}</el-button>
      </div>

      <el-table :data="items" border stripe v-loading="loading">
        <el-table-column :label="t('position.colTitle')" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <el-link type="primary" @click="goDetail(scope.row)">{{ scope.row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column :label="t('position.colCompany')" min-width="150" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.company_name || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('position.colCategory')" width="110" show-overflow-tooltip>
          <template #default="scope">{{ scope.row.category_name || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('position.colCity')" width="110" prop="city" show-overflow-tooltip />
        <el-table-column :label="t('position.colSalary')" width="140">
          <template #default="scope">{{ salaryText(scope.row) }}</template>
        </el-table-column>
        <el-table-column :label="t('position.colQuantity')" width="90" prop="quantity" align="center" />
        <el-table-column :label="t('common.status')" width="100" align="center">
          <template #default="scope">
            <el-tag :type="stateTag(scope.row.state)" size="small">{{ stateText(scope.row.state) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('position.colHits')" width="80" prop="hits" align="center" />
        <el-table-column :label="t('position.colRelease')" width="120">
          <template #default="scope">{{ (scope.row.release_date || '').slice(0, 10) || '-' }}</template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="90" fixed="right" align="center">
          <template #default="scope">
            <el-button size="small" type="primary" plain @click="goDetail(scope.row)">{{ t('common.detail') }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination layout="total, prev, pager, next" :total="total" :page-size="count"
        :current-page="page + 1" @current-change="p => loadPage(p - 1)" style="margin-top: 12px; justify-content: flex-end" />
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { getPositionsAdmin, POSITION_STATES } from '@/api/position'

const { t } = useI18n()
const router = useRouter()
const items = ref([])
const total = ref(0)
const page = ref(0)
const count = 10
const loading = ref(false)
const keyword = ref('')
const state = ref(-1)

function salaryText(row) {
  if (!row.salary_down && !row.salary_up) return '-'
  return `${row.salary_down || 0} - ${row.salary_up || 0} K`
}

function stateText(s) {
  const translated = t(`position.state.${s}`)
  return translated === `position.state.${s}` ? String(s) : translated
}

function stateTag(s) {
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
}

function goDetail(row) {
  router.push(`/position/detail/${row.id}`)
}

async function loadPage(p) {
  page.value = p
  loading.value = true
  try {
    const params = { page: p, count, state: state.value }
    if (keyword.value) params.keyword = keyword.value
    const res = await getPositionsAdmin(params)
    items.value = res.items || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPage(0))
</script>

<style scoped>
.page-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 12px;
}
</style>
