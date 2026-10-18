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
import com.bolehunter.mapper.LoginLogMapper;
import com.bolehunter.model.LoginLogDO;
import com.bolehunter.service.LoginLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志（迭代 32）：登录成功/失败全量落库，失败原因明确到环节。
 * 写入失败不影响登录主流程（catch 兜底）。
 */
@Slf4j
@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLogDO> implements LoginLogService {

    @Override
    public void record(String username, Integer userId, String ip, String browser, String os,
                       boolean success, String failReason, long startMs) {
        try {
            LoginLogDO logDO = new LoginLogDO();
            logDO.setUsername(username);
            logDO.setUserId(userId);
            logDO.setIp(ip);
            logDO.setBrowser(browser);
            logDO.setOs(os);
            logDO.setResult(success);
            logDO.setFailReason(failReason);
            logDO.setElapsedMs((int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startMs));
            logDO.setCreateTime(LocalDateTime.now());
            save(logDO);
        } catch (Exception e) {
            log.warn("[login-log] 登录日志写入失败（不影响登录流程）：{}", e.getMessage());
        }
    }
}
