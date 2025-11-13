package com.ecomm.aop;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter implements Filter {

    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest http = (HttpServletRequest) request;
            String cid = http.getHeader(CORRELATION_ID);
            if (cid == null || cid.isBlank()) {
                cid = UUID.randomUUID().toString();
            }
            MDC.put("cid", cid);
            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

