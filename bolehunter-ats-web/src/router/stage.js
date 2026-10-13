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
/**
 * 路由配置（与旧版 stage 机制同构）：
 * folder（分组导航）→ children 中 view 挂组件；permission 数组为可见权限名。
 */

export const adminStage = {
  title: '权限管理',
  type: 'folder',
  icon: 'User',
  inNav: true,
  permission: ['管理员'],
  children: [
    {
      title: '用户管理',
      type: 'view',
      name: 'userList',
      route: '/admin/user/list',
      filePath: 'view/admin/user-list.vue',
      inNav: true,
      icon: 'UserFilled',
      permission: ['查询所有用户'],
    },
    {
      title: '角色管理',
      type: 'view',
      name: 'groupList',
      route: '/admin/group/list',
      filePath: 'view/admin/group-list.vue',
      inNav: true,
      icon: 'Avatar',
      permission: ['查询所有权限组'],
    },
  ],
}

export const positionStage = {
  title: '职位管理',
  type: 'folder',
  icon: 'Briefcase',
  inNav: true,
  permission: ['管理职位分页'],
  children: [
    {
      title: '职位列表',
      type: 'view',
      name: 'positionList',
      route: '/position/list',
      filePath: 'view/position/position-list.vue',
      inNav: true,
      icon: 'Suitcase',
      permission: ['管理职位分页'],
    },
  ],
}

export const settingStage = {
  title: '系统设置',
  type: 'folder',
  icon: 'Setting',
  inNav: true,
  permission: ['查询部门树', '查询字典类型', '查询功能开关', '查询代码生成表列表'],
  children: [
    {
      title: '部门管理',
      type: 'view',
      name: 'deptList',
      route: '/setting/dept/list',
      filePath: 'view/setting/dept-list.vue',
      inNav: true,
      icon: 'OfficeBuilding',
      permission: ['查询部门树'],
    },
    {
      title: '字典管理',
      type: 'view',
      name: 'dictList',
      route: '/setting/dict/list',
      filePath: 'view/setting/dict-list.vue',
      inNav: true,
      icon: 'Collection',
      permission: ['查询字典类型'],
    },
    {
      title: '功能开关',
      type: 'view',
      name: 'flagList',
      route: '/setting/flag/list',
      filePath: 'view/setting/flag-list.vue',
      inNav: true,
      icon: 'Open',
      permission: ['查询功能开关'],
    },
    {
      title: 'AI 服务配置',
      type: 'view',
      name: 'aiSetting',
      route: '/setting/ai',
      filePath: 'view/setting/ai-setting.vue',
      inNav: true,
      icon: 'MagicStick',
      permission: ['查询AI服务配置'],
    },
    {
      title: '代码生成',
      type: 'view',
      name: 'gen',
      route: '/setting/gen',
      filePath: 'view/setting/gen.vue',
      inNav: true,
      icon: 'MagicStick',
      permission: ['查询代码生成表列表'],
    },
  ],
}

export const clientStage = {
  title: '客户管理',
  type: 'folder',
  icon: 'Suitcase',
  inNav: true,
  permission: ['查询客户列表'],
  children: [
    {
      title: '客户列表',
      type: 'view',
      name: 'clientList',
      route: '/client/list',
      filePath: 'view/client/client-list.vue',
      inNav: true,
      icon: 'Suitcase',
      permission: ['查询客户列表'],
    },
  ],
}

export const candidateStage = {
  title: '候选人库',
  type: 'folder',
  icon: 'UserFilled',
  inNav: true,
  permission: ['查询候选人列表'],
  children: [
    {
      title: '候选人列表',
      type: 'view',
      name: 'candidateList',
      route: '/candidate/list',
      filePath: 'view/candidate/candidate-list.vue',
      inNav: true,
      icon: 'User',
      permission: ['查询候选人列表'],
    },
  ],
}

export const recommendationStage = {
  title: '推荐流程',
  type: 'folder',
  icon: 'Share',
  inNav: true,
  permission: ['查询推荐列表'],
  children: [
    {
      title: '推荐看板',
      type: 'view',
      name: 'recommendationKanban',
      route: '/recommendation/kanban',
      filePath: 'view/recommendation/kanban.vue',
      inNav: true,
      icon: 'Share',
      permission: ['查询推荐列表'],
    },
    {
      title: '推荐列表',
      type: 'view',
      name: 'recommendationList',
      route: '/recommendation/list',
      filePath: 'view/recommendation/list.vue',
      inNav: true,
      icon: 'Document',
      permission: ['查询推荐列表'],
    },
  ],
}

export const clientDetailStage = {
  title: '客户详情',
  type: 'view',
  name: 'clientDetail',
  route: '/client/detail',
  filePath: 'view/client/client-detail.vue',
  inNav: false,
  icon: 'Suitcase',
  permission: ['查询客户列表'],
}

export const candidateDetailStage = {
  title: '候选人详情',
  type: 'view',
  name: 'candidateDetail',
  route: '/candidate/detail',
  filePath: 'view/candidate/candidate-detail.vue',
  inNav: false,
  icon: 'User',
  permission: ['查询候选人列表'],
}

export const workbenchStage = {
  title: '工作台',
  type: 'view',
  name: 'workbench',
  route: '/workbench',
  filePath: 'view/workbench/workbench.vue',
  inNav: true,
  icon: 'HomeFilled',
  order: 0,
}

export const logStage = {
  title: '日志管理',
  type: 'view',
  name: 'logList',
  route: '/log',
  filePath: 'view/admin/log-list.vue',
  inNav: true,
  icon: 'Document',
  permission: ['查询所有日志'],
}

export const stageConfig = [
  workbenchStage,
  clientStage,
  candidateStage,
  recommendationStage,
  adminStage,
  positionStage,
  settingStage,
  logStage,
  clientDetailStage,
  candidateDetailStage,
]

/**
 * 权限过滤（与旧版 Utils.hasPermission 语义一致）：admin 用户全可见
 */
export function filterStage(stageConfigList, permissions, user) {
  if (user && user.admin) {
    return stageConfigList
  }
  const filter = item => {
    if (item.permission) {
      return item.permission.some(p => permissions.indexOf(p) > -1)
    }
    return true
  }
  const result = []
  stageConfigList.forEach(item => {
    if (!filter(item)) return
    const copy = { ...item }
    if (copy.children) {
      copy.children = copy.children.filter(filter)
    }
    result.push(copy)
  })
  return result
}
