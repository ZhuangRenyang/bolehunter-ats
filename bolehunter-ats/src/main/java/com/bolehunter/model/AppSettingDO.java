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
package com.bolehunter.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 应用级配置（键值对，管理员后台维护；AI_API_KEY 等敏感项接口只回显掩码）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("app_setting")
@EqualsAndHashCode(callSuper = true)
public class AppSettingDO extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置键（唯一）
     */
    private String settingKey;

    /**
     * 配置值
     */
    private String settingValue;
}
