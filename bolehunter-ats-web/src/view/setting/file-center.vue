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
    </div>
    <div class="m-surface table-panel">
      <div class="section-label">{{ t('fileCenter.section') }}</div>
      <el-table :data="items" v-loading="loading" size="small">
        <el-table-column prop="name" :label="t('fileCenter.colCandidate')" width="140" />
        <el-table-column prop="phone" :label="t('candidate.colPhone')" width="140" />
        <el-table-column prop="file" :label="t('fileCenter.colFile')" min-width="240">
          <template #default="{ row }">
            <a class="file-link" :href="row.file" target="_blank" rel="noopener">{{ row.file }}</a>
          </template>
        </el-table-column>
        <el-table-column :label="t('common.operation')" width="120" align="center">
          <template #default="{ row }">
            <el-button size="small" type="primary" plain @click="$router.push(`/candidate/detail/${row.candidateId}`)">
              {{ t('fileCenter.gotoCandidate') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && items.length === 0" :description="t('fileCenter.empty')" :image-size="60" />
    </div>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getFiles } from '@/api/audit'

const { t } = useI18n()
const loading = ref(false)
const items = ref([])

onMounted(async () => {
  loading.value = true
  try {
    items.value = await getFiles()
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.file-link {
  color: var(--blue);
  text-decoration: none;
  word-break: break-all;
}
.file-link:hover {
  text-decoration: underline;
}
</style>
