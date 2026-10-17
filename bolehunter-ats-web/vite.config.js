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
    rollupOptions: {
      output: {
        // 第三方库拆分独立 chunk，业务代码更新时缓存不受影响
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
          // vue-i18n 单独成 chunk（迭代 27 国际化骨架），与 vue 生态解耦缓存
          'vendor-i18n': ['vue-i18n'],
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
