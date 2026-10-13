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
package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.ImMessageDO;

import java.util.List;
import java.util.Map;

/**
 * 内部即时消息（迭代23）
 */
public interface ImMessageService extends IService<ImMessageDO> {

    /**
     * 发送单聊消息；落库后若接收者在线则经 WebSocket 实时送达。
     */
    ImMessageDO send(Integer senderId, Integer receiverId, String content);

    /**
     * 与某人的聊天历史（双向往来，按时间正序）
     * <p>返回视图 Map 而非 DO：BaseModel 的时间字段标注了 @JsonIgnore，直接序列化会丢失发送时间。</p>
     */
    List<Map<String, Object>> history(Integer me, Integer peer, Integer limit);

    /**
     * 会话列表：每个会话对象一条（含对方昵称、最后一条消息、未读数）
     */
    List<Map<String, Object>> conversations(Integer me, Integer limit);

    /**
     * 我的未读消息总数
     */
    int unreadTotal(Integer me);

    /**
     * 把来自 peer 的消息标记为已读；成功后通过 WebSocket 向对方发送已读回执。
     *
     * @return 实际被标记的条数
     */
    int markRead(Integer me, Integer peer);
}
