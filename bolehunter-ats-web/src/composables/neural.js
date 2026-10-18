/*
 * BoleHunter 珍珠玻璃 · 神经网络登录画布（迭代 29c）
 * 设计语言源自 scaffold-master 珍珠玻璃规范（MIT），独立实现：
 * 节点 = 能力模块（缓慢呼吸）、连线脉冲往返、节点漂移极缓。
 * 性能红线：DPR ≤ 2、页面隐藏/离屏暂停、每帧 try/catch、每条线脉冲 ≤ 1。
 * prefers-reduced-motion 时直接绘制静态终态，不做动画。
 */

/**
 * 创建神经网络背景动画
 * @param {HTMLCanvasElement} canvas
 * @param {Object} opts { colors: { node, line, pulse }, ambient }
 * @returns {{ destroy: Function }} 销毁函数
 */
export function createNeuralNet(canvas, opts = {}) {
  const ctx = canvas.getContext('2d')
  if (!ctx) return { destroy() {} }

  const reduced = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches
  const colors = {
    node: opts.colors?.node || 'rgba(140, 170, 235, ALPHA)',
    line: opts.colors?.line || 'rgba(110, 140, 200, ALPHA)',
    pulse: opts.colors?.pulse || '#8fb0f5',
    ...opts.colors,
  }

  let width = 0
  let height = 0
  let raf = 0
  let running = false
  let disposed = false

  // 确定性伪随机（固定 seed，节点布局每次一致）
  function seededRandom(seed) {
    let s = seed
    return () => {
      s = (s * 9301 + 49297) % 233280
      return s / 233280
    }
  }
  const rand = seededRandom(20261018)

  // 节点：相对坐标（0-1），resize 时按容器重排
  const NODE_COUNT = 14
  const nodes = Array.from({ length: NODE_COUNT }, (_, i) => ({
    rx: 0.08 + rand() * 0.84,
    ry: 0.12 + rand() * 0.76,
    phase: rand() * Math.PI * 2,      // 呼吸相位
    speed: 0.4 + rand() * 0.5,        // 呼吸速度
    drift: rand() * Math.PI * 2,      // 漂移方向
    core: i % 4 === 0,                // 少量核心节点更大更亮
  }))

  // 连线：距离阈值内的节点对（预计算索引对）
  let edges = []
  function buildEdges() {
    edges = []
    for (let i = 0; i < nodes.length; i++) {
      for (let j = i + 1; j < nodes.length; j++) {
        const dx = (nodes[i].rx - nodes[j].rx) * width
        const dy = (nodes[i].ry - nodes[j].ry) * height
        if (Math.hypot(dx, dy) < Math.min(width, height) * 0.42) {
          edges.push({ a: i, b: j })
        }
      }
    }
  }

  // 脉冲：沿某条线往返的光点（同时最多 3 个，红线内）
  const pulses = []
  function spawnPulse() {
    if (edges.length === 0 || pulses.length >= 3) return
    const edge = edges[Math.floor(Math.random() * edges.length)]
    pulses.push({ edge, t: 0, dir: Math.random() > 0.5 ? 1 : -1, speed: 0.006 + Math.random() * 0.006 })
  }

  function resize() {
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    const rect = canvas.parentElement.getBoundingClientRect()
    width = rect.width
    height = rect.height
    canvas.width = width * dpr
    canvas.height = height * dpr
    canvas.style.width = `${width}px`
    canvas.style.height = `${height}px`
    ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
    buildEdges()
  }

  function nodePos(n, time) {
    // 极缓漂移（正弦，幅度 6px）+ 呼吸
    const driftX = Math.sin(time * 0.00013 + n.drift) * 6
    const driftY = Math.cos(time * 0.00011 + n.drift) * 6
    return {
      x: n.rx * width + driftX,
      y: n.ry * height + driftY,
      glow: 0.55 + Math.sin(time * 0.001 * n.speed + n.phase) * 0.25,
    }
  }

  function paintStatic() {
    // 静态终态（减少动效偏好）：画一次网络，无脉冲
    ctx.clearRect(0, 0, width, height)
    for (const e of edges) {
      const a = nodePos(nodes[e.a], 0)
      const b = nodePos(nodes[e.b], 0)
      ctx.strokeStyle = colors.line.replace('ALPHA', '0.35')
      ctx.lineWidth = 1
      ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke()
    }
    for (const n of nodes) {
      const p = nodePos(n, 0)
      ctx.fillStyle = colors.node.replace('ALPHA', n.core ? '0.9' : '0.6')
      ctx.beginPath(); ctx.arc(p.x, p.y, n.core ? 5 : 3, 0, Math.PI * 2); ctx.fill()
    }
  }

  function frame(time) {
    if (!running || disposed) return
    try {
      ctx.clearRect(0, 0, width, height)

      // 连线
      for (const e of edges) {
        const a = nodePos(nodes[e.a], time)
        const b = nodePos(nodes[e.b], time)
        ctx.strokeStyle = colors.line.replace('ALPHA', '0.30')
        ctx.lineWidth = 1
        ctx.beginPath(); ctx.moveTo(a.x, a.y); ctx.lineTo(b.x, b.y); ctx.stroke()
      }

      // 脉冲推进与绘制
      for (let i = pulses.length - 1; i >= 0; i--) {
        const p = pulses[i]
        p.t += p.speed
        if (p.t >= 1) { pulses.splice(i, 1); continue }
        const a = nodePos(nodes[p.edge.a], time)
        const b = nodePos(nodes[p.edge.b], time)
        const t = p.dir > 0 ? p.t : 1 - p.t
        const x = a.x + (b.x - a.x) * t
        const y = a.y + (b.y - a.y) * t
        ctx.fillStyle = colors.pulse
        ctx.beginPath(); ctx.arc(x, y, 2.2, 0, Math.PI * 2); ctx.fill()
        // 脉冲光晕
        ctx.fillStyle = colors.pulse.replace('#', 'rgba(').replace(/rgb-a$/, '') // 简单光晕省略，保持性能
        ctx.globalAlpha = 0.18
        ctx.beginPath(); ctx.arc(x, y, 6, 0, Math.PI * 2); ctx.fill()
        ctx.globalAlpha = 1
      }

      // 节点呼吸
      for (const n of nodes) {
        const p = nodePos(n, time)
        ctx.fillStyle = colors.node.replace('ALPHA', String(Math.min(0.95, p.glow + (n.core ? 0.25 : 0))))
        ctx.beginPath(); ctx.arc(p.x, p.y, n.core ? 4.5 : 2.8, 0, Math.PI * 2); ctx.fill()
        if (n.core) {
          ctx.strokeStyle = colors.node.replace('ALPHA', '0.35')
          ctx.beginPath(); ctx.arc(p.x, p.y, 8, 0, Math.PI * 2); ctx.stroke()
        }
      }

      // 随机补充脉冲
      if (Math.random() < 0.02) spawnPulse()

      raf = requestAnimationFrame(frame)
    } catch (e) {
      // 每帧容错：异常时停止而非刷屏
      running = false
    }
  }

  function start() {
    if (running || disposed || reduced) return
    running = true
    raf = requestAnimationFrame(frame)
  }

  function stop() {
    running = false
    if (raf) cancelAnimationFrame(raf)
    raf = 0
  }

  // 初始化
  resize()
  if (reduced) {
    paintStatic()
  } else {
    start()
    // 随机初始脉冲，避免开场空白
    spawnPulse(); spawnPulse()
  }

  // 页面隐藏暂停（性能红线）
  const onVisibility = () => {
    if (document.hidden) stop()
    else if (!reduced) start()
  }
  document.addEventListener('visibilitychange', onVisibility)

  const onResize = () => {
    resize()
    if (reduced) paintStatic()
  }
  window.addEventListener('resize', onResize)

  return {
    destroy() {
      disposed = true
      stop()
      document.removeEventListener('visibilitychange', onVisibility)
      window.removeEventListener('resize', onResize)
    },
  }
}
