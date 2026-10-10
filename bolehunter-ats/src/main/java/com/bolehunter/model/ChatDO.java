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
package com.bolehunter.model;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Param eval
 * @Time 2022/9/28 12:13
 */
@Data
@TableName("chat")

public class ChatDO {
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     * 消息的内容
     */
    private String content;

    /**
     * 发送者id
     */
    private String sendId;

    /**
     * 接收者id
     */
    private String recvId;


    /**
     * 发送者信息
     */
    private String self;

    /**
     * 接收者信息
     */
    private String target;

    /**
     * 发送的时间
     */
    private String timestamp;

}
