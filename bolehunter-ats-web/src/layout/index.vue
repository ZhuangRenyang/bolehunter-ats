<!--
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 -->
<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="logo">{{ t('layout.logo') }}</div>
      <div class="notice-entry" v-if="userStore.user?.username">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
          <el-button :icon="'Bell'" circle size="small" :title="t('layout.noticeCenter')" @click="noticeDrawer = true" />
        </el-badge>
        <!-- 迭代23：内部消息（未读来自 REST 查询，新消息由 WebSocket 实时驱动刷新） -->
        <el-badge :value="imUnread" :hidden="imUnread === 0" :max="99">
          <el-button :icon="'ChatDotSquare'" circle size="small" :title="t('layout.imEntry')" @click="imDrawer = true" />
        </el-badge>
      </div>
      <el-scrollbar>
        <el-menu :default-active="activeMenu" router class="sidebar-menu">
          <template v-for="item in navStages" :key="item.title">
            <!-- 单页菜单 -->
            <el-menu-item v-if="!item.children || item.children.length === 0" :index="item.route">
              <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
              <span>{{ t(item.title) }}</span>
            </el-menu-item>
            <!-- folder 分组菜单（一层展开） -->
            <el-sub-menu v-else :index="item.title">
              <template #title>
                <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
                <span>{{ t(item.title) }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.route" :index="child.route">
                {{ t(child.title) }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>
    <div class="main-wrap">
      <header class="topbar">
        <div class="breadcrumb">{{ route.meta.title ? t(route.meta.title) : '' }}</div>
        <div class="topbar-right">
          <!-- 迭代 27 国际化骨架：语言切换（偏好持久化 localStorage，即时生效无需刷新） -->
          <el-dropdown v-if="userStore.user?.username" @command="switchLocale" class="locale-switch">
            <span class="locale-trigger">
              {{ currentLocaleLabel }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in SUPPORTED_LOCALES" :key="item.value" :command="item.value"
                  :class="{ 'locale-active': item.value === locale }">
                  {{ item.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="28" style="margin-right: 8px">
                {{ nicknameFirstChar }}
              </el-avatar>
              {{ nickname }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">{{ t('layout.logout') }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
      <!-- 版权页脚：曾因编辑事故被嵌进 el-sub-menu 的 #title 模板内（分组菜单标题里渲染页脚），
           迭代 27 修复为固定在内容区底部 -->
      <footer class="copyright-footer">
        © 2026 <a href="https://github.com/ZhuangRenyang/bolehunter-ats" target="_blank">BoleHunter 伯乐猎</a>
        · ZhuangRenyang · AGPL-3.0 ·
        <a href="https://gitee.com/ZhuangRenyang/bolehunter-ats" target="_blank">Gitee</a> /
        <a href="https://github.com/ZhuangRenyang/bolehunter-ats" target="_blank">GitHub</a>
      </footer>
    </div>
  </div>
    <el-drawer v-model="noticeDrawer" :title="t('layout.noticeCenter')" size="380px">
      <div class="notice-toolbar">
        <el-button size="small" plain @click="markAll">{{ t('layout.markAllRead') }}</el-button>
      </div>
      <el-empty v-if="notices.length === 0" :description="t('layout.noticeEmpty')" :image-size="60" />
      <div v-for="n in notices" :key="n.id" class="notice-row" :class="{ unread: String(n.is_read) === 'false' }">
        <div class="notice-content">{{ n.content }}</div>
        <div class="notice-time">{{ (n.time || '').slice(0, 19).replace('T', ' ') }}</div>
      </div>
    </el-drawer>
    <!-- 内部消息（迭代23） -->
    <ImDrawer v-model="imDrawer" />
</template>

<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useUserStore } from '@/stores/user'
// getToken 此前被使用却未导入，导致 mounted 里抛 ReferenceError —— 通知从未真正加载过
import { clearTokens, get, getToken, TOKEN_ACCESS, TOKEN_REFRESH } from '@/api/request'
import { getAllMyNotices, markAllRead } from '@/api/notify'
import { getImUnreadTotal } from '@/api/im'
import { useWsStore } from '@/stores/ws'
// i18n（迭代 27）：SUPPORTED_LOCALES 供切换菜单、setLocale 完成持久化切换
import { setLocale, SUPPORTED_LOCALES } from '@/locales'
// 迭代28：ImDrawer 改异步组件——静态引用使 layout→im-drawer→api/im→request→router→layout
// 形成模块循环，是 TDZ 白屏环链的一环
const ImDrawer = defineAsyncComponent(() => import('./im-drawer.vue'))

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const ws = useWsStore()

/* ===== 语言切换 ===== */
const currentLocaleLabel = computed(() => {
  const hit = SUPPORTED_LOCALES.find(item => item.value === locale.value)
  return (hit || SUPPORTED_LOCALES[0]).label
})

function switchLocale(value) {
  setLocale(value)
}

/* ===== 通知中心 ===== */
const noticeDrawer = ref(false)
const notices = ref([])
const unreadCount = ref(0)

/* ===== 内部消息（迭代23）===== */
const imDrawer = ref(false)
const imUnread = ref(0)

/** WS 不可用时的兜底轮询；同时也承担首次加载 */
let pollTimer = null
let offNotice = null
let offChat = null

async function loadNotices() {
  const username = userStore.user?.username
  if (!username) return
  try {
    const all = await getAllMyNotices(username)
    notices.value = all || []
    unreadCount.value = notices.value.filter(n => String(n.is_read) === 'false').length
  } catch (e) {
    /* 静默失败，下一轮轮询重试 */
  }
}

async function loadImUnread() {
  try {
    const res = await getImUnreadTotal()
    imUnread.value = res?.unreadTotal || 0
  } catch (e) {
    /* 静默失败：下一轮刷新重试 */
  }
}

/**
 * 建立实时通道（迭代23）。
 *
 * 之前这里只有一个从未赋值的 noticeTimer，通知实际上永远不会被加载，
 * 铃铛角标恒为 0、抽屉恒为空。现在改为：首次主动加载 + WebSocket 实时增量刷新，
 * 并以 60s 轮询兜底（WebSocket 不可用时功能不倒退）。
 */
function connectRealtime() {
  if (!getToken(TOKEN_ACCESS)) return
  offNotice = ws.on('notice', () => { loadNotices() })
  offChat = ws.on('chat', () => { loadImUnread() })
  ws.connect()
  pollTimer = setInterval(() => {
    loadNotices()
    loadImUnread()
  }, 60000)
}

async function markAll() {
  const username = userStore.user?.username
  if (!username) return
  await markAllRead(username)
  await loadNotices()
}

const navStages = computed(() => {
  const { filterStage, stageConfig } = require_stage()
  return filterStage(stageConfig, userStore.permissions, userStore.user)
})

// 避免循环依赖的简单包装（stage.js 无依赖 stores，直接 import 亦可）
function require_stage() {
  return stageModule
}

import * as stageModule from '@/router/stage'

const nickname = computed(() => (userStore.user && userStore.user.nickname) || t('layout.notLoggedIn'))
const nicknameFirstChar = computed(() => nickname.value.charAt(0))
const activeMenu = computed(() => route.path)

onMounted(async () => {
  if (getToken(TOKEN_ACCESS) && !userStore.loaded) {
    try {
      await userStore.loadPermissions()
    } catch (e) {
      // 权限接口失败时由请求层提示；无权限则登出
      if (!getToken(TOKEN_REFRESH) && !getToken(TOKEN_ACCESS)) {
        router.push('/login')
      }
    }
  }
  // 登录后补加载通知与未读消息，并接通实时通道
  await loadNotices()
  await loadImUnread()
  connectRealtime()
})

function handleCommand(command) {
  if (command === 'logout') {
    clearTokens()
    userStore.logout()
    router.push('/login')
  }
}
onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
  if (offNotice) offNotice()
  if (offChat) offChat()
  ws.disconnect()
})
</script>

<style lang="scss" scoped>
.app-layout {
  display: flex;
  height: 100%;
}

.sidebar {
  width: 220px;
  background: #001529;
  display: flex;
  flex-direction: column;

  .notice-entry {
  padding: 0 20px 10px;
  text-align: left;
}
.notice-toolbar {
  margin-bottom: 10px;
}
.notice-row {
  padding: 10px 12px;
  border-radius: 6px;
  margin-bottom: 8px;
  background: #f5f7fa;
}
.notice-row.unread {
  background: #ecf5ff;
  border-left: 3px solid #409eff;
}
.notice-content {
  font-size: 13px;
  line-height: 1.6;
}
.notice-time {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}
.logo {
    height: 56px;
    line-height: 56px;
    text-align: center;
    color: #ffffff;
    font-size: 16px;
    font-weight: 500;
  }

  .sidebar-menu {
    border-right: none;
    background: transparent;
  }
}

.main-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;

  .topbar {
    height: 56px;
    background: #ffffff;
    border-bottom: 1px solid #e4e7ed;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;

    .breadcrumb {
      font-size: 14px;
      color: #303133;
    }

    .topbar-right {
      display: flex;
      align-items: center;
      gap: 18px;
    }

    .locale-trigger {
      display: flex;
      align-items: center;
      cursor: pointer;
      font-size: 13px;
      color: #303133;
      outline: none;
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      font-size: 13px;
      color: #303133;
    }
  }

  .content {
    flex: 1;
    overflow: auto;
    padding: 16px;
  }
}
  .copyright-footer {
    padding: 10px 16px;
    font-size: 12px;
    color: #909399;
    text-align: center;
    border-top: 1px solid #ebeef5;
  }
  .copyright-footer a {
    color: #409eff;
    text-decoration: none;
  }
  .locale-active {
    color: #409eff;
    font-weight: 600;
  }
</style>
