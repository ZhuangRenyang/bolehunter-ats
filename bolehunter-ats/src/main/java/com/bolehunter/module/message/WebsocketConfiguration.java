/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以在 AGPL-3.0 许可下重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
//package com.bolehunter.module.message;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.socket.config.annotation.EnableWebSocket;
//import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
//import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
//
//@Configuration
//@ConditionalOnProperty(prefix = "lin.cms.websocket", value = "enable", havingValue = "true")
//@EnableWebSocket
//public class WebsocketConfiguration implements WebSocketConfigurer {
//
//    @Value("${lin.cms.websocket.intercept:false}")
//    private boolean intercepted;
//
//    @Bean
//    public MessageWebSocketHandler messageWebSocketHandler() {
//        return new MessageWebSocketHandler();
//    }
//
//    @Bean
//    public WsHandler wsHandler() {
//        return new WsHandlerImpl();
//    }
//
//    @Bean
//    @ConditionalOnProperty(prefix = "lin.cms.websocket", value = "intercept", havingValue = "true")
//    public WebSocketInterceptor webSocketInterceptor() {
//        return new WebSocketInterceptor();
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry handlerRegistry) {
//        if (intercepted) {
//            handlerRegistry.addHandler(messageWebSocketHandler(), "ws/message")
//                    .addInterceptors(webSocketInterceptor())
//                    .setAllowedOrigins("*");
//        } else {
//            handlerRegistry.addHandler(messageWebSocketHandler(), "ws/message")
//                    .setAllowedOrigins("*");
//        }
//    }
//}
