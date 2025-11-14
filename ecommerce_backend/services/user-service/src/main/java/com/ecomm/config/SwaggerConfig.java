package com.ecomm.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce User Service API")
                        .description("Auth, profiles, and RBAC endpoints")
                        .version("v1.0.0")
                        .contact(new Contact().name("E-Comm Team").email("support@ecomm.local"))
                        .license(new License().name("Apache-2.0")))
                // add JWT bearer as a security scheme
                .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes(
                        "bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                ))
                // make bearer the default requirement (controllers can override)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
