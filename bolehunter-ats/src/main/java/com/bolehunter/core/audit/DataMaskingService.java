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
package com.bolehunter.core.audit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.model.FeatureFlagDO;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.FeatureFlagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据脱敏（迭代 33 数据安全与治理）
 *
 * <p>开关规则：feature_flag 的 data_masking 开启时，非 ADMIN 角色在
 * 列表展示场景看到脱敏后的手机号/邮箱；ADMIN 与开关关闭时返回全量。
 * 详情/编辑接口不脱敏（编辑需要原值，避免脱敏值回写覆盖）。
 */
@Service
public class DataMaskingService {

    @Autowired
    private FeatureFlagService featureFlagService;

    /** 当前操作人是否应看到脱敏数据：开关开启 且 操作人非 ADMIN */
    public boolean shouldMask(UserDO operator) {
        if (operator != null && Boolean.TRUE.equals(operator.getAdmin())) {
            return false;
        }
        return isFlagEnabled("data_masking");
    }

    /** 手机号脱敏：138****0000（不足位按可用长度处理） */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 邮箱脱敏：a***@domain.com */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int at = email.indexOf('@');
        String local = email.substring(0, at);
        String domain = email.substring(at);
        if (local.isEmpty()) {
            return email;
        }
        return local.charAt(0) + "***" + domain;
    }

    /** 读取功能开关状态（未配置视为关闭） */
    private boolean isFlagEnabled(String flagKey) {
        try {
            FeatureFlagDO flag = featureFlagService.getOne(
                new QueryWrapper<FeatureFlagDO>().eq("flag_key", flagKey), false);
            return flag != null && flag.getEnabled() != null && flag.getEnabled() == 1;
        } catch (Exception e) {
            return false;
        }
    }
}
