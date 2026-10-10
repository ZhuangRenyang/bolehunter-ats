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
package com.bolehunter.dto.dict;

import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 新增/更新字典数据
 */
@Data
@NoArgsConstructor
public class CreateOrUpdateDictDataDTO {

    /**
     * 所属字典类型键
     */
    @NotBlank(message = "{dict.type.key.not-blank}")
    @Size(max = 64, message = "{dict.type.key.not-blank}")
    private String dictType;

    @NotBlank(message = "{dict.data.label.not-blank}")
    @Size(max = 64, message = "{dict.data.label.not-blank}")
    private String dictLabel;

    @NotBlank(message = "{dict.data.value.not-blank}")
    @Size(max = 64, message = "{dict.data.value.not-blank}")
    private String dictValue;

    /**
     * 显示顺序
     */
    private Integer dictSort;

    /**
     * 状态: 1-正常 0-停用
     */
    private Integer status;

    @Size(max = 255, message = "{dict.data.label.not-blank}")
    private String remark;
}
