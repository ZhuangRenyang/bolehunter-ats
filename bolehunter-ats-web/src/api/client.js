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

/**
 * 客户 CRM
 */
export const getClientPage = params => get('recruit/client/page', params)
export const getClientDetail = id => get(`recruit/client/${id}/detail`)
export const createClient = data => post('recruit/client', data)
export const updateClient = (id, data) => put(`recruit/client/${id}`, data)
export const assignOwner = (id, ownerId) => put(`recruit/client/${id}/owner`, { owner_id: ownerId })
export const changeClientStatus = (id, clientStatus) =>
  put(`recruit/client/${id}/status`, {}, { params: { client_status: clientStatus } })
export const getClientOwners = () => get('recruit/client/owners')
export const getClientOptions = () => get('recruit/client/options')
export const getContacts = companyId => get(`recruit/client/${companyId}/contact`)
export const createContact = (companyId, data) => post(`recruit/client/${companyId}/contact`, data)
export const updateContact = (companyId, contactId, data) =>
  put(`recruit/client/${companyId}/contact/${contactId}`, data)
export const deleteContact = (companyId, contactId) =>
  _delete(`recruit/client/${companyId}/contact/${contactId}`)
// 删除客户（迭代24 操作审计增强）：后端在名下存在推荐记录时会拒绝（80290）
export const deleteClient = id => _delete(`recruit/client/${id}`)
