import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

const backend = 'http://127.0.0.1:5000'

export default defineConfig({
  plugins: [
    vue(),
    // Element Plus 按需引入（迭代17 性能加固）：模板 el-* 组件与样式自动导入
    AutoImport({
      resolvers: [ElementPlusResolver()],
    }),
    Components({
      resolvers: [ElementPlusResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  build: {
    // 迭代28 实测：Vite 产物默认在 /assets/，与后端静态资源路径（简历/头像，nginx 反代 /assets/**）
    // 冲突——前端 JS/CSS 被 nginx 反代到 backend 导致 404 白屏。产物移至 /static/ 彻底解耦
    assetsDir: 'static',
    // 迭代28 TDZ 白屏终版修复：manualChunks 自定义已整体移除。根因是业务源码循环依赖
    // （router→layout→im-drawer/stores/api→request→router），i18n 加入改变 Rollup 环内
    // 求值顺序后触发 TDZ；已通过 router 布局懒加载 + ImDrawer 异步组件断环。
    // manualChunks 的任何自定义划分在环存在时都无法根治，交还 Rollup 自动分包
    rollupOptions: {
      output: {
        // echarts 体积大，保持独立 chunk 单独缓存
        manualChunks: {
          'echarts': ['echarts'],
        },
      },
    },
  },
  server: {
    port: 8081,
    proxy: {
      // 后端业务接口；ws:true 让 WebSocket（迭代23 的 /recruit/ws）同样走该代理
      '/recruit': { target: backend, changeOrigin: true, ws: true },
      // 头像/简历等静态资源
      '/assets': { target: backend, changeOrigin: true },
    },
  },
})
