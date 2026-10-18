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
      <h2>{{ t('loginLog.title') }}</h2>
    </div>

    <!-- 筛选面板 -->
    <div class="m-surface filter-panel">
      <div class="section-label">{{ t('loginLog.filterSection') }}</div>
      <el-form inline @submit.prevent>
        <el-form-item :label="t('loginLog.filterUser')">
          <el-input v-model="filter.username" clearable style="width: 150px"
            :placeholder="t('loginLog.filterUserPlaceholder')" @keyup.enter="loadPage(1)" @clear="loadPage(1)" />
        </el-form-item>
        <el-form-item :label="t('loginLog.filterResult')">
          <el-select v-model="filter.result" clearable style="width: 130px" @change="loadPage(1)">
            <el-option :label="t('loginLog.resultOk')" :value="true" />
            <el-option :label="t('loginLog.resultFail')" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="loadPage(1)">{{ t('common.search') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 明细面板 -->
    <div class="m-surface table-panel">
      <div class="section-label">{{ t('loginLog.tableSection') }}</div>
      <el-table :data="items" v-loading="loading" size="small">
        <el-table-column prop="create_time" :label="t('loginLog.colTime')" width="175" align="center">
          <template #default="{ row }">{{ fmt(row.create_time) }}</template>
        </el-table-column>
        <el-table-column prop="username" :label="t('loginLog.colUser')" width="120" />
        <el-table-column prop="result" :label="t('loginLog.colResult')" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.result ? 'success' : 'danger'">
              {{ row.result ? t('loginLog.resultOk') : t('loginLog.resultFail') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="fail_reason" :label="t('loginLog.colFailReason')" min-width="130" show-overflow-tooltip />
        <el-table-column prop="ip" :label="t('loginLog.colIp')" width="135" />
        <el-table-column prop="browser" :label="t('loginLog.colBrowser')" width="90" align="center" />
        <el-table-column prop="os" :label="t('loginLog.colOs')" width="95" align="center" />
        <el-table-column prop="elapsed_ms" :label="t('loginLog.colElapsed')" width="95" align="center">
          <template #default="{ row }">{{ row.elapsed_ms }}ms</template>
        </el-table-column>
      </el-table>
      <div class="pagination">
        <el-pagination background layout="prev, pager, next" :total="total" :page-size="count"
          :current-page="page" @current-change="loadPage" />
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getLoginLogPage } from '@/api/audit'

const { t } = useI18n()

const loading = ref(false)
const items = ref([])
const total = ref(0)
const page = ref(1)
const count = 20
const filter = reactive({ username: '', result: null })

function fmt(val) {
  return String(val || '').replace('T', ' ').slice(0, 19)
}

async function loadPage(p = 1) {
  loading.value = true
  try {
    page.value = p
    const params = { page: p, count }
    if (filter.username) params.username = filter.username
    if (filter.result !== null && filter.result !== undefined && filter.result !== '') params.result = filter.result
    const res = await getLoginLogPage(params)
    items.value = res.items || []
    total.value = res.total || 0
  } catch (e) {
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

onMounted(() => loadPage(1))
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
