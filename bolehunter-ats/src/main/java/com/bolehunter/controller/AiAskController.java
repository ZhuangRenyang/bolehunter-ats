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

import com.bolehunter.core.annotation.AdminRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.service.AiAskService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AI 找人问答（RAG，迭代20；受功能开关 ai_ask 控制）
 */
@RestController
@RequestMapping("/recruit/ai")
@PermissionModule(value = "AI 助手")
@Validated
public class AiAskController {

    @Autowired
    private AiAskService aiAskService;

    /**
     * 自然语言搜索候选人
     */
    @Logger(template = "AI 找人问答")
    @PostMapping("/ask")
    @LoginRequired
    public Map<String, Object> ask(@RequestBody Map<String, String> body) {
        return aiAskService.ask(body == null ? null : body.get("question"), null);
    }

    /**
     * 重建候选人向量索引（管理员；需已配置支持 embeddings 的供应商）
     */
    @Logger(template = "重建 AI 向量索引")
    @PostMapping("/rebuild-index")
    @AdminRequired
    public Map<String, Object> rebuildIndex() {
        return aiAskService.rebuildIndex();
    }
}
