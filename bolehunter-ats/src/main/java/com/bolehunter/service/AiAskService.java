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

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.CandidateDO;

import java.util.List;
import java.util.Map;

/**
 * AI 找人问答（RAG，迭代20）
 */
public interface AiAskService {

    /**
     * 自然语言搜索候选人：检索知识库 → LLM 归纳回答
     *
     * @return {answer: 归纳回答或 null(LLM不可用), llm: 是否由大模型生成,
     *          candidates: 命中候选人列表（id/name/currentCompany/currentPosition/city/phone 摘要）}
     */
    Map<String, Object> ask(String question, Integer currentUserId);

    /**
     * 重建候选人向量索引（管理员）：为存在简历文本/字段的候选人批量生成 ai_embedding
     *
     * @return 处理条数与失败条数
     */
    Map<String, Object> rebuildIndex();
}
