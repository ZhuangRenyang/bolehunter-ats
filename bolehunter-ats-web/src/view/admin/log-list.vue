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
      <h2>{{ t('log.title') }}</h2>
    </div>

    <!-- 审计流水筛选（迭代24）：操作人模糊 / 状态码 / 时间范围 —— 迭代 29d 横带分层：筛选面板 -->
    <div class="m-surface filter-panel">
      <div class="section-label">{{ t('log.filterSection') }}</div>
      <el-form inline class="filter-bar" @submit.prevent>
      <el-form-item :label="t('log.filterUser')">
        <el-input v-model="filter.username" :placeholder="t('log.filterUserPlaceholder')" clearable style="width: 140px"
          @keyup.enter="handleSearch" @clear="handleSearch" />
      </el-form-item>
      <el-form-item :label="t('log.filterStatus')">
        <el-select v-model="filter.statusCode" :placeholder="t('common.all')" clearable style="width: 120px" @change="handleSearch">
          <el-option v-for="code in statusOptions" :key="code" :label="code" :value="code" />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('log.filterRange')">
        <el-date-picker v-model="range" type="datetimerange" :range-separator="t('log.rangeTo')"
          :start-placeholder="t('log.rangeStart')" :end-placeholder="t('log.rangeEnd')"
          value-format="YYYY-MM-DD HH:mm:ss" style="width: 340px" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">{{ t('log.search') }}</el-button>
        <el-button plain @click="handleReset">{{ t('log.reset') }}</el-button>
      </el-form-item>
    </el-form>
    </div>

    <div class="m-surface table-panel">
      <div class="section-label">{{ t('log.detailSection') }}</div>
      <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="create_time" :label="t('log.colTime')" width="170" align="center">
        <template #default="scope">{{ formatTime(scope.row.create_time) }}</template>
      </el-table-column>
      <el-table-column prop="message" :label="t('log.colEvent')" min-width="190" show-overflow-tooltip />
      <el-table-column prop="username" :label="t('log.colUser')" width="110" />
      <el-table-column prop="permission" :label="t('log.colPermission')" width="130" show-overflow-tooltip />
      <el-table-column prop="method" :label="t('log.colMethod')" width="76" align="center" />
      <el-table-column prop="path" :label="t('log.colPath')" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status_code" :label="t('log.colStatus')" width="92" align="center">
        <template #default="scope">
          <el-tag size="small" :type="statusTagType(scope.row.status_code)">{{ scope.row.status_code }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLogs } from '@/api/user'

const { t } = useI18n()
const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageCount = 10

const filter = reactive({ username: '', statusCode: null })
const range = ref(null)

// 常见状态码速选：快速定位失败请求（407 功能未开启 / 429 限流 / 5xx 服务端错误）
const statusOptions = [200, 401, 403, 404, 407, 429, 500]

function buildParams() {
  const params = { page: currentPage.value - 1, count: pageCount }
  if (filter.username && filter.username.trim()) params.name = filter.username.trim()
  if (filter.statusCode) params.statusCode = filter.statusCode
  if (Array.isArray(range.value) && range.value.length === 2) {
    params.start = range.value[0]
    params.end = range.value[1]
  }
  return params
}

async function getAll() {
  loading.value = true
  try {
    const res = await getLogs(buildParams())
    tableData.value = res.items || res || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  getAll()
}

function handleReset() {
  filter.username = ''
  filter.statusCode = null
  range.value = null
  handleSearch()
}

async function handlePageChange(val) {
  currentPage.value = val
  await getAll()
}

function formatTime(value) {
  if (!value) return ''
  return String(value).slice(0, 19).replace('T', ' ')
}

function statusTagType(code) {
  if (code >= 500) return 'danger'
  if (code >= 400) return 'warning'
  return 'success'
}

onMounted(getAll)
</script>

<style scoped>
.filter-panel {
  padding: 14px 16px 2px;
  margin-bottom: 16px;
}
.filter-bar {
  margin-bottom: 6px;
}
.table-panel {
  padding: 14px 16px;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
