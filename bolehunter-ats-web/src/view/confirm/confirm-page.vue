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
  <div class="confirm-page">
    <div class="confirm-card" v-loading="loading">
      <template v-if="error">
        <div class="state-block">
          <el-icon :size="56" color="#f56c6c"><CircleCloseFilled /></el-icon>
          <h2>链接无效或已过期</h2>
          <p class="muted">请联系您的猎头顾问重新获取确认链接</p>
        </div>
      </template>
      <template v-else-if="info">
        <div class="brand">
          <span class="logo">伯</span>
          <span class="brand-name">伯乐猎 · 推荐确认</span>
        </div>
        <h2 class="title">候选人推荐摘要</h2>
        <div class="summary">
          <div class="row"><span class="label">候选人</span><b>{{ info.candidate_name }}</b></div>
          <div class="row" v-if="info.candidate_position">
            <span class="label">候选人背景</span><span>{{ info.candidate_position }}<template v-if="info.candidate_company"> · {{ info.candidate_company }}</template></span>
          </div>
          <div class="row"><span class="label">推荐职位</span><b>{{ info.job_title }}</b></div>
          <div class="row"><span class="label">客户公司</span><span>{{ info.client_name }}</span></div>
          <div class="row"><span class="label">当前阶段</span>
            <el-tag size="small">{{ stageLabel(info.current_stage) }}</el-tag>
          </div>
          <div class="row" v-if="info.feedback"><span class="label">最近反馈</span><span>{{ info.feedback }}</span></div>
          <div class="row"><span class="label">链接有效期至</span><span>{{ formatDate(info.expire) }}</span></div>
        </div>

        <template v-if="!done">
          <el-input
            v-model="feedback"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="补充反馈（可选）：例如安排下周三下午面试 / 暂不考虑该候选人…"
          />
          <div class="actions">
            <el-button type="primary" :loading="acting" @click="act('read')">已读确认</el-button>
            <el-button type="success" :loading="acting" @click="act('interview')">约面试</el-button>
            <el-button type="danger" plain :loading="acting" @click="act('reject')">婉拒</el-button>
          </div>
        </template>
        <div v-else class="state-block done">
          <el-icon :size="56" color="#67c23a"><SuccessFilled /></el-icon>
          <h2>{{ doneMessage }}</h2>
          <p class="muted">您的反馈已通知负责顾问，感谢配合！</p>
        </div>
      </template>
    </div>
    <p class="footer">本页面由伯乐猎猎头系统生成 · 无需登录</p>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCloseFilled, SuccessFilled } from '@element-plus/icons-vue'
import { getConfirmInfo, submitConfirmAction } from '@/api/open'

const route = useRoute()
const token = route.params.token
const loading = ref(true)
const error = ref(false)
const info = ref(null)
const feedback = ref('')
const acting = ref(false)
const done = ref(false)
const doneMessage = ref('')

const STAGE_LABELS = {
  PENDING: '待推荐',
  RECOMMENDED: '已推荐',
  CLIENT_READ: '客户已读',
  INTERVIEW_PENDING: '待面试',
  INTERVIEWING: '面试中',
  OFFER_PENDING: '待Offer',
  OFFERED: 'Offer',
  ONBOARDED: '入职',
  PAID: '回款',
  ARCHIVED: '归档',
}
const stageLabel = value => STAGE_LABELS[value] || value
const formatDate = value => (value ? new Date(value).toLocaleString('zh-CN', { hour12: false }) : '-')

onMounted(async () => {
  try {
    info.value = await getConfirmInfo(token)
  } catch (e) {
    error.value = true
  } finally {
    loading.value = false
  }
})

async function act(action) {
  acting.value = true
  try {
    const result = await submitConfirmAction(token, action, feedback.value || undefined)
    doneMessage.value = result.message || '操作成功'
    done.value = true
  } catch (e) {
    if (e && (e.code === 80250 || e.code === 80230)) {
      error.value = true
    } else {
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    acting.value = false
  }
}
</script>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: linear-gradient(160deg, #eef2f8 0%, #e3ecf5 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 24px;
}
.confirm-card {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 8px 30px rgba(30, 60, 100, 0.10);
  padding: 28px 26px;
  min-height: 320px;
}
.brand { display: flex; align-items: center; gap: 8px; margin-bottom: 18px; }
.logo {
  width: 30px; height: 30px; border-radius: 8px;
  background: #2f54eb; color: #fff; font-weight: 600;
  display: flex; align-items: center; justify-content: center;
}
.brand-name { font-weight: 600; color: #333; }
.title { margin: 0 0 16px; font-size: 18px; color: #222; }
.summary { margin-bottom: 18px; }
.row {
  display: flex; align-items: baseline; gap: 12px;
  padding: 8px 0; border-bottom: 1px dashed #eee; font-size: 14px; color: #444;
}
.row:last-child { border-bottom: none; }
.label { flex-shrink: 0; width: 92px; color: #999; font-size: 13px; }
.actions { display: flex; gap: 10px; margin-top: 14px; }
.actions .el-button { flex: 1; }
.state-block { text-align: center; padding: 30px 0; }
.state-block h2 { margin: 14px 0 6px; font-size: 18px; color: #333; }
.muted { color: #999; font-size: 13px; }
.done { margin-top: 10px; }
.footer { margin-top: 18px; color: #a8abb2; font-size: 12px; }
</style>
