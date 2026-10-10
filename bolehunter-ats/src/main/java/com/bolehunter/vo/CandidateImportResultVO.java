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
package com.bolehunter.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 候选人导入结果报告
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateImportResultVO {

    /**
     * 总条数（文件行数/单文件计 1）
     */
    private int total;

    /**
     * 导入成功数
     */
    private int successCount;

    /**
     * 重复跳过数（手机号或邮箱已存在）
     */
    private int duplicateCount;

    /**
     * 解析/校验失败数
     */
    private int failCount;

    /**
     * 重复明细（候选人标识 + 冲突原因）
     */
    @Builder.Default
    private List<String> duplicates = new ArrayList<>();

    /**
     * 失败明细（原因）
     */
    @Builder.Default
    private List<String> failures = new ArrayList<>();
}
