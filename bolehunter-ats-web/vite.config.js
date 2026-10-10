import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const backend = 'http://127.0.0.1:5000'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 8081,
    proxy: {
      // 后端业务接口
      '/recruit': { target: backend, changeOrigin: true },
      // 头像/简历等静态资源
      '/assets': { target: backend, changeOrigin: true },
    },
  },
})
