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

import com.alibaba.fastjson.JSONObject;

/**
 * @Param eval
 * @Time 2022/8/27 16:14
 */
public interface RedisService{
    // 设置验证码缓存, 如果没有使用,那么5分钟后删除这个key
    void setMailCode(String code);
    Object getMailCode(String code);

    void delMailCode(String code);

    // 将聊天内容存储
    void saveChatData(String token, String data);
    // 根据传进来的角色认证头字段,取到对应的聊天数据
    String getChatData(String token);
}
