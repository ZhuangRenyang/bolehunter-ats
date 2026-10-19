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

import com.bolehunter.core.annotation.AdminRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.migration.DbMigrationService;
import com.bolehunter.core.upgrade.DbBackupService;
import com.bolehunter.core.upgrade.SelfUpgradeService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 系统升级（迭代26，仅管理员）
 *
 * <p>两条链路：数据库增量（{@link DbMigrationService}，新版本 backend 启动时也会自动跑）
 * 与镜像自升级（{@link SelfUpgradeService}，触发宿主机 compose pull/up）。
 * 触发自升级会重建 backend 容器自身——接口只负责点火，前端轮询健康接口等服务恢复。
 */
@RestController
@RequestMapping("/recruit/admin/upgrade")
public class AdminUpgradeController {

    @Autowired
    private DbMigrationService dbMigrationService;

    @Autowired
    private SelfUpgradeService selfUpgradeService;

    @Autowired
    private DbBackupService dbBackupService;

    /**
     * 迁移状态：待执行脚本列表 + 执行历史
     */
    @GetMapping("/migration/status")
    @AdminRequired
    @PermissionMeta(value = "查看系统升级状态")
    public Map<String, Object> migrationStatus() {
        return dbMigrationService.status();
    }

    /**
     * 手动执行待迁移（正常情况下新版本 backend 启动时会自动跑，这里作为兜底）
     */
    @Logger(template = "手动执行数据库迁移")
    @PostMapping("/migration/run")
    @AdminRequired
    @PermissionMeta(value = "执行数据库迁移")
    public Map<String, Object> runMigration() {
        List<DbMigrationService.ExecResult> results = dbMigrationService.runPending();
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("executed", results.size());
        result.put("results", results);
        return result;
    }

    /**
     * 本地构建版本（迭代 37）：CI 构建时注入的 git commit sha，
     * 前端与 GitHub 主分支最新 commit 比对，展示"已是最新/有新版本"状态。
     */
    @org.springframework.beans.factory.annotation.Value("${GIT_SHA:}")
    private String localGitSha;

    @GetMapping("/version")
    @AdminRequired
    @PermissionMeta(value = "查看系统升级状态")
    public Map<String, Object> version() {
        return Map.of("localSha", localGitSha == null ? "" : localGitSha);
    }

    /**
     * 自升级总状态（含最近一次升级日志尾部）
     */
    @GetMapping("/status")
    @AdminRequired
    @PermissionMeta(value = "查看系统升级状态")
    public Map<String, Object> status() {
        return selfUpgradeService.status();
    }

    /**
     * 触发自升级：拉取最新镜像并重建有变化的容器（异步，立即返回）
     */
    @Logger(template = "触发系统升级")
    @PostMapping("/run")
    @AdminRequired
    @PermissionMeta(value = "触发系统升级")
    public Map<String, Object> run() {
        return selfUpgradeService.trigger();
    }

    /**
     * 数据库备份（迭代 30）：整库 mysqldump 流式下载。
     * 供管理员从旧系统/历史备份整库迁移，或升级前手动留存快照。
     */
    @Logger(template = "导出数据库备份")
    @GetMapping("/db/export")
    @AdminRequired
    @PermissionMeta(value = "导出数据库备份")
    public void exportDb(HttpServletResponse response) throws IOException, InterruptedException {
        String filename = "bolehunter-db-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now()) + ".sql";
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);
        dbBackupService.exportTo(response.getOutputStream());
    }

    /**
     * 数据库恢复（迭代 30，危险操作）：上传 .sql 整库覆盖。
     * 采用 raw 流式上传（application/octet-stream）而非 multipart——不受
     * multipart 体积上限约束，大体积备份亦可恢复。
     * 恢复前自动将当前库完整备份（可回滚）；仅 .sql 文件且内容校验通过。
     */
    @Logger(template = "恢复数据库")
    @PostMapping(value = "/db/import", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @AdminRequired
    @PermissionMeta(value = "恢复数据库")
    public Map<String, Object> importDb(jakarta.servlet.http.HttpServletRequest request) throws IOException, InterruptedException {
        String filename = request.getHeader("X-Filename");
        try (InputStream in = request.getInputStream()) {
            dbBackupService.validate(filename, in);
        }
        try (InputStream in = request.getInputStream()) {
            return dbBackupService.importFrom(in);
        }
    }
}
