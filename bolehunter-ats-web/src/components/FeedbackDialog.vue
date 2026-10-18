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
  <el-dialog :model-value="modelValue" :title="t('feedback.title')" width="460px"
    @update:model-value="$emit('update:modelValue', $event)">
    <el-input v-model="content" type="textarea" :rows="4" maxlength="2000" show-word-limit
      :placeholder="t('feedback.placeholder')" />
    <p class="feedback-hint">{{ t('feedback.hint') }}</p>
    <template #footer>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ t('common.submit') }}</el-button>
      <el-button @click="$emit('update:modelValue', false)">{{ t('common.cancel') }}</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { submitFeedback } from '@/api/ux'

defineProps({ modelValue: { type: Boolean, default: false } })
const emit = defineEmits(['update:modelValue'])

const { t } = useI18n()
const content = ref('')
const submitting = ref(false)

async function handleSubmit() {
  if (!content.value.trim()) {
    ElMessage.warning(t('feedback.required'))
    return
  }
  submitting.value = true
  try {
    await submitFeedback(content.value.trim())
    ElMessage.success(t('feedback.ok'))
    content.value = ''
    emit('update:modelValue', false)
  } catch (e) {
    // 请求层统一提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.feedback-hint {
  font-size: 12px;
  color: var(--ink-faint);
  margin: 8px 0 0;
}
</style>
