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
