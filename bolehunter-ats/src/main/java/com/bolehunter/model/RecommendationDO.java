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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 推荐记录（核心表：职位 × 候选人 × 阶段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("recommendation")
@EqualsAndHashCode(callSuper = true)
public class RecommendationDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 推荐单号（REC-yyyyMMdd-id，迭代 33） */
    private String orderNo;

    /**
     * 职位id
     */
    private Integer jobId;

    /**
     * 候选人id
     */
    private Integer candidateId;

    /**
     * 客户公司id（自职位冗余）
     */
    private Integer clientId;

    /**
     * 负责顾问用户id
     */
    private Integer ownerId;

    /**
     * 当前阶段（字典 recommendation_stage）
     */
    private String currentStage;

    /**
     * 进入当前阶段时间
     */
    private Date stageTime;

    /**
     * 客户反馈记录（顾问代录）
     */
    private String feedback;

    /**
     * 最近反馈时间
     */
    private Date feedbackTime;

    /**
     * 免登录确认链接 token（v1.1 预留，本期不使用）
     */
    private String confirmToken;

    /**
     * 确认链接有效期（v1.1 预留，本期不使用）
     */
    private Date confirmExpire;

    /**
     * 备注
     */
    private String remark;

    /**
     * 租户id（开源版固定 1）
     */
    private Integer tenantId;

    // ---------------- 展示字段（非数据库列，批量填充避免 N+1） ----------------

    @TableField(exist = false)
    private String candidateName;

    @TableField(exist = false)
    private String candidateCompany;

    @TableField(exist = false)
    private String candidatePosition;

    @TableField(exist = false)
    private String jobTitle;

    @TableField(exist = false)
    private String clientName;

    @TableField(exist = false)
    private String ownerName;
}
