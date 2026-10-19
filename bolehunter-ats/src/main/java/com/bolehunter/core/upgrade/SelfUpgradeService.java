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

    /** 老机制的标记文件名（upgrader 容器消费），仅作兜底保留 */
    static final String MARKER_FILE = ".upgrade-requested";

    /** 容器内 compose 编排目录（与 compose 里的 UPGRADE_COMPOSE_DIR 对应） */
    private static final String CONTAINER_COMPOSE_DIR = "/app/deploy";

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
                // 第一步：拉取最新镜像（不触碰运行中的容器，进程安全）
                runComposeCommand("docker compose pull");

                // 第二步：容器重建。
                // 迭代39 修复：原实现把 .upgrade-requested 写进 composeDir（容器内
                // /app/deploy），但 compose 里该目录只是三个 :ro 的**单文件挂载**，
                // 目录本身属于容器可写层——宿主机看不到，upgrader 永远等不到标记，
                // 于是"升级提示成功、容器从不重建"。现改为：用自身镜像起一个一次性
                // 容器（挂 docker.sock + 宿主机 compose 目录）后台执行 compose up -d，
                // 与 backend 容器生命周期解耦，也不依赖宿主机 compose 是否含 upgrader。
                String hostDir = resolveHostComposeDir();
                String selfImage = resolveSelfImage();
                if (hostDir != null && selfImage != null) {
                    String helper = runComposeUpInHelper(hostDir, selfImage);
                    appendLog("\n[upgrade] 最新镜像已拉取，容器重建已交由独立容器 " + helper + " 执行（" +
                        hostDir + "）。backend/frontend 将在数秒内重建，服务短暂中断后自动恢复。\n");
                } else {
                    // 兜底：老机制——写标记文件，由宿主机上运行的 upgrader 容器消费
                    Files.writeString(Paths.get(composeDir, MARKER_FILE),
                        String.valueOf(System.currentTimeMillis()),
                        java.nio.file.StandardOpenOption.CREATE,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                        java.nio.file.StandardOpenOption.WRITE);
                    appendLog("\n[upgrade] 最新镜像已拉取，但无法定位宿主机 compose 目录，" +
                        "已回退为标记文件模式（需宿主机上存在并运行 upgrader 容器）。\n");
                }
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
     * 从自身容器的挂载表反推「宿主机上的 compose 目录」。
     *
     * <p>compose 把 {@code /app/deploy} 下的编排文件以**单文件**方式挂进来，
     * 所以容器的 {@code .Mounts} 里能看到每个文件的宿主机 Source，取父目录即得部署目录。
     * 若将来改成整目录挂载（Destination 正好是 /app/deploy），直接取 Source。
     */
    private String resolveHostComposeDir() {
        String selfId = resolveSelfContainerId();
        if (selfId == null) {
            return null;
        }
        try {
            // {{println}} 让 destination / source 各占一行，避免 shell 引号转义问题
            List<String> lines = execCaptureLines("docker", "inspect", selfId, "--format",
                "{{range .Mounts}}{{println .Destination}}{{println .Source}}{{end}}");
            String fileDir = null;
            for (int i = 0; i + 1 < lines.size(); i += 2) {
                String dest = lines.get(i).trim();
                String src = lines.get(i + 1).trim();
                if (dest.equals(CONTAINER_COMPOSE_DIR) && !src.isEmpty()) {
                    return src;
                }
                if (fileDir == null && dest.startsWith(CONTAINER_COMPOSE_DIR + "/") && !src.isEmpty()) {
                    Path parent = Paths.get(src).getParent();
                    if (parent != null) {
                        fileDir = parent.toString();
                    }
                }
            }
            return fileDir;
        } catch (Exception e) {
            log.warn("[upgrade] 解析宿主机 compose 目录失败：{}", e.getMessage());
            return null;
        }
    }

    /** 自身容器使用的镜像名（用于启动一次性升级容器——本地必然已存在该镜像） */
    private String resolveSelfImage() {
        String selfId = resolveSelfContainerId();
        if (selfId == null) {
            return null;
        }
        try {
            String image = execCapture("docker", "inspect", selfId, "--format", "{{.Config.Image}}").trim();
            return image.isEmpty() ? null : image;
        } catch (Exception e) {
            log.warn("[upgrade] 获取自身镜像名失败：{}", e.getMessage());
            return null;
        }
    }

    /** 自身容器 ID：优先 hostname（docker 默认把它设为容器 ID），失败再按容器名兜底 */
    private String resolveSelfContainerId() {
        try {
            String host = execCapture("hostname").trim();
            if (!host.isEmpty()) {
                String id = execCapture("docker", "inspect", "--format", "{{.Id}}", host).trim();
                // 只有真的解析出 64 位容器 ID 才认，避免把 docker 的错误信息当容器名
                if (id.matches("[0-9a-f]{64}")) {
                    return host;
                }
            }
        } catch (Exception ignored) {
            // 落到下面的名字兜底
        }
        try {
            List<String> ids = execCaptureLines("docker", "ps", "--filter", "name=bolehunter-backend",
                "--format", "{{.ID}}");
            for (String id : ids) {
                if (!id.isBlank()) {
                    return id.trim();
                }
            }
        } catch (Exception e) {
            log.warn("[upgrade] 定位自身容器失败：{}", e.getMessage());
        }
        return null;
    }

    /**
     * 用自身镜像启动一次性容器执行 {@code docker compose up -d}。
     *
     * <p>为什么必须换个容器执行：backend 自己就在被重建之列，进程内执行会在
     * "重建 backend"那一步随容器一起被杀，导致 frontend 永远停在旧镜像。
     * 独立容器带 {@code --rm} + 与 compose 项目无关（无 compose 标签），
     * 不会被 {@code --remove-orphans} 清理，做完即退出。
     */
    private String runComposeUpInHelper(String hostDir, String image) throws IOException, InterruptedException {
        String name = "bolehunter-upgrade-" + System.currentTimeMillis();
        appendLog("\n$ docker run --rm --name " + name + " ... docker compose up -d（宿主目录 " + hostDir + "）\n");
        ProcessBuilder pb = new ProcessBuilder(
            "docker", "run", "-d", "--rm",
            "--name", name,
            "-v", "/var/run/docker.sock:/var/run/docker.sock",
            "-v", hostDir + ":" + hostDir,
            "-w", hostDir,
            "--entrypoint", "sh",
            image,
            "-c", "sleep 3; docker compose up -d --remove-orphans")
            .redirectErrorStream(true);
        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        int code = p.waitFor();
        if (code != 0) {
            throw new IllegalStateException("启动升级容器失败（退出码 " + code + "）：" + output);
        }
        log.info("[upgrade] 升级容器已启动：{}", output.isEmpty() ? name : output);
        return name;
    }

    /** 执行命令并返回全部输出行（stderr 合并，15 秒超时保护） */
    private List<String> execCaptureLines(String... command) throws IOException, InterruptedException {
        Process p = new ProcessBuilder(command)
            .directory(Paths.get(composeDir).toFile())
            .redirectErrorStream(true)
            .start();
        List<String> lines = new java.io.BufferedReader(
            new java.io.InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8)).lines().toList();
        p.waitFor(15, TimeUnit.SECONDS);
        return lines;
    }

    private String execCapture(String... command) throws IOException, InterruptedException {
        return String.join("\n", execCaptureLines(command));
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
