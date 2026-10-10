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
