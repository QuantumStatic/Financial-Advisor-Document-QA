package com.advisor.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String requestId = request.getHeader("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        request.setAttribute("X-Request-Id", requestId);
        response.setHeader("X-Request-Id", requestId);

        long start = System.currentTimeMillis();
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - start;

        log.info("method={} path={} status={} durationMs={} requestId={}",
                request.getMethod(), request.getRequestURI(),
                response.getStatus(), duration, requestId);
    }
}
