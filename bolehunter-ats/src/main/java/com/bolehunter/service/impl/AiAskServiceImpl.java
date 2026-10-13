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
import com.bolehunter.extension.ai.AiClient;
import com.bolehunter.mapper.CandidateMapper;
import com.bolehunter.model.CandidateDO;
import com.bolehunter.service.AiAskService;
import com.bolehunter.service.FeatureFlagService;
import com.bolehunter.core.exception.ForbiddenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 找人问答实现（迭代20）
 * <p>流程：ai_ask 开关 → 检索 top5（向量/关键词）→ 命中为空直接返回；
 * LLM 可用则归纳回答（强制引用来源、不编造），不可用则仅返回命中列表（降级）。
 * 无 embedding 向量时（DeepSeek 无 embeddings）自动走关键词检索，功能依旧可用。</p>
 */
@Service
public class AiAskServiceImpl implements AiAskService {

    private static final Logger log = LoggerFactory.getLogger(AiAskServiceImpl.class);

    private static final int TOP_K = 5;

    private static final String ASK_SYSTEM_PROMPT = """
            你是猎头团队的候选人检索助手。仅基于提供的候选人资料回答用户问题：
            1. 用 2-5 句话归纳最匹配的人选及理由，明确引用候选人姓名；
            2. 资料不足以回答时明确说明"知识库中未找到相关候选人"，不得编造；
            3. 不要输出电话等联系方式之外的建议，不要输出 markdown 表格。
            """;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private CandidateAiSupport candidateAiSupport;

    @Autowired
    private FeatureFlagService featureFlagService;

    @Autowired
    private CandidateMapper candidateMapper;

    @Override
    public Map<String, Object> ask(String question, Integer currentUserId) {
        if (!featureFlagService.isEnabled("ai_ask")) {
            throw new ForbiddenException(8060);
        }
        if (question == null || question.isBlank()) {
            throw new ForbiddenException(8061);
        }
        List<CandidateDO> hits = candidateAiSupport.search(question.trim(), TOP_K, true);
        List<Map<String, Object>> candidateCards = new ArrayList<>();
        for (CandidateDO c : hits) {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", c.getId());
            card.put("name", c.getName());
            card.put("current_company", c.getCurrentCompany());
            card.put("current_position", c.getCurrentPosition());
            card.put("city", c.getCity());
            card.put("phone", c.getPhone());
            candidateCards.add(card);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("candidates", candidateCards);
        // LLM 归纳：不可用则 answer 为 null，前端仅展示命中列表（降级）
        if (!hits.isEmpty() && aiClient.isChatAvailable()) {
            try {
                StringBuilder context = new StringBuilder();
                for (CandidateDO c : hits) {
                    context.append(candidateAiSupport.buildDoc(c)).append("\n---\n");
                }
                String answer = aiClient.chat(ASK_SYSTEM_PROMPT,
                        "用户问题：" + question.trim() + "\n\n候选人资料：\n" + context, 400);
                result.put("answer", answer.trim());
                result.put("llm", true);
            } catch (Exception e) {
                log.warn("AI 问答归纳失败，仅返回检索结果: {}", e.getMessage());
                result.put("answer", null);
                result.put("llm", false);
            }
        } else {
            result.put("answer", null);
            result.put("llm", false);
        }
        return result;
    }

    @Override
    public Map<String, Object> rebuildIndex() {
        QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().last("LIMIT 2000");
        List<CandidateDO> all = candidateMapper.selectList(wrapper);
        int ok = 0, fail = 0;
        List<CandidateDO> pending = all.stream()
                .filter(c -> c.getAiEmbedding() == null || c.getAiEmbedding().isBlank())
                .toList();
        if (pending.isEmpty()) {
            return Map.of("processed", 0, "failed", 0, "message", "索引已全部就绪");
        }
        // 分批 embed（每批 10 条），单批失败不中断
        for (int i = 0; i < pending.size(); i += 10) {
            List<CandidateDO> batch = pending.subList(i, Math.min(i + 10, pending.size()));
            try {
                List<String> docs = batch.stream().map(candidateAiSupport::buildDoc).toList();
                List<double[]> vectors = aiClient.embed(docs);
                for (int j = 0; j < batch.size() && j < vectors.size(); j++) {
                    CandidateDO patch = new CandidateDO();
                    patch.setId(batch.get(j).getId());
                    patch.setAiEmbedding(toJson(vectors.get(j)));
                    candidateMapper.updateById(patch);
                    ok++;
                }
            } catch (Exception e) {
                log.warn("向量重建单批失败: {}", e.getMessage());
                fail += batch.size();
            }
        }
        return Map.of("processed", ok, "failed", fail);
    }

    private String toJson(double[] vec) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vec.length; i++) {
            sb.append(i == 0 ? "" : ",").append(vec[i]);
        }
        return sb.append(']').toString();
    }
}
