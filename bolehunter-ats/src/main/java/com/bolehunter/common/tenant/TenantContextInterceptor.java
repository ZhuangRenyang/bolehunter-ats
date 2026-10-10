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
package com.bolehunter.common.tenant;

import com.bolehunter.common.LocalUser;
import com.bolehunter.model.UserDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 租户上下文拦截器（Spring MVC）：
 * 必须注册在 AuthorizeInterceptor 之后——鉴权拦截器解析 JWT 并写入 LocalUser 后，
 * 本拦截器才能从登录用户读取 tenant_id 写入 TenantContext；请求结束后清理，防止线程池串数据。
 */
@Slf4j
@Component
public class TenantContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UserDO user = LocalUser.getLocalUser();
        if (user != null) {
            TenantContext.setTenantId(user.getTenantId());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) {
        TenantContext.clear();
    }
}
