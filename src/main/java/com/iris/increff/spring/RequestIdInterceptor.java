package com.iris.increff.spring;

import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * Interceptor that assigns a requestId to each incoming HTTP request
 * and stores it in SLF4J MDC so it appears in all log lines.
 *
 * Preference order:
 * - X-Request-Id header if present
 * - Generated short UUID
 */
public class RequestIdInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID_KEY = "requestId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String headerId = request.getHeader("X-Request-Id");
        String requestId = (headerId != null && !headerId.trim().isEmpty())
                ? headerId.trim()
                : UUID.randomUUID().toString().substring(0, 8);
        MDC.put(REQUEST_ID_KEY, requestId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.remove(REQUEST_ID_KEY);
    }
}


