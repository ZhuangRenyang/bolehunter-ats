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
package com.bolehunter.common.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 任务到期提醒调度器（独立守护线程，每 60 秒扫描一次）
 * <p>扫描规则：到期前 24 小时提醒一次、逾期后提醒一次（仅未完成任务）。
 * 幂等策略：以 content 中的 [任务#id][类型] 标记在 notify 表查重，避免重复通知。</p>
 */
@Component
public class TaskReminderScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskReminderScheduler.class);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JdbcTemplate jdbcTemplate;

    public TaskReminderScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void start() {
        log.info("TaskReminderScheduler 启动：独立线程每 60 秒扫描任务到期状态");
        Thread worker = new Thread(() -> {
            try {
                Thread.sleep(30_000);
            } catch (InterruptedException ignored) {
                return;
            }
            while (true) {
                try {
                    scanDueTasks();
                } catch (Exception e) {
                    log.warn("任务提醒扫描失败: {}", e.getMessage());
                }
                try {
                    Thread.sleep(60_000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }, "task-reminder");
        worker.setDaemon(true);
        worker.start();
    }

    /** 扫描：24 小时内到期提醒 + 逾期提醒 */
    void scanDueTasks() {
        // 时间窗口由 MySQL NOW() 计算，避免 JVM 与数据库时区不一致
        List<Map<String, Object>> dueSoon = jdbcTemplate.queryForList(
                "SELECT id, title, due_time, owner_id FROM task "
                        + "WHERE status = 0 AND delete_time IS NULL "
                        + "AND due_time > NOW(3) AND due_time <= DATE_ADD(NOW(3), INTERVAL 24 HOUR)");
        for (Map<String, Object> row : dueSoon) {
            sendNotice(row, "到期提醒");
        }

        List<Map<String, Object>> overdue = jdbcTemplate.queryForList(
                "SELECT id, title, due_time, owner_id FROM task "
                        + "WHERE status = 0 AND delete_time IS NULL AND due_time < NOW(3)");
        for (Map<String, Object> row : overdue) {
            sendNotice(row, "已逾期");
        }
        if (!dueSoon.isEmpty() || !overdue.isEmpty()) {
            log.info("任务提醒扫描完成: 24h内到期 {} 条, 已逾期 {} 条", dueSoon.size(), overdue.size());
        }
    }

    /** 幂等发送：同任务同类型只发一次 */
    private void sendNotice(Map<String, Object> row, String typeWord) {
        long id = ((Number) row.get("id")).longValue();
        String title = String.valueOf(row.get("title"));
        Object dueObj = row.get("due_time");
        LocalDateTime due = (dueObj instanceof Timestamp ts) ? ts.toLocalDateTime()
                : (LocalDateTime) dueObj;

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM notify WHERE delete_time IS NULL AND content LIKE ?",
                Integer.class, "%[任务#" + id + "%][" + typeWord + "]%");
        if (exists != null && exists > 0) {
            return;
        }
        Integer ownerId = (Integer) row.get("owner_id");
        if (ownerId == null) {
            return;
        }
        List<String> usernames = jdbcTemplate.queryForList(
                "SELECT username FROM user WHERE id = ? AND delete_time IS NULL", String.class, ownerId);
        if (usernames.isEmpty()) {
            return;
        }
        String content = "[任务#" + id + "][" + typeWord + "] 「" + title + "」"
                + due.format(FMT) + " " + (typeWord.equals("已逾期") ? "已逾期，请尽快处理" : "到期，请及时跟进");
        int affected = jdbcTemplate.update(
                "INSERT INTO notify (content, user_name, is_read, time, create_time, update_time) "
                        + "VALUES (?, ?, 'false', NOW(3), NOW(3), NOW(3))",
                content, usernames.get(0));
        log.info("已发送任务提醒: affected={} content={}", affected, content);
    }
}
