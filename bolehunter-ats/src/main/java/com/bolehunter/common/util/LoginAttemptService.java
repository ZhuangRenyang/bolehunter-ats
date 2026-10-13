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
package com.bolehunter.common.util;

import com.bolehunter.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录失败限流（迭代15 安全加固）
 * <p>规则：同一「用户名+IP」10 分钟内连续失败 5 次 → 锁定 10 分钟；登录成功清零。
 * 计数优先存 Redis（多实例共享）；Redis 不可用时自动降级为进程内 Map（单机兜底）。</p>
 */
@Component
public class LoginAttemptService {

    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);

    /** 窗口内允许的最大失败次数 */
    private static final int MAX_ATTEMPTS = 5;
    /** 计数窗口与锁定时长（秒） */
    private static final long WINDOW_SECONDS = 600;

    private static final String KEY_PREFIX = "login:fail:";

    @Autowired
    private RedisService redisService;

    /** Redis 不可用时的进程内降级存储：key -> [失败计数, 窗口起始毫秒] */
    private final Map<String, long[]> fallbackStore = new ConcurrentHashMap<>();

    /** 是否已被锁定（锁定中直接拒绝登录尝试，防爆破） */
    public boolean isBlocked(String username, String ip) {
        String key = key(username, ip);
        try {
            return redisService.counter(key) >= MAX_ATTEMPTS;
        } catch (Exception e) {
            return fallbackCount(key) >= MAX_ATTEMPTS;
        }
    }

    /** 记录一次失败；返回当前累计失败次数 */
    public int recordFailure(String username, String ip) {
        String key = key(username, ip);
        try {
            return (int) Math.min(redisService.increment(key, WINDOW_SECONDS), Integer.MAX_VALUE);
        } catch (Exception e) {
            log.warn("Redis 不可用，登录限流降级为进程内计数: {}", e.getMessage());
            return (int) fallbackIncrement(key);
        }
    }

    /** 登录成功后清零 */
    public void clear(String username, String ip) {
        String key = key(username, ip);
        try {
            redisService.delete(key);
        } catch (Exception ignored) {
            // Redis 异常时清降级存储即可
        }
        fallbackStore.remove(key);
    }

    private long fallbackIncrement(String key) {
        long now = System.currentTimeMillis();
        long[] slot = fallbackStore.compute(key, (k, old) -> {
            if (old == null || now - old[1] > WINDOW_SECONDS * 1000) {
                return new long[]{1L, now};
            }
            return new long[]{old[0] + 1, old[1]};
        });
        return slot[0];
    }

    private long fallbackCount(String key) {
        long[] slot = fallbackStore.get(key);
        if (slot == null) {
            return 0L;
        }
        if (System.currentTimeMillis() - slot[1] > WINDOW_SECONDS * 1000) {
            fallbackStore.remove(key);
            return 0L;
        }
        return slot[0];
    }

    private String key(String username, String ip) {
        return KEY_PREFIX + (username == null ? "-" : username.toLowerCase()) + ":" + (ip == null ? "-" : ip);
    }
}
