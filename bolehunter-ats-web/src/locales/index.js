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
/**
 * i18n 实例工厂（迭代 27 国际化骨架）。
 *
 * 设计要点：
 * - legacy:false —— Composition API 模式，页面里 `const { t } = useI18n()`
 * - 语言偏好持久化 localStorage（app_locale），默认 zh-CN，未知值一律回落
 * - setLocale() 同时维护 <html lang>，供无障碍与浏览器翻译判断
 * - 非 JS 模块（如 api/request.js）用 i18n.global.t 取文案
 *
 * 后续页面批量抽取约定见 doc/开发记录日志.md 迭代 27。
 */
import { createI18n } from 'vue-i18n'
import zhCN from './zh-CN'
import enUS from './en-US'

export const LOCALE_KEY = 'app_locale'

export const SUPPORTED_LOCALES = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' },
]

export function isSupported(locale) {
  return SUPPORTED_LOCALES.some(item => item.value === locale)
}

/** 从 localStorage 读取偏好；任何异常/未知值都回落 zh-CN（基准语言） */
function detectLocale() {
  try {
    const saved = localStorage.getItem(LOCALE_KEY)
    return isSupported(saved) ? saved : 'zh-CN'
  } catch (e) {
    return 'zh-CN'
  }
}

const i18n = createI18n({
  legacy: false,
  locale: detectLocale(),
  fallbackLocale: 'zh-CN',
  missingWarn: false,
  fallbackWarn: false,
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS,
  },
})

function syncHtmlLang(locale) {
  document.documentElement.setAttribute('lang', locale === 'en-US' ? 'en' : 'zh-CN')
}

/** 切换语言：i18n 实例 + 持久化 + <html lang> 一次到位（Element Plus locale 由 App.vue 响应式跟随） */
export function setLocale(locale) {
  if (!isSupported(locale)) return
  i18n.global.locale.value = locale
  try {
    localStorage.setItem(LOCALE_KEY, locale)
  } catch (e) {
    /* localStorage 不可用时仅本次会话生效 */
  }
  syncHtmlLang(locale)
}

// 首次加载即同步 <html lang>
syncHtmlLang(i18n.global.locale.value)

export default i18n
