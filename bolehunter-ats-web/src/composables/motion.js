/*
 * BoleHunter 珍珠玻璃 · 动效统一入口（迭代 29e，GSAP 3）
 * 设计规范源自 scaffold-master 珍珠玻璃（MIT），独立实现。
 * 原则：动效服务于状态变化；只在 transform/opacity 上动画；尊重减少动效偏好。
 */
import gsap from 'gsap'

/** 统一缓动与时长 */
export const EASE = 'power2.out'
export const EASE_IN = 'power2.inOut'
export const DUR = { fast: 0.18, base: 0.35, slow: 0.6 }

/** 是否尊重减少动效偏好（调用方据此跳过动画直达终态） */
export function prefersReducedMotion() {
  return typeof window !== 'undefined'
    && window.matchMedia
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches
}

/**
 * 面板内容级联进场：直接子元素依次淡入上移（约 70-90ms 级联）。
 * 用法：面板 mounted/数据就绪后 revealCascade(panelRef.value)
 */
export function revealCascade(el, selector = '> *', stagger = 0.07) {
  if (!el || prefersReducedMotion()) return
  gsap.from(el.querySelectorAll(selector), {
    opacity: 0,
    y: 12,
    duration: DUR.base,
    stagger,
    ease: EASE,
    clearProps: 'all',
  })
}

/**
 * 读数滚动落定：数字从 from 滚动到 to（"结"语态——读数逐位落定）。
 * @param {HTMLElement} el 显示数值的元素
 * @param {number} to 目标值
 * @param {Function} render 格式化函数（默认取整）
 */
export function countTo(el, to, render = v => String(Math.round(v))) {
  if (!el) return
  const from = Number(el.dataset.from || 0)
  el.dataset.from = String(to)
  if (prefersReducedMotion() || from === to) {
    el.textContent = render(to)
    return
  }
  const obj = { v: from }
  gsap.to(obj, {
    v: to,
    duration: DUR.slow,
    ease: 'power1.out',
    onUpdate: () => { el.textContent = render(obj.v) },
  })
}

/** 按压反馈（"放"语态的轻量版：CSS 不可达处的 JS 补充，一般用 CSS 即可） */
export function pressFeedback(el) {
  if (!el || prefersReducedMotion()) return
  gsap.fromTo(el, { scale: 0.97 }, { scale: 1, duration: DUR.fast, ease: EASE, clearProps: 'scale' })
}

/** SVG 描边生长（"导"语态：折线描绘/连线点亮） */
export function drawLine(el, duration = DUR.slow, delay = 0) {
  if (!el) return
  if (prefersReducedMotion()) return
  const len = el.getTotalLength ? el.getTotalLength() : 0
  if (!len) return
  gsap.fromTo(el,
    { strokeDasharray: len, strokeDashoffset: len },
    { strokeDashoffset: 0, duration, delay, ease: EASE, clearProps: 'strokeDasharray,strokeDashoffset' })
}

/** 节点级联点亮（"结"语态：图谱节点逐个落定） */
export function popIn(el, delay = 0) {
  if (!el || prefersReducedMotion()) return
  gsap.from(el, { scale: 0, opacity: 0, duration: DUR.base, delay, ease: 'back.out(1.8)', transformOrigin: 'center', clearProps: 'all' })
}
