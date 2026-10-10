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
package com.bolehunter.dto.resume;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * @author Eval
 * @create 2021/4/1 13:03
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateResumeDTO {

    @NotEmpty(message = "{resume.ability.not-empty}")
    @Length(max = 1000, message = "{resume.ability.length}")
    private String ability;


    @Length(max = 1000, message = "{resume.personalSummary.length}")
    private String personalSummary;

    @NotNull(message = "{resume.userId.not-empty}")
    private Integer userId;

    /**
     * 英语等级
     */
    private String english;

    /**
     * 求职意向
     */
    private String jobIntention;

    /**
     * 居住地
     */
    private String home;

    /**
     * 年龄
     */
    private Integer age;

    /**
     * 性别
     */
    private Integer sex;
}
