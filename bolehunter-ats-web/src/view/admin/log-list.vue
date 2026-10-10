<!--
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 -->
<template>
  <el-card shadow="never">
    <div class="toolbar">
      <span class="page-title">日志管理</span>
    </div>
    <el-table :data="tableData" border v-loading="loading">
      <el-table-column prop="message" label="事件" min-width="200" show-overflow-tooltip />
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="permission" label="权限" width="140" show-overflow-tooltip />
      <el-table-column prop="method" label="方法" width="80" align="center" />
      <el-table-column prop="path" label="路径" min-width="200" show-overflow-tooltip />
      <el-table-column prop="status_code" label="状态码" width="90" align="center" />
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next" :total="total" :page-size="pageCount"
        :current-page="currentPage" @current-change="handlePageChange" />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { getLogs } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageCount = 10

async function getAll() {
  loading.value = true
  try {
    const res = await getLogs({ page: currentPage.value - 1, count: pageCount })
    tableData.value = res.items || res
    total.value = res.total || (res.items || []).length
  } finally {
    loading.value = false
  }
}

async function handlePageChange(val) {
  currentPage.value = val
  await getAll()
}

onMounted(getAll)
</script>

<style scoped>
.toolbar {
  margin-bottom: 14px;
}
.page-title {
  font-size: 15px;
  font-weight: 500;
}
.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
