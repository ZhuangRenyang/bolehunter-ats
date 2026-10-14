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
      <span class="page-title">系统升级</span>
      <el-tag :type="upgrade.enabled ? 'success' : 'info'" size="small">
        {{ upgrade.enabled ? '自升级已启用' : '自升级未启用（需部署时挂载 docker.sock）' }}
      </el-tag>
    </div>

    <el-alert type="warning" :closable="false" class="hint">
      <p>升级会拉取最新镜像并重建有变化的容器，数据库增量会在新版本启动时自动补齐。</p>
      <p>升级过程中服务会短暂中断（后端容器自身也会重启），完成后本页自动恢复，请勿关闭浏览器。</p>
    </el-alert>

    <div class="actions">
      <el-button type="primary" :loading="upgrading" :disabled="!upgrade.enabled || upgrading"
        @click="confirmUpgrade">
        检查并升级
      </el-button>
      <el-button :loading="migrating" :disabled="migration.pendingCount === 0 || migrating"
        @click="confirmMigrate">
        执行数据库迁移（{{ migration.pendingCount || 0 }}）
      </el-button>
      <el-button plain @click="loadAll">刷新</el-button>
    </div>

    <!-- 待执行迁移 -->
    <div class="section-title">待执行的数据库增量</div>
    <el-table :data="migration.pending || []" border size="small" v-loading="loading">
      <el-table-column prop="filename" label="脚本" min-width="220" />
      <el-table-column prop="title" label="内容" min-width="280" show-overflow-tooltip />
      <el-table-column label="状态" width="120" align="center">
        <template #default="scope">
          <el-tag size="small" type="warning">{{ scope.row.previouslyFailed ? '上次失败，待重试' : '待执行' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!(migration.pending || []).length" description="数据库结构已是最新"
      :image-size="50" />

    <!-- 已执行历史 -->
    <div class="section-title">迁移记录</div>
    <el-table :data="migration.executed || []" border size="small" max-height="220">
      <el-table-column prop="filename" label="脚本" min-width="200" show-overflow-tooltip />
      <el-table-column prop="executed_at" label="时间" width="170" align="center">
        <template #default="scope">{{ fmtTime(scope.row.executedAt || scope.row.executed_at) }}</template>
      </el-table-column>
      <el-table-column label="类型" width="90" align="center">
        <template #default="scope">
          <el-tag size="small" :type="scope.row.baseline ? 'info' : 'success'">
            {{ scope.row.baseline ? '基线' : '增量' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="elapsed_ms" label="耗时(ms)" width="100" align="center" />
    </el-table>

    <!-- 升级日志 -->
    <div class="section-title">最近升级日志</div>
    <pre class="upgrade-log">{{ (upgrade.logTail || []).join('\n') || '（尚无升级记录）' }}</pre>
  </el-card>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMigrationStatus, getUpgradeStatus, runMigration, triggerUpgrade } from '@/api/upgrade'

const loading = ref(false)
const upgrading = ref(false)
const migrating = ref(false)
const upgrade = reactive({ enabled: false, running: false, logTail: [] })
const migration = reactive({ pending: [], executed: [], pendingCount: 0 })

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
    '将拉取最新镜像并重建有变化的容器，期间服务短暂中断。' +
    '如确认执行，请在下方输入“升级”：',
    '系统升级', { type: 'warning', confirmButtonText: '开始升级', cancelButtonText: '取消' },
  ).then(async ({ value }) => {
    if ((value || '').trim() !== '升级') {
      ElMessage.error('输入不一致，已取消')
      return
    }
    try {
      await triggerUpgrade()
    } catch (e) {
      /* 触发失败时请求层已提示；不进入轮询 */
      return
    }
    upgrading.value = true
    ElMessage.info('升级流程已启动，服务重启中，页面将自动恢复…')
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
    ElMessage.success('升级完成，服务已恢复')
    await loadAll()
  } else {
    ElMessage.error('长时间未恢复，请到服务器查看：docker compose logs -f backend')
  }
}

function confirmMigrate() {
  ElMessageBox.confirm(
    `将手动执行 ${migration.pendingCount} 个待迁移脚本，通常无需手动执行（新版本启动时会自动补齐）。继续？`,
    '执行数据库迁移', { type: 'warning' },
  ).then(async () => {
    migrating.value = true
    try {
      const res = await runMigration()
      ElMessage.success(`已执行 ${res?.executed ?? 0} 个脚本`)
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

onMounted(loadAll)
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
</style>
