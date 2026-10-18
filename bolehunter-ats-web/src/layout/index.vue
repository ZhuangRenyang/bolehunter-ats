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
  <WatermarkOverlay />
  <div class="shell">
    <!-- 左：导航轨（820px 以下转抽屉） -->
    <aside class="shell-rail m-rail" :class="{ 'is-open': railOpen }">
      <div class="shell-rail-brand">
        <div class="brand-logo">伯</div>
        <div class="brand-name">
          {{ t('layout.logo') }}
          <small>HEADHUNTER ATS</small>
        </div>
      </div>
      <el-scrollbar class="shell-menu">
        <el-menu :default-active="activeMenu" router>
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
      <div class="shell-rail-footer">
        © 2026 BoleHunter 伯乐猎
      </div>
    </aside>
    <!-- 抽屉遮罩（仅窄屏显示） -->
    <div v-if="railOpen" class="shell-drawer-mask" @click="railOpen = false"></div>

    <!-- 右：顶栏 + 内容区 -->
    <div class="shell-main">
      <header class="shell-topbar">
        <button class="shell-drawer-btn" :title="t('layout.menu')" @click="railOpen = !railOpen">
          <el-icon><Menu /></el-icon>
        </button>
        <div class="topbar-breadcrumb">
          <span class="current">{{ route.meta.title ? t(route.meta.title) : '' }}</span>
        </div>
        <div class="topbar-actions">
          <!-- 帮助与反馈（迭代 34） -->
          <button v-if="userStore.user?.username" class="topbar-icon-btn" :title="t('help.title')" @click="helpVisible = true">
            <el-icon><QuestionFilled /></el-icon>
          </button>
          <button v-if="userStore.user?.username" class="topbar-icon-btn" :title="t('feedback.entry')" @click="feedbackVisible = true">
            <el-icon><ChatDotRound /></el-icon>
          </button>
          <!-- 通知中心 -->
          <el-badge v-if="userStore.user?.username" :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <button class="topbar-icon-btn" :title="t('layout.noticeCenter')" @click="noticeDrawer = true">
              <el-icon><Bell /></el-icon>
            </button>
          </el-badge>
          <!-- 内部消息（迭代23：未读来自 REST 查询，新消息由 WebSocket 实时驱动刷新） -->
          <el-badge v-if="userStore.user?.username" :value="imUnread" :hidden="imUnread === 0" :max="99">
            <button class="topbar-icon-btn" :title="t('layout.imEntry')" @click="imDrawer = true">
              <el-icon><ChatDotSquare /></el-icon>
            </button>
          </el-badge>
          <!-- 外观（迭代 29a 珍珠玻璃：浅/深/跟随系统） -->
          <el-dropdown @command="switchTheme">
            <button class="topbar-icon-btn" :title="t('appearance.title')">
              <el-icon><component :is="themeIcon" /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="item in THEMES" :key="item.value" :command="item.value"
                  :class="{ 'locale-active': item.value === theme }">
                  {{ t(item.labelKey) }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <!-- 语言切换（迭代 27 国际化骨架） -->
          <el-dropdown v-if="userStore.user?.username" @command="switchLocale">
            <button class="topbar-icon-btn" :title="currentLocaleLabel">
              {{ currentLocaleLabel }}
            </button>
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
              <el-avatar :size="28">{{ nicknameFirstChar }}</el-avatar>
              <span class="user-nickname">{{ nickname }}</span>
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
      <main class="shell-content">
        <router-view />
      </main>
      <!-- 版权页脚：曾因编辑事故被嵌进 el-sub-menu 的 #title 模板内（迭代 27 修复为内容区底部） -->
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
  <HelpPanel v-model="helpVisible" />
  <FeedbackDialog v-model="feedbackVisible" />
</template>

<script setup>
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import HelpPanel from '@/components/HelpPanel.vue'
import FeedbackDialog from '@/components/FeedbackDialog.vue'
import WatermarkOverlay from '@/components/WatermarkOverlay.vue'
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
// 迭代 29a 珍珠玻璃：外观偏好（浅/深/跟随系统）
import { getTheme, setTheme, THEMES } from '@/composables/appearance'

const { t, locale } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const ws = useWsStore()

/* ===== 主题切换（迭代 29a） ===== */
const theme = ref(getTheme())
const themeIcon = computed(() => (theme.value === 'dark' ? 'Moon' : 'Sunny'))
function switchTheme(value) {
  setTheme(value)
  theme.value = getTheme()
}

/* ===== 窄屏抽屉 ===== */
const railOpen = ref(false)
watch(() => route.path, () => { railOpen.value = false })

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
const helpVisible = ref(false)
const feedbackVisible = ref(false)

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
 * 首次主动加载 + WebSocket 实时增量刷新，并以 60s 轮询兜底（WebSocket 不可用时功能不倒退）。
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

<style scoped>
/* 外壳结构与材质在 styles/layout.css + materials.css（全局层，本组件只留页面级细节） */

/* 顶栏用户区 */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  font-size: 13px;
  color: var(--ink);
  outline: none;
}
.user-nickname {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 通知行（抽屉内） */
.notice-toolbar { margin-bottom: 10px; }
.notice-row {
  padding: 10px 12px;
  border-radius: var(--r-xs);
  margin-bottom: 8px;
  background: var(--paper-deep);
}
.notice-row.unread {
  background: var(--blue-soft);
  border-left: 3px solid var(--blue);
}
.notice-content {
  font-size: 13px;
  line-height: 1.6;
}
.notice-time {
  margin-top: 4px;
  font-size: 12px;
  color: var(--ink-faint);
}

/* 版权页脚 */
.copyright-footer {
  padding: 10px 16px;
  font-size: 12px;
  color: var(--ink-faint);
  text-align: center;
  border-top: 1px solid var(--glass-line);
}
.copyright-footer a {
  color: var(--blue);
  text-decoration: none;
}
.locale-active {
  color: var(--blue);
  font-weight: 600;
}
</style>
