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
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.service.AppSettingService;
import com.bolehunter.vo.UpdatedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 应用级配置管理（迭代19+：仅管理员）
 * <p>AI 服务配置的后台填写入口——非开发用户无需配置环境变量。
 * API Key 回显一律脱敏；保存时 Key 输入框留空表示保持不变。</p>
 */
@RestController
@RequestMapping("/recruit/admin/setting")
@PermissionModule(value = "系统配置")
@Validated
public class AdminSettingController {

    @Autowired
    private AppSettingService appSettingService;

    /**
     * 查看 AI 配置（API Key 脱敏回显）
     */
    @Logger(template = "查询AI服务配置")
    @GetMapping("/ai")
    @LoginRequired
    @AdminRequired
    @PermissionMeta(value = "查询AI服务配置")
    public Map<String, Object> getAiConfig() {
        return appSettingService.getAiConfigMasked();
    }

    /**
     * 保存 AI 配置（api_key 传空串=清除配置回落环境变量；传 null=保持不变）
     */
    @Logger(template = "保存 AI 服务配置")
    @PutMapping("/ai")
    @LoginRequired
    @AdminRequired
    public UpdatedVO saveAiConfig(@RequestBody Map<String, String> body) {
        if (body.containsKey("base_url")) {
            appSettingService.setValue(AppSettingService.KEY_AI_BASE_URL, body.get("base_url"));
        }
        if (body.containsKey("model")) {
            appSettingService.setValue(AppSettingService.KEY_AI_MODEL, body.get("model"));
        }
        // api_key 为 null（未提交该字段）= 保持不变，便于"只改模型不动 Key"
        String apiKey = body.get("api_key");
        if (apiKey != null) {
            appSettingService.setValue(AppSettingService.KEY_AI_API_KEY, apiKey);
        }
        return new UpdatedVO(8050);
    }
}
