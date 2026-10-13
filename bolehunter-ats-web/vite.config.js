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
    rollupOptions: {
      output: {
        // 第三方库拆分独立 chunk，业务代码更新时缓存不受影响
        manualChunks: {
          'vendor-vue': ['vue', 'vue-router', 'pinia'],
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
