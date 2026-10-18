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

import com.bolehunter.common.LocalUser;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.model.UserDO;
import com.bolehunter.service.UserPreferenceService;
import com.bolehunter.vo.UpdatedVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户偏好（迭代 34）：表格自定义列等用户级配置的读写。
 * 偏好值以 JSON 字符串持久化，键由前端按「类型:页面」约定命名。
 */
@RestController
@RequestMapping("/recruit/user/pref")
public class UserPreferenceController {

    @Autowired
    private UserPreferenceService userPreferenceService;

    /**
     * 读取当前用户某偏好键的值
     */
    @GetMapping("/{key}")
    @LoginRequired
    public Map<String, Object> get(@PathVariable String key) {
        UserDO user = LocalUser.getLocalUser();
        String value = user == null ? null : userPreferenceService.getValue(user.getId(), key);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", value);
        return result;
    }

    /**
     * 写入当前用户某偏好键的值（存在则覆盖）
     */
    @PutMapping("/{key}")
    @LoginRequired
    public UpdatedVO put(@PathVariable String key, @RequestBody Map<String, String> body) {
        UserDO user = LocalUser.getLocalUser();
        if (user != null) {
            userPreferenceService.setValue(user.getId(), key, body.get("value"));
        }
        return new UpdatedVO();
    }
}
