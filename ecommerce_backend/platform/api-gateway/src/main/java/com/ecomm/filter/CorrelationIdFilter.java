package com.ecomm.filter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    private static final Logger log = LogManager.getLogger(CorrelationIdFilter.class);

    public static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var req = exchange.getRequest();
        var existing = req.getHeaders().getFirst(CORRELATION_ID);
        var cid = existing != null ? existing : UUID.randomUUID().toString();

        var mutated = exchange.getRequest().mutate()
                .headers(http -> http.set(CORRELATION_ID, cid))
                .build();

        log.debug("{} {} cid={}", req.getMethod(), req.getURI(), cid);

        return chain.filter(exchange.mutate().request(mutated).build())
                .then(Mono.fromRunnable(() -> exchange.getResponse()
                        .getHeaders().set(CORRELATION_ID, cid)));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
