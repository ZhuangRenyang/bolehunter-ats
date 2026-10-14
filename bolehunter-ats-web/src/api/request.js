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
import axios from 'axios'
import Cookies from 'js-cookie'
import { ElMessage } from 'element-plus'
import router from '@/router'
// 兜底提示走 i18n（迭代 27）；i18n 实例不依赖本模块，无循环引用
import i18n from '@/locales'

/** 请求层内取当前语言文案（请求层不是组件，用 global API） */
function t(key) {
  return i18n.global.t(key)
}

/**
 * 请求层：令牌存 Cookie（不落 localStorage）；
 * access token 过期时用 refresh token 静默续期一次后重试；
 * 后端错误结构 { code, message, request }，4xx 不重试、5xx 不重试（提示即可）。
 */

const instance = axios.create({
  baseURL: '',
  timeout: 15000,
  validateStatus: status => status >= 200 && status < 500,
})

export const TOKEN_ACCESS = 'access_token'
export const TOKEN_REFRESH = 'refresh_token'

export function getToken(key) {
  return Cookies.get(key)
}

export function saveTokens(accessToken, refreshToken) {
  Cookies.set(TOKEN_ACCESS, accessToken, { expires: 1 })
  if (refreshToken) {
    Cookies.set(TOKEN_REFRESH, refreshToken, { expires: 30 })
  }
}

export function clearTokens() {
  Cookies.remove(TOKEN_ACCESS)
  Cookies.remove(TOKEN_REFRESH)
}

/**
 * 请求体键名 camelCase → snake_case（后端全局 Jackson SNAKE_CASE 策略）
 */
function toSnakeKey(key) {
  return key.replace(/([A-Z])/g, '_$1').toLowerCase()
}

function deepToSnake(data) {
  if (data === null || typeof data !== 'object') return data
  if (data instanceof FormData || data instanceof File || data instanceof Blob) return data
  if (Array.isArray(data)) return data.map(deepToSnake)
  const result = {}
  Object.keys(data).forEach(key => {
    result[toSnakeKey(key)] = deepToSnake(data[key])
  })
  return result
}

instance.interceptors.request.use(originConfig => {
  const config = { ...originConfig }
  const accessToken = getToken(TOKEN_ACCESS)
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
  }
  // 请求体统一转 snake_case（FormData 跳过）
  if (config.data && typeof config.data === 'object' && !(config.data instanceof FormData)) {
    config.data = deepToSnake(config.data)
  }
  return config
})

let refreshing = false

instance.interceptors.response.use(
  response => {
    if (response.status === 401) {
      // 尝试用 refresh token 续期一次
      if (!refreshing) {
        refreshing = true
        const refreshToken = getToken(TOKEN_REFRESH)
        return axios
          .create()
          .get(
            '/recruit/user/refresh',
            { headers: refreshToken ? { Authorization: `Bearer ${refreshToken}` } : {} },
          )
          .then(res => {
            saveTokens(res.data.access_token, res.data.refresh_token)
            refreshing = false
            return instance(response.config)
          })
          .catch(() => {
            refreshing = false
            clearTokens()
            router.push('/login')
            return Promise.reject(response.data)
          })
      }
      clearTokens()
      router.push('/login')
      return Promise.reject(response.data)
    }
    if (response.status >= 400) {
      const body = response.data || {}
      const message = typeof body.message === 'string' ? body.message : t('request.failed')
      ElMessage.error(message)
      return Promise.reject(body)
    }
    return response.data
  },
  error => {
    // 网络层失败（断网/超时）
    ElMessage.error(t('request.networkError'))
    return Promise.reject(error)
  },
)

export function get(url, params = {}) {
  return instance({ url, method: 'get', params })
}

export function post(url, data = {}) {
  return instance({ url, method: 'post', data })
}

export function put(url, data = {}) {
  return instance({ url, method: 'put', data })
}

export function _delete(url, params = {}) {
  return instance({ url, method: 'delete', params })
}

export default instance
