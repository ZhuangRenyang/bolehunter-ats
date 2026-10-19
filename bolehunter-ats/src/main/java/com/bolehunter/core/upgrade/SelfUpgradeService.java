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

import com.bolehunter.core.exception.ForbiddenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 系统自升级执行器（迭代26）
 *
 * <p>管理员点按钮后，经挂载的 docker.sock 在宿主机上执行
 * {@code docker compose pull && docker compose up -d}：
 * <ul>
 *   <li>compose 对无变化的容器幂等——没有新版本时什么都不会重建，秒级返回；</li>
 *   <li>有新版本时 backend/frontend 容器被重建，<b>包括发出本次请求的 backend 自己</b>；
 *       因此触发接口立刻返回，前端转去轮询健康接口等服务恢复（期间 502 属预期）；</li>
 *   <li>数据库增量由新版本 backend 启动时的 {@code DbMigrationService} 自动补齐，本服务不碰 SQL。</li>
 * </ul>
 *
 * <p><b>启用条件（缺一不可，缺省关闭）</b>：{@code app.upgrade.enabled=true} +
 * 容器内可用 docker CLI + compose 文件目录已挂载（{@code app.upgrade.compose-dir}）。
 * 安全边界：挂载 docker.sock 等价于授予宿主机 root 级能力，入口必须管理员专属并全程审计。
 */
@Slf4j
@Service
public class SelfUpgradeService {

    /** 每次升级的输出写到这里（覆盖式），供前端轮询展示进度 */
    static final String LOG_FILENAME = "upgrade.log";

    private final boolean enabled;
    private final String composeDir;
    private final Path logFile;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "self-upgrade");
        t.setDaemon(true);
        return t;
    });

    public SelfUpgradeService(
            @Value("${app.upgrade.enabled:false}") boolean enabled,
            @Value("${app.upgrade.compose-dir:.}") String composeDir,
            @Value("${app.upgrade.log-dir:logs}") String logDir) {
        this.enabled = enabled;
        this.composeDir = composeDir;
        this.logFile = Paths.get(logDir, LOG_FILENAME);
    }

    /** 状态视图（前端轮询用） */
    public Map<String, Object> status() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled", enabled);
        result.put("running", running.get());
        result.put("logTail", logTail(60));
        result.put("composeDir", composeDir);
        return result;
    }

    /**
     * 触发升级（异步、立即返回）。已在进行中时拒绝，避免并发 pull/up。
     */
    public Map<String, Object> trigger() {
        if (!enabled) {
            throw new ForbiddenException(80302);
        }
        if (running.get()) {
            throw new ForbiddenException(80303);
        }
        Path composeFile = Paths.get(composeDir, "docker-compose.yml");
        if (!Files.isRegularFile(composeFile)) {
            throw new ForbiddenException(80304);
        }
        ensureDockerAvailable();

        running.set(true);
        worker.submit(() -> {
            try {
                // 迭代 29：两段式升级——①本服务只做 compose pull + 写标记文件
                // （拉镜像不触碰运行中的容器，进程安全）；②容器滚动重建由独立的
                // upgrader 容器（docker:cli）检测标记后执行，彻底规避
                // "backend 升级自己时升级进程随容器被杀"的架构缺陷
                runComposeCommand("docker compose pull");
                Files.writeString(Paths.get(composeDir, ".upgrade-requested"),
                    String.valueOf(System.currentTimeMillis()),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                    java.nio.file.StandardOpenOption.WRITE);
                appendLog("\n[upgrade] 最新镜像已拉取，滚动更新请求已发出。" +
                    "upgrader 将在 30 秒内执行容器滚动重启（期间服务短暂中断，页面将自动恢复）。\n");
            } catch (Exception e) {
                appendLog("\n[upgrade] 升级失败：" + e.getMessage() + "\n");
                log.error("[upgrade] 自升级失败", e);
            } finally {
                running.set(false);
            }
        });
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("triggered", true);
        result.put("hint", "升级流程已启动；若包含后端更新，服务会短暂重启，页面稍后自动恢复");
        return result;
    }

    /**
     * 检查更新（迭代 37）：记录当前 latest 镜像 ID → compose pull（只拉不启）→ 对比镜像 ID。
     * 拉取不会重启容器（旧容器继续用旧镜像运行），确认有更新后再点「检查并升级」。
     * 完全基于 docker 原生能力，不依赖任何构建时版本注入。
     */
    public Map<String, Object> checkForUpdates() {
        ensureDockerAvailable();
        appendLog("\n===== 检查更新 =====\n");
        Map<String, String> before = latestImageIds();
        try {
            runComposeCommand("docker compose pull");
        } catch (Exception e) {
            throw new IllegalStateException("拉取最新镜像失败：" + e.getMessage());
        }
        Map<String, String> after = latestImageIds();
        boolean hasUpdate = !before.equals(after);
        appendLog("检查结果：" + (hasUpdate ? "有新版本（镜像 ID 已变化）" : "已是最新版") + "\n");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("hasUpdate", hasUpdate);
        result.put("checkedAt", LocalDateTime.now().toString().replace('T', ' ').substring(0, 19));
        return result;
    }

    /**
     * 当前本地 backend/frontend 的 latest 镜像 ID 表
     */
    private Map<String, String> latestImageIds() {
        Map<String, String> ids = new LinkedHashMap<>();
        try {
            Process p = new ProcessBuilder("sh", "-c",
                "docker images --format '{{.Repository}}:{{.Tag}} {{.ID}}'")
                .directory(Paths.get(composeDir).toFile())
                .start();
            List<String> lines = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))
                .lines().toList();
            p.waitFor();
            for (String line : lines) {
                if (line.contains("bolehunter") && (line.contains("backend:latest") || line.contains("frontend:latest"))) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 2) {
                        ids.put(parts[0], parts[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[upgrade] 查询本地镜像 ID 失败：{}", e.getMessage());
        }
        return ids;
    }

    /** docker CLI 可用性预检（给出可操作的报错，而不是让 compose 半路失败） */
    private void ensureDockerAvailable() {
        try {
            Process p = new ProcessBuilder("docker", "version", "--format", "{{.Server.Version}}")
                .directory(Paths.get(composeDir).toFile())
                .start();
            boolean finished = p.waitFor(10, TimeUnit.SECONDS);
            if (!finished || p.exitValue() != 0) {
                throw new ForbiddenException(80305);
            }
        } catch (ForbiddenException e) {
            throw e;
        } catch (Exception e) {
            log.warn("[upgrade] docker CLI 预检失败：{}", e.getMessage());
            throw new ForbiddenException(80305);
        }
    }

    private void runComposeCommand(String command) throws IOException, InterruptedException {
        appendLog("\n$ " + command + "\n");
        log.info("[upgrade] $ {}", command);
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", command)
            .directory(Paths.get(composeDir).toFile())
            .redirectErrorStream(true)
            .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
        Process p = pb.start();
        int code = p.waitFor();
        if (code != 0) {
            throw new IllegalStateException("命令退出码 " + code + "：" + command);
        }
        // 容器重建后 .env 可能被用户改过端口等，无需额外处理：up -d 幂等
    }

    private void appendLog(String text) {
        try {
            Files.createDirectories(logFile.getParent());
            Files.writeString(logFile, text, StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.warn("[upgrade] 写升级日志失败：{}", e.getMessage());
        }
    }

    private List<String> logTail(int maxLines) {
        if (!Files.isRegularFile(logFile)) {
            return List.of("(尚无升级记录)");
        }
        try {
            List<String> all = Files.readAllLines(logFile, StandardCharsets.UTF_8);
            int from = Math.max(0, all.size() - maxLines);
            return new ArrayList<>(all.subList(from, all.size()));
        } catch (IOException e) {
            return List.of("(日志读取失败)");
        }
    }

    @PreDestroy
    void shutdown() {
        worker.shutdownNow();
    }
}
