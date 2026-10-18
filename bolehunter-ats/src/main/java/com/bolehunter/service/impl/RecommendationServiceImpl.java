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
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.dto.recommendation.CreateRecommendationDTO;
import com.bolehunter.dto.recommendation.UpdateStageDTO;
import com.bolehunter.mapper.CandidateMapper;
import com.bolehunter.mapper.CompanyMapper;
import com.bolehunter.mapper.PositionMapper;
import com.bolehunter.mapper.RecommendationMapper;
import com.bolehunter.mapper.RecommendationStageHistoryMapper;
import com.bolehunter.mapper.UserMapper;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.model.NotifyDO;
import com.bolehunter.model.PositionDO;
import com.bolehunter.model.RecommendationDO;
import com.bolehunter.model.RecommendationStageHistoryDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.NotifyService;
import com.bolehunter.service.RecommendationService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 推荐记录服务实现
 */
@Service
public class RecommendationServiceImpl extends ServiceImpl<RecommendationMapper, RecommendationDO>
        implements RecommendationService {

    @Autowired
    private RecommendationStageHistoryMapper stageHistoryMapper;

    @Autowired
    private CandidateMapper candidateMapper;

    @Autowired
    private PositionMapper positionMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean createRecommendation(CreateRecommendationDTO validator, Integer currentUserId) {
        PositionDO position = positionMapper.selectById(validator.getJobId());
        if (position == null) {
            throw new NotFoundException(41000);
        }
        CandidateDO candidate = candidateMapper.selectById(validator.getCandidateId());
        if (candidate == null) {
            throw new NotFoundException(80170);
        }
        // 同一职位同一候选人仅允许一条进行中的推荐
        QueryWrapper<RecommendationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RecommendationDO::getJobId, validator.getJobId())
                .eq(RecommendationDO::getCandidateId, validator.getCandidateId());
        if (this.count(wrapper) > 0) {
            throw new ForbiddenException(80240);
        }
        RecommendationDO recommendation = RecommendationDO.builder()
                .jobId(validator.getJobId())
                .candidateId(validator.getCandidateId())
                .clientId(position.getCompanyId())
                .ownerId(validator.getOwnerId() == null ? currentUserId : validator.getOwnerId())
                .currentStage("PENDING")
                .remark(validator.getRemark())
                .build();
        boolean saved = this.save(recommendation);
        if (saved) {
            // 迭代 33：推荐单号 REC-yyyyMMdd-id（id 全局唯一保证单号唯一，日期为创建标识）
            String orderNo = "REC-" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
                .format(java.time.LocalDate.now()) + "-" + String.format("%04d", recommendation.getId());
            recommendation.setOrderNo(orderNo);
            this.updateById(recommendation);
            recordHistory(recommendation.getId(), null, "PENDING", null, currentUserId);
        }
        return saved;
    }

    @Override
    public boolean updateStage(Integer id, UpdateStageDTO validator, Integer operatorId) {
        RecommendationDO recommendation = this.getById(id);
        if (recommendation == null) {
            throw new NotFoundException(80230);
        }
        String fromStage = recommendation.getCurrentStage();
        recommendation.setCurrentStage(validator.getStage());
        recommendation.setStageTime(new Date());
        String feedback = validator.getFeedback();
        if (feedback != null && !feedback.trim().isEmpty()) {
            recommendation.setFeedback(feedback.trim());
            recommendation.setFeedbackTime(new Date());
        }
        boolean ok = this.updateById(recommendation);
        if (ok) {
            recordHistory(id, fromStage, validator.getStage(), feedback, operatorId);
        }
        return ok;
    }

    @Override
    public boolean updateFeedback(Integer id, String feedback) {
        RecommendationDO recommendation = this.getById(id);
        if (recommendation == null) {
            throw new NotFoundException(80230);
        }
        recommendation.setFeedback(feedback == null ? "" : feedback.trim());
        recommendation.setFeedbackTime(new Date());
        return this.updateById(recommendation);
    }

    @Override
    public boolean deleteRecommendation(Integer id) {
        RecommendationDO recommendation = this.getById(id);
        if (recommendation == null) {
            throw new NotFoundException(80230);
        }
        return this.removeById(id);
    }

    @Override
    public IPage<RecommendationDO> getPage(Integer jobId, Integer candidateId, Integer clientId, String stage,
                                           Integer ownerId, Integer page, Integer count) {
        Page<RecommendationDO> pager = new Page<>(page, count);
        QueryWrapper<RecommendationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(jobId != null, RecommendationDO::getJobId, jobId)
                .eq(candidateId != null, RecommendationDO::getCandidateId, candidateId)
                .eq(clientId != null, RecommendationDO::getClientId, clientId)
                .eq(stage != null && !stage.isEmpty(), RecommendationDO::getCurrentStage, stage)
                .eq(ownerId != null, RecommendationDO::getOwnerId, ownerId)
                .orderByDesc(RecommendationDO::getStageTime);
        IPage<RecommendationDO> result = this.page(pager, wrapper);
        fillDisplayFields(result.getRecords());
        return result;
    }

    @Override
    public String exportCsv(Integer jobId, Integer clientId) {
        QueryWrapper<RecommendationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(jobId != null, RecommendationDO::getJobId, jobId)
                .eq(clientId != null, RecommendationDO::getClientId, clientId)
                .orderByDesc(RecommendationDO::getStageTime)
                .last("LIMIT 5000");
        List<RecommendationDO> list = this.list(wrapper);
        fillDisplayFields(list);
        StringBuilder csv = new StringBuilder();
        // UTF-8 BOM：让 Excel 正确识别编码
        csv.append('\ufeff');
        csv.append("候选人,电话,当前公司,当前职位,职位,客户,阶段,客户反馈,负责顾问,进入阶段时间\n");
        for (RecommendationDO item : list) {
            csv.append(csvCell(item.getCandidateName())).append(',')
                    .append(csvCell(item.getCandidateCompany())).append(',')
                    .append(csvCell(item.getCandidatePosition())).append(',')
                    .append(csvCell(item.getJobTitle())).append(',')
                    .append(csvCell(item.getClientName())).append(',')
                    .append(csvCell(item.getCurrentStage())).append(',')
                    .append(csvCell(item.getFeedback())).append(',')
                    .append(csvCell(item.getOwnerName())).append(',')
                    .append(csvCell(item.getStageTime() == null ? "" : new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.getStageTime())))
                    .append('\n');
        }
        return csv.toString();
    }

    /**
     * CSV 单元格转义：包含逗号/引号/换行时用引号包裹
     */
    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public List<RecommendationDO> getKanban(Integer jobId, Integer ownerId) {
        QueryWrapper<RecommendationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(jobId != null, RecommendationDO::getJobId, jobId)
                .eq(ownerId != null, RecommendationDO::getOwnerId, ownerId)
                .orderByAsc(RecommendationDO::getStageTime)
                .last("LIMIT 1000");
        List<RecommendationDO> list = this.list(wrapper);
        fillDisplayFields(list);
        return list;
    }

    @Override
    public List<Map<String, Object>> getJobOptions() {
        QueryWrapper<PositionDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().select(PositionDO::getId, PositionDO::getTitle, PositionDO::getCity,
                        PositionDO::getCompanyId, PositionDO::getState)
                .orderByDesc(PositionDO::getId)
                .last("LIMIT 500");
        List<PositionDO> positions = positionMapper.selectList(wrapper);
        if (positions.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> companyIds = positions.stream()
                .map(PositionDO::getCompanyId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Integer, String> companyNameMap = companyIds.isEmpty()
                ? new HashMap<>()
                : companyMapper.selectBatchIds(companyIds).stream()
                        .collect(Collectors.toMap(CompanyDO::getId, CompanyDO::getName));
        List<Map<String, Object>> options = new ArrayList<>();
        for (PositionDO position : positions) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", position.getId());
            item.put("title", position.getTitle());
            item.put("city", position.getCity());
            item.put("company_id", position.getCompanyId());
            item.put("company_name", companyNameMap.get(position.getCompanyId()));
            item.put("state", position.getState());
            options.add(item);
        }
        return options;
    }

    @Override
    public List<RecommendationStageHistoryDO> getStageHistory(Integer recommendationId) {
        QueryWrapper<RecommendationStageHistoryDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RecommendationStageHistoryDO::getRecommendationId, recommendationId)
                .orderByAsc(RecommendationStageHistoryDO::getId);
        List<RecommendationStageHistoryDO> histories = stageHistoryMapper.selectList(wrapper);
        if (!histories.isEmpty()) {
            List<Integer> userIds = histories.stream()
                    .map(RecommendationStageHistoryDO::getOperatorId)
                    .filter(id -> id != null)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Integer, String> nameMap = userIds.isEmpty()
                    ? new HashMap<>()
                    : userMapper.selectBatchIds(userIds).stream()
                            .collect(Collectors.toMap(UserDO::getId, UserDO::getNickname));
            histories.forEach(item -> item.setOperatorName(nameMap.get(item.getOperatorId())));
        }
        return histories;
    }

    /**
     * 写入阶段流转历史（append-only）
     */
    private void recordHistory(Integer recommendationId, String fromStage, String toStage,
                               String feedback, Integer operatorId) {
        RecommendationStageHistoryDO history = RecommendationStageHistoryDO.builder()
                .recommendationId(recommendationId)
                .fromStage(fromStage)
                .toStage(toStage)
                .feedback(feedback == null ? null : feedback.trim())
                .operatorId(operatorId)
                .build();
        stageHistoryMapper.insert(history);
    }

    /**
     * 批量填充展示字段（候选人/职位/客户/顾问），避免 N+1
     */
    private void fillDisplayFields(List<RecommendationDO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Integer> candidateIds = collectIds(list, RecommendationDO::getCandidateId);
        List<Integer> jobIds = collectIds(list, RecommendationDO::getJobId);
        List<Integer> clientIds = collectIds(list, RecommendationDO::getClientId);
        List<Integer> ownerIds = collectIds(list, RecommendationDO::getOwnerId);

        Map<Integer, CandidateDO> candidateMap = candidateIds.isEmpty()
                ? new HashMap<>()
                : candidateMapper.selectBatchIds(candidateIds).stream()
                        .collect(Collectors.toMap(CandidateDO::getId, c -> c));
        Map<Integer, PositionDO> positionMap = jobIds.isEmpty()
                ? new HashMap<>()
                : positionMapper.selectBatchIds(jobIds).stream()
                        .collect(Collectors.toMap(PositionDO::getId, p -> p));
        Map<Integer, CompanyDO> companyMap = clientIds.isEmpty()
                ? new HashMap<>()
                : companyMapper.selectBatchIds(clientIds).stream()
                        .collect(Collectors.toMap(CompanyDO::getId, c -> c));
        Map<Integer, UserDO> userMap = ownerIds.isEmpty()
                ? new HashMap<>()
                : userMapper.selectBatchIds(ownerIds).stream()
                        .collect(Collectors.toMap(UserDO::getId, u -> u));

        for (RecommendationDO item : list) {
            CandidateDO candidate = candidateMap.get(item.getCandidateId());
            if (candidate != null) {
                item.setCandidateName(candidate.getName());
                item.setCandidateCompany(candidate.getCurrentCompany());
                item.setCandidatePosition(candidate.getCurrentPosition());
            }
            PositionDO position = positionMap.get(item.getJobId());
            if (position != null) {
                item.setJobTitle(position.getTitle());
            }
            CompanyDO company = companyMap.get(item.getClientId());
            if (company != null) {
                item.setClientName(company.getName());
            }
            UserDO user = userMap.get(item.getOwnerId());
            if (user != null) {
                item.setOwnerName(user.getNickname());
            }
        }
    }

    private List<Integer> collectIds(List<RecommendationDO> list,
                                     java.util.function.Function<RecommendationDO, Integer> getter) {
        List<Integer> ids = list.stream()
                .map(getter)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        return ids.isEmpty() ? Collections.emptyList() : ids;
    }

    // ==================== 免登录确认链接（v1.1） ====================

    private static final long CONFIRM_EXPIRE_MILLIS = 7L * 24 * 3600 * 1000;

    @Autowired
    private NotifyService notifyService;

    @Override
    public String generateConfirmToken(Integer id) {
        RecommendationDO recommendation = this.getById(id);
        if (recommendation == null) {
            throw new NotFoundException(80230);
        }
        // SecureRandom 生成 32 位十六进制 token
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            token.append(Integer.toHexString(random.nextInt(16)));
        }
        RecommendationDO patch = new RecommendationDO();
        patch.setId(id);
        patch.setConfirmToken(token.toString());
        patch.setConfirmExpire(new Date(System.currentTimeMillis() + CONFIRM_EXPIRE_MILLIS));
        this.updateById(patch);
        return token.toString();
    }

    /**
     * 校验 token：存在对应推荐 + 未过期；返回推荐记录
     */
    private RecommendationDO requireValidByToken(String token) {
        if (token == null || token.length() != 32) {
            throw new NotFoundException(80230);
        }
        QueryWrapper<RecommendationDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RecommendationDO::getConfirmToken, token).last("LIMIT 1");
        RecommendationDO recommendation = this.getOne(wrapper);
        if (recommendation == null) {
            throw new NotFoundException(80230);
        }
        if (recommendation.getConfirmExpire() == null
                || recommendation.getConfirmExpire().before(new Date())) {
            throw new ForbiddenException(80250);
        }
        return recommendation;
    }

    @Override
    public Map<String, Object> getConfirmInfo(String token) {
        RecommendationDO recommendation = requireValidByToken(token);
        fillDisplayFields(List.of(recommendation));
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("candidate_name", recommendation.getCandidateName());
        info.put("candidate_position", recommendation.getCandidatePosition());
        info.put("job_title", recommendation.getJobTitle());
        info.put("client_name", recommendation.getClientName());
        info.put("current_stage", recommendation.getCurrentStage());
        info.put("stage_time", recommendation.getStageTime());
        info.put("feedback", recommendation.getFeedback());
        info.put("expire", recommendation.getConfirmExpire());
        return info;
    }

    @Override
    public Map<String, Object> handleConfirmAction(String token, String action, String feedback) {
        RecommendationDO recommendation = requireValidByToken(token);
        String fromStage = recommendation.getCurrentStage();
        // 动作 → 目标阶段（状态机前进，不回退）
        String toStage;
        String actionText;
        switch (action == null ? "" : action) {
            case "read" -> {
                toStage = "CLIENT_READ";
                actionText = "客户已读";
            }
            case "interview" -> {
                toStage = "INTERVIEW_PENDING";
                actionText = "客户约面";
            }
            case "reject" -> {
                toStage = "ARCHIVED";
                actionText = "客户婉拒";
            }
            default -> throw new ForbiddenException(80260);
        };
        // 客户反馈（可选）
        String fb = feedback == null ? null : feedback.trim();
        if (fb != null && !fb.isEmpty()) {
            RecommendationDO fbPatch = new RecommendationDO();
            fbPatch.setId(recommendation.getId());
            fbPatch.setFeedback(fb);
            fbPatch.setFeedbackTime(new Date());
            this.updateById(fbPatch);
        }
        RecommendationDO stagePatch = new RecommendationDO();
        stagePatch.setId(recommendation.getId());
        stagePatch.setCurrentStage(toStage);
        stagePatch.setStageTime(new Date());
        this.updateById(stagePatch);
        recordHistory(recommendation.getId(), fromStage, toStage, fb == null || fb.isEmpty() ? "[客户确认链接] " + actionText : fb, null);
        // 站内通知负责顾问（按用户名投递）
        if (recommendation.getOwnerId() != null) {
            UserDO owner = userMapper.selectById(recommendation.getOwnerId());
            if (owner != null) {
                NotifyDO notify = new NotifyDO();
                notify.setContent("[客户确认] 推荐记录#" + recommendation.getId() + " 客户执行了「" + actionText + "」"
                        + (fb == null || fb.isEmpty() ? "" : "，反馈：" + fb));
                notify.setUserName(owner.getUsername());
                notify.setIsRead("false");
                notify.setTime(new Date());
                // 走 create 而非 save：create 会在入库后经 WebSocket 实时推送给在线顾问
                notifyService.create(notify);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("stage", toStage);
        result.put("message", actionText + "成功");
        return result;
    }
}
