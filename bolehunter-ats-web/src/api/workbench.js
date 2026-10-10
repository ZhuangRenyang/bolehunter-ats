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
