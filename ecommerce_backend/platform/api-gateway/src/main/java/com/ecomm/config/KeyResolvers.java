package com.ecomm.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class KeyResolvers {

    /** Use IP as the default for SCG auto-config. */
    @Bean
    @Primary  // <-- this resolves the ambiguity
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            var xff = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (xff != null && !xff.isBlank()) return Mono.just(xff.split(",")[0].trim());
            return Mono.just(
                    Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                            .map(a -> a.getAddress().getHostAddress())
                            .orElse("unknown"));
        };
    }

    /** Optional: resolve by authenticated principal (if available). */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
                String.valueOf(exchange.getPrincipal().map(p -> p.getName()).defaultIfEmpty("anonymous")));
    }

    @Bean
    public ApplicationRunner verifyBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("Has ipKeyResolver: " + ctx.containsBean("ipKeyResolver"));
        };
    }

}
