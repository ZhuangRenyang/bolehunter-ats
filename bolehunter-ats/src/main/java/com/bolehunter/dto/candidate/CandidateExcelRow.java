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
package com.bolehunter.dto.candidate;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Excel 导入的候选人行数据（表头映射后）
 */
@Data
@NoArgsConstructor
public class CandidateExcelRow {

    /**
     * Excel 行号（1 开始，用于错误定位）
     */
    private int rowIndex;

    private String name;

    private String phone;

    private String email;

    private String currentCompany;

    private String currentPosition;

    /**
     * 原始学历文本（如"本科"，导入时映射为字典键）
     */
    private String educationText;

    private String city;

    private String remark;
}
