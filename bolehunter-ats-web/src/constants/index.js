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
 * 前端枚举常量（迭代 35 规范工程）——拒绝魔法数字，页面判断一律引用此处。
 * 说明：字典类取值（行业/阶段等）由后端字典接口下发，业务状态类在此集中。
 */

/** 任务状态 */
export const TASK_STATUS = {
  TODO: 0,
  DONE: 1,
}

/** 意见反馈状态 */
export const FEEDBACK_STATUS = {
  PENDING: 0,
  HANDLED: 1,
}

/** 登录日志结果 */
export const LOGIN_RESULT = {
  SUCCESS: true,
  FAILURE: false,
}

/** 用户类型（与后端 user.user_type 对应） */
export const USER_TYPE = {
  ADMIN: 'ADMIN',
  CONSULTANT: 'CONSULTANT',
  CLIENT_HR: 'CLIENT_HR',
  READONLY: 'READONLY',
}

/** 客户状态（字典值，后端 sys_dict_data.client_status） */
export const CLIENT_STATUS = {
  POTENTIAL: 'potential',
  SIGNING: 'signing',
  SUSPENDED: 'suspended',
  LOST: 'lost',
}
