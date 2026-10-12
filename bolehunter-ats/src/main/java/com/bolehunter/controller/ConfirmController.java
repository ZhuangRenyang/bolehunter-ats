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

import com.bolehunter.service.RecommendationService;
import com.bolehunter.core.annotation.Logger;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 免登录推荐确认（客户 HR 专用）
 * <p>顾问生成确认链接 → HR 免登录查看推荐摘要 → 确认"已读/约面/婉拒" → 写回流转历史并通知顾问。
 * 全部接口不加认证注解 = 匿名可访问，安全性由 32 位随机 token + 7 天有效期保障。</p>
 */
@RestController
@RequestMapping("/recruit/open/confirm")
@Validated
public class ConfirmController {

    @Autowired
    private RecommendationService recommendationService;

    /**
     * 按 token 查看推荐摘要（脱敏，仅展示客户关心的字段）
     */
    @GetMapping("/{token}")
    public Map<String, Object> info(@PathVariable("token") @NotBlank @Size(min = 32, max = 32) String token) {
        return recommendationService.getConfirmInfo(token);
    }

    /**
     * 客户确认动作：read=已读 / interview=约面 / reject=婉拒
     */
    @Logger(template = "客户确认推荐（免登录）")
    @PostMapping("/{token}/action")
    public Map<String, Object> action(@PathVariable("token") @NotBlank @Size(min = 32, max = 32) String token,
                                      @RequestBody Map<String, String> body) {
        return recommendationService.handleConfirmAction(token,
                body.get("action"), body.get("feedback"));
    }
}
