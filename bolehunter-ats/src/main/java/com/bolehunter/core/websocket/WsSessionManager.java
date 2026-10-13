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
package com.bolehunter.core.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket 在线会话注册表。
 *
 * <p>按用户维度维护会话，一个用户可在多个浏览器标签/设备同时在线（多端），
 * 因此值是会话集合而非单个会话。会话容器随连接建立/关闭自动增删，
 * 并在发送失败（连接已断但尚未触发关闭回调）时即时剔除，避免泄漏。
 *
 * <p><b>单机内存实现的边界</b>：本项目定位单机 Docker Compose 部署（见路线图"部署承诺"），
 * 因此不上分布式消息中间件；若将来多实例横向扩展，需把推送改为 Redis Pub/Sub 或多播——
 * 届时只需替换本类的 {@link #push(Integer, Object)} 实现，上层调用方无需改动。
 */
@Slf4j
@Component
public class WsSessionManager {

    private final Map<Integer, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    public WsSessionManager(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 登记会话
     */
    public void add(Integer userId, WebSocketSession session) {
        sessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
    }

    /**
     * 移除会话（用户已无在线端时连带清理 key）
     */
    public void remove(Integer userId, WebSocketSession session) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null) {
            return;
        }
        set.remove(session);
        if (set.isEmpty()) {
            sessions.remove(userId, set);
        }
    }

    /**
     * 用户是否在线（至少一个端）
     */
    public boolean isOnline(Integer userId) {
        Set<WebSocketSession> set = sessions.get(userId);
        return set != null && !set.isEmpty();
    }

    /**
     * 在线用户数（去重）
     */
    public int onlineUserCount() {
        return sessions.size();
    }

    /**
     * 在线会话总数
     */
    public int onlineSessionCount() {
        return sessions.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * 在线用户 id 快照
     */
    public Set<Integer> onlineUserIds() {
        return Collections.unmodifiableSet(new HashSet<>(sessions.keySet()));
    }

    /**
     * 向指定用户的所有在线端推送消息对象（JSON 序列化）。
     *
     * @return 实际送达的端数（0 表示离线或全部发送失败）
     */
    public int push(Integer userId, Object payload) {
        Set<WebSocketSession> set = sessions.get(userId);
        if (set == null || set.isEmpty()) {
            return 0;
        }
        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (IOException e) {
            log.error("[ws] 消息序列化失败: {}", e.getMessage());
            return 0;
        }
        TextMessage message = new TextMessage(json);
        int delivered = 0;
        // WebSocketSession 非线程安全，单会话上的并发 send 需串行化
        for (WebSocketSession session : new HashSet<>(set)) {
            synchronized (session) {
                if (!session.isOpen()) {
                    sessions.getOrDefault(userId, Collections.emptySet()).remove(session);
                    continue;
                }
                try {
                    session.sendMessage(message);
                    delivered++;
                } catch (IOException e) {
                    log.warn("[ws] 推送失败并剔除失效会话，userId={}: {}", userId, e.getMessage());
                    remove(userId, session);
                }
            }
        }
        return delivered;
    }
}
