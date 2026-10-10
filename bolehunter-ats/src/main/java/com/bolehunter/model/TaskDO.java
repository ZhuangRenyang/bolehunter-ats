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
 * 任务（到期提醒；v1.0 工作台聚合查询，推送能力后续迭代接入）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task")
@EqualsAndHashCode(callSuper = true)
public class TaskDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务标题
     */
    private String title;

    /**
     * 任务说明
     */
    private String content;

    /**
     * 到期时间
     */
    private Date dueTime;

    /**
     * 负责顾问用户id
     */
    private Integer ownerId;

    /**
     * 关联对象类型（recommendation/client/candidate）
     */
    private String relatedType;

    /**
     * 关联对象id
     */
    private Integer relatedId;

    /**
     * 状态：0待办 1完成
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date doneTime;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 负责人昵称（非数据库列，查询时填充）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String ownerName;
}
