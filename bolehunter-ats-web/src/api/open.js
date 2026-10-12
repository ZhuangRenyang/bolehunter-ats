/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
import axios from 'axios'
import { ElMessage } from 'element-plus'

/**
 * 匿名请求层：免登录确认页专用。
 * 不带 token、401 时不做登录跳转（游客页面无登录态）。
 */
const openInstance = axios.create({
  baseURL: '',
  timeout: 15000,
  validateStatus: status => status >= 200 && status < 500,
})

openInstance.interceptors.response.use(
  response => {
    if (response.status >= 400) {
      const body = response.data || {}
      const message = typeof body.message === 'string' ? body.message : '请求失败，请稍后重试'
      ElMessage.error(message)
      return Promise.reject(body)
    }
    return response.data
  },
  () => {
    ElMessage.error('网络异常，请检查网络连接')
    return Promise.reject(new Error('network'))
  },
)

/** 按 token 查看推荐摘要（脱敏） */
export const getConfirmInfo = token => openInstance.get(`recruit/open/confirm/${token}`)

/** 客户确认动作：read=已读 / interview=约面 / reject=婉拒 */
export const submitConfirmAction = (token, action, feedback) =>
  openInstance.post(`recruit/open/confirm/${token}/action`, { action, feedback })
