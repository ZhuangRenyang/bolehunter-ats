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
package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.mapper.CandidateMapper;
import com.bolehunter.mapper.CompanyMapper;
import com.bolehunter.mapper.RecommendationMapper;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.RecommendationDO;
import com.bolehunter.service.TaskService;
import com.bolehunter.service.WorkbenchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 工作台聚合服务实现（数据量小，统计直接走内存分组）
 */
@Service
public class WorkbenchServiceImpl implements WorkbenchService {

    private static final List<String> STAGE_KEYS = Arrays.asList(
            "PENDING", "RECOMMENDED", "CLIENT_READ", "INTERVIEW_PENDING", "INTERVIEWING",
            "OFFER_PENDING", "OFFERED", "ONBOARDED", "PAID", "ARCHIVED");

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private TaskService taskService;

    @Override
    public Map<String, Object> summary(Integer userId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("candidateTotal", candidateMapper.selectCount(new QueryWrapper<CandidateDO>()));
        summary.put("clientTotal", companyMapper.selectCount(new QueryWrapper<CompanyDO>()));
        summary.put("recommendationTotal", recommendationMapper.selectCount(new QueryWrapper<RecommendationDO>()));

        // 阶段分布：全量取 current_stage 字段后内存分组
        QueryWrapper<RecommendationDO> stageWrapper = new QueryWrapper<>();
        stageWrapper.lambda().select(RecommendationDO::getCurrentStage).last("LIMIT 2000");
        Map<String, Long> stageCount = new HashMap<>();
        recommendationMapper.selectList(stageWrapper)
                .forEach(item -> stageCount.merge(item.getCurrentStage(), 1L, Long::sum));
        List<Map<String, Object>> stageStats = new ArrayList<>();
        for (String stage : STAGE_KEYS) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stage", stage);
            item.put("count", stageCount.getOrDefault(stage, 0L));
            stageStats.add(item);
        }
        summary.put("stageStats", stageStats);

        summary.put("todayTasks", taskService.getMyTodo(userId, "today"));
        summary.put("overdueTasks", taskService.getMyTodo(userId, "overdue"));
        return summary;
    }
}
