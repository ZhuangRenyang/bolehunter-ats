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

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人信息收集链接（迭代 36）：顾问创建，候选人免登录自助填写
 */
@Data
@TableName("collect_link")
public class CollectLinkDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 免登录访问令牌 */
    private String token;

    /** 收集用途说明 */
    private String title;

    /** 创建顾问id */
    private Integer createdBy;

    /** 状态：1 收集中 / 0 已关闭 */
    private Integer status;

    /** 过期时间（空=长期有效） */
    private LocalDateTime expiresAt;

    private LocalDateTime createTime;
}
