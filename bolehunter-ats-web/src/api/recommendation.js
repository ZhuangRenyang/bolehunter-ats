/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
import { get, post, put, _delete } from './request'
import defaultAxios from './request'

/**
 * 推荐流程
 */
export const STAGES = [
  { value: 'PENDING', label: '待推荐' },
  { value: 'RECOMMENDED', label: '已推荐' },
  { value: 'CLIENT_READ', label: '客户已读' },
  { value: 'INTERVIEW_PENDING', label: '待面试' },
  { value: 'INTERVIEWING', label: '面试中' },
  { value: 'OFFER_PENDING', label: '待Offer' },
  { value: 'OFFERED', label: 'Offer' },
  { value: 'ONBOARDED', label: '入职' },
  { value: 'PAID', label: '回款' },
  { value: 'ARCHIVED', label: '归档' },
]

export const getRecommendationPage = params => get('recruit/recommendation/page', params)
export const getKanban = params => get('recruit/recommendation/kanban', params)
export const createRecommendation = data => post('recruit/recommendation', data)
export const updateRecommendationStage = (id, data) => put(`recruit/recommendation/${id}/stage`, data)
export const updateRecommendationFeedback = (id, feedback) =>
  put(`recruit/recommendation/${id}/feedback`, { feedback })
export const deleteRecommendation = id => _delete(`recruit/recommendation/${id}`)
export const getJobOptions = () => get('recruit/recommendation/job-options')

export function exportCsv(params) {
  return defaultAxios.get('recruit/recommendation/export', { params, responseType: 'blob' })
}
