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
package com.bolehunter.service;

import java.util.List;
import java.util.Map;

/**
 * 工作台聚合服务
 */
public interface WorkbenchService {

    /**
     * 工作台聚合：统计计数 + 阶段分布 + 今日到期/已逾期任务
     */
    Map<String, Object> summary(Integer userId);

    /**
     * 数据看板增强（迭代14）：
     * - funnelTrend：近 30 天 vs 前 30 天各阶段新增推荐数（时间维度对比）
     * - consultantRanking：本自然月顾问业绩排行（推荐/客户已读/面试/Offer/回款）
     * - clientConversion：客户转化率（按公司：推荐→面试→Offer→回款）
     */
    Map<String, Object> dashboard();

    /**
     * 客户转化率报表 CSV（UTF-8 BOM 兼容 Excel）
     */
    String clientConversionCsv();
}
