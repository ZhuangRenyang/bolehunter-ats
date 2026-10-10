package com.bolehunter.core.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 请求信息工具（替代 talelin RequestUtil）
 */
public class RequestUtil {

    private RequestUtil() {
    }

    /**
     * 无参版本：从 RequestContextHolder 取当前请求
     */
    public static String getSimpleRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes) {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return getSimpleRequest(attributes.getRequest());
        }
        return "";
    }

    public static String getSimpleRequest(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return request.getMethod() + " " + request.getRequestURI();
    }
}
