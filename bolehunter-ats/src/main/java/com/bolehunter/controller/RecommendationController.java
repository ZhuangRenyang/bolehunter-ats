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
package com.bolehunter.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.common.LocalUser;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.dto.recommendation.CreateRecommendationDTO;
import com.bolehunter.dto.recommendation.UpdateFeedbackDTO;
import com.bolehunter.dto.recommendation.UpdateStageDTO;
import com.bolehunter.model.RecommendationDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.RecommendationService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 推荐管理（核心流程：推荐 → 面试 → Offer → 入职 → 回款）
 */
@RestController
@RequestMapping("/recruit/recommendation")
@PermissionModule(value = "推荐管理")
@Validated
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 新增推荐（同一职位同一候选人唯一）
     */
    @Logger(template = "新增推荐")
    @PostMapping("")
    @PermissionMeta(value = "新增推荐")
    public CreatedVO create(@RequestBody @Validated CreateRecommendationDTO validator) {
        UserDO user = LocalUser.getLocalUser();
        recommendationService.createRecommendation(validator, user == null ? null : user.getId());
        return new CreatedVO(8700);
    }

    /**
     * 阶段流转（可同时代录客户反馈），自动写入流转历史
     */
    @Logger(template = "推荐阶段流转")
    @PutMapping("/{id}/stage")
    @PermissionMeta(value = "流转推荐")
    public UpdatedVO updateStage(@PathVariable @Positive(message = "{id.positive}") Integer id,
                                 @RequestBody @Validated UpdateStageDTO validator) {
        UserDO user = LocalUser.getLocalUser();
        recommendationService.updateStage(id, validator, user == null ? null : user.getId());
        return new UpdatedVO(8710);
    }

    /**
     * 代录客户反馈
     */
    @Logger(template = "代录客户反馈")
    @PutMapping("/{id}/feedback")
    @PermissionMeta(value = "流转推荐")
    public UpdatedVO updateFeedback(@PathVariable @Positive(message = "{id.positive}") Integer id,
                                    @RequestBody @Validated UpdateFeedbackDTO validator) {
        recommendationService.updateFeedback(id, validator.getFeedback());
        return new UpdatedVO(8720);
    }

    /**
     * 删除推荐记录
     */
    @Logger(template = "删除推荐")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "删除推荐")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        recommendationService.deleteRecommendation(id);
        return new DeletedVO(8730);
    }

    /**
     * 推荐分页列表（支持按客户聚合）
     */
    @Logger(template = "查询推荐列表")
    @GetMapping("/page")
    @PermissionMeta(value = "查询推荐列表")
    public PageResponseVO<RecommendationDO> page(
            @RequestParam(name = "job_id", required = false) Integer jobId,
            @RequestParam(name = "candidate_id", required = false) Integer candidateId,
            @RequestParam(name = "client_id", required = false) Integer clientId,
            @RequestParam(name = "stage", required = false) String stage,
            @RequestParam(name = "owner_id", required = false) Integer ownerId,
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page) {
        IPage<RecommendationDO> iPage = recommendationService.getPage(jobId, candidateId, clientId, stage, ownerId, page, count);
        return PageUtil.build(iPage);
    }

    /**
     * 推荐记录 CSV 导出（job_id 或 client_id 至少传一个）
     */
    @Logger(template = "导出推荐记录")
    @GetMapping("/export")
    @PermissionMeta(value = "查询推荐列表")
    public void export(
            @RequestParam(name = "job_id", required = false) Integer jobId,
            @RequestParam(name = "client_id", required = false) Integer clientId,
            HttpServletResponse response) throws IOException {
        if (jobId == null && clientId == null) {
            throw new com.bolehunter.core.exception.ParameterException(10030);
        }
        response.setContentType("text/csv;charset=UTF-8");
        String filename = jobId != null ? "recommendations_job_" + jobId : "recommendations_client_" + clientId;
        response.setHeader("Content-Disposition", "attachment; filename=" + filename + ".csv");
        response.getWriter().write(recommendationService.exportCsv(jobId, clientId));
    }

    /**
     * 看板数据（平铺列表，前端按阶段分组）
     */
    @Logger(template = "查询推荐看板")
    @GetMapping("/kanban")
    @PermissionMeta(value = "查询推荐列表")
    public List<RecommendationDO> kanban(
            @RequestParam(name = "job_id", required = false) Integer jobId,
            @RequestParam(name = "owner_id", required = false) Integer ownerId) {
        return recommendationService.getKanban(jobId, ownerId);
    }

    /**
     * 推荐阶段流转历史（时间正序）
     */
    @GetMapping("/{id}/history")
    @LoginRequired
    public List<com.bolehunter.model.RecommendationStageHistoryDO> history(
            @PathVariable @Positive(message = "{id.positive}") Integer id) {
        return recommendationService.getStageHistory(id);
    }

    /**
     * 可推荐职位下拉选项
     */
    @GetMapping("/job-options")
    @LoginRequired
    public List<Map<String, Object>> jobOptions() {
        return recommendationService.getJobOptions();
    }

    /**
     * 生成/刷新免登录确认链接 token（有效期 7 天）
     */
    @Logger(template = "生成客户确认链接")
    @PostMapping("/{id}/confirm-token")
    @LoginRequired
    public Map<String, Object> generateConfirmToken(
            @PathVariable @Positive(message = "{id.positive}") Integer id) {
        String token = recommendationService.generateConfirmToken(id);
        return Map.of("confirm_token", token,
                "url", "/confirm/" + token,
                "expire_days", 7);
    }
}
