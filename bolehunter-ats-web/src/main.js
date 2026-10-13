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
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElLoading } from 'element-plus'
import App from './App.vue'
import router from './router'
import './styles/global.scss'

// ---------- Element Plus 按需引入（迭代17 性能加固） ----------
// 模板中的 el-* 组件由 unplugin-vue-components 自动按需导入（见 vite.config.js）；
// 这里只补齐无法自动处理的三类：
// 1. v-loading 指令（模板指令不走组件 resolver）
app.directive('loading', ElLoading)
// 2. 指令式 API 的样式（ElMessage / ElMessageBox 的 JS 由摇树处理，样式需显式引入）
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import 'element-plus/es/components/loading/style/css'
// 3. 字符串形式使用的图标（el-button icon="Plus"、菜单 icon: 'HomeFilled'、动态 :is）
//    全量注册约 300 个图标会显著增大主包，改为项目实际用到的白名单
import {
  Avatar, Back, Bell, Briefcase, ChatDotSquare, Collection, Download, Edit, HomeFilled,
  MagicStick, Menu, OfficeBuilding, Open, Plus, Position, PriceTag, Promotion,
  Search, Setting, Share, Suitcase, Tickets, Upload, User, UserFilled,
} from '@element-plus/icons-vue'

const ICONS = {
  Avatar, Back, Bell, Briefcase, ChatDotSquare, Collection, Download, Edit, HomeFilled,
  MagicStick, Menu, OfficeBuilding, Open, Plus, Position, PriceTag, Promotion,
  Search, Setting, Share, Suitcase, Tickets, Upload, User, UserFilled,
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
// 注册白名单图标（el-icon / icon 属性 / 动态 component :is 用）
for (const [key, component] of Object.entries(ICONS)) {
  app.component(key, component)
}

app.mount('#app')
