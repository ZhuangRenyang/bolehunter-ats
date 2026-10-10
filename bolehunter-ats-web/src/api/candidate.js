import { get, post, put, _delete } from './request'

/**
 * 候选人
 */
export const getCandidatePage = params => get('recruit/candidate/page', params)
export const getCandidateDetail = id => get(`recruit/candidate/${id}`)
export const createCandidate = data => post('recruit/candidate', data)
export const updateCandidate = (id, data) => put(`recruit/candidate/${id}`, data)
export const deleteCandidate = id => _delete(`recruit/candidate/${id}`)
export const setCandidateTags = (id, tagIds) => put(`recruit/candidate/${id}/tags`, { tag_ids: tagIds })
export const duplicateCheck = params => get('recruit/candidate/duplicate/check', params)
export const getCandidateOptions = () => get('recruit/candidate/options')
export const importResume = formData => post('recruit/candidate/import/resume', formData)
export const importExcel = formData => post('recruit/candidate/import/excel', formData)

/**
 * 标签
 */
export const getTags = () => get('recruit/tag/list')
export const createTag = name => post('recruit/tag', { name })
export const updateTag = (id, name) => put(`recruit/tag/${id}`, { name })
export const deleteTag = id => _delete(`recruit/tag/${id}`)
