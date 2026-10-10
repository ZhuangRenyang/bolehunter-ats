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
package com.bolehunter.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.recommendation.CreateRecommendationDTO;
import com.bolehunter.dto.recommendation.UpdateStageDTO;
import com.bolehunter.model.RecommendationDO;
import com.bolehunter.model.RecommendationStageHistoryDO;

import java.util.List;
import java.util.Map;

/**
 * 推荐记录服务
 */
public interface RecommendationService extends IService<RecommendationDO> {

    boolean createRecommendation(CreateRecommendationDTO validator, Integer currentUserId);

    boolean updateStage(Integer id, UpdateStageDTO validator, Integer operatorId);

    boolean updateFeedback(Integer id, String feedback);

    boolean deleteRecommendation(Integer id);

    /**
     * 推荐分页（按职位/候选人/阶段/负责人/客户筛选）
     */
    IPage<RecommendationDO> getPage(Integer jobId, Integer candidateId, Integer clientId, String stage,
                                    Integer ownerId, Integer page, Integer count);

    /**
     * 推荐记录 CSV 导出（按职位或客户维度，UTF-8 BOM 兼容 Excel）
     */
    String exportCsv(Integer jobId, Integer clientId);

    /**
     * 看板数据：平铺列表，前端按阶段分组（上限 1000 条）
     */
    List<RecommendationDO> getKanban(Integer jobId, Integer ownerId);

    /**
     * 可推荐职位下拉选项（含客户名）
     */
    List<Map<String, Object>> getJobOptions();

    /**
     * 推荐阶段流转历史（时间正序）
     */
    List<RecommendationStageHistoryDO> getStageHistory(Integer recommendationId);
}
