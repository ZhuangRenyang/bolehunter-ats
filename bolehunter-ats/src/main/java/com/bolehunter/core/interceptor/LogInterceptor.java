package com.bolehunter.core.interceptor;

import com.bolehunter.common.interceptor.LoggerImpl;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.bean.MetaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 行为日志拦截器：命中 @Logger 的接口在请求完成后调用 LoggerImpl 落库。
 */
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Autowired
    private LoggerImpl loggerImpl;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        Logger logger = method.getAnnotation(Logger.class);
        if (logger == null) {
            return;
        }
        PermissionMetaWrapper meta = resolvePermissionMeta(handlerMethod);
        loggerImpl.handle(meta.permissionMeta, meta.metaInfo, logger, request, response);
    }

    private PermissionMetaWrapper resolvePermissionMeta(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        com.bolehunter.core.annotation.PermissionMeta pm =
                method.getAnnotation(com.bolehunter.core.annotation.PermissionMeta.class);
        if (pm == null) {
            return new PermissionMetaWrapper(null, null);
        }
        com.bolehunter.core.annotation.PermissionModule module =
                method.getAnnotation(com.bolehunter.core.annotation.PermissionModule.class);
        if (module == null) {
            module = handlerMethod.getBeanType().getAnnotation(
                    com.bolehunter.core.annotation.PermissionModule.class);
        }
        String moduleName = module == null ? "" : module.value();
        MetaInfo metaInfo = MetaInfo.builder().module(moduleName).permission(pm.value()).build();
        return new PermissionMetaWrapper(pm, metaInfo);
    }

    private static class PermissionMetaWrapper {
        final com.bolehunter.core.annotation.PermissionMeta permissionMeta;
        final MetaInfo metaInfo;

        PermissionMetaWrapper(com.bolehunter.core.annotation.PermissionMeta permissionMeta, MetaInfo metaInfo) {
            this.permissionMeta = permissionMeta;
            this.metaInfo = metaInfo;
        }
    }
}
