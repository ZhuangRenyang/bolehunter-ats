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
