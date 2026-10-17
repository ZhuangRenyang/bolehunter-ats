/*
 * BoleHunter 珍珠玻璃 · 外观偏好（迭代 29a）
 * 浅色 / 深色 / 跟随系统 → 写入根元素 data-theme；偏好存 localStorage(app_theme)。
 * 与语言偏好（app_locale）同款模式：一次调用完成切换 + 持久化 + 生效。
 */

const KEY = 'app_theme'
export const THEMES = [
  { value: 'light', labelKey: 'appearance.light' },
  { value: 'dark', labelKey: 'appearance.dark' },
  { value: 'system', labelKey: 'appearance.system' },
]

function systemPrefersDark() {
  return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches
}

function applyTheme(theme) {
  const effective = theme === 'system' ? (systemPrefersDark() ? 'dark' : 'light') : theme
  document.documentElement.setAttribute('data-theme', effective)
}

export function getTheme() {
  const saved = localStorage.getItem(KEY)
  return THEMES.some(t => t.value === saved) ? saved : 'light'
}

export function setTheme(theme) {
  if (!THEMES.some(t => t.value === theme)) return
  localStorage.setItem(KEY, theme)
  applyTheme(theme)
}

// 跟随系统时监听系统切换
if (window.matchMedia) {
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener?.('change', () => {
    if (getTheme() === 'system') applyTheme('system')
  })
}

// 启动即应用（避免首屏闪白/闪黑）
applyTheme(getTheme())
