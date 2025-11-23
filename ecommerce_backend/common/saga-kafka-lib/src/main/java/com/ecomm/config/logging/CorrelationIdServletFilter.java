package com.ecomm.config.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class CorrelationIdServletFilter implements Filter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest http = (HttpServletRequest) request;
            String cid = http.getHeader(CORRELATION_ID_HEADER);
            if (cid == null || cid.isBlank()) {
                cid = UUID.randomUUID().toString();
            }
            MDC.put(CORRELATION_ID_MDC_KEY, cid);
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}


//<pattern>%d{yyyy-MM-dd HH:mm:ss} [%X{correlationId}] %-5level [%thread] %logger{36} - %msg%n</pattern>