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
import { defineStore } from 'pinia'
import { getToken, TOKEN_ACCESS } from '@/api/request'

/**
 * WebSocket 连接单例（迭代23）
 *
 * 令牌随握手查询参数传递（浏览器 WebSocket 无法自定义请求头），服务端用同一个 JWT 签名校验，
 * 因此不会出现"改一下用户 id 就能冒充别人"的问题。
 *
 * 断线处理分三层：
 * 1. 指数退避重连（1s → 30s 上限）
 * 2. 连续失败超过阈值判定为不可用，停止无意义重试（常见于未开启 im_realtime 开关或令牌已失效）
 * 3. 上层功能（通知/聊天）检测到不可用后自动降级为 REST 轮询/发送，功能不中断
 */
export const useWsStore = defineStore('ws', {
  state: () => ({
    socket: null,
    connected: false,
    unavailable: false,
    handlers: {},
    heartbeatTimer: null,
    reconnectTimer: null,
    retries: 0,
  }),

  actions: {
    connect() {
      if (typeof WebSocket === 'undefined') return
      if (this.unavailable) return
      const alive = this.socket &&
        (this.socket.readyState === WebSocket.OPEN || this.socket.readyState === WebSocket.CONNECTING)
      if (alive) return

      const token = getToken(TOKEN_ACCESS)
      if (!token) return

      const scheme = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
      const url = `${scheme}//${window.location.host}/recruit/ws?token=${encodeURIComponent(token)}`

      const ws = new WebSocket(url)
      this.socket = ws
      ws.onopen = () => {
        this.connected = true
        this.retries = 0
        this.unavailable = false
        this.startHeartbeat()
      }
      ws.onmessage = evt => this.dispatch(evt.data)
      ws.onerror = () => { /* 详细原因在 onclose 中统一处理 */ }
      ws.onclose = () => {
        this.connected = false
        this.stopHeartbeat()
        this.socket = null
        this.scheduleReconnect()
      }
    },

    /** 按消息类型分发给订阅者 */
    dispatch(raw) {
      let payload
      try {
        payload = JSON.parse(raw)
      } catch (e) {
        return
      }
      const type = payload && payload.type
      const list = this.handlers[type]
      if (list && list.length) {
        list.slice().forEach(fn => fn(payload.data, payload))
      }
    },

    /** 订阅某类消息，返回取消订阅函数 */
    on(type, callback) {
      if (!this.handlers[type]) this.handlers[type] = []
      this.handlers[type].push(callback)
      return () => {
        const arr = this.handlers[type] || []
        const idx = arr.indexOf(callback)
        if (idx >= 0) arr.splice(idx, 1)
      }
    },

    send(payload) {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(JSON.stringify(payload))
        return true
      }
      return false
    },

    startHeartbeat() {
      this.stopHeartbeat()
      // 30s 一次心跳：保活并让服务端感知失效连接
      this.heartbeatTimer = setInterval(() => this.send({ type: 'ping' }), 30000)
    },

    stopHeartbeat() {
      if (this.heartbeatTimer) {
        clearInterval(this.heartbeatTimer)
        this.heartbeatTimer = null
      }
    },

    scheduleReconnect() {
      if (this.unavailable || this.reconnectTimer) return
      this.retries += 1
      if (this.retries > 5) {
        // 多半是功能未开启或令牌失效，继续重试只会刷日志
        this.unavailable = true
        return
      }
      const delay = Math.min(1000 * 2 ** (this.retries - 1), 30000)
      this.reconnectTimer = setTimeout(() => {
        this.reconnectTimer = null
        this.connect()
      }, delay)
    },

    disconnect() {
      this.unavailable = true
      this.stopHeartbeat()
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
        this.reconnectTimer = null
      }
      if (this.socket) {
        try {
          this.socket.close()
        } catch (e) { /* 忽略关闭异常 */ }
        this.socket = null
      }
      this.connected = false
    },
  },
})
