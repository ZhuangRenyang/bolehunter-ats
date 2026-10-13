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

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.bolehunter.core.token.DoubleJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

/**
 * WebSocket 握手鉴权。
 *
 * <p>浏览器原生 WebSocket 无法自定义请求头，因此约定令牌通过查询参数 {@code token} 传递：
 * {@code ws://host/recruit/ws?token=<access_token>}。服务端用与 HTTP 接口完全相同的
 * {@link DoubleJWT} 验签，从中取出 {@code identity}（用户 id）写入会话属性。
 *
 * <p><b>安全要点</b>：用户身份<b>绝不</b>由请求方自己声称（不能放在路径参数里），
 * 必须是令牌验签后的结果——这正是被替换掉的旧 {@code /ws/message/{uID}} 端点的致命缺陷。
 * 验签失败一律返回 401 并拒绝握手，不建立任何会话。
 */
@Slf4j
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "wsUserId";

    private final DoubleJWT doubleJWT;

    public AuthHandshakeInterceptor(DoubleJWT doubleJWT) {
        this.doubleJWT = doubleJWT;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            log.warn("[ws] 握手被拒：缺少 token（remote={}）", request.getRemoteAddress());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        try {
            Map<String, Claim> claims = doubleJWT.decodeAccessToken(token);
            Claim identity = claims.get("identity");
            if (identity == null || identity.isNull()) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
            attributes.put(ATTR_USER_ID, identity.asInt());
            return true;
        } catch (JWTVerificationException e) {
            log.warn("[ws] 握手被拒：token 无效或已过期（remote={}）", request.getRemoteAddress());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 无需处理：成功与否已在 beforeHandshake 决定
    }

    /**
     * 从握手请求的 {@code token} 查询参数中取令牌。
     * 降级兼容 {@code Authorization: Bearer xxx}（部分非浏览器客户端支持自定义头）。
     */
    private String resolveToken(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            return auth.substring(7);
        }
        URI uri = request.getURI();
        String query = uri == null ? null : uri.getQuery();
        if (!StringUtils.hasText(query)) {
            return null;
        }
        for (String pair : query.split("&")) {
            int eq = pair.indexOf('=');
            if (eq > 0 && "token".equals(pair.substring(0, eq))) {
                return pair.substring(eq + 1);
            }
        }
        return null;
    }
}
