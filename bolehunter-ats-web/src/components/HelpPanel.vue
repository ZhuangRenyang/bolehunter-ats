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
  <el-drawer :model-value="modelValue" :title="t('help.title')" size="440px"
    @update:model-value="$emit('update:modelValue', $event)">
    <div v-if="content" class="help-body">
      <h3>{{ t(content.titleKey) }}</h3>
      <div v-for="(line, i) in content.lines" :key="i" class="help-line">
        <span class="help-bullet">·</span>
        <span>{{ line }}</span>
      </div>
    </div>
    <el-empty v-else :description="t('help.empty')" :image-size="60" />
    <div class="help-footer">
      <el-button size="small" plain icon="ChatDotRound" @click="feedbackVisible = true">{{ t('help.giveFeedback') }}</el-button>
    </div>
    <FeedbackDialog v-model="feedbackVisible" />
  </el-drawer>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute } from 'vue-router'
import FeedbackDialog from '@/components/FeedbackDialog.vue'

defineProps({ modelValue: { type: Boolean, default: false } })
defineEmits(['update:modelValue'])

const { t } = useI18n()
const route = useRoute()
const feedbackVisible = ref(false)

/** 每个页面的帮助说明（route.name → 内容键），未覆盖的页面显示空态 */
const HELP_MAP = {
  workbench: 'workbench',
  clientList: 'clientList',
  clientDetail: 'clientDetail',
  candidateList: 'candidateList',
  candidateDetail: 'candidateDetail',
  kanban: 'kanban',
  recommendationList: 'recommendationList',
  settingFlag: 'settingFlag',
  settingDict: 'settingDict',
  logList: 'logList',
  loginLog: 'loginLog',
}

const content = computed(() => {
  const key = HELP_MAP[route.name]
  if (!key) return null
  const lines = []
  for (let i = 1; i <= 6; i++) {
    const lineKey = `help.${key}.line${i}`
    const translated = t(lineKey)
    if (translated !== lineKey) {
      lines.push(translated)
    }
  }
  return lines.length ? { titleKey: `help.${key}.title`, lines } : null
})
</script>

<style scoped>
.help-body h3 {
  margin: 0 0 12px;
  font-size: 15px;
  color: var(--ink);
}
.help-line {
  display: flex;
  gap: 8px;
  font-size: 13px;
  color: var(--ink-soft);
  line-height: 1.7;
  margin-bottom: 6px;
}
.help-bullet {
  color: var(--blue);
  font-weight: 700;
}
.help-footer {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid var(--glass-line);
}
</style>
