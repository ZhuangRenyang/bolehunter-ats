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

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 推荐阶段流转历史（append-only，只增不改不删）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recommendation_stage_history")
@EqualsAndHashCode(callSuper = true)
public class RecommendationStageHistoryDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 推荐记录id
     */
    private Integer recommendationId;

    /**
     * 原阶段（首次创建为 NULL）
     */
    private String fromStage;

    /**
     * 新阶段
     */
    private String toStage;

    /**
     * 流转时代录的客户反馈
     */
    private String feedback;

    /**
     * 操作人用户id
     */
    private Integer operatorId;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    /**
     * 操作人昵称（非数据库列，查询时填充）
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String operatorName;
}
