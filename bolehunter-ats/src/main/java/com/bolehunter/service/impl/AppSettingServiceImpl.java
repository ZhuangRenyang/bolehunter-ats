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
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.mapper.AppSettingMapper;
import com.bolehunter.model.AppSettingDO;
import com.bolehunter.service.AppSettingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 应用级配置服务实现
 * <p>优先级：数据库（后台填写）> 环境变量；保存空串=清除配置（回落环境变量）。
 * API Key 等敏感项在任何读取出口都只能以掩码形态离开服务层。</p>
 */
@Service
public class AppSettingServiceImpl extends ServiceImpl<AppSettingMapper, AppSettingDO> implements AppSettingService {

    @Value("${ai.base-url:https://api.deepseek.com}")
    private String envBaseUrl;

    @Value("${ai.model:deepseek-chat}")
    private String envModel;

    @Value("${ai.api-key:}")
    private String envApiKey;

    @Override
    public String getValue(String key, String envFallback) {
        QueryWrapper<AppSettingDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AppSettingDO::getSettingKey, key).last("LIMIT 1");
        AppSettingDO setting = this.getOne(wrapper);
        if (setting != null && setting.getSettingValue() != null && !setting.getSettingValue().isBlank()) {
            return setting.getSettingValue().trim();
        }
        return envFallback;
    }

    @Override
    public void setValue(String key, String value) {
        String trimmed = value == null ? "" : value.trim();
        QueryWrapper<AppSettingDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(AppSettingDO::getSettingKey, key).last("LIMIT 1");
        AppSettingDO setting = this.getOne(wrapper);
        if (trimmed.isEmpty()) {
            // 空值 = 清除，回落环境变量
            if (setting != null) {
                this.removeById(setting.getId());
            }
            return;
        }
        if (setting == null) {
            setting = AppSettingDO.builder().settingKey(key).settingValue(trimmed).build();
            this.save(setting);
        } else {
            setting.setSettingValue(trimmed);
            this.updateById(setting);
        }
    }

    @Override
    public String mask(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String v = value.trim();
        if (v.length() <= 8) {
            return "****";
        }
        return v.substring(0, 4) + "****" + v.substring(v.length() - 4);
    }

    @Override
    public Map<String, Object> getAiConfigMasked() {
        String apiKey = getValue(KEY_AI_API_KEY, envApiKey);
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("base_url", getValue(KEY_AI_BASE_URL, envBaseUrl));
        config.put("model", getValue(KEY_AI_MODEL, envModel));
        config.put("api_key_masked", mask(apiKey));
        config.put("ready", !apiKey.isBlank());
        return config;
    }
}
