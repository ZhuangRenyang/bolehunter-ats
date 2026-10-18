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
  <Teleport to="body">
    <div v-if="visible && userText" class="watermark-overlay" aria-hidden="true">
      <div class="watermark-inner">
        <span v-for="n in tileCount" :key="n" class="watermark-tile">{{ userText }}</span>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { getFlags } from '@/api/setting'
import { get } from '@/api/request'

const visible = ref(false)

/** 当前用户名（登录后才有水印） */
const userText = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || 'null')
    return user?.nickname || user?.username || ''
  } catch (e) {
    return ''
  }
})

/** 检查 sys_watermark 开关（登录后拉取一次） */
async function checkFlag() {
  if (!userText.value) {
    visible.value = false
    return
  }
  try {
    const flags = await getFlags()
    const wm = (Array.isArray(flags) ? flags : []).find(f => f.flag_key === 'sys_watermark')
    visible.value = !!wm && Number(wm.enabled) === 1
  } catch (e) {
    visible.value = false
  }
}

watch(userText, checkFlag, { immediate: true })

/** 平铺密度：视口面积估算 */
const tileCount = 60
</script>

<style scoped>
.watermark-overlay {
  position: fixed;
  inset: 0;
  z-index: 9998;
  pointer-events: none;
  overflow: hidden;
}
.watermark-inner {
  position: absolute;
  inset: -80px;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 140px));
  transform: rotate(-24deg);
}
.watermark-tile {
  font-size: 13px;
  color: rgba(128, 128, 128, 0.13);
  white-space: nowrap;
  padding: 42px 8px;
  user-select: none;
}
</style>
