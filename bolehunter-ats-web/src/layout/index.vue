<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="logo">猎头 ATS</div>
      <el-scrollbar>
        <el-menu :default-active="activeMenu" router class="sidebar-menu">
          <template v-for="item in navStages" :key="item.title">
            <!-- 单页菜单 -->
            <el-menu-item v-if="!item.children || item.children.length === 0" :index="item.route">
              <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
            <!-- folder 分组菜单（一层展开） -->
            <el-sub-menu v-else :index="item.title">
              <template #title>
                <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
                <span>{{ item.title }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.route" :index="child.route">
                {{ child.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>
    </aside>
    <div class="main-wrap">
      <header class="topbar">
        <div class="breadcrumb">{{ route.meta.title || '' }}</div>
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
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { clearTokens, get, TOKEN_ACCESS, TOKEN_REFRESH } from '@/api/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const navStages = computed(() => {
  const { filterStage, stageConfig } = require_stage()
  return filterStage(stageConfig, userStore.permissions, userStore.user)
})

// 避免循环依赖的简单包装（stage.js 无依赖 stores，直接 import 亦可）
function require_stage() {
  return stageModule
}

import * as stageModule from '@/router/stage'

const nickname = computed(() => (userStore.user && userStore.user.nickname) || '未登录')
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
})

function handleCommand(command) {
  if (command === 'logout') {
    clearTokens()
    userStore.logout()
    router.push('/login')
  }
}
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
</style>
