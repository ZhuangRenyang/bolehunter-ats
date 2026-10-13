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
package com.bolehunter.module.im;

import com.bolehunter.core.websocket.AuthHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

/**
 * WebSocket 端点注册（迭代23）
 *
 * <p>端点路径刻意沿用 HTTP 接口的 {@code /recruit} 前缀：
 * Docker Compose 部署时前端 nginx 已把 {@code /recruit} 反代到后端，
 * WebSocket 请求随之自动走同一条链路，无需额外暴露端口（仅需 nginx 转发 Upgrade 头）。
 */
@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    public static final String ENDPOINT = "/recruit/ws";

    private final ImWebSocketHandler imWebSocketHandler;

    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    /** 允许握手的来源，逗号分隔，默认放行全部（开发便利；生产建议在 env 中显式指定） */
    @Value("${app.cors.allowed-origin-patterns:*}")
    private String allowedOriginPatterns;

    public WebSocketConfiguration(ImWebSocketHandler imWebSocketHandler,
                                  AuthHandshakeInterceptor authHandshakeInterceptor) {
        this.imWebSocketHandler = imWebSocketHandler;
        this.authHandshakeInterceptor = authHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] patterns = Arrays.stream(allowedOriginPatterns.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .toArray(String[]::new);
        registry.addHandler(imWebSocketHandler, ENDPOINT)
            .addInterceptors(authHandshakeInterceptor)
            .setAllowedOriginPatterns(patterns.length == 0 ? new String[]{"*"} : patterns);
    }
}
