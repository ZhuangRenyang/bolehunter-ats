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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebSocket 下行消息统一信封。
 *
 * <p>前端只需按 {@code type} 分发：
 * <ul>
 *   <li>{@code notice}：新的站内通知（铃铛角标 + 抽屉列表即时刷新）</li>
 *   <li>{@code chat}：新的单聊消息（聊天抽屉追加气泡）</li>
 *   <li>{@code read}：对方已读回执（己方气泡变"已读"）</li>
 *   <li>{@code pong}：心跳应答</li>
 * </ul>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WsPayload {

    /** 消息类型：notice / chat / read / pong */
    private String type;

    /** 业务负载 */
    private Object data;

    /** 服务端时间戳，前端可用于本地去重与排序 */
    private String timestamp;

    public WsPayload() {
    }

    public WsPayload(String type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = LocalDateTime.now().toString();
    }

    public static WsPayload of(String type, Object data) {
        return new WsPayload(type, data);
    }

    /** 消息类型常量 */
    public static final class Type {
        public static final String NOTICE = "notice";
        public static final String CHAT = "chat";
        public static final String READ = "read";
        public static final String PONG = "pong";
        public static final String ERROR = "error";

        private Type() {
        }
    }
}
