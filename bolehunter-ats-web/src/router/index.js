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
import { createRouter, createWebHistory } from 'vue-router'
import Cookies from 'js-cookie'

/**
 * 迭代28 修复：此处不再 import { getToken } from '@/api/request'——
 * request.js 顶部 import router 用于 401 跳转，二者构成模块循环依赖；
 * 迭代 27 新增 i18n 改变 Rollup 模块求值顺序后，循环内绑定在初始化前被访问，
 * 浏览器报 "Cannot access 'Po' before initialization" 白屏。
 * 守卫里只需读 access_token cookie，直接用 js-cookie 内联，环解除
 * （request.js → router 仍为单向依赖）。
 */
const TOKEN_ACCESS = 'access_token'

const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/view/login/login.vue'),
    meta: { public: true },
  },
  {
    // 免登录推荐确认页（客户 HR 用，public 放行）
    path: '/confirm/:token',
    name: 'confirmPage',
    component: () => import('@/view/confirm/confirm-page.vue'),
    // meta.title 存 i18n key（menu.*），渲染时经 t() 翻译（迭代 27 国际化骨架）
    meta: { public: true, title: 'menu.confirm' },
  },
  {
    // 候选人自助信息收集页（免登录，迭代 36）
    path: '/collect/:token',
    name: 'collectForm',
    component: () => import('@/view/collect/collect-form.vue'),
    meta: { public: true, title: 'menu.collect' },
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'), // 懒加载布局：router 求值图不再包含 layout 及其依赖链
    redirect: '/workbench',
    children: [
      {
        path: '/workbench',
        name: 'workbench',
        component: () => import('@/view/workbench/workbench.vue'),
        meta: { title: 'menu.workbench' },
      },
      {
        path: '/admin/user/list',
        name: 'userList',
        component: () => import('@/view/admin/user-list.vue'),
        meta: { title: 'menu.userList' },
      },
      {
        path: '/admin/group/list',
        name: 'groupList',
        component: () => import('@/view/admin/group-list.vue'),
        meta: { title: 'menu.groupList' },
      },
      {
        path: '/log',
        name: 'logList',
        component: () => import('@/view/admin/log-list.vue'),
        meta: { title: 'menu.log' },
      },
      {
        // 登录日志（此前仅存在于 stage.js 菜单与 view 文件，路由漏注册 → 点击/直链一律 404）
        path: '/admin/login-log',
        name: 'loginLog',
        component: () => import('@/view/admin/login-log.vue'),
        meta: { title: 'menu.loginLog' },
      },
      {
        path: '/setting/dept/list',
        name: 'deptList',
        component: () => import('@/view/setting/dept-list.vue'),
        meta: { title: 'menu.dept' },
      },
      {
        path: '/setting/dict/list',
        name: 'dictList',
        component: () => import('@/view/setting/dict-list.vue'),
        meta: { title: 'menu.dict' },
      },
      {
        path: '/setting/flag/list',
        name: 'flagList',
        component: () => import('@/view/setting/flag-list.vue'),
        meta: { title: 'menu.flag' },
      },
      {
        path: '/setting/ai',
        name: 'aiSetting',
        component: () => import('@/view/setting/ai-setting.vue'),
        meta: { title: 'menu.ai' },
      },
      {
        path: '/setting/gen',
        name: 'gen',
        component: () => import('@/view/setting/gen.vue'),
        meta: { title: 'menu.gen' },
      },
      {
        path: '/setting/upgrade',
        name: 'systemUpgrade',
        component: () => import('@/view/setting/upgrade.vue'),
        meta: { title: 'menu.upgrade' },
      },
      {
        // 以下三页同样漏注册（菜单有入口、文件存在、路由缺失）：服务器心跳 / 文件中心 / 版本记录
        path: '/setting/heartbeat',
        name: 'heartbeat',
        component: () => import('@/view/setting/heartbeat.vue'),
        meta: { title: 'menu.heartbeat' },
      },
      {
        path: '/setting/file-center',
        name: 'fileCenter',
        component: () => import('@/view/setting/file-center.vue'),
        meta: { title: 'menu.fileCenter' },
      },
      {
        path: '/setting/changelog',
        name: 'changelog',
        component: () => import('@/view/setting/changelog.vue'),
        meta: { title: 'menu.changelog' },
      },
      {
        path: '/position/list',
        name: 'positionList',
        component: () => import('@/view/position/position-list.vue'),
        meta: { title: 'menu.positionList' },
      },
      {
        path: '/position/detail/:id',
        name: 'positionDetail',
        component: () => import('@/view/position/position-detail.vue'),
        meta: { title: 'menu.positionDetail' },
      },
      {
        path: '/client/list',
        name: 'clientList',
        component: () => import('@/view/client/client-list.vue'),
        meta: { title: 'menu.clientList' },
      },
      {
        path: '/client/detail',
        name: 'clientDetail',
        component: () => import('@/view/client/client-detail.vue'),
        meta: { title: 'menu.clientDetail' },
      },
      {
        path: '/candidate/list',
        name: 'candidateList',
        component: () => import('@/view/candidate/candidate-list.vue'),
        meta: { title: 'menu.candidateList' },
      },
      {
        path: '/candidate/detail',
        name: 'candidateDetail',
        component: () => import('@/view/candidate/candidate-detail.vue'),
        meta: { title: 'menu.candidateDetail' },
      },
      {
        path: '/recommendation/kanban',
        name: 'recommendationKanban',
        component: () => import('@/view/recommendation/kanban.vue'),
        meta: { title: 'menu.kanban' },
      },
      {
        path: '/recommendation/list',
        name: 'recommendationList',
        component: () => import('@/view/recommendation/list.vue'),
        meta: { title: 'menu.recommendationList' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'notFound',
    component: () => import('@/view/error/404.vue'),
    meta: { public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach(to => {
  if (to.meta.public) {
    return true
  }
  const token = Cookies.get(TOKEN_ACCESS)
  if (!token) {
    return { path: '/login', query: to.path === '/login' ? {} : { redirect: to.path } }
  }
  return true
})

export default router
