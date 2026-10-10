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
package com.bolehunter.module.log;

public class MDCAccessConstant {
    public static final String REQUEST_METHOD_MDC_KEY = "req.method";
    public static final String RESPONSE_STATUS_MDC_KEY = "res.status";
    public static final String REQUEST_REFERER_MDC_KEY = "req.referer";
    public static final String REQUEST_PROTOCOL_MDC_KEY = "req.protocol";
    public static final String REQUEST_USER_AGENT_MDC_KEY = "req.userAgent";
    public static final String REQUEST_REMOTE_HOST_MDC_KEY = "req.remoteHost";
    public static final String REQUEST_REMOTE_ADDR_MDC_KEY = "req.remoteAddr";
    public static final String REQUEST_REQUEST_URI_MDC_KEY = "req.requestURI";
    public static final String REQUEST_REQUEST_URL_MDC_KEY = "req.requestURL";
    public static final String REQUEST_QUERY_STRING_MDC_KEY = "req.queryString";
    public static final String REQUEST_X_FORWARDED_FOR_MDC_KEY = "req.xForwardedFor";
    public static final String REQUEST_BODY_BYTES_SENT_MDC_KEY = "req.bodyBytesSent";
    public static final String REQUEST_REMOTE_PORT_MDC_KEY = "req.remotePort";
}
