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

/**
 * 工作台聚合
 */
export function getWorkbenchSummary() {
  return get('recruit/workbench/summary')
}

/**
 * 任务
 */
export function getMyTasks(scope) {
  return get('recruit/task/mine', scope ? { scope } : {})
}

export function createTask(data) {
  return post('recruit/task', data)
}

export function doneTask(id) {
  return put(`recruit/task/${id}/done`)
}

export function deleteTask(id) {
  return _delete(`recruit/task/${id}`)
}

export function getRelatedTasks(relatedType, relatedId) {
  return get('recruit/task/related', { related_type: relatedType, related_id: relatedId })
}

/**
 * 沟通记录
 */
export function getCommunications(recommendationId, page = 0, count = 20) {
  return get('recruit/communication/page', { recommendation_id: recommendationId, page, count })
}

export function createCommunication(data) {
  return post('recruit/communication', data)
}

export function deleteCommunication(id) {
  return _delete(`recruit/communication/${id}`)
}

/**
 * 推荐阶段流转历史
 */
export function getStageHistory(recommendationId) {
  return get(`recruit/recommendation/${recommendationId}/history`)
}
