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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 找人（RAG 候选人问答，迭代20）：检索层
 * <p>策略：优先向量语义检索（候选人 ai_embedding 预计算），失败/缺失降级关键词 LIKE。
 * 语料 = 结构化字段 + 简历全文（截断 1200 字）。</p>
 */
@Service
public class CandidateAiSupport {

    private static final Logger log = LoggerFactory.getLogger(CandidateAiSupport.class);

    /** 检索扫描上限（全量语义检索在此范围内进行） */
    private static final int SCAN_LIMIT = 2000;

    /** 简历全文参与检索的截断长度 */
    private static final int RESUME_TEXT_LIMIT = 1200;

    @Autowired
    private AiClient aiClient;

    @Autowired
    private CandidateMapper candidateMapper;

    /**
     * 候选人 → 检索文档（结构化字段 + 简历全文截断）
     */
    public String buildDoc(CandidateDO c) {
        StringBuilder sb = new StringBuilder();
        sb.append("姓名:").append(nvl(c.getName()));
        sb.append("；电话:").append(nvl(c.getPhone()));
        sb.append("；邮箱:").append(nvl(c.getEmail()));
        sb.append("；当前公司:").append(nvl(c.getCurrentCompany()));
        sb.append("；当前职位:").append(nvl(c.getCurrentPosition()));
        sb.append("；学历:").append(nvl(c.getEducation()));
        sb.append("；城市:").append(nvl(c.getCity()));
        if (c.getResumeText() != null && !c.getResumeText().isBlank()) {
            String text = c.getResumeText();
            sb.append("；简历要点:").append(text, 0, Math.min(text.length(), RESUME_TEXT_LIMIT));
        }
        return sb.toString();
    }

    /** 简单余弦相似度 */
    public double cosine(double[] a, double[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0;
        }
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0) {
            return 0;
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    /**
     * 检索 topK 候选人：优先向量语义（候选人已有 ai_embedding），失败/缺失降级关键词 LIKE
     *
     * @param useEmbedding 是否尝试向量检索
     * @return 排序后的候选人列表，size ≤ topK
     */
    public List<CandidateDO> search(String question, int topK, boolean useEmbedding) {
        QueryWrapper<CandidateDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().last("LIMIT " + SCAN_LIMIT);
        List<CandidateDO> all = candidateMapper.selectList(wrapper);
        if (all.isEmpty()) {
            return all;
        }
        if (useEmbedding && aiClient.isChatAvailable()) {
            try {
                double[] queryVec = aiClient.embed(List.of(question)).get(0);
                record Scored(CandidateDO c, double score) {}
                List<Scored> scored = new ArrayList<>();
                for (CandidateDO c : all) {
                    double[] vec = parseVector(c.getAiEmbedding());
                    if (vec != null) {
                        scored.add(new Scored(c, cosine(queryVec, vec)));
                    }
                }
                if (!scored.isEmpty()) {
                    scored.sort((x, y) -> Double.compare(y.score(), x.score()));
                    return scored.stream().limit(topK).map(Scored::c).collect(java.util.stream.Collectors.toList());
                }
                // 无任何候选人已生成向量 → 落到关键词
            } catch (Exception e) {
                log.warn("向量检索失败，降级关键词检索: {}", e.getMessage());
            }
        }
        return keywordSearch(all, question, topK);
    }

    /** 关键词检索：按空白/标点拆词，命中（姓名/公司/职位/城市/全文）即入选，命中多者优先 */
    private List<CandidateDO> keywordSearch(List<CandidateDO> all, String question, int topK) {
        String[] tokens = question.split("[\\s，。？?！!、,\\.]+");
        record Scored2(CandidateDO c, int hits) {}
        List<Scored2> scored = new ArrayList<>();
        for (CandidateDO c : all) {
            String doc = buildDoc(c).toLowerCase();
            int hits = 0;
            for (String token : tokens) {
                if (token.length() >= 2 && doc.contains(token.toLowerCase())) {
                    hits++;
                }
            }
            if (hits > 0) {
                scored.add(new Scored2(c, hits));
            }
        }
        scored.sort((x, y) -> Integer.compare(y.hits(), x.hits()));
        return scored.stream().limit(topK).map(Scored2::c).collect(java.util.stream.Collectors.toList());
    }

    private double[] parseVector(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            String[] parts = json.replaceAll("[\\[\\]\\s]", "").split(",");
            double[] vec = new double[parts.length];
            for (int i = 0; i < parts.length; i++) {
                vec[i] = Double.parseDouble(parts[i]);
            }
            return vec;
        } catch (Exception e) {
            return null;
        }
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }
}
