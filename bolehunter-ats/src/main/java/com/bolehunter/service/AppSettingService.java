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

import java.util.Map;

/**
 * 应用级配置服务（键值对；管理员后台维护，敏感项回显一律脱敏）
 */
public interface AppSettingService {

    /** AI 配置键 */
    String KEY_AI_BASE_URL = "ai_base_url";
    String KEY_AI_MODEL = "ai_model";
    String KEY_AI_API_KEY = "ai_api_key";

    /**
     * 读取配置：数据库值优先，未配置时回落环境变量默认值
     */
    String getValue(String key, String envFallback);

    /**
     * 写入/更新配置（upsert），空串视为删除该配置（回落环境变量）
     */
    void setValue(String key, String value);

    /**
     * 脱敏：sk-abcd…wxyz，仅暴露前 4 后 4（长度不足 8 时全掩码）
     */
    String mask(String value);

    /**
     * 汇总 AI 配置（回显用，API Key 已脱敏）
     */
    Map<String, Object> getAiConfigMasked();
}
