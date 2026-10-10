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
