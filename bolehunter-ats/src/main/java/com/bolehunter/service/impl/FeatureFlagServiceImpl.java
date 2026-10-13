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
import com.bolehunter.dto.flag.CreateOrUpdateFeatureFlagDTO;
import com.bolehunter.mapper.FeatureFlagMapper;
import com.bolehunter.model.FeatureFlagDO;
import com.bolehunter.service.FeatureFlagService;
import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 功能开关服务实现
 */
@Service
public class FeatureFlagServiceImpl extends ServiceImpl<FeatureFlagMapper, FeatureFlagDO> implements FeatureFlagService {

    @Override
    public List<FeatureFlagDO> getAllFlags() {
        QueryWrapper<FeatureFlagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByAsc(FeatureFlagDO::getId);
        return this.list(wrapper);
    }

    @Override
    public boolean createFlag(CreateOrUpdateFeatureFlagDTO validator) {
        if (this.checkKeyExist(validator.getFlagKey())) {
            throw new ForbiddenException(80131);
        }
        FeatureFlagDO flag = FeatureFlagDO.builder()
                .flagKey(validator.getFlagKey())
                .flagName(validator.getFlagName())
                .description(validator.getDescription())
                .enabled(validator.getEnabled() == null ? 0 : validator.getEnabled())
                .build();
        return this.save(flag);
    }

    @Override
    public boolean updateFlag(FeatureFlagDO flag, CreateOrUpdateFeatureFlagDTO validator) {
        if (!flag.getFlagKey().equals(validator.getFlagKey()) && this.checkKeyExist(validator.getFlagKey())) {
            throw new ForbiddenException(80131);
        }
        flag.setFlagKey(validator.getFlagKey());
        flag.setFlagName(validator.getFlagName());
        flag.setDescription(validator.getDescription());
        if (validator.getEnabled() != null) {
            flag.setEnabled(validator.getEnabled());
        }
        return this.updateById(flag);
    }

    @Override
    public int toggleFlag(Integer id) {
        FeatureFlagDO flag = this.getById(id);
        if (flag == null) {
            throw new NotFoundException(80130);
        }
        int newEnabled = (flag.getEnabled() != null && flag.getEnabled() == 1) ? 0 : 1;
        flag.setEnabled(newEnabled);
        this.updateById(flag);
        return newEnabled;
    }

    @Override
    public boolean checkKeyExist(String flagKey) {
        QueryWrapper<FeatureFlagDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(FeatureFlagDO::getFlagKey, flagKey);
        return this.count(wrapper) > 0;
    }

    @Override
    public boolean isEnabled(String flagKey) {
        try {
            QueryWrapper<FeatureFlagDO> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(FeatureFlagDO::getFlagKey, flagKey).last("LIMIT 1");
            FeatureFlagDO flag = this.getOne(wrapper);
            return flag != null && Integer.valueOf(1).equals(flag.getEnabled());
        } catch (Exception e) {
            return false;
        }
    }
}
