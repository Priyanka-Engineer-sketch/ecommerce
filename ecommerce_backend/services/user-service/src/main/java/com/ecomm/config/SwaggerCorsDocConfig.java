package com.ecomm.config;

import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerCorsDocConfig {

    @Bean
    public OpenApiCustomizer corsHeadersCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    // For the 200/201 and default responses, add typical CORS headers (documentation only)
                    op.getResponses().forEach((code, resp) -> addCorsHeaders(resp));
                })
        );
    }

    private void addCorsHeaders(ApiResponse response) {
        if (response.getHeaders() == null) return;
        response.getHeaders().putIfAbsent("Access-Control-Allow-Origin",
                new Header().description("CORS allowed origin")
                        .schema(new StringSchema()._default("http://localhost:3000, http://localhost:5173")));
        response.getHeaders().putIfAbsent("Access-Control-Allow-Methods",
                new Header().description("CORS allowed methods")
                        .schema(new StringSchema()._default("GET,POST,PUT,DELETE,PATCH,OPTIONS")));
        response.getHeaders().putIfAbsent("Access-Control-Allow-Headers",
                new Header().description("CORS allowed headers")
                        .schema(new StringSchema()._default("Origin, Content-Type, Accept, Authorization")));
        response.getHeaders().putIfAbsent("Access-Control-Expose-Headers",
                new Header().description("CORS exposed headers")
                        .schema(new StringSchema()._default("Authorization")));
    }
}

