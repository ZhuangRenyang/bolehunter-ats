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
    <div class="toolbar"><span class="page-title">AI 服务配置</span></div>
    <el-alert type="info" :closable="false" style="margin-bottom: 16px"
      title="供“简历 AI 解析”等功能调用大模型。兼容 OpenAI 协议的服务均可（DeepSeek / 通义 / Kimi 等）。在此填写后无需配置服务器环境变量；未填写时回落环境变量，均未配置则 AI 功能自动关闭、规则解析兜底。" />

    <el-form label-width="140px" style="max-width: 640px">
      <el-form-item label="接口地址">
        <el-input v-model="form.base_url" placeholder="https://api.deepseek.com" />
      </el-form-item>
      <el-form-item label="模型名">
        <el-input v-model="form.model" placeholder="deepseek-chat" />
      </el-form-item>
      <el-form-item label="API Key">
        <el-input v-model="form.api_key" type="password" show-password
          :placeholder="keyMasked ? `已配置（${keyMasked}），留空保持不变` : 'sk-...'" />
      </el-form-item>
      <el-form-item label="向量模型（可选）">
        <el-input v-model="form.embedding_model" placeholder="text-embedding-v3（需供应商支持 embeddings 接口）" />
        <div class="field-tip">用于 AI 找人的语义检索；DeepSeek 不提供 embeddings，可留空走关键词检索</div>
      </el-form-item>
      <el-form-item>
        <el-tag v-if="ready" type="success" size="small">AI 服务已就绪</el-tag>
        <el-tag v-else type="info" size="small">未配置 Key，AI 功能将使用规则解析兜底</el-tag>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保 存</el-button>
        <el-button @click="clearKey" :disabled="!keyMasked">清除 Key</el-button>
      </el-form-item>
    </el-form>

    <el-divider />
    <p class="tip">提示：启用“设置 → 功能开关”中的 <b>resume_parse</b> 开关后，导入简历将自动解析字段（需先在上方完成配置）。</p>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { get, put } from '@/api/request'

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
    ElMessage.success(res.message || '保存成功')
    form.api_key = ''
    await load()
  } finally {
    saving.value = false
  }
}

async function clearKey() {
  await put('recruit/admin/setting/ai', { api_key: '' })
  ElMessage.success('已清除 Key，回落环境变量')
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
