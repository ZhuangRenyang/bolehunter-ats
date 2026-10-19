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
import { onBeforeUnmount, onMounted, ref } from 'vue'

/**
 * 移动端断点判定（与 styles/layout.css 侧栏抽屉的 820px 保持一致）。
 *
 * 列表页据此在「表格（宽屏）」与「卡片流（窄屏，无需横向滚动）」之间切换。
 * 用 matchMedia 而非 window.resize：无高频回调，且首次赋值即为正确值，避免闪烁。
 */
export function useIsMobile(query = '(max-width: 820px)') {
  const isMobile = ref(typeof window !== 'undefined' && window.matchMedia(query).matches)
  let mql = null

  const update = e => { isMobile.value = e.matches }

  onMounted(() => {
    mql = window.matchMedia(query)
    isMobile.value = mql.matches
    // 旧版 Safari 只有 addListener
    if (mql.addEventListener) mql.addEventListener('change', update)
    else mql.addListener(update)
  })

  onBeforeUnmount(() => {
    if (!mql) return
    if (mql.removeEventListener) mql.removeEventListener('change', update)
    else mql.removeListener(update)
  })

  return isMobile
}
