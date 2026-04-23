package com.ticketbooking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 100;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIp = request.getRemoteAddr();
        long currentTime = System.currentTimeMillis();
        
        requestCounts.entrySet().removeIf(entry -> currentTime - entry.getValue().timestamp > TimeUnit.MINUTES.toMillis(1));
        
        RequestInfo info = requestCounts.computeIfAbsent(clientIp, k -> new RequestInfo(currentTime));
        
        if (info.count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    private static class RequestInfo {
        private final long timestamp;
        private final AtomicInteger count;

        public RequestInfo(long timestamp) {
            this.timestamp = timestamp;
            this.count = new AtomicInteger(0);
        }
    }
}
