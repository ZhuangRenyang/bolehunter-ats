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
import com.bolehunter.mapper.UserPreferenceMapper;
import com.bolehunter.model.UserPreferenceDO;
import com.bolehunter.service.UserPreferenceService;
import org.springframework.stereotype.Service;

/**
 * 用户偏好（迭代 34）
 */
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreferenceDO> implements UserPreferenceService {

    @Override
    public String getValue(Integer userId, String prefKey) {
        UserPreferenceDO pref = getOne(new QueryWrapper<UserPreferenceDO>()
            .eq("user_id", userId).eq("pref_key", prefKey), false);
        return pref == null ? null : pref.getPrefValue();
    }

    @Override
    public void setValue(Integer userId, String prefKey, String value) {
        UserPreferenceDO pref = getOne(new QueryWrapper<UserPreferenceDO>()
            .eq("user_id", userId).eq("pref_key", prefKey), false);
        if (pref == null) {
            pref = new UserPreferenceDO();
            pref.setUserId(userId);
            pref.setPrefKey(prefKey);
            pref.setPrefValue(value);
            save(pref);
        } else {
            pref.setPrefValue(value);
            updateById(pref);
        }
    }
}
