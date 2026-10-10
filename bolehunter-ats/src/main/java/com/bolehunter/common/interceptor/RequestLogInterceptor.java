package com.bolehunter.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Eval
 */
@Slf4j
public class RequestLogInterceptor implements HandlerInterceptor {


    private ThreadLocal<Long> startTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        startTime.set(System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long start = startTime.get();
        if (start != null) {
            log.info("[{}] -> [{}] from: {} costs: {}ms",
                    request.getMethod(),
                    request.getServletPath(),
                    request.getRemoteAddr(),
                    System.currentTimeMillis() - start
            );
        }
        startTime.remove();
    }
}
