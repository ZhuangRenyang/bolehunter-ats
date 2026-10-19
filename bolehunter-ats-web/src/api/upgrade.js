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
import { get, post } from './request'

/**
 * 系统升级（迭代26，仅管理员）
 * 自升级 = 触发宿主机 compose pull/up 拉取新镜像重建容器；
 * 数据库增量 = 新版本 backend 启动时自动执行（此处提供手动兜底）。
 */

/** 自升级状态：enabled / running / logTail */
export const getVersion = () => defaultAxios.get('recruit/admin/upgrade/version', { timeout: 180000 })
export const getUpgradeStatus = () => get('recruit/admin/upgrade/status')

/** 数据库迁移状态：待执行脚本 + 已执行历史 */
export const getMigrationStatus = () => get('recruit/admin/upgrade/migration/status')

/** 手动执行待迁移（正常由新版本启动时自动完成，此处兜底） */
export const runMigration = () => post('recruit/admin/upgrade/migration/run')

/** 触发自升级（异步：立即返回，服务随后可能重启） */
export const triggerUpgrade = () => post('recruit/admin/upgrade/run')

// 迭代 30：数据库备份与恢复（整库 .sql，raw 流式上传不受 multipart 限制）
export const exportDb = () => defaultAxios.get('recruit/admin/upgrade/db/export', { responseType: 'blob' })
export const importDb = (file, filename) => defaultAxios.post('recruit/admin/upgrade/db/import', file, {
  headers: { 'Content-Type': 'application/octet-stream', 'X-Filename': encodeURIComponent(filename) },
})
