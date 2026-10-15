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
  <el-card shadow="never" v-loading="loading">
    <div class="toolbar"><span class="page-title">{{ t('ai.title') }}</span></div>
    <el-alert type="info" :closable="false" style="margin-bottom: 16px" :title="t('ai.alert')" />

    <el-form label-width="140px" style="max-width: 640px">
      <el-form-item :label="t('ai.baseUrl')">
        <el-input v-model="form.base_url" placeholder="https://api.deepseek.com" />
      </el-form-item>
      <el-form-item :label="t('ai.model')">
        <el-input v-model="form.model" placeholder="deepseek-chat" />
      </el-form-item>
      <el-form-item label="API Key">
        <el-input v-model="form.api_key" type="password" show-password
          :placeholder="keyMasked ? t('ai.keyConfigured', { mask: keyMasked }) : 'sk-...'" />
      </el-form-item>
      <el-form-item :label="t('ai.embedding')">
        <el-input v-model="form.embedding_model" placeholder="text-embedding-v3" />
        <div class="field-tip">{{ t('ai.embeddingTip') }}</div>
      </el-form-item>
      <el-form-item>
        <el-tag v-if="ready" type="success" size="small">{{ t('ai.ready') }}</el-tag>
        <el-tag v-else type="info" size="small">{{ t('ai.notReady') }}</el-tag>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">{{ t('common.save') }}</el-button>
        <el-button @click="clearKey" :disabled="!keyMasked">{{ t('ai.clearKey') }}</el-button>
      </el-form-item>
    </el-form>

    <el-divider />
    <p class="tip">{{ t('ai.hint') }}</p>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { get, put } from '@/api/request'

const { t } = useI18n()
const loading = ref(false)
const saving = ref(false)
const keyMasked = ref('')
const ready = ref(false)
const form = reactive({ base_url: '', model: '', api_key: '', embedding_model: '' })

async function load() {
  loading.value = true
  try {
    const res = await get('recruit/admin/setting/ai')
    form.base_url = res.base_url || ''
    form.model = res.model || ''
    form.embedding_model = res.embedding_model || ''
    keyMasked.value = res.api_key_masked || ''
    ready.value = !!res.ready
  } finally {
    loading.value = false
  }
}

async function save() {
  saving.value = true
  try {
    // api_key 输入为空且原已配置 → 不提交该字段，保持不变
    const body = { base_url: form.base_url, model: form.model, embedding_model: form.embedding_model || '' }
    if (form.api_key && form.api_key.trim()) {
      body.api_key = form.api_key.trim()
    }
    const res = await put('recruit/admin/setting/ai', body)
    ElMessage.success(res.message || t('common.saveOk'))
    form.api_key = ''
    await load()
  } finally {
    saving.value = false
  }
}

async function clearKey() {
  await put('recruit/admin/setting/ai', { api_key: '' })
  ElMessage.success(t('ai.keyCleared'))
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar { margin-bottom: 14px; }
.page-title { font-size: 15px; font-weight: 500; }
.tip { color: #909399; font-size: 12px; }
.field-tip { color: #909399; font-size: 12px; line-height: 1.6; margin-top: 4px; }
</style>
