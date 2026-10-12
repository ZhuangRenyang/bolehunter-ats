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
 * 职位管理（管理端）
 */
export const POSITIONS_STATES = [
  { value: -1, label: '全部' },
  { value: 1, label: '已通过' },
  { value: 0, label: '未审核' },
  { value: 2, label: '不通过' },
]

export const getPositionsAdmin = params => get('recruit/position/page/admin', params)
export const getPositionById = id => get(`recruit/position/${id}`)
export const createPosition = data => post('recruit/position', data)
export const updatePosition = (id, data) => put(`recruit/position/${id}`, data)
export const updatePositionState = (id, state) => put(`recruit/position/state/${id}`, { state })
export const deletePosition = id => _delete(`recruit/position/${id}`)
