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
