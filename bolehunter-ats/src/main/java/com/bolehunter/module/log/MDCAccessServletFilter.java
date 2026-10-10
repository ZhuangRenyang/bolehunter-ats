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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 记录 access log 的过滤器
 * 会把 request、response 相关信息（如 IP、URL）放入 Logback 的 MDC中
 */
public class MDCAccessServletFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(MDCAccessServletFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        putRequestMDC(request);
        try {
            chain.doFilter(request, response);
            putResponseMDC(response);
            accessLog();
        } finally {
            clearMDC();
        }
    }

    /**
     * 记录 access log
     */
    public void accessLog() {
        log.info("");
    }

    public void putResponseMDC(ServletResponse servletResponse) {
        if (servletResponse instanceof HttpServletResponse) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
            MDC.put(MDCAccessConstant.RESPONSE_STATUS_MDC_KEY, String.valueOf(httpServletResponse.getStatus()));
        }
    }

    /**
     * 放入 request 信息
     */
    public void putRequestMDC(ServletRequest servletRequest) throws IOException {
        MDC.put(MDCAccessConstant.REQUEST_REMOTE_HOST_MDC_KEY, servletRequest.getRemoteHost());
        MDC.put(MDCAccessConstant.REQUEST_REMOTE_ADDR_MDC_KEY, servletRequest.getRemoteAddr());
        MDC.put(MDCAccessConstant.REQUEST_PROTOCOL_MDC_KEY, servletRequest.getProtocol());
        MDC.put(MDCAccessConstant.REQUEST_PROTOCOL_MDC_KEY, servletRequest.getProtocol());
        MDC.put(MDCAccessConstant.REQUEST_REMOTE_PORT_MDC_KEY, String.valueOf(servletRequest.getRemotePort()));
        MDC.put(MDCAccessConstant.REQUEST_BODY_BYTES_SENT_MDC_KEY, String.valueOf(servletRequest.getContentLength()));

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            MDC.put(MDCAccessConstant.REQUEST_REQUEST_URI_MDC_KEY, httpServletRequest.getRequestURI());
            StringBuffer requestUrl = httpServletRequest.getRequestURL();
            if (requestUrl != null) {
                MDC.put(MDCAccessConstant.REQUEST_REQUEST_URL_MDC_KEY, requestUrl.toString());
            }
            MDC.put(MDCAccessConstant.REQUEST_METHOD_MDC_KEY, httpServletRequest.getMethod());
            MDC.put(MDCAccessConstant.REQUEST_QUERY_STRING_MDC_KEY, httpServletRequest.getQueryString());
            MDC.put(MDCAccessConstant.REQUEST_USER_AGENT_MDC_KEY, httpServletRequest.getHeader("User-Agent"));
            MDC.put(MDCAccessConstant.REQUEST_X_FORWARDED_FOR_MDC_KEY, httpServletRequest.getHeader("X-Forwarded-For"));
            MDC.put(MDCAccessConstant.REQUEST_REFERER_MDC_KEY, httpServletRequest.getHeader("referer"));
        }
    }

    /**
     * 清理 MDC，特别重要
     * 因为 MDC 由 ThreadLocal 实现，如果不清理，线程池的复用机制会导致 MDC 数据受到污染
     */
    public void clearMDC() {
        MDC.remove(MDCAccessConstant.REQUEST_METHOD_MDC_KEY);
        MDC.remove(MDCAccessConstant.RESPONSE_STATUS_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_REFERER_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_PROTOCOL_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_USER_AGENT_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_REMOTE_HOST_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_REMOTE_ADDR_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_REQUEST_URI_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_REQUEST_URL_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_QUERY_STRING_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_X_FORWARDED_FOR_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_BODY_BYTES_SENT_MDC_KEY);
        MDC.remove(MDCAccessConstant.REQUEST_REMOTE_PORT_MDC_KEY);
    }

}
