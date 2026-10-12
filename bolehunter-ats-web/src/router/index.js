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
import { getToken, TOKEN_ACCESS } from '@/api/request'
import Layout from '@/layout/index.vue'

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
    meta: { public: true, title: '推荐确认' },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/workbench',
    children: [
      {
        path: '/workbench',
        name: 'workbench',
        component: () => import('@/view/workbench/workbench.vue'),
        meta: { title: '工作台' },
      },
      {
        path: '/admin/user/list',
        name: 'userList',
        component: () => import('@/view/admin/user-list.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: '/admin/group/list',
        name: 'groupList',
        component: () => import('@/view/admin/group-list.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: '/log',
        name: 'logList',
        component: () => import('@/view/admin/log-list.vue'),
        meta: { title: '日志管理' },
      },
      {
        path: '/setting/dept/list',
        name: 'deptList',
        component: () => import('@/view/setting/dept-list.vue'),
        meta: { title: '部门管理' },
      },
      {
        path: '/setting/dict/list',
        name: 'dictList',
        component: () => import('@/view/setting/dict-list.vue'),
        meta: { title: '字典管理' },
      },
      {
        path: '/setting/flag/list',
        name: 'flagList',
        component: () => import('@/view/setting/flag-list.vue'),
        meta: { title: '功能开关' },
      },
      {
        path: '/setting/gen',
        name: 'gen',
        component: () => import('@/view/setting/gen.vue'),
        meta: { title: '代码生成' },
      },
      {
        path: '/position/list',
        name: 'positionList',
        component: () => import('@/view/position/position-list.vue'),
        meta: { title: '职位列表' },
      },
      {
        path: '/position/detail/:id',
        name: 'positionDetail',
        component: () => import('@/view/position/position-detail.vue'),
        meta: { title: '职位详情' },
      },
      {
        path: '/client/list',
        name: 'clientList',
        component: () => import('@/view/client/client-list.vue'),
        meta: { title: '客户列表' },
      },
      {
        path: '/client/detail',
        name: 'clientDetail',
        component: () => import('@/view/client/client-detail.vue'),
        meta: { title: '客户详情' },
      },
      {
        path: '/candidate/list',
        name: 'candidateList',
        component: () => import('@/view/candidate/candidate-list.vue'),
        meta: { title: '候选人列表' },
      },
      {
        path: '/candidate/detail',
        name: 'candidateDetail',
        component: () => import('@/view/candidate/candidate-detail.vue'),
        meta: { title: '候选人详情' },
      },
      {
        path: '/recommendation/kanban',
        name: 'recommendationKanban',
        component: () => import('@/view/recommendation/kanban.vue'),
        meta: { title: '推荐看板' },
      },
      {
        path: '/recommendation/list',
        name: 'recommendationList',
        component: () => import('@/view/recommendation/list.vue'),
        meta: { title: '推荐列表' },
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
  const token = getToken(TOKEN_ACCESS)
  if (!token) {
    return { path: '/login', query: to.path === '/login' ? {} : { redirect: to.path } }
  }
  return true
})

export default router
