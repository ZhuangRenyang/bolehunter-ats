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
      <div class="section-label">{{ t('changelog.section') }}</div>
      <el-timeline style="padding-left: 6px">
        <el-timeline-item v-for="v in changelog" :key="v.version" placement="top" type="primary"
          :timestamp="v.date">
          <div class="version-card">
            <div class="version-head">
              <span class="version-tag">{{ v.version }}</span>
              <span class="version-title">{{ v.title }}</span>
            </div>
            <div v-for="(e, i) in v.entries" :key="i" class="version-entry">· {{ e }}</div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </div>
  </el-card>
</template>

<script setup>
import { useI18n } from 'vue-i18n'
import changelog from '@/data/changelog.js'

const { t } = useI18n()
</script>

<style scoped>
.version-card {
  padding: 10px 14px;
  border: 1px solid var(--glass-line);
  border-radius: var(--r-md);
  background: var(--pearl);
}
.version-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}
.version-tag {
  font-family: var(--font-mono);
  font-size: 13px;
  font-weight: 700;
  color: var(--blue);
  background: var(--blue-soft);
  border-radius: 999px;
  padding: 2px 10px;
}
.version-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}
.version-entry {
  font-size: 12.5px;
  color: var(--ink-soft);
  line-height: 1.8;
}
</style>
