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
