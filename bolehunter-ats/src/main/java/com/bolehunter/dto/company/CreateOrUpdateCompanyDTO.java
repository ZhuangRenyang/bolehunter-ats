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
package com.bolehunter.dto.company;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * @author Eval
 * @create 2021/4/4 13:14
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateCompanyDTO {

    @NotEmpty(message = "{company.name.not-empty}")
    private String name;

    private String foreignName;

    @NotEmpty(message = "{company.city.not-empty}")
    private String city;

    private String logo;

    private String poster;

    private String mission;

    private String description;

    private Integer state;

    /**
     * 所属行业（字典 industry 键，可选）
     */
    @Size(max = 32, message = "{company.industry.length}")
    private String industry;

    /**
     * 客户等级（字典 client_level 键，可选）
     */
    @Size(max = 32, message = "{company.level.length}")
    private String level;

    /**
     * 客户合作状态（字典 client_status 键，可选，默认 potential）
     */
    @Pattern(regexp = "potential|signing|suspended|lost", message = "{client.status.illegal}")
    private String clientStatus;

    /**
     * 负责顾问用户id（可选）
     */
    @Min(value = 1, message = "{id.positive}")
    private Integer ownerId;
}
