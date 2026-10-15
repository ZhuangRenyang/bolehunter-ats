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
import { get, post, put, _delete } from './request'
import defaultAxios from './request'

/**
 * 推荐流程
 *
 * 迭代 27 i18n：STAGES 仅保留枚举值（保序，看板列头/下拉顺序依赖它），
 * 显示文案统一走语言包 stage.* 命名空间（t(`stage.${value}`)）。
 */
export const STAGES = [
  'PENDING', 'RECOMMENDED', 'CLIENT_READ', 'INTERVIEW_PENDING', 'INTERVIEWING',
  'OFFER_PENDING', 'OFFERED', 'ONBOARDED', 'PAID', 'ARCHIVED',
]

export const getRecommendationPage = params => get('recruit/recommendation/page', params)
export const getKanban = params => get('recruit/recommendation/kanban', params)
export const createRecommendation = data => post('recruit/recommendation', data)
export const updateRecommendationStage = (id, data) => put(`recruit/recommendation/${id}/stage`, data)
export const updateRecommendationFeedback = (id, feedback) =>
  put(`recruit/recommendation/${id}/feedback`, { feedback })
export const deleteRecommendation = id => _delete(`recruit/recommendation/${id}`)
export const getJobOptions = () => get('recruit/recommendation/job-options')

/** 生成/刷新免登录确认链接（返回 confirm_token + url） */
export const generateConfirmToken = id => post(`recruit/recommendation/${id}/confirm-token`, {})

/** ===== 免登录确认（匿名，不需要 token 的 request 实例见 open.js） ===== */

/** 批量统计关联对象紧急任务数（看板角标） */
export const getUrgentTaskCounts = ids =>
  get('recruit/task/urgent-counts', { related_type: 'recommendation', ids: ids.join(',') })

export function exportCsv(params) {
  return defaultAxios.get('recruit/recommendation/export', { params, responseType: 'blob' })
}
