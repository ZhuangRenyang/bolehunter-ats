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
package com.bolehunter.core.migration;

import com.bolehunter.core.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 数据库增量迁移执行器（迭代26，Flyway 思路简化版）
 *
 * <p>记账表 {@code schema_history} 记录每个迁移脚本的执行结果；
 * 待执行 = 目录中存在而 history 里没有（或上次失败）的 {@code migration_iter*.sql}，按文件名排序执行。
 *
 * <p><b>基线对齐</b>：老库首次接入（history 为空但业务表已存在）时，把现存脚本全部标记为
 * baseline 记入 history 而不执行——否则 compose 首次部署（initdb 已跑完全部脚本）后
 * 会在后台把它们再次执行一遍并撞上"表已存在"。
 *
 * <p><b>启动自动执行</b>：应用就绪后自动跑一遍 pending（可用 {@code app.migration.auto-run=false} 关闭），
 * 这是"后台一键升级"闭环的关键一环——新镜像的 backend 起来时自行补齐新表/新字段，
 * 老容器无需知道新迁移长什么样。执行失败则快速失败终止启动（容器重启重试，
 * 已成功的脚本因有记账不会重复执行）。
 *
 * <p>单机部署假设（见路线图部署承诺），不处理多实例并发迁移。
 */
@Slf4j
@Service
public class DbMigrationService {

    private static final String HISTORY_TABLE = "schema_history";
    /** 只认这个前缀，防止误执行目录里的其它 SQL（如基线 bolehunter.sql——那是 initdb 的职责） */
    private static final String SCRIPT_PREFIX = "migration_iter";

    private final JdbcTemplate jdbcTemplate;

    /** 迁移脚本目录：本地默认工作目录下的 sql；容器经 volume 挂载并由 MIGRATION_DIR 指向 */
    private final String migrationDir;

    /**
     * 镜像内置迁移目录（迭代 38 修复）：宿主机挂载目录可能与镜像版本脱节
     * （发版只 pull 镜像、没同步源码时，新迁移文件永远到不了服务器，接口静默 500）。
     * 这里与挂载目录合并扫描，文件名重复时以挂载目录为准（允许运维热修单脚本）。
     */
    private final String bundledMigrationDir;

    private final boolean autoRun;

    public DbMigrationService(JdbcTemplate jdbcTemplate,
                              @Value("${app.migration.dir:sql}") String migrationDir,
                              @Value("${app.migration.bundled-dir:/app/sql-bundled}") String bundledMigrationDir,
                              @Value("${app.migration.auto-run:true}") boolean autoRun) {
        this.jdbcTemplate = jdbcTemplate;
        this.migrationDir = migrationDir;
        this.bundledMigrationDir = bundledMigrationDir;
        this.autoRun = autoRun;
    }

    /** 迁移记录 */
    public record MigrationRecord(String filename, boolean success, boolean baseline,
                                  Timestamp executedAt, Long elapsedMs, String errorMsg) {
    }

    /** 升级执行报告 */
    public record ExecResult(String filename, boolean success, long elapsedMs, String errorMsg) {
    }

    /** 状态视图：待执行 + 已执行 */
    public Map<String, Object> status() {
        ensureHistoryTable();
        List<Path> scripts = scanScripts();
        Map<String, MigrationRecord> history = loadHistory();

        List<Map<String, Object>> pending = new ArrayList<>();
        for (Path p : scripts) {
            MigrationRecord rec = history.get(p.getFileName().toString());
            if (rec == null || !rec.success()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("filename", p.getFileName().toString());
                item.put("title", extractTitle(p));
                item.put("previouslyFailed", rec != null);
                pending.add(item);
            }
        }

        List<Map<String, Object>> executed = new ArrayList<>();
        Comparator<MigrationRecord> byTimeDesc = Comparator.comparing(
            MigrationRecord::executedAt,
            Comparator.<Timestamp>nullsLast(Comparator.<Timestamp>naturalOrder())).reversed();
        history.values().stream()
            .sorted(byTimeDesc)
            .forEach(r -> {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("filename", r.filename());
                item.put("success", r.success());
                item.put("baseline", r.baseline());
                item.put("executedAt", r.executedAt());
                item.put("elapsedMs", r.elapsedMs());
                item.put("errorMsg", r.errorMsg());
                executed.add(item);
            });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("migrationDir", migrationDir);
        result.put("bundledMigrationDir", bundledMigrationDir);
        result.put("pendingCount", pending.size());
        result.put("pending", pending);
        result.put("executed", executed);
        return result;
    }

    /**
     * 执行全部待迁移脚本（按文件名顺序），全部成功返回报告；任一失败立即停止并抛出。
     */
    public List<ExecResult> runPending() {
        ensureHistoryTable();
        alignBaselineIfNeeded();
        Map<String, MigrationRecord> history = loadHistory();
        List<ExecResult> results = new ArrayList<>();
        for (Path script : scanScripts()) {
            String filename = script.getFileName().toString();
            MigrationRecord rec = history.get(filename);
            if (rec != null && rec.success()) {
                continue;
            }
            ExecResult result = executeScript(script);
            results.add(result);
            if (!result.success()) {
                throw new IllegalStateException(
                    "迁移脚本执行失败：" + filename + " —— " + result.errorMsg());
            }
        }
        return results;
    }

    /**
     * 应用就绪后自动补齐增量（可配置关闭）。
     * Order(LOWEST) 保证排在其它 ready 监听之后，尽量不拖慢别的初始化。
     */
    @Order(Integer.MAX_VALUE)
    @EventListener(ApplicationReadyEvent.class)
    public void autoRunOnStartup() {
        if (!autoRun) {
            log.info("[migration] 自动执行已关闭（app.migration.auto-run=false）");
            return;
        }
        try {
            List<ExecResult> results = runPending();
            if (results.isEmpty()) {
                log.info("[migration] 数据库结构已是最新（无待执行迁移）");
            } else {
                results.forEach(r -> log.info("[migration] 已执行 {} ({} ms)", r.filename(), r.elapsedMs()));
            }
        } catch (Exception e) {
            // 快速失败：结构落后于代码时继续运行往往更糟；容器会重启重试（已成功脚本有记账不会重跑）
            throw new IllegalStateException("启动迁移失败：" + e.getMessage(), e);
        }
    }

    // ---------- 内部实现 ----------

    private void ensureHistoryTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS " + HISTORY_TABLE + " ("
            + "filename varchar(128) NOT NULL,"
            + "success tinyint NOT NULL DEFAULT 1,"
            + "baseline tinyint NOT NULL DEFAULT 0,"
            + "executed_at datetime(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),"
            + "elapsed_ms int NULL,"
            + "error_msg varchar(500) NULL,"
            + "PRIMARY KEY (filename)"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据库迁移记账'");
    }

    /**
     * 老库首次接入：history 为空但 user 表已存在 → 现存脚本全部记为 baseline，不执行。
     */
    private void alignBaselineIfNeeded() {
        Map<String, MigrationRecord> history = loadHistory();
        if (!history.isEmpty()) {
            return;
        }
        Integer userTableCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM information_schema.TABLES "
                + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user'", Integer.class);
        if (userTableCount == null || userTableCount == 0) {
            return; // 全新库：initdb 还没跑（理论上不会发生在 ready 之后），保持空账
        }
        List<Path> scripts = scanScripts();
        if (scripts.isEmpty()) {
            return;
        }
        for (Path script : scripts) {
            jdbcTemplate.update("INSERT INTO " + HISTORY_TABLE
                    + " (filename, success, baseline, executed_at, error_msg) VALUES (?, 1, 1, CURRENT_TIMESTAMP(3), NULL)",
                script.getFileName().toString());
        }
        log.info("[migration] 首次接入对齐基线：{} 个现存脚本记入 schema_history（不执行）", scripts.size());
    }

    private Map<String, MigrationRecord> loadHistory() {
        Map<String, MigrationRecord> map = new HashMap<>();
        jdbcTemplate.query(
            "SELECT filename, success, baseline, executed_at, elapsed_ms, error_msg FROM " + HISTORY_TABLE,
            rs -> {
                map.put(rs.getString("filename"), new MigrationRecord(
                    rs.getString("filename"),
                    rs.getInt("success") == 1,
                    rs.getInt("baseline") == 1,
                    rs.getTimestamp("executed_at"),
                    rs.getObject("elapsed_ms") == null ? null : rs.getLong("elapsed_ms"),
                    rs.getString("error_msg")));
            });
        return map;
    }

    private List<Path> scanScripts() {
        // 先收镜像内置目录，再用挂载目录覆盖同名项（挂载目录优先），按文件名排序执行
        Map<String, Path> byName = new LinkedHashMap<>();
        collectScripts(bundledMigrationDir, byName);
        collectScripts(migrationDir, byName);
        return byName.values().stream()
            .sorted(Comparator.comparing(p -> p.getFileName().toString()))
            .toList();
    }

    /** 把目录下符合前缀/后缀的脚本收进 map；目录不存在仅告警（本地无内置目录属正常） */
    private void collectScripts(String dirPath, Map<String, Path> byName) {
        if (dirPath == null || dirPath.isBlank()) {
            return;
        }
        Path dir = Paths.get(dirPath);
        if (!Files.isDirectory(dir)) {
            log.debug("[migration] 迁移目录不存在（跳过）：{}", dir.toAbsolutePath());
            return;
        }
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().startsWith(SCRIPT_PREFIX)
                    && p.getFileName().toString().endsWith(".sql"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .forEach(p -> byName.put(p.getFileName().toString(), p));
        } catch (IOException e) {
            throw new IllegalStateException("扫描迁移目录失败：" + e.getMessage(), e);
        }
    }

    private ExecResult executeScript(Path script) {
        String filename = script.getFileName().toString();
        long start = System.currentTimeMillis();
        try {
            String content = Files.readString(script, StandardCharsets.UTF_8);
            int count = 0;
            for (String statement : splitStatements(content)) {
                jdbcTemplate.execute(statement);
                count++;
            }
            long elapsed = System.currentTimeMillis() - start;
            jdbcTemplate.update("INSERT INTO " + HISTORY_TABLE
                    + " (filename, success, baseline, executed_at, elapsed_ms, error_msg) "
                    + "VALUES (?, 1, 0, CURRENT_TIMESTAMP(3), ?, NULL) "
                    + "ON DUPLICATE KEY UPDATE success = 1, executed_at = CURRENT_TIMESTAMP(3), "
                    + "elapsed_ms = VALUES(elapsed_ms), error_msg = NULL",
                filename, elapsed);
            log.info("[migration] 执行 {} 完成：{} 条语句，{} ms", filename, count, elapsed);
            return new ExecResult(filename, true, elapsed, null);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            String msg = StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getSimpleName();
            if (msg.length() > 480) {
                msg = msg.substring(0, 480);
            }
            String finalMsg = msg;
            jdbcTemplate.update("INSERT INTO " + HISTORY_TABLE
                    + " (filename, success, baseline, executed_at, elapsed_ms, error_msg) "
                    + "VALUES (?, 0, 0, CURRENT_TIMESTAMP(3), ?, ?) "
                    + "ON DUPLICATE KEY UPDATE success = 0, executed_at = CURRENT_TIMESTAMP(3), "
                    + "elapsed_ms = VALUES(elapsed_ms), error_msg = VALUES(error_msg)",
                filename, elapsed, finalMsg);
            log.error("[migration] 执行 {} 失败：{}", filename, msg);
            return new ExecResult(filename, false, elapsed, msg);
        }
    }

    /**
     * 极简 SQL 拆分：逐行拼接，{@code --} 注释行与空行跳过，遇分号成句。
     * 本项目迁移脚本不含存储过程/DELIMITER，够用；将来引入复杂对象再升级解析器。
     */
    static List<String> splitStatements(String content) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String rawLine : content.split("\n", -1)) {
            String line = rawLine.trim();
            if (line.isEmpty() || line.startsWith("--") || line.startsWith("#")) {
                continue;
            }
            current.append(line).append('\n');
            if (line.endsWith(";")) {
                String stmt = current.toString().trim();
                stmt = stmt.substring(0, stmt.length() - 1).trim();
                if (!stmt.isEmpty()) {
                    statements.add(stmt);
                }
                current.setLength(0);
            }
        }
        String rest = current.toString().trim();
        if (!rest.isEmpty()) {
            statements.add(rest); // 末尾无分号也能执行
        }
        return statements;
    }

    /** 从脚本头部的第一行注释提取标题（"-- 迭代23：xxx" → "迭代23：xxx"） */
    private static String extractTitle(Path script) {
        try {
            for (String line : Files.readAllLines(script, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.startsWith("--")) {
                    String title = t.substring(2).trim();
                    if (title.startsWith("迭代") || title.length() > 4) {
                        return title;
                    }
                }
            }
        } catch (IOException ignored) {
            // 标题拿不到不影响功能
        }
        return script.getFileName().toString();
    }

    /** 管理员手动触发的入口校验（防止在配置错误的目录上盲跑） */
    void assertDirConfigured() {
        if (!Files.isDirectory(Paths.get(migrationDir))) {
            throw new ForbiddenException(80301);
        }
    }
}
