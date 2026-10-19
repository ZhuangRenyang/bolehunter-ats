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

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.mapper.CollectLinkMapper;
import com.bolehunter.model.CollectLinkDO;
import com.bolehunter.service.CollectLinkService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 候选人信息收集链接（迭代 36）
 */
@Service
public class CollectLinkServiceImpl extends ServiceImpl<CollectLinkMapper, CollectLinkDO> implements CollectLinkService {

    @Override
    public boolean isValid(String token) {
        CollectLinkDO link = getOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<CollectLinkDO>()
            .eq("token", token), false);
        if (link == null || link.getStatus() == null || link.getStatus() != 1) {
            return false;
        }
        return link.getExpiresAt() == null || link.getExpiresAt().isAfter(LocalDateTime.now());
    }
}
