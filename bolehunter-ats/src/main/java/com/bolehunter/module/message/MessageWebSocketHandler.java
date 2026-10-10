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
//package com.bolehunter.module.message;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.socket.*;
//
//public class MessageWebSocketHandler implements WebSocketHandler {
//
//    @Autowired
//    private WsHandler wsHandler;
//
//    @Override
//    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//        wsHandler.handleOpen(session);
//    }
//
//    @Override
//    public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
//        if (webSocketMessage instanceof TextMessage) {
//            TextMessage textMessage = (TextMessage) webSocketMessage;
//            wsHandler.handleMessage(session, textMessage.getPayload());
//        }
//    }
//
//    @Override
//    public void handleTransportError(WebSocketSession session, Throwable throwable) throws Exception {
//        wsHandler.handleError(session, throwable);
//    }
//
//    @Override
//    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//        wsHandler.handleClose(session);
//    }
//
//    @Override
//    public boolean supportsPartialMessages() {
//        return false;
//    }
//}
