/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.flag.CreateOrUpdateFeatureFlagDTO;
import com.bolehunter.model.FeatureFlagDO;

import java.util.List;

/**
 * 功能开关服务
 */
public interface FeatureFlagService extends IService<FeatureFlagDO> {

    List<FeatureFlagDO> getAllFlags();

    boolean createFlag(CreateOrUpdateFeatureFlagDTO validator);

    boolean updateFlag(FeatureFlagDO flag, CreateOrUpdateFeatureFlagDTO validator);

    /**
     * 切换开关状态，返回切换后的状态（1 开 / 0 关）
     */
    int toggleFlag(Integer id);

    boolean checkKeyExist(String flagKey);
}
