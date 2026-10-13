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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 健康检查（迭代15 上线加固清单）
 * <p>匿名可访问（无认证注解即公开接口），供负载均衡/运维探活使用。
 * 返回服务存活 + 数据库连通性 + 进程运行时长，不暴露任何敏感信息。</p>
 */
@RestController
@RequestMapping("/recruit")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final long START_MILLIS = System.currentTimeMillis();

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");
        result.put("uptime_seconds", (System.currentTimeMillis() - START_MILLIS) / 1000);
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            result.put("database", "UP");
        } catch (Exception e) {
            result.put("status", "DEGRADED");
            result.put("database", "DOWN");
        }
        return result;
    }
}
