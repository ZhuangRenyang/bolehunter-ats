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
 * 登录/用户
 */
export function login(data) {
  return post('recruit/user/login', data)
}

export function getPermissions() {
  return get('recruit/user/permissions')
}

export function register(data) {
  return post('recruit/user/register', data)
}

/**
 * 管理员：用户/角色
 */
export function getAdminUsers(params) {
  return get('recruit/admin/users', params)
}

export function getAllGroups() {
  return get('recruit/admin/group/all')
}

export function getGroupPage(params) {
  return get('recruit/admin/group', params)
}

export function getGroupDetail(id) {
  return get(`recruit/admin/group/${id}`)
}

export function createGroup(data) {
  return post('recruit/admin/group', data)
}

export function updateGroup(id, data) {
  return put(`recruit/admin/group/${id}`, data)
}

export function deleteGroup(id) {
  return _delete(`recruit/admin/group/${id}`)
}

export function getAllPermissions() {
  return get('recruit/admin/permission')
}

export function dispatchPermissions(data) {
  return post('recruit/admin/permission/dispatch/batch', data)
}

export function removePermissions(data) {
  return post('recruit/admin/permission/remove', data)
}

export function updateAdminUser(id, data) {
  return put(`recruit/admin/user/${id}`, data)
}

export function deleteUser(id) {
  return _delete(`recruit/admin/user/${id}`)
}

export function resetUserPassword(id, data) {
  return put(`recruit/admin/user/${id}/password`, data)
}

/**
 * 日志
 */
export function getLogs(params) {
  return get('recruit/log', params)
}
