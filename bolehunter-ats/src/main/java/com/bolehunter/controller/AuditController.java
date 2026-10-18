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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.core.annotation.AdminRequired;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.model.DataChangeLogDO;
import com.bolehunter.model.LoginLogDO;
import com.bolehunter.service.DataChangeLogService;
import com.bolehunter.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 安全审计查询（迭代 32，仅管理员）：登录日志 + 数据变更记录
 */
@RestController
@RequestMapping("/recruit/admin/audit")
public class AuditController {

    @Autowired
    private LoginLogService loginLogService;

    @Autowired
    private DataChangeLogService dataChangeLogService;

    /**
     * 登录日志分页（用户名模糊筛选 + 结果筛选）
     */
    @GetMapping("/login-log/page")
    @AdminRequired
    @PermissionMeta(value = "查看登录日志")
    public IPage<LoginLogDO> loginLogPage(
            @RequestParam(name = "page", required = false, defaultValue = "1")
            Integer page,
            @RequestParam(name = "count", required = false, defaultValue = "10")
            Integer count,
            @RequestParam(name = "username", required = false)
            String username,
            @RequestParam(name = "result", required = false)
            Boolean result) {
        QueryWrapper<LoginLogDO> wrapper = new QueryWrapper<>();
        if (username != null && !username.isBlank()) {
            wrapper.like("username", username.trim());
        }
        if (result != null) {
            wrapper.eq("result", result);
        }
        wrapper.orderByDesc("create_time");
        IPage<LoginLogDO> iPage = loginLogService.page(new Page<>(page, count), wrapper);
        return iPage;
    }

    /**
     * 数据变更历史：按数据表与记录主键查询（时间倒序）
     */
    @GetMapping("/change-log/list")
    @AdminRequired
    @PermissionMeta(value = "查看数据变更记录")
    public List<DataChangeLogDO> changeLog(
            @RequestParam String tableName,
            @RequestParam Integer recordId) {
        return dataChangeLogService.listByRecord(tableName, recordId);
    }
}
