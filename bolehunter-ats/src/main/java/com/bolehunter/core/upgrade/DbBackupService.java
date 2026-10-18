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
package com.bolehunter.core.upgrade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 数据库备份与恢复（迭代 30，供管理员从其他系统/历史备份整库迁移）
 *
 * <p>实现方式：经 docker.sock 在 <b>mysql 容器内</b>执行 mysqldump / mysql 客户端，
 * backend 容器无需安装任何数据库客户端。整库 SQL 以流式传输，不占用 JVM 内存。
 *
 * <p><b>恢复是覆盖性操作</b>：恢复前自动将当前库完整备份到
 * {@code app.upgrade.backup-dir}（默认挂载卷内，宿主机可直接取回），失败可回滚。
 */
@Slf4j
@Service
public class DbBackupService {

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final String mysqlContainer;
    private final String dbName;
    private final Path backupDir;

    public DbBackupService(
            @Value("${app.upgrade.mysql-container:bolehunter-mysql}") String mysqlContainer,
            @Value("${app.upgrade.db-name:bolehunter}") String dbName,
            @Value("${app.upgrade.backup-dir:/app/deploy/backups}") String backupDir) {
        this.mysqlContainer = mysqlContainer;
        this.dbName = dbName;
        this.backupDir = Paths.get(backupDir);
    }

    /**
     * 导出整库 SQL 到输出流（mysqldump 单事务一致性快照，含存储过程与触发器）。
     * stderr 中的错误信息会拼进异常消息，便于前端提示。
     */
    public void exportTo(OutputStream out) throws IOException, InterruptedException {
        Process p = startDump(out);
        int code = p.waitFor();
        if (code != 0) {
            String err = drain();
            throw new IllegalStateException("mysqldump 退出码 " + code + (err.isBlank() ? "" : "：" + err));
        }
    }

    /** mysqldump 执行（stdout → 指定输出流；stderr 并行收集供报错） */
    private Process startDump(OutputStream out) throws IOException {
        Process p = new ProcessBuilder("docker", "exec", mysqlContainer, "sh", "-c",
            "mysqldump -uroot -p\"$MYSQL_ROOT_PASSWORD\" --single-transaction --routines --triggers --set-gtid-purged=OFF " + dbName)
            .start();
        // stderr 必须并行消费，否则可能阻塞子进程
        AtomicReference<String> errBox = new AtomicReference<>("");
        Thread errDrain = new Thread(() -> errBox.set(drainStream(p.getErrorStream())), "mysqldump-stderr");
        errDrain.setDaemon(true);
        errDrain.start();
        CURRENT_STDERR.set(errBox);
        try (InputStream in = p.getInputStream()) {
            in.transferTo(out);
        }
        return p;
    }

    /** 将整库导出到备份目录文件（恢复前自动调用） */
    private Path backupCurrent(String filename) throws IOException, InterruptedException {
        Files.createDirectories(backupDir);
        Path target = backupDir.resolve(filename);
        try (OutputStream out = Files.newOutputStream(target)) {
            startDump(out);
        }
        log.info("[db-backup] 恢复前自动备份当前库 → {}", target);
        return target;
    }

    /**
     * 从上传的 SQL 流恢复整库。
     * 流程：①当前库自动备份 → ②上传内容经 stdin 管道执行。
     *
     * @return { backupFile: 自动备份文件名, restored: true }
     */
    public Map<String, Object> importFrom(InputStream sqlStream) throws IOException, InterruptedException {
        String ts = TS.format(LocalDateTime.now());
        String backupName = "pre-restore-" + ts + ".sql";
        backupCurrent(backupName);

        Process p = new ProcessBuilder("docker", "exec", "-i", mysqlContainer, "sh", "-c",
            "mysql -uroot -p\"$MYSQL_ROOT_PASSWORD\" --default-character-set=utf8mb4 " + dbName)
            .start();
        AtomicReference<String> errBox = new AtomicReference<>("");
        Thread errDrain = new Thread(() -> errBox.set(drainStream(p.getErrorStream())), "mysql-restore-stderr");
        errDrain.setDaemon(true);
        errDrain.start();
        try (OutputStream stdin = p.getOutputStream()) {
            sqlStream.transferTo(stdin);
        }
        int code = p.waitFor();
        String err = drain();
        if (code != 0) {
            throw new IllegalStateException("mysql 恢复退出码 " + code + (err.isBlank() ? "" : "：" + err));
        }
        log.info("[db-backup] 整库恢复完成，恢复前备份：{}", backupName);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("restored", true);
        result.put("backupFile", backupName);
        return result;
    }

    /** 上传文件的基础校验：后缀与 SQL 特征，防误传 */
    public void validate(String filename, InputStream peek) throws IOException, IllegalStateException {
        if (filename == null || !filename.toLowerCase().endsWith(".sql")) {
            throw new IllegalStateException("仅支持 .sql 备份文件");
        }
        byte[] head = peek.readNBytes(2048);
        String headStr = new String(head, StandardCharsets.UTF_8);
        if (!(headStr.contains("--") || headStr.contains("SET") || headStr.contains("CREATE") || headStr.contains("/*"))) {
            throw new IllegalStateException("文件内容不是有效的 SQL 备份");
        }
    }

    private final ThreadLocal<AtomicReference<String>> CURRENT_STDERR = new ThreadLocal<>();

    /** 取当前线程最近一次子进程的 stderr 内容 */
    private String drain() {
        AtomicReference<String> box = CURRENT_STDERR.get();
        return box == null ? "" : box.get();
    }

    /** 读取并收集流内容（用于 stderr） */
    private String drainStream(InputStream in) {
        try {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }
}
