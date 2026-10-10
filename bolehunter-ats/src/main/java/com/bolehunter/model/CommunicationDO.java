/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 沟通记录（挂推荐记录，顾问逐次代录与客户 HR 的沟通）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("communication")
@EqualsAndHashCode(callSuper = true)
public class CommunicationDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐记录id
     */
    private Integer recommendationId;

    /**
     * 沟通方式（字典 communication_type）
     */
    private String type;

    /**
     * 沟通内容
     */
    private String content;

    /**
     * 记录人用户id
     */
    private Integer communicatorId;

    /**
     * 沟通时间
     */
    private Date communicateTime;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 记录人昵称（非数据库列，查询时填充）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String communicatorName;
}
