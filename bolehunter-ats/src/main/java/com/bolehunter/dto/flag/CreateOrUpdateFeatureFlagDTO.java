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
package com.bolehunter.dto.flag;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新功能开关
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateFeatureFlagDTO {

    /**
     * 开关键，如 resume_parse
     */
    @NotBlank(message = "{flag.key.not-blank}")
    @Size(max = 64, message = "{flag.key.not-blank}")
    private String flagKey;

    @NotBlank(message = "{flag.name.not-blank}")
    @Size(max = 64, message = "{flag.name.not-blank}")
    private String flagName;

    @Size(max = 255, message = "{flag.name.not-blank}")
    private String description;

    /**
     * 是否开启: 1-开 0-关
     */
    private Integer enabled;
}
