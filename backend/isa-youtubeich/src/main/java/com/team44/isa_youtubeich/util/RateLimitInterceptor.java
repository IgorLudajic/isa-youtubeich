package com.team44.isa_youtubeich.util;

import com.team44.isa_youtubeich.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Interceptor that enforces rate limiting based on IP address
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    // Map to store request timestamps for each IP address
    private final Map<String, Queue<Long>> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // Check for @RateLimited annotation on method or class
        RateLimited rateLimited = handlerMethod.getMethodAnnotation(RateLimited.class);
        if (rateLimited == null) {
            rateLimited = handlerMethod.getBeanType().getAnnotation(RateLimited.class);
        }

        if (rateLimited == null) {
            return true; // No rate limiting configured
        }

        String clientIp = getClientIp(request);
        String rateLimitKey = getRateLimitKey(rateLimited, handlerMethod);
        int limit = rateLimited.limit();
        long windowMillis = rateLimited.windowSeconds() * 1000L;

        return checkRateLimit(clientIp, rateLimitKey, limit, windowMillis);
    }

    /**
     * Generate a unique rate limit key for this endpoint
     */
    private String getRateLimitKey(RateLimited rateLimited, HandlerMethod handlerMethod) {
        String key = rateLimited.key();
        if (key == null || key.isEmpty()) {
            // Use method signature as default key
            return handlerMethod.getMethod().getDeclaringClass().getName() + "#" +
                    handlerMethod.getMethod().getName();
        }
        return key;
    }

    /**
     * Check if the request is within the rate limit
     */
    private boolean checkRateLimit(String clientIp, String rateLimitKey, int limit, long windowMillis) {
        long currentTime = System.currentTimeMillis();

        // Create composite key: IP + rate limit key for separate tracking per endpoint
        String compositeKey = clientIp + ":" + rateLimitKey;

        Queue<Long> timestamps = requestCounts.computeIfAbsent(compositeKey, ignored -> new ConcurrentLinkedQueue<>());

        // Remove timestamps outside the current window
        timestamps.removeIf(timestamp -> currentTime - timestamp > windowMillis);

        // Check if limit is exceeded
        if (timestamps.size() >= limit) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Maximum " + limit + " requests allowed per " +
                            (windowMillis / 1000) + " seconds. Please try again later."
            );
        }

        // Add current request timestamp
        timestamps.add(currentTime);

        return true;
    }

    /**
     * Extract client IP address from request, considering proxy headers
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // If multiple IPs in X-Forwarded-For, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}

