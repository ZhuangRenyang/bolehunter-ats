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
