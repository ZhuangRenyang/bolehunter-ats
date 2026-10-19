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
    <div class="toolbar">
      <el-tag :type="upgrade.enabled ? 'success' : 'info'" size="small">
        {{ upgrade.enabled ? t('upgrade.enabledTag') : t('upgrade.disabledTag') }}
      </el-tag>
      <!-- 版本状态（迭代 37）：本地构建 sha vs GitHub 主分支最新 sha -->
      <el-tag v-if="versionStatus === 'latest'" type="success" size="small" effect="dark">
        {{ t('upgrade.versionLatest') }}
      </el-tag>
      <el-tag v-else-if="versionStatus === 'update-available'" type="warning" size="small" effect="dark">
        {{ t('upgrade.versionUpdate') }}
      </el-tag>
      <el-tag v-else-if="versionStatus === 'checking'" type="info" size="small">
        {{ t('upgrade.versionChecking') }}
      </el-tag>
      <el-tag v-else type="info" size="small" style="cursor: pointer" @click="checkVersion">
        {{ t('upgrade.versionUnknown') }}
      </el-tag>
    </div>

    <el-alert type="warning" :closable="false" class="hint">
      <p>{{ t('upgrade.alertLine1') }}</p>
      <p>{{ t('upgrade.alertLine2') }}</p>
    </el-alert>

    <div class="actions">
      <el-button type="primary" :loading="upgrading" :disabled="!upgrade.enabled || upgrading"
        @click="confirmUpgrade">
        {{ t('upgrade.upgradeBtn') }}
      </el-button>
      <el-button :loading="migrating" :disabled="migration.pendingCount === 0 || migrating"
        @click="confirmMigrate">
        {{ t('upgrade.migrateBtn', { n: migration.pendingCount || 0 }) }}
      </el-button>
      <el-button plain @click="loadAll">{{ t('upgrade.refresh') }}</el-button>
    </div>

    <!-- 待执行迁移 -->
    <div class="section-title">{{ t('upgrade.pendingSection') }}</div>
    <el-table :data="migration.pending || []" border size="small" v-loading="loading">
      <el-table-column prop="filename" :label="t('upgrade.colScript')" min-width="220" />
      <el-table-column prop="title" :label="t('upgrade.colContent')" min-width="280" show-overflow-tooltip />
      <el-table-column :label="t('common.status')" width="120" align="center">
        <template #default="scope">
          <el-tag size="small" type="warning">{{ scope.row.previouslyFailed ? t('upgrade.statusRetry') : t('upgrade.statusPending') }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!(migration.pending || []).length" :description="t('upgrade.pendingEmpty')"
      :image-size="50" />

    <!-- 已执行历史 -->
    <div class="section-title">{{ t('upgrade.historySection') }}</div>
    <el-table :data="migration.executed || []" border size="small" max-height="220">
      <el-table-column prop="filename" :label="t('upgrade.colScript')" min-width="200" show-overflow-tooltip />
      <el-table-column prop="executed_at" :label="t('upgrade.colTime')" width="170" align="center">
        <template #default="scope">{{ fmtTime(scope.row.executedAt || scope.row.executed_at) }}</template>
      </el-table-column>
      <el-table-column :label="t('upgrade.colType')" width="90" align="center">
        <template #default="scope">
          <el-tag size="small" :type="scope.row.baseline ? 'info' : 'success'">
            {{ scope.row.baseline ? t('upgrade.typeBaseline') : t('upgrade.typeIncremental') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="elapsed_ms" :label="t('upgrade.colElapsed')" width="100" align="center" />
    </el-table>

    <!-- 升级日志 -->
    <div class="section-title">{{ t('upgrade.logSection') }}</div>
    <pre class="upgrade-log">{{ (upgrade.logTail || []).join('\n') || t('upgrade.logEmpty') }}</pre>

    <!-- 数据备份与恢复（迭代 30）：整库 .sql 导出/导入，管理员从旧系统或历史备份迁移 -->
    <div class="section-title">{{ t('upgrade.dbSection') }}</div>
    <el-alert type="warning" :closable="false" style="margin-bottom: 12px" :title="t('upgrade.dbWarning')" />
    <div class="db-actions">
      <el-button type="primary" plain icon="Download" :loading="exporting" @click="handleExportDb">{{ t('upgrade.dbExport') }}</el-button>
      <el-upload :show-file-list="false" accept=".sql" :before-upload="beforeImportDb">
        <el-button type="danger" plain icon="Upload" :loading="importing">{{ t('upgrade.dbImport') }}</el-button>
      </el-upload>
      <span class="db-hint">{{ t('upgrade.dbHint') }}</span>
    </div>
  </el-card>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { getMigrationStatus, getUpgradeStatus, runMigration, triggerUpgrade, getVersion } from '@/api/upgrade'
import { exportDb, importDb } from '@/api/upgrade'

const { t, locale } = useI18n()
const loading = ref(false)
const upgrading = ref(false)
const migrating = ref(false)
const upgrade = reactive({ enabled: false, running: false, logTail: [] })
const migration = reactive({ pending: [], executed: [], pendingCount: 0 })

// 强确认词跟随语言（中文"升级" / 英文 UPGRADE），防误触机制不绑定单一语言
const confirmWord = computed(() => (locale.value === 'en-US' ? 'UPGRADE' : '升级'))

const exporting = ref(false)
const importing = ref(false)

// 迭代 30：整库备份下载（blob 流式）
async function handleExportDb() {
  exporting.value = true
  try {
    const res = await exportDb()
    const url = URL.createObjectURL(new Blob([res.data], { type: 'application/octet-stream' }))
    const a = document.createElement('a')
    a.href = url
    a.download = 'bolehunter-db-' + new Date().toISOString().slice(0, 10).replace(/-/g, '') + '.sql'
    a.click()
    URL.revokeObjectURL(url)
    ElMessage.success(t('upgrade.dbExportOk'))
  } catch (e) {
    // 错误提示由请求层统一处理
  } finally {
    exporting.value = false
  }
}

// 迭代 30：整库恢复（危险操作——输入"恢复"强确认，后端自动先备份当前库）
function beforeImportDb(file) {
  if (!file.name.toLowerCase().endsWith('.sql')) {
    ElMessage.error(t('upgrade.dbSqlOnly'))
    return false
  }
  ElMessageBox.prompt(t('upgrade.dbConfirmPrompt'), t('upgrade.dbConfirmTitle'), {
    inputPattern: /^恢复$/,
    inputErrorMessage: t('upgrade.dbConfirmInput'),
    type: 'warning',
  }).then(async () => {
    importing.value = true
    try {
      const res = await importDb(file, file.name)
      ElMessage.success(t('upgrade.dbImportOk', { backup: res.data.backupFile }))
      await loadAll()
    } catch (e) {
      // 错误提示由请求层统一处理
    } finally {
      importing.value = false
    }
  }).catch(() => {})
  return false
}

// ===== 版本比对（迭代 37，方案 B）：compose pull 前后镜像 ID 对比 =====
const hasUpdate = ref(null)
const checking = ref(false)
const versionStatus = computed(() => {
  if (checking.value) return 'checking'
  if (hasUpdate.value === null) return 'unknown'
  return hasUpdate.value ? 'update-available' : 'latest'
})

async function checkVersion() {
  if (checking.value) return
  checking.value = true
  try {
    const res = await getVersion()
    hasUpdate.value = !!res.hasUpdate
  } catch (e) {
    hasUpdate.value = null
  } finally {
    checking.value = false
  }
}

async function loadAll() {
  loading.value = true
  try {
    const [u, m] = await Promise.all([getUpgradeStatus(), getMigrationStatus()])
    Object.assign(upgrade, u || {})
    Object.assign(migration, m || {})
    migration.pendingCount = (m && m.pendingCount) || 0
  } catch (e) {
    /* 服务重启窗口期会失败，由轮询兜底 */
  } finally {
    loading.value = false
  }
}

/**
 * 触发自升级：强确认后点火，然后进入"恢复轮询"——
 * 期间 backend 容器自身会被重建，请求可能 502/超时，均为预期，一直轮到健康接口恢复。
 */
function confirmUpgrade() {
  ElMessageBox.prompt(
    t('upgrade.confirmPrompt', { word: confirmWord.value }),
    t('upgrade.confirmTitle'),
    { type: 'warning', confirmButtonText: t('upgrade.confirmOk'), cancelButtonText: t('common.cancel') },
  ).then(async ({ value }) => {
    if ((value || '').trim() !== confirmWord.value) {
      ElMessage.error(t('upgrade.wordMismatch'))
      return
    }
    try {
      await triggerUpgrade()
    } catch (e) {
      /* 触发失败时请求层已提示；不进入轮询 */
      return
    }
    upgrading.value = true
    ElMessage.info(t('upgrade.started'))
    await pollUntilRecovered()
  }).catch(() => {})
}

async function pollUntilRecovered() {
  // 服务重启窗口：请求会失败（网络错误/502），持续轮询直到恢复
  await sleep(6000)
  let recovered = false
  for (let i = 0; i < 100 && !recovered; i++) {
    try {
      const s = await getUpgradeStatus()
      if (s && typeof s.enabled === 'boolean') {
        recovered = true
        Object.assign(upgrade, s)
      }
    } catch (e) {
      /* 仍在重启，继续等 */
    }
    if (!recovered) await sleep(4000)
  }
  upgrading.value = false
  if (recovered) {
    ElMessage.success(t('upgrade.recovered'))
    await loadAll()
  } else {
    ElMessage.error(t('upgrade.notRecovered'))
  }
}

function confirmMigrate() {
  ElMessageBox.confirm(
    t('upgrade.migrateConfirm', { n: migration.pendingCount }),
    t('upgrade.migrateTitle'),
    { type: 'warning' },
  ).then(async () => {
    migrating.value = true
    try {
      const res = await runMigration()
      ElMessage.success(t('upgrade.migratedOk', { n: res?.executed ?? 0 }))
      await loadAll()
    } catch (e) {
      /* 错误由请求层统一提示 */
    } finally {
      migrating.value = false
    }
  }).catch(() => {})
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function fmtTime(v) {
  return v ? String(v).slice(0, 19).replace('T', ' ') : ''
}

onMounted(() => {
  loadAll()
  checkVersion()
})
onBeforeUnmount(() => { /* 轮询循环有上限，页面离开自然结束 */ })
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.page-title {
  font-size: 15px;
  font-weight: 500;
}
.hint {
  margin-bottom: 14px;
}
.actions {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.section-title {
  font-size: 13px;
  font-weight: 600;
  margin: 16px 0 8px;
  color: #303133;
}
.upgrade-log {
  background: #0b1020;
  color: #d6e2f0;
  font-size: 12px;
  line-height: 1.6;
  padding: 12px;
  border-radius: 6px;
  max-height: 240px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
.db-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}
.db-hint {
  font-size: 12px;
  color: var(--ink-faint);
}
</style>
