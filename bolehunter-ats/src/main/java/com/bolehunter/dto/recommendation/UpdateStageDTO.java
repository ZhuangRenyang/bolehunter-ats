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
package com.bolehunter.dto.recommendation;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 推荐阶段流转
 */
@Data
@NoArgsConstructor
public class UpdateStageDTO {

    @NotBlank(message = "{recommendation.stage.illegal}")
    @Pattern(regexp = "PENDING|RECOMMENDED|CLIENT_READ|INTERVIEW_PENDING|INTERVIEWING|OFFER_PENDING|OFFERED|ONBOARDED|PAID|ARCHIVED",
            message = "{recommendation.stage.illegal}")
    private String stage;

    /**
     * 本次流转同时代录的客户反馈（可选）
     */
    private String feedback;
}
