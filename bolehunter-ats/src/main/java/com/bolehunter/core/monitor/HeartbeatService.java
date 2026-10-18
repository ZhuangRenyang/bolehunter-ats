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
package com.bolehunter.core.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 服务器心跳（迭代 35）：每 30 秒采集系统 CPU / 系统内存 / JVM 堆使用率，
 * 内存环形缓冲保留最近 1 小时（120 点），供管理端曲线展示。
 * 指标仅驻留内存，重启清零，不做持久化。
 */
@Slf4j
@Service
public class HeartbeatService {

    private static final int MAX_POINTS = 120;
    private final ArrayDeque<Map<String, Object>> buffer = new ArrayDeque<>(MAX_POINTS);

    private final OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    /** 每 30 秒采集一次 */
    @Scheduled(fixedRate = 30_000, initialDelay = 10_000)
    public void collect() {
        try {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("time", LocalDateTime.now().toString().replace('T', ' ').substring(0, 19));
            double cpu = osBean.getCpuLoad();
            point.put("cpu", Math.round(Math.max(0, Math.min(1, cpu)) * 1000) / 10.0);
            long totalMem = osBean.getTotalMemorySize();
            long freeMem = osBean.getFreeMemorySize();
            point.put("sysMem", totalMem <= 0 ? 0
                : Math.round((1.0 - (double) freeMem / totalMem) * 1000) / 10.0);
            var memBean = ManagementFactory.getMemoryMXBean();
            long used = memBean.getHeapMemoryUsage().getUsed();
            long max = memBean.getHeapMemoryUsage().getMax();
            point.put("jvmHeap", max <= 0 ? 0 : Math.round((double) used / max * 1000) / 10.0);
            synchronized (buffer) {
                if (buffer.size() >= MAX_POINTS) {
                    buffer.pollFirst();
                }
                buffer.addLast(point);
            }
        } catch (Exception e) {
            log.warn("[heartbeat] 采集失败：{}", e.getMessage());
        }
    }

    /** 最近心跳快照（时间升序） */
    public List<Map<String, Object>> snapshot() {
        synchronized (buffer) {
            return new ArrayList<>(buffer);
        }
    }
}
