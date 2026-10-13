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
// axios 实例是 default 导出，这里沿用 notify.js 的别名习惯
import defaultAxios from './request'

/**
 * 内部即时消息（迭代23）
 * WebSocket 负责实时送达，这里的一组接口负责历史回看 / 会话列表 / 未读数 / 已读标记，
 * 同时也是 WebSocket 不可用时的降级发送通道。
 */

/** 会话列表（含对方昵称、最后一条消息、未读数） */
export const getConversations = (limit = 50) => get('recruit/im/conversations', { limit })

/** 与某人的聊天历史 */
export const getImHistory = (peerId, limit = 100) => get('recruit/im/history', { peerId, limit })

/** 我的未读消息总数 */
export const getImUnreadTotal = () => get('recruit/im/unread')

/** 把来自 peerId 的消息标记为已读 */
export const markImRead = peerId => defaultAxios.put('recruit/im/read', null, { params: { peerId } })

/** 可发起会话的同事（排除自己） */
export const getColleagues = () => get('recruit/im/colleagues')

/** HTTP 通道发送消息（WebSocket 断开时的降级） */
export const sendImMessage = (receiverId, content) => post('recruit/im/send', { receiverId, content })
