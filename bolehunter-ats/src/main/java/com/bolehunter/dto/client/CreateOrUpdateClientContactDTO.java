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
package com.bolehunter.dto.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新客户联系人
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateClientContactDTO {

    @NotBlank(message = "{contact.name.not-blank}")
    @Size(max = 50, message = "{contact.name.not-blank}")
    private String name;

    @Size(max = 20, message = "{contact.name.not-blank}")
    private String phone;

    @Email(message = "{email}")
    @Size(max = 100, message = "{contact.name.not-blank}")
    private String email;

    @Size(max = 50, message = "{contact.name.not-blank}")
    private String title;

    /**
     * 是否主联系人: 1-是 0-否（同公司仅一个）
     */
    private Integer isPrimary;

    @Size(max = 255, message = "{contact.name.not-blank}")
    private String remark;
}
