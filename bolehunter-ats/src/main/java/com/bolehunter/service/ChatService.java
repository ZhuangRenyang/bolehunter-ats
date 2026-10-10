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

import com.bolehunter.model.ChatDO;

import java.util.List;

/**
 * @Param eval
 * @Time 2022/9/28 12:50
 */
public interface ChatService {
    /**
     * 根据id获取到所有的聊天记录
     * @param id
     * @return
     */
    List<ChatDO> getChatById(Integer id);

    /**
     * 将双方的聊天记录插入数据库
     * ps: 反正就是个毕设, 暂不用考虑性能问题
     */
    void insertChat(ChatDO chat);

    /**
     * 根据id判断是否已经存在了聊天数据, 如果存在 则不用重新写入认证信息
     * @param selfID
     * @param targetID
     * @return
     */
    boolean getChatExistById(Integer selfID,Integer targetID);

    /**
     * 修改当前消息的已读状态
     * @param sendID
     * @param recvID
     * @param code   0 增加 ,1 删除
     */
    void changeChatState(String sendID,String recvID,boolean code);

    /**
     * 获取自己所有的未读消息id
     * @param userID
     * @return
     */
    String getChatState(String userID);
}
