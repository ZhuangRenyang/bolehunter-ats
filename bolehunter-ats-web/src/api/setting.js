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
 * 部门
 */
export const getDeptTree = () => get('recruit/dept/tree')
export const getDeptList = () => get('recruit/dept/list')
export const createDept = data => post('recruit/dept', data)
export const updateDept = (id, data) => put(`recruit/dept/${id}`, data)
export const deleteDept = id => _delete(`recruit/dept/${id}`)

/**
 * 字典
 */
export const getDictTypePage = params => get('recruit/dict/types/page', params)
export const getAllDictTypes = () => get('recruit/dict/types')
export const createDictType = data => post('recruit/dict/types', data)
export const updateDictType = (id, data) => put(`recruit/dict/types/${id}`, data)
export const deleteDictType = id => _delete(`recruit/dict/types/${id}`)
export const getDictDataPage = params => get('recruit/dict/data/page', params)
export const getDictDataByType = dictType => get(`recruit/dict/data/all/${dictType}`)
export const createDictData = data => post('recruit/dict/data', data)
export const updateDictData = (id, data) => put(`recruit/dict/data/${id}`, data)
export const deleteDictData = id => _delete(`recruit/dict/data/${id}`)

/**
 * 功能开关
 */
export const getFlags = () => get('recruit/flag/list')
export const createFlag = data => post('recruit/flag', data)
export const updateFlag = (id, data) => put(`recruit/flag/${id}`, data)
export const toggleFlag = id => put(`recruit/flag/${id}/toggle`)
export const deleteFlag = id => _delete(`recruit/flag/${id}`)
