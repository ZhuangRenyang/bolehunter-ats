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
import com.bolehunter.model.UserDO;
import com.bolehunter.service.WorkbenchService;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 工作台聚合
 */
@RestController
@RequestMapping("/recruit/workbench")
@PermissionModule(value = "工作台")
public class WorkbenchController {

    @Autowired
    private WorkbenchService workbenchService;

    /**
     * 工作台汇总：统计 + 阶段分布 + 今日/逾期任务
     */
    @GetMapping("/summary")
    @LoginRequired
    public Map<String, Object> summary() {
        UserDO user = LocalUser.getLocalUser();
        return workbenchService.summary(user == null ? null : user.getId());
    }
}
