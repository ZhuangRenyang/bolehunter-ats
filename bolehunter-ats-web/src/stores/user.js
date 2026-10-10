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
import { defineStore } from 'pinia'
import { get } from '@/api/request'

/**
 * 用户状态：信息 + 权限（菜单按权限名过滤，root/admin 全放行）
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    permissions: [],
    loaded: false,
  }),
  getters: {
    isLoggedIn: state => !!state.user,
  },
  actions: {
    setUser(user) {
      this.user = user
    },
    /**
     * 登录后拉取用户信息与权限（/recruit/user/permissions）
     */
    async loadPermissions() {
      const info = await get('recruit/user/permissions')
      this.user = { ...info }
      const list = []
      ;(info.permissions || []).forEach(group => {
        Object.keys(group).forEach(key => {
          group[key].forEach(item => {
            list.push(item.permission)
          })
        })
      })
      this.permissions = list
      this.loaded = true
      return info
    },
    logout() {
      this.user = null
      this.permissions = []
      this.loaded = false
    },
  },
})
