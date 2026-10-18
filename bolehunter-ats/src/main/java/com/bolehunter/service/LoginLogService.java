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
package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.model.LoginLogDO;

/**
 * 登录日志（迭代 32）
 */
public interface LoginLogService extends IService<LoginLogDO> {

    /**
     * 记录一次登录（成功或失败）
     *
     * @param username   用户名
     * @param userId     用户id（用户不存在传 null）
     * @param ip         登录 IP
     * @param browser    浏览器
     * @param os         操作系统
     * @param success    是否成功
     * @param failReason 失败原因（成功传 null）
     * @param startMs    请求开始时间戳（计算耗时）
     */
    void record(String username, Integer userId, String ip, String browser, String os,
                boolean success, String failReason, long startMs);

    /**
     * 解析浏览器（轻量 UA 匹配，不引入第三方依赖）
     */
    static String parseBrowser(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        if (userAgent.contains("MicroMessenger")) return "WeChat";
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("OPR/") || userAgent.contains("Opera")) return "Opera";
        if (userAgent.contains("Firefox/")) return "Firefox";
        if (userAgent.contains("Chrome/") && !userAgent.contains("Edg/")) return "Chrome";
        if (userAgent.contains("Safari/")) return "Safari";
        return "Other";
    }

    /**
     * 解析操作系统
     */
    static String parseOs(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Unknown";
        }
        if (userAgent.contains("Windows")) return "Windows";
        if (userAgent.contains("Android")) return "Android";
        if (userAgent.contains("iPhone") || userAgent.contains("iPad")) return "iOS";
        if (userAgent.contains("Mac OS")) return "macOS";
        if (userAgent.contains("Linux")) return "Linux";
        return "Other";
    }
}
