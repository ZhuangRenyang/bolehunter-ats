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

import com.bolehunter.core.exception.ForbiddenException;
import com.bolehunter.core.exception.HttpException;
import com.bolehunter.core.websocket.AuthHandshakeInterceptor;
import com.bolehunter.core.websocket.WsPayload;
import com.bolehunter.core.websocket.WsSessionManager;
import com.bolehunter.model.ImMessageDO;
import com.bolehunter.service.ImMessageService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 实时消息 WebSocket 端点处理器（迭代23）
 *
 * <p><b>上行</b>（客户端 → 服务端）：{@code ping} 心跳、{@code chat} 发消息、{@code read} 已读上报。
 * <b>下行</b>统一走 {@link WsPayload} 信封，由服务端按 {@link WsPayload.Type} 主动推送。
 *
 * <p>任何业务异常都不会关闭连接，而是以 {@code error} 信封回告（含错误码），
 * 便于前端给出可读提示，同时避免把异常堆栈暴露给客户端。
 */
@Component
public class ImWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ImWebSocketHandler.class);

    private final WsSessionManager sessionManager;

    private final ImMessageService imMessageService;

    private final ObjectMapper objectMapper;

    public ImWebSocketHandler(WsSessionManager sessionManager,
                              ImMessageService imMessageService,
                              ObjectMapper objectMapper) {
        this.sessionManager = sessionManager;
        this.imMessageService = imMessageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Integer userId = resolveUserId(session);
        if (userId == null) {
            // 理论不可达：握手拦截器已保证身份；此处兜底关闭以杜绝匿名连接
            log.warn("[ws] 会话缺少身份信息，拒绝建立连接");
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        sessionManager.add(userId, session);
        log.info("[ws] 用户 {} 上线（在线 {} 人 / {} 端）",
            userId, sessionManager.onlineUserCount(), sessionManager.onlineSessionCount());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage raw) throws Exception {
        Integer userId = resolveUserId(session);
        if (userId == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE);
            return;
        }
        JsonNode node;
        try {
            node = objectMapper.readTree(raw.getPayload());
        } catch (Exception e) {
            pushError(userId, "消息格式非法，需为 JSON");
            return;
        }
        String type = node.path("type").asText("");
        switch (type) {
            case "ping":
                sessionManager.push(userId, WsPayload.of(WsPayload.Type.PONG, null));
                break;
            case "chat":
                handleChat(userId, node);
                break;
            case "read":
                Integer peerId = node.path("peerId").isIntegralNumber() ? node.path("peerId").asInt() : -1;
                imMessageService.markRead(userId, peerId);
                break;
            default:
                pushError(userId, "未知消息类型：" + type);
        }
    }

    /**
     * 发消息：入库 + 已送达回执（回执用于让发送者的其它标签页同步，并携带服务端消息 id）
     */
    private void handleChat(Integer userId, JsonNode node) {
        Integer receiverId = node.path("receiverId").isIntegralNumber() ? node.path("receiverId").asInt() : -1;
        String content = node.path("content").asText("");
        try {
            ImMessageDO saved = imMessageService.send(userId, receiverId, content);
            // 回执给发送者本人（多端同步），接收者由 ImMessageServiceImpl 内部推送
            sessionManager.push(userId, WsPayload.of(WsPayload.Type.CHAT, buildSelfEcho(userId, saved)));
        } catch (ForbiddenException e) {
            pushError(userId, e, e.getCode());
        } catch (HttpException e) {
            pushError(userId, e, e.getCode());
        } catch (Exception e) {
            log.error("[ws] 处理 chat 异常", e);
            pushError(userId, "发送失败，请稍后重试");
        }
    }

    private static java.util.Map<String, Object> buildSelfEcho(Integer userId, ImMessageDO m) {
        java.util.Map<String, Object> echo = new java.util.LinkedHashMap<>();
        echo.put("id", m.getId());
        echo.put("senderId", m.getSenderId());
        echo.put("receiverId", m.getReceiverId());
        echo.put("content", m.getContent());
        echo.put("isRead", 0);
        echo.put("outgoing", true);
        echo.put("senderIdIsMe", java.util.Objects.equals(m.getSenderId(), userId));
        return echo;
    }

    private void pushError(Integer userId, String message) {
        sessionManager.push(userId, WsPayload.of(WsPayload.Type.ERROR, message));
    }

    private void pushError(Integer userId, HttpException e, int code) {
        sessionManager.push(userId, WsPayload.of(WsPayload.Type.ERROR, e.getMessage() + "（" + code + "）"));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = resolveUserId(session);
        if (userId != null) {
            sessionManager.remove(userId, session);
            log.info("[ws] 用户 {} 下线（在线 {} 人 / {} 端），原因={}",
                userId, sessionManager.onlineUserCount(), sessionManager.onlineSessionCount(), status.getReason());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        Integer userId = resolveUserId(session);
        if (userId != null) {
            log.warn("[ws] 用户 {} 连接异常：{}", userId, exception.getMessage());
            sessionManager.remove(userId, session);
        }
    }

    private static Integer resolveUserId(WebSocketSession session) {
        Object value = session.getAttributes().get(AuthHandshakeInterceptor.ATTR_USER_ID);
        return value instanceof Integer ? (Integer) value : null;
    }
}
